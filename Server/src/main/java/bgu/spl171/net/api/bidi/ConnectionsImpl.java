package bgu.spl171.net.api.bidi;

import bgu.spl171.net.srv.bidi.ConnectionHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    private AtomicInteger connId = new AtomicInteger(0);
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> allConnection =new ConcurrentHashMap<>();

    public int getNewConnectionId(){
        return connId.getAndIncrement();
    }

    public boolean send(int connectionId, T msg) {
        if (allConnection.containsKey(connectionId)) {
            allConnection.get(connectionId).send(msg);
            return true;
        }
        else {
            return false;
        }
    }

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getAllConnection() {
        return allConnection;
    }

    public void broadcast(T msg) {
        allConnection.forEach( (k,v) -> v.send(msg) );
    }

    public void disconnect(int connectionId) throws IOException {
        allConnection.get(connectionId).close();
        allConnection.remove(connectionId);
    }

    public void add(ConnectionHandler<T> conH, Integer id) {
        allConnection.put(id, conH);
    }



}
