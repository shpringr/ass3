package bgu.spl171.net.api.bidi;

import bgu.spl171.net.srv.bidi.ConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandlerImpl;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentLinkedQueue<ConnectionHandlerImpl> conL = null;

    public boolean send(int connectionId, T msg) {
        for (ConnectionHandlerImpl connection: conL) {
            if (connection.getConnId() == connectionId){
                connection.send(msg);
                return true;
            }
        }
        return false;
    }

    public void broadcast(T msg) {
        for (ConnectionHandlerImpl connection: conL) {
            connection.send(msg);
        }
    }

    public void disconnect(int connectionId) throws IOException {
        for (ConnectionHandlerImpl connection: conL) {
            if (connection.getConnId() == connectionId){
                connection.close();
            }
        }
    }

    public void add(ConnectionHandlerImpl conH) {
        this.conL.add(conH);
    }

}
