package bgu.spl171.net.impl.packet;

public class ERRORPackets extends Packets {
    short errorCode;
    String errMsg;

    public ERRORPackets(short errorCode, String errMsg) {
        this.errorCode = errorCode;
        this.errMsg = errMsg;
        super.opCode = 5;
    }
}
