package bgu.spl171.net.srv;

import bgu.spl171.net.impl.TFTP.MessageEncoderDecoderImpl;
import bgu.spl171.net.impl.TFTP.BidiMessagingProtocolImpl;

/**
 * Created by Orel Hazan on 17/01/2017.
 */
public class BidiMain {

    public static void main(String[] args) {
// you can use any server...
        Server.threadPerClient(
                7777, //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();
//
//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () ->  new BidiMessagingProtocolImpl(), //protocol factory
//                MessageEncoderDecoderImpl::new //message encoder decoder factory
//        ).serve();

    }

}
