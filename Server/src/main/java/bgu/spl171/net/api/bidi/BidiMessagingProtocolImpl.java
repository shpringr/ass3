package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.Packets;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.File;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;
    File file = new File("Server/Files");
    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packets message) {
        switch (message.getOpCode()){
            //RRQ
            case 1 :
                file.list()

                break;
            case 2 :
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                shouldTerminate = true;
                break;
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
