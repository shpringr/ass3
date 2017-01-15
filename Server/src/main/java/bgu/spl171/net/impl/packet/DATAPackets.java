package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class DATAPackets extends Packets {
    short packetSize;
    short block;
    byte[] data;

    public DATAPackets(short packetSize, short block, byte[] data) {
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
        ByteBuffer lengthBuffer = ByteBuffer.allocate(518);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(packetSize));
        lengthBuffer.put(shortToBytes(block));
        lengthBuffer.put(data);
        return lengthBuffer.array();
    }
}
