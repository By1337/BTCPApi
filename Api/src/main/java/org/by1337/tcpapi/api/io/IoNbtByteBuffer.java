package org.by1337.tcpapi.api.io;

import org.by1337.blib.nbt.NbtByteBuffer;

public class IoNbtByteBuffer implements NbtByteBuffer {
    private final ByteBuffer source;

    public IoNbtByteBuffer(ByteBuffer source) {
        this.source = source;
    }

    @Override
    public void writeByte(byte b) {
        source.writeByte(b);
    }

    @Override
    public void writeByte(int b) {
        source.writeByte(b);
    }

    @Override
    public void writeDouble(double b) {
        source.writeDouble(b);
    }

    @Override
    public double readDouble() {
        return source.readDouble();
    }

    @Override
    public long readVarLong() {
        return source.readVarLong();
    }

    @Override
    public int readVarInt() {
        return source.readVarInt();
    }

    @Override
    public void writeVarLong(long l) {
        source.writeVarLong(l);
    }

    @Override
    public void writeVarInt(int i) {
        source.writeVarInt(i);
    }

    @Override
    public void writeShort(int value) {
        source.writeShort(value);
    }

    @Override
    public short readShort() {
        return source.readShort();
    }

    @Override
    public void writeUtf(String string) {
        source.writeUtf(string);
    }

    @Override
    public String readUtf() {
        return source.readUtf();
    }

    @Override
    public void writeFloat(float f) {
        source.writeFloat(f);
    }

    @Override
    public float readFloat() {
        return source.readFloat();
    }

    @Override
    public byte readByte() {
        return source.readByte();
    }

    @Override
    public byte[] toByteArray() {
        byte[] arr = new byte[source.readableBytes()];
        source.readBytes(arr);
        return arr;
    }

    @Override
    public void readBytes(byte[] arr) {
        source.readBytes(arr);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        source.writeBytes(bytes);
    }
}
