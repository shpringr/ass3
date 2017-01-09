package bgu.spl171.net.impl.packet;


public class RRQPackets extends Packets{
    private String Filename;

    public RRQPackets(String filename) {
        Filename = filename;
        super.opCode = 1;
    }

    public String getFilename() {
        return Filename;
    }
}
