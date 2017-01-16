package bgu.spl171.net.api;

import bgu.spl171.net.impl.packet.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

/**
 * Created by Ofir & Nitsan on 11/01/2017.
 */
public class EncoderDecTest {

    private static MessageEncoderDecoder<Packets> p = new MessageEncoderDecoderImpl();

    public static void main (String []args){

        // log request test
        LOGRQtest();
        DELRQtest();
        WRQTest ();
        RRQTest ();
        DISCTest ();
        ACKTest ();
        DIRQtest ();
        DATAtest ();
        ERRORtest ();
    }

    private static void ERRORtest() {
        Packets tmp = null;
        String error = "azov_oti_beshket";
        ERRORPackets er = new ERRORPackets((short) 4, error);
        byte [] en = er.toByteArr();
        for (int i = 0 ; i < en.length ; i++){
            try {
                tmp = p.decodeNextByte(en[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof ERRORPackets);
    }

    private static byte[] shortTObyte(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private static void DATAtest() {
        Random rnd = new Random(new Date().getTime());
        Packets tmp = null;
        byte[] size = shortTObyte((short)400);
        byte[] Opcode = shortTObyte((short) 3);
        byte[] block = shortTObyte((short) 7);
        byte[] bytes = new byte[6 + 400];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1] ; bytes[2] =  size[0] ; bytes[3] = size[1] ;bytes[4] = block[0] ;bytes[5] = block[1];
        for (int i = 6 ; i < bytes.length ; i++){
            bytes[i] = (byte) (rnd.nextInt(255) + 1);
        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof DATAPackets);
    }

    private static void DIRQtest() {
        Packets tmp = null;
        byte[] Opcode = shortTObyte((short) 6);
        for (int i = 0 ; i < 2 ; i++){
            try {
                tmp = p.decodeNextByte(Opcode[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println(tmp instanceof DIRQPacket);
    }

    private static void ACKTest() {
        Packets tmp = null;
        byte[] Opcode = shortTObyte((short) 4);
        byte[] block = shortTObyte((short) 0);
        byte[] bytes = new byte[] {Opcode[0] , Opcode[1] , block[0] , block[1]};
        for (int i = 0 ; i < 4 ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println(tmp instanceof ACKPackets);
    }

    private static void DISCTest() {
        Packets tmp = null;
        byte[] Opcode = shortTObyte((short) 10);
        for (int i = 0 ; i < 2 ; i++){
            try {
                tmp = p.decodeNextByte(Opcode[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println(tmp instanceof DISCPackets);
    }

    private static void RRQTest() {
        Packets tmp = null;
        String fileName = "I-have-a-programmer-hands";
        byte[] name = (fileName + '\0').getBytes();
        byte[] Opcode = shortTObyte((short) 1);
        byte[] bytes = new byte[2 + name.length];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof RRQPackets);
    }

    private static void WRQTest() {
        Packets tmp = null;
        String fileName = "chocolate_or_strongberry";
        byte[] name = (fileName + '\0').getBytes();
        byte[] Opcode = shortTObyte((short) 2);
        byte[] bytes = new byte[2 + name.length];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof WRQPackets);
    }

    private static void DELRQtest() {
        Packets tmp = null;
        String fileName = "Neto-Avoda";
        byte[] name = (fileName + '\0').getBytes();
        byte[] Opcode = shortTObyte((short) 8);
        byte[] bytes = new byte[2 + name.length];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof DELRQPackets);
    }


    private static void LOGRQtest() {
        Packets tmp = null;
        String nickName = "Spl_Took_My_Life";
        byte[] name = (nickName + '\0').getBytes();
        byte[] Opcode = shortTObyte((short)7);
        byte[] bytes = new byte [2 + name.length];
        bytes[0] = Opcode[0] ; bytes [1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            try {
                tmp = p.decodeNextByte(bytes[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tmp instanceof LOGRQPackets);
    }
}