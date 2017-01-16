package bgu.spl171.net.impl.packet;

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
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+getUserName().length()+1);
        lengthBuffer.put(shortToBytes(opCode));
        try {
            lengthBuffer.put(userName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lengthBuffer.put((byte)0);
        return lengthBuffer.array();
    }
}
