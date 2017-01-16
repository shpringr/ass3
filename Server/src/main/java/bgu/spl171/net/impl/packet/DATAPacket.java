package bgu.spl171.net.impl.packet;

import java.nio.ByteBuffer;

public class DATAPacket extends Packet {
    short packetSize;
    short block;
    byte[] data;

    public DATAPacket(short packetSize, short block, byte[] data) {
        this.packetSize = packetSize;
        this.block = block;
        this.data = data;
        super.opCode = 3;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public short getBlock() {
        return block;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] toByteArr() {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+2+2+data.length);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(packetSize));
        lengthBuffer.put(shortToBytes(block));
        lengthBuffer.put(data);
        return lengthBuffer.array();
    }
}
