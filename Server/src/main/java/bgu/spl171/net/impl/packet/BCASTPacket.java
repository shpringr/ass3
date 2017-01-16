package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class BCASTPacket extends Packet {
    byte deletedAdd;
    String fileName;

    public BCASTPacket(byte deletedAdd, String fileName) {
        this.deletedAdd = deletedAdd;
        this.fileName = fileName;
        super.opCode = 9;
    }

    public byte getDeletedAdd() {
        return deletedAdd;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+1+fileName.length()+1);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(deletedAdd);
        try {
            lengthBuffer.put(fileName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lengthBuffer.put((byte)0);
        return lengthBuffer.array();
    }
}
