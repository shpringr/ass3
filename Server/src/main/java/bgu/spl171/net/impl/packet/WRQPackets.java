package bgu.spl171.net.impl.packet;

public class WRQPackets extends Packets {
    private String Filename;

    public WRQPackets(String filename) {
        Filename = filename;
        super.opCode = 2;
    }

    public String getFilename() {
        return Filename;
    }

}

