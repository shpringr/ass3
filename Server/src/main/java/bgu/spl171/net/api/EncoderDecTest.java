package bgu.spl171.net.api;

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
        DIRQtest ();
        DATAtest ();
        ERRORtest ();
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

        System.out.println(tmp instanceof ERRORPacket);
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
        Packet tmp = null;
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

        System.out.println(tmp instanceof DATAPacket);
    }

    private static void DIRQtest() {
        Packet tmp = null;
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
        Packet tmp = null;
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
        System.out.println(tmp instanceof ACKPacket);
    }

    private static void DISCTest() {
        Packet tmp = null;
        byte[] Opcode = shortTObyte((short) 10);
        for (int i = 0 ; i < 2 ; i++){
            try {
                tmp = p.decodeNextByte(Opcode[i]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println(tmp instanceof DISCPacket);
    }

    private static void RRQTest() {
        Packet tmp = null;
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

        System.out.println(tmp instanceof RRQPacket);
    }

    private static void WRQTest() {
        Packet tmp = null;
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

        System.out.println(tmp instanceof WRQPacket);
    }

    private static void DELRQtest() {
        Packet tmp = null;
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

        System.out.println(tmp instanceof DELRQPacket);
    }


    private static void LOGRQtest() {
        Packet tmp = null;
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

        System.out.println(tmp instanceof LOGRQPacket);
    }
}