public abstract class ImageOutputStreamImpl extends ImageInputStreamImpl implements
        ImageOutputStream {
    public ImageOutputStreamImpl() {
    }
    public abstract void write(int b) throws IOException;
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    public abstract void write(byte[] b, int off, int len) throws IOException;
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }
    public void writeByte(int v) throws IOException {
        write(v);
    }
    public void writeShort(int v) throws IOException {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
        } else {
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }
    public void writeInt(int v) throws IOException {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
        } else {
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeLong(long v) throws IOException {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
        } else {
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }
    public void writeBytes(String s) throws IOException {
        write(s.getBytes());
    }
    public void writeChars(String s) throws IOException {
        char[] chs = s.toCharArray();
        writeChars(chs, 0, chs.length);
    }
    public void writeUTF(String s) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeShorts(short[] s, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeChars(char[] c, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeInts(int[] i, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeLongs(long[] l, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeFloats(float[] f, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeDoubles(double[] d, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeBit(int bit) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    public void writeBits(long bits, int numBits) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    protected final void flushBits() throws IOException {
        if (bitOffset == 0) {
            return;
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
