package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private final static short END_OF_PACKET = -1;
    private final static short ACK_OK = 0;

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;
    private File file = new File("Server/Files");

    private static List<Integer> logOns = new ArrayList<>();
    private static int block = 0;
    private static boolean isFirstCommand = true;
    private LinkedBlockingQueue<DATAPackets> dataQueue = new LinkedBlockingQueue<>();
    private static String state = "";



    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packets message) {

        if (isLegalFirstCommand(message)) {
            switch ((message.getOpCode())) {
                case 1:
                    handleReadPacket((RRQPackets) message);
                    break;

                case 2:
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
                    handleDirqPacket();
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
                    handleDiscPacket((DISCPackets) message);
                    break;
                default:
                    sendError(ERRORPackets.Errors.ILLEGAL_TFTP_OPERATION);
                    break;
            }
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

    }

    private void handleErrorPacket(ERRORPackets message) {
        message.toByteArr();
    }

    private void handleAckPacket(ACKPackets message) {
        if (message.getBlock()!=0) {
            if (state.equals("reading")) {
                DATAPackets dataToSend = dataQueue.poll();
                if (dataToSend != null) {
                    connections.send(connectionId, dataToSend);
                } else {
                    state = "";
                    dataQueue.clear();
                }
            }
            if (state.equals("writing")) {

            }
        }
    }

    private void handleDataPacket(DATAPackets message) {
        message.toByteArr();
    }

    private void handleWritePacket(WRQPackets message) {
        String fileName = message.getFileName();
        boolean fileExciste = false;
        if (file.listFiles()!=null){
            for ( File file : file.listFiles()) {
                if(fileName.equals(file.getName())){
                    fileExciste = true;
                }
            }
        }
        else {
            //TODO throw error
        }
        if (!fileExciste){
            
        }

    }

    private void handleReadPacket(RRQPackets message) {
        String fileName = message.getFileName();
        if (file.listFiles()!=null){
            for ( File file : file.listFiles()) {
                if(fileName.equals(file.getName())){
                    state = "reading";
                    try {
                        getDataQueue(file);
                        //send the first packet
                        DATAPackets dataToSend = dataQueue.poll();
                        if (dataToSend!=null){
                            connections.send(connectionId, dataToSend );
                        }
                    } catch (IOException e) {
                        sendError(ERRORPackets.Errors.FILE_CANT_BE_READ);
                    }
                }
                else{
                    sendError(ERRORPackets.Errors.FILE_NOT_FOUND);
                }
            }
        }
        else{
            sendError(ERRORPackets.Errors.THERE_IS_NO_FILES_IN_THE_SERVER);
        }
    }

    private boolean isLegalFirstCommand(Packets message) {
        if (isFirstCommand) {
            isFirstCommand = false;

            if (message.getOpCode() != 7) {
                sendError(ERRORPackets.Errors.NOT_LOGGED_IN);
                return false;
            }
        }

        return true;
    }

    //TODO: make sure it doesnt return file that are uploading.
    //TODO: what happens if the list is more than 512b?
    private void handleDirqPacket() {

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
        try {
            if (file.delete()) {
                connections.send(connectionId, new ACKPackets(ACK_OK));
                broadcastMessage((byte) 0, message.getFilename());
            }
            else
                sendError(ERRORPackets.Errors.FILE_NOT_FOUND);
        } catch (SecurityException e) {
            sendError(ERRORPackets.Errors.ACCESS_VIOLATION);
        }
    }

    private void broadcastMessage(byte delOrIns, String filename) {

        for (Integer conId : logOns)
        {
            connections.send(connectionId, new BCASTPackets(delOrIns, filename));
        }
    }

    private void sendError(ERRORPackets.Errors errorCode) {
        connections.send(connectionId ,
                new ERRORPackets((short) errorCode.ordinal(), errorCode.getErrorMsg()));
    }

    private void handleLoginPacket(LOGRQPackets message) {
        String userName = message.getUserName();

        if (logOns.contains(userName)) {
            sendError(ERRORPackets.Errors.ALREADY_LOGGED_IN);
        } else
            {
            logOns.add(connectionId);
            connections.send(connectionId, new ACKPackets(ACK_OK));
        }
    }

    private void getDataQueue(File file) throws IOException {
        short blockPacket=1;
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] dataBytes = new byte[512];
        short packetSize =  (short)fileInputStream.read(dataBytes);
        while (packetSize ==512) {
            DATAPackets dataToSend = new DATAPackets(packetSize, blockPacket, dataBytes);
            dataQueue.add(dataToSend);
            blockPacket++;
            packetSize = (short)fileInputStream.read(dataBytes);
        }
        if (packetSize==512){
            byte[] lastDataBytes = new byte[0];
            DATAPackets dataToSend = new DATAPackets((short)0, blockPacket, lastDataBytes );
            dataQueue.add(dataToSend);
        }
        else {
            dataBytes = Arrays.copyOf(dataBytes, packetSize);
            DATAPackets dataToSend = new DATAPackets(packetSize, blockPacket, dataBytes);
            dataQueue.add(dataToSend);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
