package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class DIRQPacket extends Packets {

    public DIRQPacket() {
    super.opCode = 6;
    }


    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
        lengthBuffer.put(shortToBytes(opCode));
        return lengthBuffer.array();
    }
}
