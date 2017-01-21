package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.TFTP.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.TFTP.BidiMessagingProtocolImpl;
import bgu.spl171.net.srv.Server;



public class TPCMain {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        Server.threadPerClient(

                port, //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();

}
}
