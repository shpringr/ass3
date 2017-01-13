package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ERRORPackets extends Packets {
    short errorCode;
    String errMsg;

    public ERRORPackets(short errorCode, String errMsg) {
        this.errorCode = errorCode;
        this.errMsg = errMsg;
        super.opCode = 5;
    }


    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(518);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(errorCode));
        try {
            lengthBuffer.put(errMsg.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return lengthBuffer.array();
    }
}
