package bgu.spl171.net.impl.packet;

public class ACKPackets extends Packets {
    short block;

    public ACKPackets(short block) {
        this.block = block;
        super.opCode = 4;
    }
}
