package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.TFTP.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.TFTP.BidiMessagingProtocolImpl;
import bgu.spl171.net.srv.Server;


public class TPCMain {

    public static void main(String[] args) {

        Server.threadPerClient(
                7777, //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();

}
}
