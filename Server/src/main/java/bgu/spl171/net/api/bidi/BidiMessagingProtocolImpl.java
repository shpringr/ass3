package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;
    private static List<String> logOns = new ArrayList<>();
    private static String state = "";
    private static int block = 0;

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

                handleDirPacket((DIRQPacket) message);
                break;

            case 7:
                handleLoginPacket((LOGRQPackets) message);
                break;

            case 8:
                handleDelReqPacket((DELRQPackets) message);
                break;

            case 9:

                ((BCASTPackets)message).toByteArr();
                break;
            case 10:
                shouldTerminate = true;
                break;
        }

    }

    private void handleDirPacket(DIRQPacket message) {

        File file = new File("");
        File[] files = file.listFiles();

        String filesList = "";
        for (File f: files) {
            filesList += f.getName() + '\0';
        }

        connections.send(connectionId,
                new DATAPackets((short) connectionId, (short)filesList.length(), filesList.getBytes()));
    }

    private void handleDelReqPacket(DELRQPackets message) {
        File file = new File(message.getFilename());
        file.delete();
    }

    private void handleLoginPacket(LOGRQPackets message) {
        String userName = message.getUserName();
        if (logOns.contains(userName))
        {
            connections.send(connectionId, new ERRORPackets((short) ERRORPackets.Errors.ALREADY_LOGGED_IN.ordinal(),
                    ERRORPackets.Errors.ALREADY_LOGGED_IN.getErrorMsg()));
        }
        else
        {
            logOns.add(userName);
            connections.send(connectionId, new ACKPackets((short)0));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
