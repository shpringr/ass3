package bgu.spl171.net.api.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class LOGRQPacket extends Packet {
    private String userName;


    public LOGRQPacket(String userName) {
        this.userName = userName;
        super.opCode = 7;
    }

    public String getUserName() {
        return userName;
    }


    @Override
    public byte[] toByteArr() {
        try {
            byte[] msgBytes = userName.getBytes("UTF-8");
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
