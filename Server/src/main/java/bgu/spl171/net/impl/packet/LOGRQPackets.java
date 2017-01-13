package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class LOGRQPackets extends Packets{
    private String userName;


    public LOGRQPackets(String userName) {
        this.userName = userName;
        super.opCode = 7;
    }

    public String getUserName() {
        return userName;
    }


    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(518);
        lengthBuffer.put(shortToBytes(opCode));
        try {
            lengthBuffer.put(userName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return lengthBuffer.array();
    }
}
