public abstract class ImageInputStreamImpl implements ImageInputStream {
    private Stack markByteStack = new Stack();
    private Stack markBitStack = new Stack();
    private boolean isClosed = false;
    private static final int BYTE_BUF_LENGTH = 8192;
    byte[] byteBuf = new byte[BYTE_BUF_LENGTH];
    protected ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    protected long streamPos;
    protected int bitOffset;
    protected long flushedPos = 0;
    public ImageInputStreamImpl() {
    }
    protected final void checkClosed() throws IOException {
        if (isClosed) {
            throw new IOException("closed");
        }
    }
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }
    public ByteOrder getByteOrder() {
        return byteOrder;
    }
    public abstract int read() throws IOException;
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public abstract int read(byte[] b, int off, int len) throws IOException;
    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException("len < 0!");
        }
        if (buf == null) {
            throw new NullPointerException("buf == null!");
        }
        byte[] data = new byte[len];
        len = read(data, 0, len);
        buf.setData(data);
        buf.setOffset(0);
        buf.setLength(len);
    }
    public boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }
    public byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte)ch;
    }
    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }
    public short readShort() throws IOException {
        if (read(byteBuf, 0, 2) < 0) {
            throw new EOFException();
        }
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (short)
                (((byteBuf[0] & 0xff) << 8) | ((byteBuf[1] & 0xff) << 0));
        } else {
            return (short)
                (((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0));
        }
    }
    public int readUnsignedShort() throws IOException {
        return ((int)readShort()) & 0xffff;
    }
    public char readChar() throws IOException {
        return (char)readShort();
    }
    public int readInt() throws IOException {
        if (read(byteBuf, 0, 4) < 0) {
            throw new EOFException();
        }
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return
                (((byteBuf[0] & 0xff) << 24) | ((byteBuf[1] & 0xff) << 16) |
                 ((byteBuf[2] & 0xff) <<  8) | ((byteBuf[3] & 0xff) <<  0));
        } else {
            return
                (((byteBuf[3] & 0xff) << 24) | ((byteBuf[2] & 0xff) << 16) |
                 ((byteBuf[1] & 0xff) <<  8) | ((byteBuf[0] & 0xff) <<  0));
        }
    }
    public long readUnsignedInt() throws IOException {
        return ((long)readInt()) & 0xffffffffL;
    }
    public long readLong() throws IOException {
        int i1 = readInt();
        int i2 = readInt();
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return ((long)i1 << 32) + (i2 & 0xFFFFFFFFL);
        } else {
            return ((long)i2 << 32) + (i1 & 0xFFFFFFFFL);
        }
    }
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    public String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;
        while (!eol) {
            switch (c = read()) {
            case -1:
            case '\n':
                eol = true;
                break;
            case '\r':
                eol = true;
                long cur = getStreamPosition();
                if ((read()) != '\n') {
                    seek(cur);
                }
                break;
            default:
                input.append((char)c);
                break;
            }
        }
        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }
    public String readUTF() throws IOException {
        this.bitOffset = 0;
        ByteOrder oldByteOrder = getByteOrder();
        setByteOrder(ByteOrder.BIG_ENDIAN);
        String ret;
        try {
            ret = DataInputStream.readUTF(this);
        } catch (IOException e) {
            setByteOrder(oldByteOrder);
            throw e;
        }
        setByteOrder(oldByteOrder);
        return ret;
    }
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > b.length!");
        }
        while (len > 0) {
            int nbytes = read(b, off, len);
            if (nbytes == -1) {
                throw new EOFException();
            }
            off += nbytes;
            len -= nbytes;
        }
    }
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }
    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > s.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/2);
            readFully(byteBuf, 0, nelts*2);
            toShorts(byteBuf, s, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    public void readFully(char[] c, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > c.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/2);
            readFully(byteBuf, 0, nelts*2);
            toChars(byteBuf, c, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    public void readFully(int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > i.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/4);
            readFully(byteBuf, 0, nelts*4);
            toInts(byteBuf, i, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    public void readFully(long[] l, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > l.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > l.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/8);
            readFully(byteBuf, 0, nelts*8);
            toLongs(byteBuf, l, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    public void readFully(float[] f, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > f.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > f.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/4);
            readFully(byteBuf, 0, nelts*4);
            toFloats(byteBuf, f, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    public void readFully(double[] d, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > d.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > d.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/8);
            readFully(byteBuf, 0, nelts*8);
            toDoubles(byteBuf, d, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }
    private void toShorts(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                s[off + j] = (short)((b0 << 8) | b1);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 1];
                int b1 = b[boff] & 0xff;
                s[off + j] = (short)((b0 << 8) | b1);
                boff += 2;
            }
        }
    }
    private void toChars(byte[] b, char[] c, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                c[off + j] = (char)((b0 << 8) | b1);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 1];
                int b1 = b[boff] & 0xff;
                c[off + j] = (char)((b0 << 8) | b1);
                boff += 2;
            }
        }
    }
    private void toInts(byte[] b, int[] i, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                int b2 = b[boff + 2] & 0xff;
                int b3 = b[boff + 3] & 0xff;
                i[off + j] = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 3];
                int b1 = b[boff + 2] & 0xff;
                int b2 = b[boff + 1] & 0xff;
                int b3 = b[boff] & 0xff;
                i[off + j] = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                boff += 4;
            }
        }
    }
    private void toLongs(byte[] b, long[] l, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                int b2 = b[boff + 2] & 0xff;
                int b3 = b[boff + 3] & 0xff;
                int b4 = b[boff + 4];
                int b5 = b[boff + 5] & 0xff;
                int b6 = b[boff + 6] & 0xff;
                int b7 = b[boff + 7] & 0xff;
                int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
                l[off + j] = ((long)i0 << 32) | (i1 & 0xffffffffL);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 7];
                int b1 = b[boff + 6] & 0xff;
                int b2 = b[boff + 5] & 0xff;
                int b3 = b[boff + 4] & 0xff;
                int b4 = b[boff + 3];
                int b5 = b[boff + 2] & 0xff;
                int b6 = b[boff + 1] & 0xff;
                int b7 = b[boff]     & 0xff;
                int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
                l[off + j] = ((long)i0 << 32) | (i1 & 0xffffffffL);
                boff += 8;
            }
        }
    }
    private void toFloats(byte[] b, float[] f, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                int b2 = b[boff + 2] & 0xff;
                int b3 = b[boff + 3] & 0xff;
                int i = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                f[off + j] = Float.intBitsToFloat(i);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 3];
                int b1 = b[boff + 2] & 0xff;
                int b2 = b[boff + 1] & 0xff;
                int b3 = b[boff + 0] & 0xff;
                int i = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                f[off + j] = Float.intBitsToFloat(i);
                boff += 4;
            }
        }
    }
    private void toDoubles(byte[] b, double[] d, int off, int len) {
        int boff = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff];
                int b1 = b[boff + 1] & 0xff;
                int b2 = b[boff + 2] & 0xff;
                int b3 = b[boff + 3] & 0xff;
                int b4 = b[boff + 4];
                int b5 = b[boff + 5] & 0xff;
                int b6 = b[boff + 6] & 0xff;
                int b7 = b[boff + 7] & 0xff;
                int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
                long l = ((long)i0 << 32) | (i1 & 0xffffffffL);
                d[off + j] = Double.longBitsToDouble(l);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; j++) {
                int b0 = b[boff + 7];
                int b1 = b[boff + 6] & 0xff;
                int b2 = b[boff + 5] & 0xff;
                int b3 = b[boff + 4] & 0xff;
                int b4 = b[boff + 3];
                int b5 = b[boff + 2] & 0xff;
                int b6 = b[boff + 1] & 0xff;
                int b7 = b[boff] & 0xff;
                int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
                int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
                long l = ((long)i0 << 32) | (i1 & 0xffffffffL);
                d[off + j] = Double.longBitsToDouble(l);
                boff += 8;
            }
        }
    }
    public long getStreamPosition() throws IOException {
        checkClosed();
        return streamPos;
    }
    public int getBitOffset() throws IOException {
        checkClosed();
        return bitOffset;
    }
    public void setBitOffset(int bitOffset) throws IOException {
        checkClosed();
        if (bitOffset < 0 || bitOffset > 7) {
            throw new IllegalArgumentException("bitOffset must be betwwen 0 and 7!");
        }
        this.bitOffset = bitOffset;
    }
    public int readBit() throws IOException {
        checkClosed();
        int newBitOffset = (this.bitOffset + 1) & 0x7;
        int val = read();
        if (val == -1) {
            throw new EOFException();
        }
        if (newBitOffset != 0) {
            seek(getStreamPosition() - 1);
            val >>= 8 - newBitOffset;
        }
        this.bitOffset = newBitOffset;
        return val & 0x1;
    }
    public long readBits(int numBits) throws IOException {
        checkClosed();
        if (numBits < 0 || numBits > 64) {
            throw new IllegalArgumentException();
        }
        if (numBits == 0) {
            return 0L;
        }
        int bitsToRead = numBits + bitOffset;
        int newBitOffset = (this.bitOffset + numBits) & 0x7;
        long accum = 0L;
        while (bitsToRead > 0) {
            int val = read();
            if (val == -1) {
                throw new EOFException();
            }
            accum <<= 8;
            accum |= val;
            bitsToRead -= 8;
        }
        if (newBitOffset != 0) {
            seek(getStreamPosition() - 1);
        }
        this.bitOffset = newBitOffset;
        accum >>>= (-bitsToRead); 
        accum &= (-1L >>> (64 - numBits));
        return accum;
    }
    public long length() {
        return -1L;
    }
    public int skipBytes(int n) throws IOException {
        long pos = getStreamPosition();
        seek(pos + n);
        return (int)(getStreamPosition() - pos);
    }
    public long skipBytes(long n) throws IOException {
        long pos = getStreamPosition();
        seek(pos + n);
        return getStreamPosition() - pos;
    }
    public void seek(long pos) throws IOException {
        checkClosed();
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        this.streamPos = pos;
        this.bitOffset = 0;
    }
    public void mark() {
        try {
            markByteStack.push(Long.valueOf(getStreamPosition()));
            markBitStack.push(Integer.valueOf(getBitOffset()));
        } catch (IOException e) {
        }
    }
    public void reset() throws IOException {
        if (markByteStack.empty()) {
            return;
        }
        long pos = ((Long)markByteStack.pop()).longValue();
        if (pos < flushedPos) {
            throw new IIOException
                ("Previous marked position has been discarded!");
        }
        seek(pos);
        int offset = ((Integer)markBitStack.pop()).intValue();
        setBitOffset(offset);
    }
    public void flushBefore(long pos) throws IOException {
        checkClosed();
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        if (pos > getStreamPosition()) {
            throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
        }
        flushedPos = pos;
    }
    public void flush() throws IOException {
        flushBefore(getStreamPosition());
    }
    public long getFlushedPosition() {
        return flushedPos;
    }
    public boolean isCached() {
        return false;
    }
    public boolean isCachedMemory() {
        return false;
    }
    public boolean isCachedFile() {
        return false;
    }
    public void close() throws IOException {
        checkClosed();
        isClosed = true;
    }
    protected void finalize() throws Throwable {
        if (!isClosed) {
            try {
                close();
            } catch (IOException e) {
            }
        }
        super.finalize();
    }
}
