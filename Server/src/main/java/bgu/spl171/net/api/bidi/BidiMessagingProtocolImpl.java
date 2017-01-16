package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private final static short END_OF_PACKET = -1;
    private final static short ACK_OK = 0;

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;
    private File file = new File("Server/Files");

    private static List<String> logOns = new ArrayList<>();
    private static String state = "";
    private static int block = 0;
    private static boolean isFirstCommand = true;
    private static ConcurrentHashMap<byte[], Integer> dataBytes;

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packets message) {

        checkIfLoggedIn(message);

        switch ((message.getOpCode())){
            case 1 :
                handleReadPacket((RRQPackets) message);
                break;

            case 2 :
                handleWritePacket((WRQPackets) message);
                break;

            case 3:
                handleDataPacket((DATAPackets) message);
                break;

            case 4:
                handleAckPacket((ACKPackets) message);
                break;

            case 5:
                handleErrorPacket((ERRORPackets) message);
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
                handleBCastPacket((BCASTPackets) message);
                break;

            case 10:
                handleDiscPacket((DISCPackets)message);
                break;
        }
    }

    private void handleDiscPacket(DISCPackets message) {
        try {
            connections.disconnect(connectionId);
            shouldTerminate = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBCastPacket(BCASTPackets message) {
        message.toByteArr();
    }

    private void handleErrorPacket(ERRORPackets message) {
        message.toByteArr();
    }

    private void handleAckPacket(ACKPackets message) {
        block = message.getBlock();
    }

    private void handleDataPacket(DATAPackets message) {
        message.toByteArr();
    }

    private void handleWritePacket(WRQPackets message) {
        message.getFileName();
    }

    private void handleReadPacket(RRQPackets message) {
        String fileName = message.getFileName();
        state = "reading";
        short blockPacket=1;
        if (file.listFiles()!=null){
            for ( File file : file.listFiles()) {
                if(fileName.equals(file.getName())){
                    try {
                        dataBytes = getVectorOfBytesArr(file);
                        while (state.equals("reading")){

                            for ( byte[] bytes: dataBytes.keySet()) {
                                short packetSize = dataBytes.get(bytes).shortValue();
                                DATAPackets dataToSend = new DATAPackets(packetSize, blockPacket, bytes);
                                connections.send(connectionId, dataToSend);
                                while (blockPacket>block)
                                {}
                                blockPacket++;
                            }
                        }



                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void checkIfLoggedIn(Packets message) {
        if (isFirstCommand) {
            isFirstCommand = false;

            if (message.getOpCode() != 7) {
                connections.send(connectionId, new ERRORPackets((short) ERRORPackets.Errors.NOT_LOGGED_IN.ordinal(),
                        ERRORPackets.Errors.NOT_LOGGED_IN.getErrorMsg()));
            }

        }
    }

    private void handleDirPacket(DIRQPacket message) {

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

        if (logOns.contains(userName)) {
            connections.send(connectionId, new ERRORPackets((short) ERRORPackets.Errors.ALREADY_LOGGED_IN.ordinal(),
                    ERRORPackets.Errors.ALREADY_LOGGED_IN.getErrorMsg()));
        } else {
            logOns.add(userName);
            connections.send(connectionId, new ACKPackets((short) 0));
        }
    }

    private ConcurrentHashMap<byte[] , Integer > getVectorOfBytesArr(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] dataBytes = new byte[512];
        ConcurrentHashMap<byte[] , Integer > mapOfByteArr = new ConcurrentHashMap<>();
        int dvs = fileInputStream.read(dataBytes);
        while (dvs ==512) {
            mapOfByteArr.put(dataBytes, dvs);
            dvs = fileInputStream.read(dataBytes);
        }
        mapOfByteArr.put(dataBytes, dvs);
        ;
        return mapOfByteArr;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
