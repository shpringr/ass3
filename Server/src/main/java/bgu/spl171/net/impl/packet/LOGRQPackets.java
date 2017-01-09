package bgu.spl171.net.impl.packet;

public class LOGRQPackets extends Packets{
    private String UserName;


    public LOGRQPackets(String userName) {
        UserName = userName;
        super.opCode = 7;
    }

    public String getUserName() {
        return UserName;
    }
}
