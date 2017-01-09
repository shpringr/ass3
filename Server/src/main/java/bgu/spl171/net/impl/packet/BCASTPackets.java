package bgu.spl171.net.impl.packet;

public class BCASTPackets extends Packets {
    byte deletedAdd;
    String fileName;

    public BCASTPackets(byte deletedAdd, String fileName) {
        this.deletedAdd = deletedAdd;
        this.fileName = fileName;
        super.opCode = 9;
    }
}
