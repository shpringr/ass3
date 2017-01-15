package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;


    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packets message) {
        switch ((message.getOpCode())){
            case 1 :
                ((RRQPackets)message).getFileName();
                break;
            case 2 :

                ((WRQPackets)message).getFileName();
                break;
            case 3:

                ((DATAPackets)message).toByteArr();
                break;
            case 4:

                ((ACKPackets)message).toByteArr();
                break;
            case 5:

                ((ERRORPackets)message).toByteArr();
                break;
            case 6:

                ((DIRQPacket)message).toByteArr();
                break;

            case 7:

                ((LOGRQPackets)message).getUserName();
                break;
            case 8:

                ((DELRQPackets)message).getFilename();
                break;
            case 9:

                ((BCASTPackets)message).toByteArr();
                break;
            case 10:

                ((DISCPackets)message).toByteArr();
                shouldTerminate = true;
                break;
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
