package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class BCASTPackets extends Packets {
    byte deletedAdd;
    String fileName;

    public BCASTPackets(byte deletedAdd, String fileName) {
        this.deletedAdd = deletedAdd;
        this.fileName = fileName;
        super.opCode = 9;
    }


    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(518);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(deletedAdd));
        try {
            lengthBuffer.put(fileName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return lengthBuffer.array();
    }
}
