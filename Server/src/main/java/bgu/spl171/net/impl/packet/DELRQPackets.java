package bgu.spl171.net.impl.packet;

public class DELRQPackets extends Packets {
    private String Filename;

    public DELRQPackets(String filename) {
        Filename = filename;
        super.opCode = 8;
    }

    public String getFilename() {
        return Filename;
    }
}
