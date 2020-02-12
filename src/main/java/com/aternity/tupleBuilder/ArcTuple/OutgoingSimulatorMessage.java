package com.aternity.tupleBuilder.ArcTuple;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: seagull
 * Date: 30/06/14
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class OutgoingSimulatorMessage {

    protected ByteArrayOutputStream baos;
    protected int length = 0;
    private final boolean addMessageLength;

    protected OutgoingSimulatorMessage(byte opcode, boolean addMessageLength) throws IOException {
        this.addMessageLength = addMessageLength;
        baos = new ByteArrayOutputStream();
        if (this.addMessageLength){
            baos.write(new byte[]{0, 0, 0, 0});
        }
        baos.write(opcode);
        length++;
    }

    /**
     * write a string value of double into float bytes
     */
    protected void writeDouble(Double value) {
        int bits = Float.floatToRawIntBits(value.floatValue());
        baos.write(bits & 0xFF);
        baos.write((bits >> 8) & 0xFF);
        baos.write((bits >> 16) & 0xFF);
        baos.write((bits >> 24) & 0xFF);
        length += 4;
    }

    protected void writeDouble(String value) {
        writeDouble(Double.valueOf(value));
    }

    /**
     * Write to the stream 'F' for false 'T' for true.
     */
    protected void writeBool(Boolean value) {
        boolean b = Boolean.valueOf(value);
        baos.write((b) ? (byte) 'T' : (byte) 'F');
        length += 1;
    }

    protected void writeBool(String value) {
        writeBool(Boolean.valueOf(value));
    }

    protected void writeInteger(Integer value) {
        baos.write(value & 0xFF);
        baos.write((value >> 8) & 0xFF);
        baos.write((value >> 16) & 0xFF);
        baos.write((value >> 24) & 0xFF);
        length += 4;
    }

    protected void writeInteger(String value) {
        writeInteger(Integer.valueOf(value));
    }

    protected byte[] getIntegerBytes(int i) {
        byte[] b = new byte[4];

        b[0] = (byte) (i & 0xFF);
        b[1] = (byte) (i >> 8 & 0xFF);
        b[2] = (byte) (i >> 16 & 0xFF);
        b[3] = (byte) (i >> 24 & 0xFF);

        return b;
    }

    protected void writeShort(short value) {
        baos.write(value & 0xFF);
        baos.write((value >> 8) & 0xFF);
        length += 2;
    }

    protected void writeStringShortPrefix(String value) throws IOException {
        writeShort((short) value.length());
        baos.write(value.getBytes());
        length += value.length();
    }

    /**
     * length-prefixed string
     *
     * @param value
     * @throws IOException
     */
    protected void writeStringIntPrefix(String value) throws IOException {
        writeInteger(value.length());
        baos.write(value.getBytes());
        length += value.length();
    }

    protected void writeByte(String value) {
        short val = Short.valueOf(value);
        baos.write(val & 0xFF);
        length += 1;
    }

    protected void writeByte(int value) {
        baos.write(value & 0xFF);
        length += 1;
    }

    protected void writeBytes(byte[] bytes) throws IOException {
        baos.write(bytes);
        length += bytes.length;
    }


    protected void writeLong(long n) throws IOException {

        byte[] b = new byte[8];
        b[0] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[3] = (byte) (n);
        n >>>= 8;
        b[4] = (byte) (n);
        n >>>= 8;
        b[5] = (byte) (n);
        n >>>= 8;
        b[6] = (byte) (n);
        n >>>= 8;
        b[7] = (byte) (n);
        baos.write(b);
        length += 8;
    }

    public byte[] encode() throws IOException {
        byte[] output = baos.toByteArray();
        if (addMessageLength) {
            byte[] lenBytes = getIntegerBytes(length);
            System.arraycopy(lenBytes, 0, output, 0, 4);
        }
        return output;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

	public byte[] encode(String fileStr) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
