public class DeflaterOutputStream extends FilterOutputStream {
    static final int BUF_SIZE = 512;
    protected byte[] buf;
    protected Deflater def;
    boolean done = false;
    private final boolean syncFlush;
    public DeflaterOutputStream(OutputStream os, Deflater def) {
        this(os, def, BUF_SIZE, false);
    }
    public DeflaterOutputStream(OutputStream os) {
        this(os, new Deflater(), BUF_SIZE, false);
    }
    public DeflaterOutputStream(OutputStream os, Deflater def, int bsize) {
        this(os, def, bsize, false);
    }
    public DeflaterOutputStream(OutputStream os, boolean syncFlush) {
        this(os, new Deflater(), BUF_SIZE, syncFlush);
    }
    public DeflaterOutputStream(OutputStream os, Deflater def, boolean syncFlush) {
        this(os, def, BUF_SIZE, syncFlush);
    }
    public DeflaterOutputStream(OutputStream os, Deflater def, int bsize, boolean syncFlush) {
        super(os);
        if (os == null || def == null) {
            throw new NullPointerException();
        }
        if (bsize <= 0) {
            throw new IllegalArgumentException();
        }
        this.def = def;
        this.syncFlush = syncFlush;
        buf = new byte[bsize];
    }
    protected void deflate() throws IOException {
        int x = 0;
        do {
            x = def.deflate(buf);
            out.write(buf, 0, x);
        } while (!def.needsInput());
    }
    @Override
    public void close() throws IOException {
        if (!def.finished()) {
            finish();
        }
        def.end();
        out.close();
    }
    public void finish() throws IOException {
        if (done) {
            return;
        }
        def.finish();
        int x = 0;
        while (!def.finished()) {
            if (def.needsInput()) {
                def.setInput(buf, 0, 0);
            }
            x = def.deflate(buf);
            out.write(buf, 0, x);
        }
        done = true;
    }
    @Override
    public void write(int i) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte) i;
        write(b, 0, 1);
    }
    @Override
    public void write(byte[] buffer, int off, int nbytes) throws IOException {
        if (done) {
            throw new IOException(Messages.getString("archive.26")); 
        }
        if (off <= buffer.length && nbytes >= 0 && off >= 0
                && buffer.length - off >= nbytes) {
            if (!def.needsInput()) {
                throw new IOException();
            }
            def.setInput(buffer, off, nbytes);
            deflate();
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    @Override public void flush() throws IOException {
        if (syncFlush) {
            int count = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);
            out.write(buf, 0, count);
        }
        out.flush();
    }
}
