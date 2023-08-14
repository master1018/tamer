public class InflaterInputStream extends FilterInputStream {
    protected Inflater inf;
    protected byte[] buf;
    protected int len;
    boolean closed;
    boolean eof;
    static final int BUF_SIZE = 512;
    int nativeEndBufSize = 0; 
    public InflaterInputStream(InputStream is) {
        this(is, new Inflater(), BUF_SIZE);
    }
    public InflaterInputStream(InputStream is, Inflater inf) {
        this(is, inf, BUF_SIZE);
    }
    public InflaterInputStream(InputStream is, Inflater inf, int bsize) {
        super(is);
        if (is == null || inf == null) {
            throw new NullPointerException();
        }
        if (bsize <= 0) {
            throw new IllegalArgumentException();
        }
        this.inf = inf;
        if (is instanceof ZipFile.RAFStream) {
            nativeEndBufSize = bsize;
        } else {
            buf = new byte[bsize];
        }
    }
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) == -1) {
            return -1;
        }
        return b[0] & 0xff;
    }
    @Override
    public int read(byte[] buffer, int off, int nbytes) throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (null == buffer) {
            throw new NullPointerException();
        }
        if (off < 0 || nbytes < 0 || off + nbytes > buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        if (nbytes == 0) {
            return 0;
        }
        if (eof) {
            return -1;
        }
        if (off > buffer.length || nbytes < 0 || off < 0
                || buffer.length - off < nbytes) {
            throw new ArrayIndexOutOfBoundsException();
        }
        do {
            if (inf.needsInput()) {
                fill();
            }
            try {
                int result = inf.inflate(buffer, off, nbytes);
                eof = inf.finished();
                if (result > 0) {
                    return result;
                } else if (eof) {
                    return -1;
                } else if (inf.needsDictionary()) {
                    eof = true;
                    return -1;
                } else if (len == -1) {
                    eof = true;
                    throw new EOFException();
                }
            } catch (DataFormatException e) {
                eof = true;
                if (len == -1) {
                    throw new EOFException();
                }
                throw (IOException) (new IOException().initCause(e));
            }
        } while (true);
    }
    protected void fill() throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (nativeEndBufSize > 0) {
            ZipFile.RAFStream is = (ZipFile.RAFStream)in;
            synchronized (is.mSharedRaf) {
                long len = is.mLength - is.mOffset;
                if (len > nativeEndBufSize) len = nativeEndBufSize;
                int cnt = inf.setFileInput(is.mSharedRaf.getFD(), is.mOffset, (int)nativeEndBufSize);
                is.skip(cnt);
            }
        } else {
            if ((len = in.read(buf)) > 0) {
                inf.setInput(buf, 0, len);
            }
        }
    }
    @Override
    public long skip(long nbytes) throws IOException {
        if (nbytes >= 0) {
            if (buf == null) {
                buf = new byte[(int)Math.min(nbytes, BUF_SIZE)];
            }
            long count = 0, rem = 0;
            while (count < nbytes) {
                int x = read(buf, 0,
                        (rem = nbytes - count) > buf.length ? buf.length
                                : (int) rem);
                if (x == -1) {
                    return count;
                }
                count += x;
            }
            return count;
        }
        throw new IllegalArgumentException();
    }
    @Override
    public int available() throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (eof) {
            return 0;
        }
        return 1;
    }
    @Override
    public void close() throws IOException {
        if (!closed) {
            inf.end();
            closed = true;
            eof = true;
            super.close();
        }
    }
    @Override
    public void mark(int readlimit) {
    }
    @Override
    public void reset() throws IOException {
        throw new IOException();
    }
    @Override
    public boolean markSupported() {
        return false;
    }
}
