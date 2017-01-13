package bgu.spl171.net.api;

import bgu.spl171.net.impl.packet.*;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by ROTEM on 09/01/2017.
 */
public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<T> {

    private final ByteBuffer opLengthBuffer = ByteBuffer.allocate(2);
    private ByteBuffer lengthBuffer;
    private ByteBuffer blockBuffer;
    private ByteBuffer packetSizeBuffer;

    private ByteBuffer errorBuffer;
    private short errorCode;
    private byte [] errorArr = null;

    private short opCode=0;
    private byte[] objectBytes = null;
    private byte[] opObjectBytes = null;

    private byte [] packetSizeArr = null;
    private short packetSize;
    private byte [] blockArr = null;
    private short block;
    private byte deletedAdd = 'e';

    private Packets res = null;



    @Override
    public T decodeNextByte(byte nextByte) throws UnsupportedEncodingException {
        if (opCode==0){
            opLengthBuffer.put(nextByte);
            if (!opLengthBuffer.hasRemaining()){
                opLengthBuffer.flip();
                opObjectBytes = opLengthBuffer.array();
                opCode = bytesToShort(opObjectBytes);
                switch (opCode){
                    case 1 : case 2 :  case 7: case 8: case 9: lengthBuffer = ByteBuffer.allocate(516);
                        break;
                    case 3:
                        packetSizeBuffer = ByteBuffer.allocate(2);
                        blockBuffer = ByteBuffer.allocate(2);
                        lengthBuffer = ByteBuffer.allocate(514);
                    case 4: lengthBuffer = ByteBuffer.allocate(2);
                        break;
                    case 5:
                        errorBuffer = ByteBuffer.allocate(2);
                        lengthBuffer = ByteBuffer.allocate(516);
                }
            }
        }
        else {
            switch (opCode) {
                case 1:
                    if (nextByte != '\0')
                        lengthBuffer.put(nextByte);
                    else { //nextByte == '\0'
                        lengthBuffer.flip();
                        objectBytes = lengthBuffer.array();
                        String filename = new String(objectBytes, "UTF-8");
                        res = new RRQPackets(filename);
                        objectBytes = null;
                    }
                    break;
                case 2:
                    if (nextByte != '\0')
                        lengthBuffer.put(nextByte);
                    else { //nextByte == '\0'
                        lengthBuffer.flip();
                        objectBytes = lengthBuffer.array();
                        String filename = new String(objectBytes, "UTF-8");
                        res = new WRQPackets(filename);
                        objectBytes = null;
                    }
                    break;
                case 3:
                    if (packetSizeArr == null) {
                        packetSizeBuffer.put(nextByte);
                        if (!packetSizeBuffer.hasRemaining()) {
                            lengthBuffer.flip();
                            packetSizeArr = packetSizeBuffer.array();
                            packetSize = bytesToShort(packetSizeArr);
                        }
                    } else if (blockArr == null) {
                        blockBuffer.put(nextByte);
                        if (!blockBuffer.hasRemaining()) {
                            lengthBuffer.flip();
                            blockArr = blockBuffer.array();
                            block = bytesToShort(blockArr);
                        }
                    } else {
                        if (packetSize != 0) {
                            lengthBuffer.put(nextByte);
                            packetSize--;
                        } else {
                            lengthBuffer.flip();
                            byte[] data = lengthBuffer.array();
                            res = new DATAPackets(packetSize, block, data);
                        }
                    }
                    break;
                case 4:
                    if (lengthBuffer.hasRemaining()) {
                        lengthBuffer.put(nextByte);
                    } else {
                        lengthBuffer.flip();
                        byte[] blockArrAck = lengthBuffer.array();
                        short blockAck = bytesToShort(blockArrAck);
                        res = new ACKPackets(blockAck);
                    }
                    break;
                case 5:
                    if (errorArr == null) {
                        errorBuffer.put(nextByte);
                        if (!errorBuffer.hasRemaining()) {
                            errorBuffer.flip();
                            errorArr = errorBuffer.array();
                            errorCode = bytesToShort(errorArr);
                        }
                    } else {
                        if (nextByte != '\0')
                            lengthBuffer.put(nextByte);
                        else { //nextByte == '\0'
                            lengthBuffer.flip();
                            objectBytes = lengthBuffer.array();
                            String errMsg = new String(objectBytes, "UTF-8");
                            res = new ERRORPackets(errorCode, errMsg);
                            objectBytes = null;
                        }
                    }
                    break;
                case 6:
                    res = new DIRQPacket();
                    break;
                case 7:
                    if (nextByte != '\0')
                        lengthBuffer.put(nextByte);
                    else { //nextByte == '\0'
                        lengthBuffer.flip();
                        objectBytes = lengthBuffer.array();
                        String userName = new String(objectBytes, "UTF-8");
                        res = new LOGRQPackets(userName);
                        objectBytes = null;
                    }
                    break;
                case 8:
                    if (nextByte != '\0')
                        lengthBuffer.put(nextByte);
                    else { //nextByte == '\0'
                        lengthBuffer.flip();
                        objectBytes = lengthBuffer.array();
                        String fileName = new String(objectBytes, "UTF-8");
                        res = new DELRQPackets(fileName);
                        objectBytes = null;
                    }
                    break;
                case 9:
                    if (deletedAdd=='e'){
                        deletedAdd = nextByte;
                    }
                    else{
                        if (nextByte != '\0')
                            lengthBuffer.put(nextByte);
                        else { //nextByte == '\0'
                            lengthBuffer.flip();
                            objectBytes = lengthBuffer.array();
                            String fileName = new String(objectBytes, "UTF-8");
                            res = new BCASTPackets(deletedAdd, fileName);
                            objectBytes = null;
                        }
                    }
                    break;
                case 10:
                    res = new DISCPackets();
            }

        }
        return (T) res;
    }




    //@TODO SHITTTTTTTTTTTTTTTTTTTTTTTTTTTT
    public byte[] encode(T message) {
        //it is not eaxcacly like this.. it is shit
        return ((Packets)message).toByteArr();
    }


    //Decode 2 bytes to short
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    //Encode short to 2 bytes
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}


/*
        if (objectBytes == null) { //indicates that we are still reading the length
            lengthBuffer.put(nextByte);
            if (!lengthBuffer.hasRemaining()) { //we read 4 bytes and therefore can take the length
                lengthBuffer.flip();
                objectBytes = new byte[lengthBuffer.getInt()];
                objectBytesIndex = 0;
                lengthBuffer.clear();
            }
        } else {
            objectBytes[objectBytesIndex] = nextByte;
            if (++objectBytesIndex == objectBytes.length) {
                opObjectBytes = new byte[2];
                opObjectBytes[0] = objectBytes[0];
                opObjectBytes[1] = objectBytes[1];
                opCode = bytesToShort(opObjectBytes);
            }
                  }

                }

*/
