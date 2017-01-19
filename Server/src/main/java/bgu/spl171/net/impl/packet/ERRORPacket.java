package bgu.spl171.net.impl.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ERRORPacket extends Packet {
    short errorCode;
    String errMsg;

    public enum Errors {
        NOT_DEFINED("Not defined, see error message (if any)"),
        FILE_NOT_FOUND("File not found – RRQ \\ DELRQ of non-existing file"),
        ACCESS_VIOLATION("Access violation – File cannot be written, read or deleted."),
        DISK_FULL("Disk full or allocation exceeded – No room in disk."),
        ILLEGAL_TFTP_OPERATION("Illegal TFTP operation – Unknown Opcode."),
        FILE_ALREADY_EXISTS("File already exists – File name exists on WRQ."),
        NOT_LOGGED_IN("User not logged in – Any opcode received before Login completes."),
        ALREADY_LOGGED_IN("User already logged in – Login username already connected.");

        private String errorMsg;

        Errors(String msg) {
            errorMsg = msg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    public ERRORPacket(short errorCode, String errMsg) {
        this.errorCode = errorCode;
        this.errMsg = errMsg;
        super.opCode = 5;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    @Override
    public byte[] toByteArr() {
        try {
            byte[] msgBytes = errMsg.getBytes("UTF-8");
        ByteBuffer lengthBuffer = ByteBuffer.allocate(2+2+msgBytes.length+1);
        lengthBuffer.put(shortToBytes(opCode));
        lengthBuffer.put(shortToBytes(errorCode));
        lengthBuffer.put(msgBytes);

        lengthBuffer.put((byte)0);
        return lengthBuffer.array();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }

    }
}
