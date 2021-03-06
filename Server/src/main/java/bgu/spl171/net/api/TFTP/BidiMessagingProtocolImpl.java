package bgu.spl171.net.api.TFTP;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.packet.*;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static bgu.spl171.net.api.packet.ERRORPacket.Errors.*;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {

    private final static short ACK_OK = 0;
    private static ConcurrentMap<Integer, String> logOns = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, File> uploadingFiles = new ConcurrentHashMap<>();
    private final static File file = new File("Files");
    private static Connections<Packet> connections;
    private static final Object lockCreateFile = new Object();
    private static final Object lockReadFile = new Object();


    private Integer connectionId;
    private boolean shouldTerminate = false;
    private boolean isFirstCommand = true;
    private LinkedBlockingQueue<DATAPacket> dataQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<DATAPacket> dirqQueue = new LinkedBlockingQueue<>();
    private String state = "";

    @Override
    public void start(int connectionId, Connections<Packet> pConnections) {
        connections = pConnections;
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
        state="disc";
        connections.send(connectionId, new ACKPacket(ACK_OK));
    }

    private void handleBCastPacket() {
        sendError(ERRORPacket.Errors.NOT_DEFINED, "called BCast on server side!");
    }

    private void handleErrorPacket() {
        state ="";
        if (uploadingFiles.get(connectionId) != null)
        {
            File file = uploadingFiles.get(connectionId);
            file.delete();
            uploadingFiles.remove(connectionId);
        }

        dataQueue.clear();
        dirqQueue.clear();
    }

    private void handleAckPacket(ACKPacket message) {
        if (state.equals("reading")) {
            DATAPacket dataToSend = dataQueue.poll();
            if (dataToSend != null) {
                connections.send(connectionId, dataToSend);
            } else {
                state = "";
                dataQueue.clear();
            }
        } else if (state.equals("dirq")) {
            DATAPacket dataToSend = dirqQueue.poll();
            if (dataToSend != null) {
                connections.send(connectionId, dataToSend);
            } else {
                state = "";
                dirqQueue.clear();
            }
        } else if (state.equals("disc")) {
            try {
                connections.disconnect(connectionId);
                logOns.remove(connectionId);
                state = "";
                shouldTerminate = true;
            } catch (IOException e) {
                sendError(ERRORPacket.Errors.NOT_DEFINED, e.getMessage());
            }
        }
    }

    private void handleDataPacket(DATAPacket message) {
        if (state.equals("writing")){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(uploadingFiles.get(connectionId),message.getBlock()!= 1);
                fileOutputStream.write(message.getData());
                connections.send(connectionId, new ACKPacket(message.getBlock()));
                fileOutputStream.close();

                if (message.getPacketSize() != 512){
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
        File fileToWrite = new File("Files" + File.separator+ fileNameToWrite);

        // tries to create new file in the system
        try {
            synchronized (lockCreateFile)
            {
                boolean createFile = fileToWrite.createNewFile();
                if (createFile) {
                    uploadingFiles.put(connectionId, fileToWrite);
                    state = "writing";
                    connections.send(connectionId, new ACKPacket(ACK_OK));
                }
                else
                    {
                    sendError(ERRORPacket.Errors.FILE_ALREADY_EXISTS,"");
                }
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
                    if (fileName.equals(file.getName()) && !uploadingFiles.containsValue(file)) {
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
        if(files!=null && files.length != 0){
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
        else{
            sendError( ERRORPacket.Errors.NOT_DEFINED , "");
        }

    }

    private void handleDelReqPacket(DELRQPacket message) {
        File fileToDel = new File("Files" + File.separator+ message.getFilename());
        try {
            if (!uploadingFiles.containsValue(fileToDel)) {
                if (fileToDel.delete()) {
                    connections.send(connectionId, new ACKPacket(ACK_OK));
                    broadcastMessageToLogons((byte) 0, message.getFilename());
                } else
                    sendError(ERRORPacket.Errors.FILE_NOT_FOUND, "");
            }
            else
            {
                sendError(ERRORPacket.Errors.NOT_DEFINED, "");
            }
        } catch (SecurityException e) {
            sendError(ERRORPacket.Errors.ACCESS_VIOLATION, "");
        }
    }

    private void broadcastMessageToLogons(byte delOrIns, String filename) {
        for (Integer conId : logOns.keySet())
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

        if (logOns.containsValue(userName) || logOns.containsKey(connectionId)) {
            sendError(ERRORPacket.Errors.ALREADY_LOGGED_IN, "");
        } else
            {
            logOns.put(connectionId, userName);
            connections.send(connectionId, new ACKPacket(ACK_OK));
        }
    }

    private void putStringIntoDirqQueue(String string)
    {
        int maxIndex = (string.length() / 512) + 1;
        int lastPartSize = (string.length() % 512) == 0  ? 512 : (string.length() % 512);

        for (int i = 1; i <= maxIndex; i++) {
            String currMessage ;
            if (i==maxIndex)
                currMessage = string.substring((i-1)*512, (lastPartSize + (i-1)*512) - 1);
            else
                currMessage = string.substring((i-1)*512,((i-1)*512+512) - 1);

            dirqQueue.add(new DATAPacket((short)currMessage.length(),
                    (short)i, currMessage.getBytes()));
        }
    }
    private void readFileIntoDataQueue(File file) throws IOException {
        synchronized (lockReadFile) {
            short blockPacket = 1;

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] dataBytes = new byte[512];
            short packetSize = (short) fileInputStream.read(dataBytes);
            while (packetSize == 512) {
                DATAPacket dataToSend = new DATAPacket(packetSize, blockPacket, dataBytes);
                dataQueue.add(dataToSend);
                blockPacket++;
                dataBytes = new byte[512];
                packetSize = (short) fileInputStream.read(dataBytes);
            }
            if (packetSize == -1) {
                byte[] lastDataBytes = new byte[0];
                DATAPacket dataToSend = new DATAPacket((short) 0, blockPacket, lastDataBytes);
                dataQueue.add(dataToSend);
            } else {
                dataBytes = Arrays.copyOf(dataBytes, packetSize);
                DATAPacket dataToSend = new DATAPacket(packetSize, blockPacket, dataBytes);
                dataQueue.add(dataToSend);
            }
            fileInputStream.close();
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}