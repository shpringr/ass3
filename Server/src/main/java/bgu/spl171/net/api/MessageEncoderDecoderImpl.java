package bgu.spl171.net.api;

import bgu.spl171.net.impl.packet.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by ROTEM on 09/01/2017.
 */
public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Packet> {

    private final ByteBuffer opLengthBuffer = ByteBuffer.allocate(2);
    private ByteBuffer lengthBuffer;
    private ByteBuffer blockBuffer;
    private ByteBuffer packetSizeBuffer;

    private ByteBuffer errorBuffer;
    private short errorCode;
    private byte [] errorArr;

    private short opCode;
    private byte[] objectBytes;
    private byte[] opObjectBytes;

    private byte [] packetSizeArr;
    private short packetSize;
    private byte [] blockArr;
    private short block;
    private byte deletedAdd;

    private Packet res;

    public MessageEncoderDecoderImpl() {
        initAll();
    }

    private void initAll() {
        if (opLengthBuffer!= null)
            opLengthBuffer.clear();
        if (lengthBuffer!= null)
            lengthBuffer.clear();
        if (blockBuffer != null)
            blockBuffer.clear();
        if (packetSizeBuffer!= null)
            packetSizeBuffer.clear();
        if (errorBuffer!= null)
            errorBuffer.clear();
        errorCode =-1;
        errorArr = null;
        opCode=0;
        objectBytes = null;
        opObjectBytes = null;
        packetSizeArr = null;
        packetSize =0;
        blockArr = null;
        block=0;
        deletedAdd = 'e';
    }

    @Override
    public Packet decodeNextByte(byte nextByte) throws UnsupportedEncodingException {
        switch (opCode)
        {
            case 0:
                initOpCodeAndBuffers(nextByte);
                break;

            case 1:
                makeRRQPacket(nextByte);
                break;

            case 2:
                makeWRQPacket(nextByte);
                break;

            case 3:
                makeDataPacket(nextByte);
                break;

            case 4:
                makeACKPacket(nextByte);
                break;

            case 5:
                makeErrorPacket(nextByte);
                break;

            case 6:
                makeDIRQPacket();
                break;

            case 7:
                makeLoginPacket(nextByte);
                break;

            case 8:
                makeDelRqPacket(nextByte);
                break;

            case 9:
                makeBCastPacket(nextByte);
                break;

            case 10:
                makeDiscPacket();
        }

        return res;
    }

    private void makeDiscPacket() {
        res = new DISCPacket();
        initAll();
    }

    private void makeBCastPacket(byte nextByte) throws UnsupportedEncodingException {
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
                res = new BCASTPacket(deletedAdd, fileName);
                initAll();
            }
        }
    }

    private void makeDelRqPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            lengthBuffer.flip();
            objectBytes = lengthBuffer.array();
            String fileName = new String(objectBytes, "UTF-8");
            res = new DELRQPacket(fileName);
            initAll();
        }
    }

    private void makeLoginPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            lengthBuffer.flip();
            objectBytes = lengthBuffer.array();
            String userName = new String(objectBytes, "UTF-8");
            res = new LOGRQPacket(userName);
            initAll();
        }
    }

    private void makeDIRQPacket() {
        res = new DIRQPacket();
        initAll();
    }

    private void makeErrorPacket(byte nextByte) throws UnsupportedEncodingException {
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
                res = new ERRORPacket(errorCode, errMsg);
                initAll();
            }
        }
    }

    private void makeACKPacket(byte nextByte) {
        if (lengthBuffer.hasRemaining()) {
            lengthBuffer.put(nextByte);
        } else {
            lengthBuffer.flip();
            byte[] blockArrAck = lengthBuffer.array();
            short blockAck = bytesToShort(blockArrAck);
            res = new ACKPacket(blockAck);
            initAll();
        }
    }

    private void makeDataPacket(byte nextByte) {
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
                res = new DATAPacket(packetSize, block, data);
                initAll();
            }
        }
    }

    private void makeWRQPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            lengthBuffer.flip();
            objectBytes = lengthBuffer.array();
            String filename = new String(objectBytes, "UTF-8");
            res = new WRQPacket(filename);
            initAll();
        }
    }

    private void makeRRQPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            lengthBuffer.flip();
            objectBytes = lengthBuffer.array();
            String filename = new String(objectBytes, "UTF-8");
            res = new RRQPacket(filename);
            initAll();
        }
    }

    private void initOpCodeAndBuffers(byte nextByte) {
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

    //@TODO SHITTTTTTTTTTTTTTTTTTTTTTTTTTTT
    public byte[] encode(Packet message) {
        return message.toByteArr();
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
