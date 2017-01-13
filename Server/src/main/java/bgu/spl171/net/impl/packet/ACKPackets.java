package bgu.spl171.net.impl.packet;

import java.nio.ByteBuffer;

public class ACKPackets extends Packets {
    short block;

    public ACKPackets(short block) {
        this.block = block;
        super.opCode = 4;
    }

    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(block));
        return lengthBuffer.array();
    }
}
