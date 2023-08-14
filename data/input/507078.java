public final class DexBuffer {
    private ByteBuffer b;
    public DexBuffer(String fileName) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = bis.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            byte[] bytes = bos.toByteArray();
            initialize(ByteBuffer.wrap(bytes));
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    public DexBuffer(byte[] bytes) {
        initialize(ByteBuffer.wrap(bytes));
    }
    private DexBuffer(ByteBuffer slice) {
        initialize(slice);
    }
    private void initialize(ByteBuffer buffer) {
        b = buffer.asReadOnlyBuffer();
        b.clear();
        b.order(ByteOrder.LITTLE_ENDIAN);
    }
    public void setPosition(int offset) {
        b.position(offset);
    }
    public void readBytes(byte[] dst) {
        b.get(dst, 0, dst.length);
    }
    public int readUleb128() {
        int endValue = 0;
        int value = 0;
        int nr = 0;
        do {
            value = (b.get() & 0xFF);
            endValue |= ((value & 0x7F) << 7 * nr);
            nr++;
        } while ((value & 0x80) != 0); 
        return endValue;
    }
    public int readInt(int nBytes) {
        int endValue = 0;
        int tmp = 0;
        for (int i = 0; i < nBytes; i++) {
            tmp = b.get() & 0xFF;
            endValue |= (tmp << i * 8);
        }
        return endValue;
    }
    public short readShort(int nBytes) {
        short endValue = 0;
        int tmp = 0;
        for (int i = 0; i < nBytes; i++) {
            tmp = b.get() & 0xFF;
            endValue |= (tmp << i * 8);
        }
        return endValue;
    }
    public char readChar(int nBytes) {
        char endValue = 0;
        int tmp = 0;
        for (int i = 0; i < nBytes; i++) {
            tmp = b.get() & 0xFF;
            endValue |= (tmp << i * 8);
        }
        return endValue;
    }
    public long readLong(int nBytes) {
        long endValue = 0;
        int tmp = 0;
        for (int i = 0; i < nBytes; i++) {
            tmp = b.get() & 0xFF;
            endValue |= (tmp << i * 8);
        }
        return endValue;
    }
    public float readFloat(int nBytes) {
        int bits = readInt(nBytes);
        int bytesToMove = (4 - nBytes) * 8;
        bits <<= bytesToMove;
        return Float.intBitsToFloat(bits);
    }
    public int readUInt() {
        int value = b.getInt();
        return value;
    }
    public int readUShort() {
        return b.getShort() & 0xFFFF;
    }
    public byte readUByte() {
        return b.get();
    }
    public DexBuffer createCopy() {
        return new DexBuffer(b.duplicate());
    }
    public double readDouble(int nBytes) {
        long bits = readLong(nBytes);
        int bytesToMove = (8 - nBytes) * 8;
        bits <<= bytesToMove;
        return Double.longBitsToDouble(bits);
    }
    public void skip(int nBytes) {
        b.position(b.position() + nBytes);
    }
}
