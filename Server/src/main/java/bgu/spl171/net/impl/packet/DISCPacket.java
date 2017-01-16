package bgu.spl171.net.impl.packet;

import java.nio.ByteBuffer;

public class DISCPacket extends Packet {

    public DISCPacket() {
        super.opCode=10;
    }


    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
        lengthBuffer.put(shortToBytes(opCode));
        return lengthBuffer.array();
    }
}
