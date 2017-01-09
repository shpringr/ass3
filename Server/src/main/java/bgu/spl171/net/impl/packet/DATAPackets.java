package bgu.spl171.net.impl.packet;

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
}
