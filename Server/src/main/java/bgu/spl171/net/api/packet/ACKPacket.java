package bgu.spl171.net.api.packet;

import java.nio.ByteBuffer;

public class ACKPacket extends Packet {
    short block;

    public ACKPacket(short block) {
        this.block = block;
        super.opCode = 4;
    }

    public short getBlock() {
        return block;
    }

    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+2);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(block));
        return lengthBuffer.array();
    }
}
