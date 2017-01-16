package bgu.spl171.net.impl.packet;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class RRQPacket extends Packet {
    private String fileName;

    public RRQPacket(String filename) {
        fileName = filename;
        super.opCode = 1;
    }

    public String getFileName() {
        return fileName;
    }


    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(518);
        lengthBuffer.put(shortToBytes(opCode));
        try {
            lengthBuffer.put(fileName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lengthBuffer.put(shortToBytes((byte)0));
        return lengthBuffer.array();
    }
}
