package bgu.spl171.net.api.packet;

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
     try{
         byte[] bytes = fileName.getBytes("UTF-8");
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+1+bytes.length+1);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(deletedAdd);
            lengthBuffer.put(bytes);
        lengthBuffer.put((byte)0);
        return lengthBuffer.array();
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        return new byte[0];
    }
    }
}
