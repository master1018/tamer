public class BufferedOutputStream extends FilterOutputStream {
    protected byte[] buf;
    protected int count;
    public BufferedOutputStream(OutputStream out) {
        super(out);
        buf = new byte[8192];
        Logger.global.info(
                "Default buffer size used in BufferedOutputStream " +
                "constructor. It would be " +
                "better to be explicit if an 8k buffer is required.");
    }
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        buf = new byte[size];
    }
    @Override
    public synchronized void flush() throws IOException {
        if (buf == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        flushInternal();
        out.flush();
    }
    @Override
    public synchronized void write(byte[] buffer, int offset, int length)
            throws IOException {
        byte[] internalBuffer = buf;
        if (internalBuffer == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if (length >= internalBuffer.length) {
            flushInternal();
            out.write(buffer, offset, length);
            return;
        }
        if (offset < 0 || offset > buffer.length - length) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        if (length >= (internalBuffer.length - count)) {
            flushInternal();
        }
        System.arraycopy(buffer, offset, internalBuffer, count, length);
        count += length;
    }
    @Override public synchronized void close() throws IOException {
        if (buf == null) {
            return;
        }
        try {
            super.close();
        } finally {
            buf = null;
        }
    }
    @Override
    public synchronized void write(int oneByte) throws IOException {
        byte[] internalBuffer = buf;
        if (internalBuffer == null) {
            throw new IOException(Msg.getString("K0059")); 
        }
        if (count == internalBuffer.length) {
            out.write(internalBuffer, 0, count);
            count = 0;
        }
        internalBuffer[count++] = (byte) oneByte;
    }
    private void flushInternal() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }
}
