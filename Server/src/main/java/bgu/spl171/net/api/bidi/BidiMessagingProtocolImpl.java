package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static bgu.spl171.net.impl.packet.ERRORPacket.Errors.*;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {

    private final static short ACK_OK = 0;
    private static List<Integer> logOns = new ArrayList<>();
    private static ConcurrentMap<Integer, File> uploadingFiles = new ConcurrentHashMap<>();
    private final static File file = new File("Server/Files");

    private boolean shouldTerminate = false;
    private Connections<Packet> connections;
    private Integer connectionId;
    private boolean isFirstCommand = true;
    private LinkedBlockingQueue<DATAPacket> dataQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<DATAPacket> dirqQueue = new LinkedBlockingQueue<>();
    private String state = "";

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
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
            connections.send(connectionId, new ACKPacket(ACK_OK));
            shouldTerminate = true;
        } catch (IOException e) {
            sendError(ERRORPacket.Errors.NOT_DEFINED, e.getMessage());
        }
    }

    private void handleBCastPacket() {
        sendError(ERRORPacket.Errors.NOT_DEFINED, "called BCast on server side!");
    }

    private void handleErrorPacket() {
        state ="";
        if (uploadingFiles.get(connectionId) != null)
        {
            uploadingFiles.remove(connectionId);
        }

        dataQueue.clear();
        dirqQueue.clear();
    }

    private void handleAckPacket(ACKPacket message) {
        if (message.getBlock()!=0)
        {
            if (state.equals("reading")) {
                DATAPacket dataToSend = dataQueue.poll();
                if (dataToSend != null) {
                    connections.send(connectionId, dataToSend);
                } else {
                    state = "";
                    dataQueue.clear();
                }
            }
            else if (state.equals("dirq"))
            {
                DATAPacket dataToSend = dirqQueue.poll();
                if (dataToSend != null) {
                    connections.send(connectionId, dataToSend);
                } else {
                    state = "";
                    dirqQueue.clear();
                }
            }
        }
    }

    private void handleDataPacket(DATAPacket message) {
        if (state.equals("writing")){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(uploadingFiles.get(connectionId));
                fileOutputStream.write(message.getData());
                connections.send(connectionId, new ACKPacket(message.getBlock()));

                if (message.getPacketSize() != 512){
                    fileOutputStream.close();
                    broadcastMessageToLogons((byte) 1, uploadingFiles.get(connectionId).getName());
                    uploadingFiles.remove(connectionId);
                    state="";
                }
            } catch (FileNotFoundException e) {
                sendError(FILE_NOT_FOUND, "");
            } catch (IOException e) {
                sendError(NOT_DEFINED,e.getMessage());
            }
        }
    }

    private void handleWritePacket(WRQPacket message) {
        String fileNameToWrite = message.getFileName();

        // create new file
        File fileToWrite = new File(file.getPath() + "/" + fileNameToWrite);

        // tries to create new file in the system
        try {
            boolean createFile = fileToWrite.createNewFile();
            if (createFile){
                uploadingFiles.put(connectionId, fileToWrite);
                state = "writing";
                connections.send(connectionId, new ACKPacket(ACK_OK));

            }
            else {
                sendError(ERRORPacket.Errors.FILE_ALREADY_EXISTS,"");
            }
        } catch (IOException e) {
            sendError(NOT_DEFINED,e.getMessage());

        } catch (SecurityException e) {
            sendError(ERRORPacket.Errors.ACCESS_VIOLATION, "");
        }
    }

    private void handleReadPacket(RRQPacket message) {
        String fileName = message.getFileName();
        boolean isFileFound = false;
        try {
            if (file.listFiles() != null) {
                for (File file : file.listFiles()) {
                    if (fileName.equals(file.getName())) {
                        state = "reading";
                        isFileFound = true;
                        try {
                            readFileIntoDataQueue(file);
                            //send the first packet
                            DATAPacket dataToSend = dataQueue.poll();
                            if (dataToSend != null) {
                                connections.send(connectionId, dataToSend);
                            }
                        } catch (IOException e) {
                            sendError(NOT_DEFINED, e.getMessage());
                        }
                        break;
                    }

                }
            }

            if (!isFileFound) {
                sendError(FILE_NOT_FOUND, "");
            }
        } catch (SecurityException e) {
            sendError(ERRORPacket.Errors.ACCESS_VIOLATION, "");
        }

    }
    private boolean isLegalFirstCommand(Packet message) {
        if (isFirstCommand) {
            if (message.getOpCode() != 7) {
                sendError(ERRORPacket.Errors.NOT_LOGGED_IN, "");
                return false;
            }
            else {
                isFirstCommand = false;
            }
        }

        return true;
    }

    private void handleDirqPacket() {

        File[] files = file.listFiles();
        List<String> filesToIgnore = new ArrayList<>();

        for (File currUpload: uploadingFiles.values()) {
            filesToIgnore.add(currUpload.getName());
        }

        String filesList = "";
        for (File f: files) {
            if (!filesToIgnore.contains(f.getName()))
                filesList += f.getName() + '\0';
        }

        state = "dirq";
        putStringIntoDirqQueue(filesList);

        DATAPacket dataToSend = dirqQueue.poll();
        if (dataToSend != null) {
            connections.send(connectionId, dataToSend);
        }
    }

    private void handleDelReqPacket(DELRQPacket message) {
        File file = new File(message.getFilename());
        try {
            if (file.delete()) {
                connections.send(connectionId, new ACKPacket(ACK_OK));
                broadcastMessageToLogons((byte) 0, message.getFilename());
            }
            else
                sendError(ERRORPacket.Errors.FILE_NOT_FOUND, "");
        } catch (SecurityException e) {
            sendError(ERRORPacket.Errors.ACCESS_VIOLATION, "");
        }
    }

    private void broadcastMessageToLogons(byte delOrIns, String filename) {
        for (Integer conId : logOns)
        {
            connections.send(conId, new BCASTPacket(delOrIns, filename));
        }
    }

    private void sendError(ERRORPacket.Errors errorCode, String extraMsg) {
        connections.send(connectionId ,
                new ERRORPacket((short) errorCode.ordinal(), errorCode.getErrorMsg() + extraMsg + '\0'));
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

    private void putStringIntoDirqQueue(String string)
    {
        int maxIndex = (string.length() / 512) + 1;

        for (int i = 1; i <= maxIndex; i++) {
            String nextMessage = string.substring(i,512);
            dirqQueue.add(new DATAPacket((short)nextMessage.length(), (short) maxIndex, string.getBytes()));
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
        if (packetSize==-1){
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