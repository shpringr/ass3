package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static bgu.spl171.net.impl.packet.ERRORPacket.Errors.*;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {

    private final static short END_OF_PACKET = -1;
    private final static short ACK_OK = 0;

    private boolean shouldTerminate = false;
    private Connections connections;
    private Integer connectionId;
    private File file = new File("Server/Files");

    private static List<Integer> logOns = new ArrayList<>();
    private static int block = 0;
    private static boolean isFirstCommand = true;
    private LinkedBlockingQueue<DATAPacket> dataQueue = new LinkedBlockingQueue<>();
    private static String state = "";
    File fileToWrite =null;



    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packet message) {

        if (isLegalFirstCommand(message)) {
            switch ((message.getOpCode())) {
                case 1:
                    handleReadPacket((RRQPacket) message);
                    break;

                case 2:
                    handleWritePacket((WRQPacket) message);
                    break;

                case 3:
                    handleDataPacket((DATAPacket) message);
                    break;

                case 4:
                    handleAckPacket((ACKPacket) message);
                    break;

                case 5:
                    handleErrorPacket();
                    break;

                case 6:
                    handleDirqPacket();
                    break;

                case 7:
                    handleLoginPacket((LOGRQPacket) message);
                    break;

                case 8:
                    handleDelReqPacket((DELRQPacket) message);
                    break;

                case 9:
                    handleBCastPacket();
                    break;

                case 10:
                    handleDiscPacket();
                    break;
                default:
                    sendError(ERRORPacket.Errors.ILLEGAL_TFTP_OPERATION, "");
                    break;
            }
        }
    }

    private void handleDiscPacket() {
        try {
            connections.disconnect(connectionId);
            logOns.remove(connectionId);
            connections.send(connectionId, new ACKPacket((short)0));
            shouldTerminate = true;
        } catch (IOException e) {
            sendError(ERRORPacket.Errors.NOT_DEFINED, e.getMessage());
        }
    }

    private void handleBCastPacket() {
        sendError(ERRORPacket.Errors.NOT_DEFINED, "");
    }

    //TODO: can we get errors from client to server?
    private void handleErrorPacket() {
        sendError(ERRORPacket.Errors.NOT_DEFINED, "");
    }

    private void handleAckPacket(ACKPacket message) {
        if (message.getBlock()!=0) {
            if (state.equals("reading")) {
                DATAPacket dataToSend = dataQueue.poll();
                if (dataToSend != null) {
                    connections.send(connectionId, dataToSend);
                } else {
                    state = "";
                    dataQueue.clear();
                }
            }
        }
    }

    private void handleDataPacket(DATAPacket message) {
        if (state.equals("writing")){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileToWrite);
                if (message.getPacketSize() == 512){
                    fileOutputStream.write(message.getData());
                    connections.send(connectionId, new ACKPacket(message.getBlock()));
                }
                else{
                    fileOutputStream.write(message.getData());
                    fileOutputStream.close();
                    connections.send(connectionId, new ACKPacket(message.getBlock()));
                    connections.broadcast(new BCASTPacket((byte) 1, fileToWrite.getName()));
                    state="";
                }
            } catch (FileNotFoundException e) {
                sendError(NOT_DEFINED,e.getMessage());
            } catch (IOException e) {
                sendError(NOT_DEFINED,e.getMessage());
            }
        }
    }

    private void handleWritePacket(WRQPacket message) {
        String fileNameToWrite = message.getFileName();

        // create new file
        fileToWrite = new File(file.getPath() + "/" + fileNameToWrite);
        // tries to create new file in the system
        boolean createFile = false;
        try {
            createFile = fileToWrite.createNewFile();
            if (createFile){
                connections.send(connectionId, new ACKPacket((short)0));
                state = "writing";
            }
            else {
                //TODO ???
                sendError(NOT_DEFINED,"FILE_ALREADY_EXISTS");
            }
        } catch (IOException e) {
            sendError(NOT_DEFINED,e.getMessage());
        }

    }

    private void handleReadPacket(RRQPacket message) {
        String fileName = message.getFileName();
        if (file.listFiles()!=null){
            for ( File file : file.listFiles()) {
                if(fileName.equals(file.getName())){
                    state = "reading";
                    try {
                        readFileIntoDataQueue(file);
                        //send the first packet
                        DATAPacket dataToSend = dataQueue.poll();
                        if (dataToSend!=null){
                            connections.send(connectionId, dataToSend );
                        }
                    } catch (IOException e) {
                        sendError(NOT_DEFINED,e.getMessage());
                    }
                }
                else{
                    sendError(ERRORPacket.Errors.FILE_NOT_FOUND,"");
                }
            }
        }
        else{
            sendError(NOT_DEFINED,"THERE_IS_NO_FILES_IN_THE_SERVER");
        }
    }

    private boolean isLegalFirstCommand(Packet message) {
        if (isFirstCommand) {
            isFirstCommand = false;

            if (message.getOpCode() != 7) {
                sendError(ERRORPacket.Errors.NOT_LOGGED_IN, "");
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
                new DATAPacket(connectionId.shortValue(), (short)filesList.length(), filesList.getBytes()));
    }

    private void handleDelReqPacket(DELRQPacket message) {
        File file = new File(message.getFilename());
        try {
            if (file.delete()) {
                connections.send(connectionId, new ACKPacket(ACK_OK));
                broadcastMessage((byte) 0, message.getFilename());
            }
            else
                sendError(ERRORPacket.Errors.FILE_NOT_FOUND, "");
        } catch (SecurityException e) {
            sendError(ERRORPacket.Errors.ACCESS_VIOLATION, "");
        }
    }

    private void broadcastMessage(byte delOrIns, String filename) {

        for (Integer conId : logOns)
        {
            connections.send(connectionId, new BCASTPacket(delOrIns, filename));
        }
    }

    private void sendError(ERRORPacket.Errors errorCode, String extraMsg) {
        connections.send(connectionId ,
                new ERRORPacket((short) errorCode.ordinal(), errorCode.getErrorMsg() + extraMsg));
    }

    private void handleLoginPacket(LOGRQPacket message) {
        String userName = message.getUserName();

        if (logOns.contains(userName)) {
            sendError(ERRORPacket.Errors.ALREADY_LOGGED_IN, "");
        } else
            {
            logOns.add(connectionId);
            connections.send(connectionId, new ACKPacket(ACK_OK));
        }
    }

    private void readFileIntoDataQueue(File file) throws IOException {
        short blockPacket=1;
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] dataBytes = new byte[512];
        short packetSize =  (short)fileInputStream.read(dataBytes);
        while (packetSize ==512) {
            DATAPacket dataToSend = new DATAPacket(packetSize, blockPacket, dataBytes);
            dataQueue.add(dataToSend);
            blockPacket++;
            packetSize = (short)fileInputStream.read(dataBytes);
        }
        if (packetSize==512){
            byte[] lastDataBytes = new byte[0];
            DATAPacket dataToSend = new DATAPacket((short)0, blockPacket, lastDataBytes );
            dataQueue.add(dataToSend);
        }
        else {
            dataBytes = Arrays.copyOf(dataBytes, packetSize);
            DATAPacket dataToSend = new DATAPacket(packetSize, blockPacket, dataBytes);
            dataQueue.add(dataToSend);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}