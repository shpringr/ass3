package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.Packets;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;


public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {

    private boolean shouldTerminate = false;
    Connections<T> connections;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        //connections.add

    }

    @Override
    public void process(T message) {
        switch ((((Packets)message).getOpCode())){
            case 10:
                shouldTerminate = true;

        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
