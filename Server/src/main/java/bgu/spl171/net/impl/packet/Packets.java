package bgu.spl171.net.impl.packet;

public abstract class Packets {
    protected short opCode;

    public short getOpCode() {
        return opCode;
    }

   // protected abstract void parssed(byte[] byteArr);

   public abstract byte[] toByteArr();


    //Encode short to 2 bytes
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


}
