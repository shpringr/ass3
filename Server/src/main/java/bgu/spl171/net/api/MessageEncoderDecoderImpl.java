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
        initOpObjects();
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
        packetSizeArr = null;
        packetSize =0;
        blockArr = null;
        block=0;
        deletedAdd = 'e';
    }

    private void initOpObjects() {
        if (opLengthBuffer!= null)
            opLengthBuffer.clear();
        opCode=0;
    }

    @Override
    public Packet decodeNextByte(byte nextByte) throws UnsupportedEncodingException {
        switch (opCode) {
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
                break;

        }

        return res;
    }

    private void makeBCastPacket(byte nextByte) throws UnsupportedEncodingException {
        if (deletedAdd=='e'){
            deletedAdd = nextByte;
        }
        else{
            if (nextByte != '\0')
                lengthBuffer.put(nextByte);
            else { //nextByte == '\0'
                String fileName = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
                res = new BCASTPacket(deletedAdd, fileName);
                initAll();
            }
        }
    }

    private void makeDelRqPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            String fileName = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
            res = new DELRQPacket(fileName);
            initAll();
        }
    }

    private void makeLoginPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            String userName = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
            res = new LOGRQPacket(userName);
            initAll();
        }
    }

    private byte[] getDataFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        byte[] objectBytes = new byte[buffer.limit()];
        buffer.get(objectBytes,0, buffer.limit());
        return objectBytes;
    }

    private void makeDIRQPacket() {
        res = new DIRQPacket();
        initAll();
    }

    private void makeErrorPacket(byte nextByte) throws UnsupportedEncodingException {
        if (errorArr == null) {
            errorBuffer.put(nextByte);
            if (!errorBuffer.hasRemaining()) {
                errorArr = getDataFromBuffer(errorBuffer);
                errorCode = bytesToShort(errorArr);
            }
        } else {
            if (nextByte != '\0')
                lengthBuffer.put(nextByte);
            else { //nextByte == '\0'
                String errMsg = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
                res = new ERRORPacket(errorCode, errMsg);
                initAll();
            }
        }
    }

    private void makeACKPacket(byte nextByte) {
        lengthBuffer.put(nextByte);
        if (!lengthBuffer.hasRemaining()) {
            short blockAck = bytesToShort(getDataFromBuffer(lengthBuffer));
            res = new ACKPacket(blockAck);
            initAll();
        }
    }

    private void makeDataPacket(byte nextByte) {
        if (packetSizeArr == null) {
            packetSizeBuffer.put(nextByte);
            if (!packetSizeBuffer.hasRemaining()) {
                packetSizeArr = getDataFromBuffer(packetSizeBuffer);
                packetSize = bytesToShort(packetSizeArr);
            }
        } else if (blockArr == null) {
            blockBuffer.put(nextByte);
            if (!blockBuffer.hasRemaining()) {
                blockArr = getDataFromBuffer(blockBuffer);
                block = bytesToShort(blockArr);
            }
        } else {
            lengthBuffer.put(nextByte);
            packetSize--;
            if (packetSize == 0) {
                byte[] bytes = getDataFromBuffer(lengthBuffer);
                res = new DATAPacket((short) bytes.length, block, bytes);
                initAll();
            }
        }
    }

    private void makeWRQPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            String filename = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
            res = new WRQPacket(filename);
            initAll();
        }
    }

    private void makeRRQPacket(byte nextByte) throws UnsupportedEncodingException {
        if (nextByte != '\0')
            lengthBuffer.put(nextByte);
        else { //nextByte == '\0'
            String filename = new String(getDataFromBuffer(lengthBuffer), "UTF-8");
            res = new RRQPacket(filename);
            initAll();
        }
    }

    private void initOpCodeAndBuffers(byte nextByte) {
        opLengthBuffer.put(nextByte);
        if (!opLengthBuffer.hasRemaining()){
            opCode = bytesToShort(getDataFromBuffer(opLengthBuffer));
            switch (opCode){
                case 1 : case 2 : case 7: case 8: case 9: lengthBuffer = ByteBuffer.allocate(516);
                    break;
                case 3:
                    packetSizeBuffer = ByteBuffer.allocate(2);
                    blockBuffer = ByteBuffer.allocate(2);
                    lengthBuffer = ByteBuffer.allocate(512);
                    break;
                case 4: lengthBuffer = ByteBuffer.allocate(2);
                    break;
                case 5:
                    errorBuffer = ByteBuffer.allocate(2);
                    lengthBuffer = ByteBuffer.allocate(516);
                    break;
                case 6:
                    res = new DIRQPacket();
                    initOpObjects();
                    break;

                case 10:
                    res = new DISCPacket();
                    initOpObjects();
                    break;
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