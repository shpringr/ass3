package bgu.spl171.net.api.bidi;

import bgu.spl171.net.impl.packet.*;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packets> {

    private boolean shouldTerminate = false;
    private Connections connections;
    private int connectionId;
    private static List<String> logOns = new ArrayList<>();
    private static String state = "";
    private static int block = 0;

    File file = new File("Server/Files");
    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packets message) {
        switch ((message.getOpCode())){
            case 1 :
                String fileName = ((RRQPackets)message).getFileName();
                state = "reading";
                short blockPacket=1;
                if (file.listFiles()!=null){
                    for ( File file : file.listFiles()) {
                        if(fileName.equals(file.getName())){
                            try {
                                ConcurrentHashMap<byte[], Integer> dataBytes = getVectorOfBytesArr(file);
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
