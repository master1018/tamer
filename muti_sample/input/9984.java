public abstract class GSSToken {
    public static final void writeLittleEndian(int value, byte[] array) {
        writeLittleEndian(value, array, 0);
    }
    public static final void writeLittleEndian(int value, byte[] array,
                                               int pos) {
        array[pos++] = (byte)(value);
        array[pos++] = (byte)((value>>>8));
        array[pos++] = (byte)((value>>>16));
        array[pos++] = (byte)((value>>>24));
    }
    public static final void writeBigEndian(int value, byte[] array) {
        writeBigEndian(value, array, 0);
    }
    public static final void writeBigEndian(int value, byte[] array,
                                               int pos) {
        array[pos++] = (byte)((value>>>24));
        array[pos++] = (byte)((value>>>16));
        array[pos++] = (byte)((value>>>8));
        array[pos++] = (byte)(value);
    }
    public static final int readLittleEndian(byte[] data, int pos, int size) {
        int retVal = 0;
        int shifter = 0;
        while (size > 0) {
            retVal += (data[pos] & 0xff) << shifter;
            shifter += 8;
            pos++;
            size--;
        }
        return retVal;
    }
    public static final int readBigEndian(byte[] data, int pos, int size) {
        int retVal = 0;
        int shifter = (size-1)*8;
        while (size > 0) {
            retVal += (data[pos] & 0xff) << shifter;
            shifter -= 8;
            pos++;
            size--;
        }
        return retVal;
    }
    public static final void writeInt(int val, OutputStream os)
        throws IOException {
        os.write(val>>>8);
        os.write(val);
    }
    public static final int writeInt(int val, byte[] dest, int pos) {
        dest[pos++] = (byte)(val>>>8);
        dest[pos++] = (byte)val;
        return pos;
    }
    public static final int readInt(InputStream is) throws IOException {
        return (((0xFF & is.read()) << 8)
                 | (0xFF & is.read()));
    }
    public static final int readInt(byte[] src, int pos) {
        return ((0xFF & src[pos])<<8 | (0xFF & src[pos+1]));
    }
    public static final void readFully(InputStream is, byte[] buffer)
        throws IOException {
        readFully(is, buffer, 0, buffer.length);
    }
    public static final void readFully(InputStream is,
                                       byte[] buffer, int offset, int len)
        throws IOException {
        int temp;
        while (len > 0) {
            temp = is.read(buffer, offset, len);
            if (temp == -1)
                throw new EOFException("Cannot read all "
                                       + len
                                       + " bytes needed to form this token!");
            offset += temp;
            len -= temp;
        }
    }
    public static final void debug(String str) {
        System.err.print(str);
    }
    public static final  String getHexBytes(byte[] bytes) {
        return getHexBytes(bytes, 0, bytes.length);
    }
    public static final  String getHexBytes(byte[] bytes, int len) {
        return getHexBytes(bytes, 0, len);
    }
    public static final String getHexBytes(byte[] bytes, int pos, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = pos; i < (pos+len); i++) {
            int b1 = (bytes[i]>>4) & 0x0f;
            int b2 = bytes[i] & 0x0f;
            sb.append(Integer.toHexString(b1));
            sb.append(Integer.toHexString(b2));
            sb.append(' ');
        }
        return sb.toString();
    }
}
