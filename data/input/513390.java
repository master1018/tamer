public class GZIPInputStream extends InflaterInputStream {
    private static final int FCOMMENT = 16;
    private static final int FEXTRA = 4;
    private static final int FHCRC = 2;
    private static final int FNAME = 8;
    public final static int GZIP_MAGIC = 0x8b1f;
    protected CRC32 crc = new CRC32();
    protected boolean eos = false;
    public GZIPInputStream(InputStream is) throws IOException {
        this(is, BUF_SIZE);
    }
    public GZIPInputStream(InputStream is, int size) throws IOException {
        super(is, new Inflater(true), size);
        byte[] header = new byte[10];
        readFully(header, 0, header.length);
        if (getShort(header, 0) != GZIP_MAGIC) {
            throw new IOException(Messages.getString("archive.1F")); 
        }
        int flags = header[3];
        boolean hcrc = (flags & FHCRC) != 0;
        if (hcrc) {
            crc.update(header, 0, header.length);
        }
        if ((flags & FEXTRA) != 0) {
            readFully(header, 0, 2);
            if (hcrc) {
                crc.update(header, 0, 2);
            }
            int length = getShort(header, 0);
            while (length > 0) {
                int max = length > buf.length ? buf.length : length;
                int result = in.read(buf, 0, max);
                if (result == -1) {
                    throw new EOFException();
                }
                if (hcrc) {
                    crc.update(buf, 0, result);
                }
                length -= result;
            }
        }
        if ((flags & FNAME) != 0) {
            readZeroTerminated(hcrc);
        }
        if ((flags & FCOMMENT) != 0) {
            readZeroTerminated(hcrc);
        }
        if (hcrc) {
            readFully(header, 0, 2);
            int crc16 = getShort(header, 0);
            if ((crc.getValue() & 0xffff) != crc16) {
                throw new IOException(Messages.getString("archive.20")); 
            }
            crc.reset();
        }
    }
    @Override
    public void close() throws IOException {
        eos = true;
        super.close();
    }
    private long getLong(byte[] buffer, int off) {
        long l = 0;
        l |= (buffer[off] & 0xFF);
        l |= (buffer[off + 1] & 0xFF) << 8;
        l |= (buffer[off + 2] & 0xFF) << 16;
        l |= ((long) (buffer[off + 3] & 0xFF)) << 24;
        return l;
    }
    private int getShort(byte[] buffer, int off) {
        return (buffer[off] & 0xFF) | ((buffer[off + 1] & 0xFF) << 8);
    }
    @Override
    public int read(byte[] buffer, int off, int nbytes) throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (eos) {
            return -1;
        }
        if (off > buffer.length || nbytes < 0 || off < 0
                || buffer.length - off < nbytes) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int bytesRead;
        try {
            bytesRead = super.read(buffer, off, nbytes);
        } finally {
            eos = eof; 
        }
        if (bytesRead != -1) {
            crc.update(buffer, off, bytesRead);
        }
        if (eos) {
            verifyCrc();
        }
        return bytesRead;
    }
    private void verifyCrc() throws IOException {
        int size = inf.getRemaining();
        final int trailerSize = 8; 
        byte[] b = new byte[trailerSize];
        int copySize = (size > trailerSize) ? trailerSize : size;
        System.arraycopy(buf, len - size, b, 0, copySize);
        readFully(b, copySize, trailerSize - copySize);
        if (getLong(b, 0) != crc.getValue()) {
            throw new IOException(Messages.getString("archive.20")); 
        }
        if ((int) getLong(b, 4) != inf.getTotalOut()) {
            throw new IOException(Messages.getString("archive.21")); 
        }
    }
    private void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        int result;
        while (length > 0) {
            result = in.read(buffer, offset, length);
            if (result == -1) {
                throw new EOFException();
            }
            offset += result;
            length -= result;
        }
    }
    private void readZeroTerminated(boolean hcrc) throws IOException {
        int result;
        while ((result = in.read()) > 0) {
            if (hcrc) {
                crc.update(result);
            }
        }
        if (result == -1) {
            throw new EOFException();
        }
        if (hcrc) {
            crc.update(result);
        }
    }
}
