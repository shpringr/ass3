package bgu.spl171.net.srv.bidi;

import java.io.IOException;

/**
 * Created by ROTEM on 06/01/2017.
 */
public class ConnectionHandlerImpl<T> implements ConnectionHandler<T> {
    int connId;

    public ConnectionHandlerImpl(int connId) {
        this.connId = connId;
    }

    public int getConnId() {
        return connId;
    }

    @Override
    public void send(T msg) {

    }

    @Override
    public void close() throws IOException {

    }
}
