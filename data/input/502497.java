public class BufferedInputStream extends FilterInputStream {
    protected volatile byte[] buf;
    protected int count;
    protected int marklimit;
    protected int markpos = -1;
    protected int pos;
    public BufferedInputStream(InputStream in) {
        super(in);
        buf = new byte[8192];
        Logger.global.info(
                "Default buffer size used in BufferedInputStream " +
                "constructor. It would be " +
                "better to be explicit if an 8k buffer is required.");
    }
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        buf = new byte[size];
    }
    @Override
    public synchronized int available() throws IOException {
        InputStream localIn = in; 
        if (buf == null || localIn == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        return count - pos + localIn.available();
    }
    @Override
    public void close() throws IOException {
        buf = null;
        InputStream localIn = in;
        in = null;
        if (localIn != null) {
            localIn.close();
        }
    }
    private int fillbuf(InputStream localIn, byte[] localBuf)
            throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
            int result = localIn.read(localBuf);
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result == -1 ? 0 : result;
            }
            return result;
        }
        if (markpos == 0 && marklimit > localBuf.length) {
            int newLength = localBuf.length * 2;
            if (newLength > marklimit) {
                newLength = marklimit;
            }
            byte[] newbuf = new byte[newLength];
            System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length);
            localBuf = buf = newbuf;
        } else if (markpos > 0) {
            System.arraycopy(localBuf, markpos, localBuf, 0, localBuf.length
                    - markpos);
        }
        pos -= markpos;
        count = markpos = 0;
        int bytesread = localIn.read(localBuf, pos, localBuf.length - pos);
        count = bytesread <= 0 ? pos : pos + bytesread;
        return bytesread;
    }
    @Override
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public synchronized int read() throws IOException {
        byte[] localBuf = buf;
        InputStream localIn = in;
        if (localBuf == null || localIn == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (pos >= count && fillbuf(localIn, localBuf) == -1) {
            return -1; 
        }
        if (localBuf != buf) {
            localBuf = buf;
            if (localBuf == null) {
                throw new IOException(Msg.getString("K0059")); 
            }
        }
        if (count - pos > 0) {
            return localBuf[pos++] & 0xFF;
        }
        return -1;
    }
    @Override
    public synchronized int read(byte[] buffer, int offset, int length)
            throws IOException {
        byte[] localBuf = buf;
        if (localBuf == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | length) < 0 || offset > buffer.length - length) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (length == 0) {
            return 0;
        }
        InputStream localIn = in;
        if (localIn == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        int required;
        if (pos < count) {
            int copylength = count - pos >= length ? length : count - pos;
            System.arraycopy(localBuf, pos, buffer, offset, copylength);
            pos += copylength;
            if (copylength == length || localIn.available() == 0) {
                return copylength;
            }
            offset += copylength;
            required = length - copylength;
        } else {
            required = length;
        }
        while (true) {
            int read;
            if (markpos == -1 && required >= localBuf.length) {
                read = localIn.read(buffer, offset, required);
                if (read == -1) {
                    return required == length ? -1 : length - required;
                }
            } else {
                if (fillbuf(localIn, localBuf) == -1) {
                    return required == length ? -1 : length - required;
                }
                if (localBuf != buf) {
                    localBuf = buf;
                    if (localBuf == null) {
                        throw new IOException(Msg.getString("K0059")); 
                    }
                }
                read = count - pos >= required ? required : count - pos;
                System.arraycopy(localBuf, pos, buffer, offset, read);
                pos += read;
            }
            required -= read;
            if (required == 0) {
                return length;
            }
            if (localIn.available() == 0) {
                return length - required;
            }
            offset += read;
        }
    }
    @Override
    public synchronized void reset() throws IOException {
        if (buf == null) {
            throw new IOException("Stream is closed");
        }
        if (-1 == markpos) {
            throw new IOException("Mark has been invalidated.");
        }
        pos = markpos;
    }
    @Override
    public synchronized long skip(long amount) throws IOException {
        byte[] localBuf = buf;
        InputStream localIn = in;
        if (localBuf == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (amount < 1) {
            return 0;
        }
        if (localIn == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (count - pos >= amount) {
            pos += amount;
            return amount;
        }
        long read = count - pos;
        pos = count;
        if (markpos != -1) {
            if (amount <= marklimit) {
                if (fillbuf(localIn, localBuf) == -1) {
                    return read;
                }
                if (count - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                read += (count - pos);
                pos = count;
                return read;
            }
        }
        return read + localIn.skip(amount - read);
    }
}
