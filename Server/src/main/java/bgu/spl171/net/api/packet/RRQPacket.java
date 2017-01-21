package bgu.spl171.net.api.packet;


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
        try {
            byte[] msgBytes = fileName.getBytes("UTF-8");
            ByteBuffer lengthBuffer = ByteBuffer.allocate(2+msgBytes.length+1);
        lengthBuffer.put(shortToBytes(opCode));
            lengthBuffer.put(msgBytes);
        lengthBuffer.put((byte)0);
        return lengthBuffer.array();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
