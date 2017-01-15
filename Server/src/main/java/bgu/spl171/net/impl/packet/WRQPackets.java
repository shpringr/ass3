package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class WRQPackets extends Packets {
    private String fileName;

    public WRQPackets(String filename) {
        fileName = filename;
        super.opCode = 2;
    }

    public String getFileName() {
        return fileName;
    }

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

