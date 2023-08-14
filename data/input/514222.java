public class DataInputStream extends FilterInputStream implements DataInput {
    byte[] buff;
    public DataInputStream(InputStream in) {
        super(in);
        buff = new byte[8];
    }
    @Override
    public final int read(byte[] buffer) throws IOException {
        return in.read(buffer, 0, buffer.length);
    }
    @Override
    public final int read(byte[] buffer, int offset, int length)
            throws IOException {
        return in.read(buffer, offset, length);
    }
    public final boolean readBoolean() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }
    public final byte readByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }
    public final char readChar() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (char) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));
    }
    private int readToBuff(int count) throws IOException {
        int offset = 0;
        while(offset < count) {
            int bytesRead = in.read(buff, offset, count - offset);
            if(bytesRead == -1) return bytesRead;
            offset += bytesRead;
        }
        return offset;
    }
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    public final void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }
    public final void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        if (length == 0) {
            return;
        }
        if (in == null) {
            throw new NullPointerException(Msg.getString("KA00b")); 
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | length) < 0 || offset > buffer.length - length) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        while (length > 0) {
            int result = in.read(buffer, offset, length);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            length -= result;
        }
    }
    public final int readInt() throws IOException {
        if (readToBuff(4) < 0){
            throw new EOFException();
        }
        return ((buff[0] & 0xff) << 24) | ((buff[1] & 0xff) << 16) |
            ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);
    }
    @Deprecated
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); 
        boolean foundTerminator = false;
        while (true) {
            int nextByte = in.read();
            switch (nextByte) {
                case -1:
                    if (line.length() == 0 && !foundTerminator) {
                        return null;
                    }
                    return line.toString();
                case (byte) '\r':
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    foundTerminator = true;
                    if (!(in.getClass() == PushbackInputStream.class)) {
                        in = new PushbackInputStream(in);
                    }
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }
    public final long readLong() throws IOException {
        if (readToBuff(8) < 0){
            throw new EOFException();
        }
        int i1 = ((buff[0] & 0xff) << 24) | ((buff[1] & 0xff) << 16) |
            ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);
        int i2 = ((buff[4] & 0xff) << 24) | ((buff[5] & 0xff) << 16) |
            ((buff[6] & 0xff) << 8) | (buff[7] & 0xff);
        return ((i1 & 0xffffffffL) << 32) | (i2 & 0xffffffffL);
    }
    public final short readShort() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (short) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));
    }
    public final int readUnsignedByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }
    public final int readUnsignedShort() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (char) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));
    }
    public final String readUTF() throws IOException {
        return decodeUTF(readUnsignedShort());
    }
    String decodeUTF(int utfSize) throws IOException {
        return decodeUTF(utfSize, this);
    }
    private static String decodeUTF(int utfSize, DataInput in) throws IOException {
        byte[] buf = new byte[utfSize];
        char[] out = new char[utfSize];
        in.readFully(buf, 0, utfSize);
        return Util.convertUTF8WithBuf(buf, out, 0, utfSize);
    }
    public static final String readUTF(DataInput in) throws IOException {
        return decodeUTF(in.readUnsignedShort(), in);
    }
    public final int skipBytes(int count) throws IOException {
        int skipped = 0;
        long skip;
        while (skipped < count && (skip = in.skip(count - skipped)) != 0) {
            skipped += skip;
        }
        return skipped;
    }
}
