package bgu.spl171.net.api;

import bgu.spl171.net.impl.TFTP.MessageEncoderDecoderImpl;
import bgu.spl171.net.impl.packet.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

/**
 * Created by Ofir & Nitsan on 11/01/2017.
 */
public class EncoderDecTest {

    private static MessageEncoderDecoder<Packet> p = new MessageEncoderDecoderImpl();

    public static void main (String []args){

        // log request test
        LOGRQtest();
        DELRQtest();
        WRQTest ();
        RRQTest ();
        DISCTest ();
        ACKTest ();
        BCASTTest();
        DIRQtest ();
        DATAtest ();
        ERRORtest ();
    }

    private static void BCASTTest() {
        Packet tmp = null;
        String filename = "azov_oti_beshket";
        BCASTPacket bcastPacket= new BCASTPacket((byte)0, filename);
        byte [] en = bcastPacket.toByteArr();
        for (int i = 0 ; i < en.length ; i++){
            try {
                tmp = p.decodeNextByte(en[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("BCASTPacket - ");
        if (tmp instanceof BCASTPacket)
            System.out.println(((BCASTPacket) tmp).getFileName().equals(filename)&&
                    ((BCASTPacket) tmp).getDeletedAdd() == (byte)0);
        else
            System.out.println(false);
    }

    private static void ERRORtest() {
        Packet tmp = null;
        String error = "azov_oti_beshket";
        ERRORPacket er = new ERRORPacket((short) 4, error);
        byte [] en = er.toByteArr();
        for (int i = 0 ; i < en.length ; i++){
            try {
                tmp = p.decodeNextByte(en[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("ERRORPacket - ");
        if (tmp instanceof ERRORPacket)
            System.out.println(((ERRORPacket) tmp).getErrMsg().equals(error)&&
                    ((ERRORPacket) tmp).getErrorCode() == (short)4);
        else
            System.out.println(false);
    }

    private static byte[] shortTObyte(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private static void DATAtest() {
        Random rnd = new Random(new Date().getTime());
        Packet tmp = null;
        byte[] data = new byte[400];
        for (int i = 0; i < 400; i++){
            data[i] = (byte) (rnd.nextInt(255) + 1);
        }

        DATAPacket dataPacket = new DATAPacket((short)400, (short) 7, data);
        byte[] bytes = dataPacket.toByteArr();
//        byte[] size = shortTObyte((short)400);
//        byte[] Opcode = shortTObyte((short) 3);
//        byte[] block = shortTObyte((short) 7);
//        byte[] bytes = new byte[6 + 400];
//        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1] ; bytes[2] =  size[0] ; bytes[3] = size[1] ;bytes[4] = block[0] ;bytes[5] = block[1];
//        for (int i = 6 ; i < bytes.length ; i++){
//            bytes[i] = (byte) (rnd.nextInt(255) + 1);
//        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.print("DATAPacket - ");
        if (tmp instanceof DATAPacket) {
            boolean issame = true;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != ((DATAPacket) tmp).getData()[i])
                    issame = false;
            }

                System.out.println(((DATAPacket) tmp).getBlock() == (short) 7 &&
                        ((DATAPacket) tmp).getPacketSize() == (short) 400 &&
                        issame);

        }
        else
        {
            System.out.println(false);
        }

    }

    private static void DIRQtest() {
        Packet tmp = null;
        byte[] Opcode = shortTObyte((short) 6);
        DIRQPacket dirqPacket = new DIRQPacket();
        Opcode = dirqPacket.toByteArr();
        for (int i = 0 ; i < 2 ; i++){
            try {
                tmp = p.decodeNextByte(Opcode[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("DIRQPacket - ");
        System.out.println(tmp instanceof DIRQPacket);
    }

    private static void ACKTest() {
        Packet tmp = null;
//        byte[] Opcode = shortTObyte((short) 4);
//        byte[] block = shortTObyte((short) 0);
//        byte[] bytes = new byte[] {Opcode[0] , Opcode[1] , block[0] , block[1]};
        ACKPacket ackPacket = new ACKPacket((short)0);
        byte[]bytes = ackPacket.toByteArr();
        for (int i = 0 ; i < 4 ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("ACKPacket - ");
        if (tmp instanceof ACKPacket)
            System.out.println(((ACKPacket) tmp).getBlock()==(short) 0);
        else
            System.out.println(false);
        }

    private static void DISCTest() {
        Packet tmp = null;
        byte[] Opcode = shortTObyte((short) 10);
        DISCPacket discPacket = new DISCPacket();
        Opcode = discPacket.toByteArr();

        for (int i = 0 ; i < 2 ; i++){
            try {
                tmp = p.decodeNextByte(Opcode[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("DISCPacket - ");
        System.out.println(tmp instanceof DISCPacket);
    }

    private static void RRQTest() {
        Packet tmp = null;
        String fileName = "I-have-a-programmer-hands";
        RRQPacket rrqPacket = new RRQPacket(fileName);
//        byte[] name = (fileName + '\0').getBytes();
//        byte[] Opcode = shortTObyte((short) 1);
//        byte[] bytes = new byte[2 + name.length];
//        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }

        byte[] bytes = rrqPacket.toByteArr();

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.print("RRQPacket - ");
        if(tmp instanceof RRQPacket)
        {
            System.out.println(((RRQPacket)tmp).getFileName().equals(fileName));
        }
        else
            System.out.println(false);
    }

    private static void WRQTest() {
        Packet tmp = null;
        String fileName = "chocolate_or_strongberry";
        WRQPacket  wrqPacket = new WRQPacket(fileName);
//        byte[] name = (fileName + '\0').getBytes();
//        byte[] Opcode = shortTObyte((short) 2);
//        byte[] bytes = new byte[2 + name.length];
//        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }

        byte[] bytes = wrqPacket.toByteArr();
        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("WRQPacket - ");
        if(tmp instanceof WRQPacket)
        {
            System.out.println(((WRQPacket)tmp).getFileName().equals(fileName));
        }
        else
            System.out.println(false);
    }

    private static void DELRQtest() {
        Packet tmp = null;
        String fileName = "Neto-Avoda";
//        byte[] name = (fileName + '\0').getBytes();
//        byte[] Opcode = shortTObyte((short) 8);
//        byte[] bytes = new byte[2 + name.length];
//        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }

        DELRQPacket delrqPacket = new DELRQPacket(fileName);
        byte[] bytes = delrqPacket.toByteArr();
        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("DELRQPacket - ");
        if(tmp instanceof DELRQPacket)
        {
            System.out.println(((DELRQPacket)tmp).getFilename().equals(fileName));
        }
        else
            System.out.println(false);
    }


    private static void LOGRQtest() {
        Packet tmp = null;
        String nickName = "Spl_Took_My_Life";
//        byte[] name = (nickName + '\0').getBytes();
//        byte[] Opcode = shortTObyte((short)7);
//        byte[] bytes = new byte [2 + name.length];
//        bytes[0] = Opcode[0] ; bytes [1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }

        LOGRQPacket logrqPacket = new LOGRQPacket(nickName);
        byte[] bytes = logrqPacket.toByteArr();
        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.print("LOGRQPacket - ");
        if(tmp instanceof LOGRQPacket)
        {
            System.out.println(((LOGRQPacket)tmp).getUserName().equals(nickName));
        }
        else
            System.out.println(false);
    }
}