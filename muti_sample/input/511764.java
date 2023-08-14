public class DataOutputStream extends FilterOutputStream implements DataOutput {
    protected int written;
    byte buff[];
    public DataOutputStream(OutputStream out) {
        super(out);
        buff = new byte[8];
    }
    @Override
    public void flush() throws IOException {
        super.flush();
    }
    public final int size() {
        if (written < 0) {
            written = Integer.MAX_VALUE;
        }
        return written;
    }
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        out.write(buffer, offset, count);
        written += count;
    }
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
        written++;
    }
    public final void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
        written++;
    }
    public final void writeByte(int val) throws IOException {
        out.write(val);
        written++;
    }
    public final void writeBytes(String str) throws IOException {
        if (str.length() == 0) {
            return;
        }
        byte bytes[] = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) str.charAt(index);
        }
        out.write(bytes);
        written += bytes.length;
    }
    public final void writeChar(int val) throws IOException {
        buff[0] = (byte) (val >> 8);
        buff[1] = (byte) val;
        out.write(buff, 0, 2);
        written += 2;
    }
    public final void writeChars(String str) throws IOException {
        byte newBytes[] = new byte[str.length() * 2];
        for (int index = 0; index < str.length(); index++) {
            int newIndex = index == 0 ? index : index * 2;
            newBytes[newIndex] = (byte) (str.charAt(index) >> 8);
            newBytes[newIndex + 1] = (byte) str.charAt(index);
        }
        out.write(newBytes);
        written += newBytes.length;
    }
    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }
    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }
    public final void writeInt(int val) throws IOException {
        buff[0] = (byte) (val >> 24);
        buff[1] = (byte) (val >> 16);
        buff[2] = (byte) (val >> 8);
        buff[3] = (byte) val;
        out.write(buff, 0, 4);
        written += 4;
    }
    public final void writeLong(long val) throws IOException {
        buff[0] = (byte) (val >> 56);
        buff[1] = (byte) (val >> 48);
        buff[2] = (byte) (val >> 40);
        buff[3] = (byte) (val >> 32);
        buff[4] = (byte) (val >> 24);
        buff[5] = (byte) (val >> 16);
        buff[6] = (byte) (val >> 8);
        buff[7] = (byte) val;
        out.write(buff, 0, 8);
        written += 8;
    }
    int writeLongToBuffer(long val,
                          byte[] buffer, int offset) throws IOException {
        buffer[offset++] = (byte) (val >> 56);
        buffer[offset++] = (byte) (val >> 48);
        buffer[offset++] = (byte) (val >> 40);
        buffer[offset++] = (byte) (val >> 32);
        buffer[offset++] = (byte) (val >> 24);
        buffer[offset++] = (byte) (val >> 16);
        buffer[offset++] = (byte) (val >> 8);
        buffer[offset++] = (byte) val;
        return offset;
    }
    public final void writeShort(int val) throws IOException {
        buff[0] = (byte) (val >> 8);
        buff[1] = (byte) val;
        out.write(buff, 0, 2);
        written += 2;
    }
    int writeShortToBuffer(int val,
                           byte[] buffer, int offset) throws IOException {
        buffer[offset++] = (byte) (val >> 8);
        buffer[offset++] = (byte) val;
        return offset;
    }
    public final void writeUTF(String str) throws IOException {
        long utfCount = countUTFBytes(str);
        if (utfCount > 65535) {
            throw new UTFDataFormatException(Msg.getString("K0068")); 
        }
        byte[] buffer = new byte[(int)utfCount + 2];
        int offset = 0;
        offset = writeShortToBuffer((int) utfCount, buffer, offset);
        offset = writeUTFBytesToBuffer(str, buffer, offset);
        write(buffer, 0, offset);
    }
    long countUTFBytes(String str) {
        int utfCount = 0, length = str.length();
        for (int i = 0; i < length; i++) {
            int charValue = str.charAt(i);
            if (charValue > 0 && charValue <= 127) {
                utfCount++;
            } else if (charValue <= 2047) {
                utfCount += 2;
            } else {
                utfCount += 3;
            }
        }
        return utfCount;
    }
    int writeUTFBytesToBuffer(String str,
            byte[] buffer, int offset) throws IOException {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            int charValue = str.charAt(i);
            if (charValue > 0 && charValue <= 127) {
                buffer[offset++] = (byte) charValue;
            } else if (charValue <= 2047) {
                buffer[offset++] = (byte) (0xc0 | (0x1f & (charValue >> 6)));
                buffer[offset++] = (byte) (0x80 | (0x3f & charValue));
            } else {
                buffer[offset++] = (byte) (0xe0 | (0x0f & (charValue >> 12)));
                buffer[offset++] = (byte) (0x80 | (0x3f & (charValue >> 6)));
                buffer[offset++] = (byte) (0x80 | (0x3f & charValue));
             }
        }
        return offset;
    }
}
