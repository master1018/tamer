class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private ZipEntry entry;
    private int flag;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];
    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;
    private boolean closed = false;
    private boolean entryEOF = false;
    private ZipCoder zc;
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }
    public ZipInputStream(InputStream in) {
        this(in, StandardCharsets.UTF_8);
    }
    public ZipInputStream(InputStream in, Charset charset) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null");
        }
        if (charset == null)
            throw new NullPointerException("charset is null");
        this.zc = ZipCoder.get(charset);
    }
    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        crc.reset();
        inf.reset();
        if ((entry = readLOC()) == null) {
            return null;
        }
        if (entry.method == STORED) {
            remaining = entry.size;
        }
        entryEOF = false;
        return entry;
    }
    public void closeEntry() throws IOException {
        ensureOpen();
        while (read(tmpbuf, 0, tmpbuf.length) != -1) ;
        entryEOF = true;
    }
    public int available() throws IOException {
        ensureOpen();
        if (entryEOF) {
            return 0;
        } else {
            return 1;
        }
    }
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (entry == null) {
            return -1;
        }
        switch (entry.method) {
        case DEFLATED:
            len = super.read(b, off, len);
            if (len == -1) {
                readEnd(entry);
                entryEOF = true;
                entry = null;
            } else {
                crc.update(b, off, len);
            }
            return len;
        case STORED:
            if (remaining <= 0) {
                entryEOF = true;
                entry = null;
                return -1;
            }
            if (len > remaining) {
                len = (int)remaining;
            }
            len = in.read(b, off, len);
            if (len == -1) {
                throw new ZipException("unexpected EOF");
            }
            crc.update(b, off, len);
            remaining -= len;
            if (remaining == 0 && entry.crc != crc.getValue()) {
                throw new ZipException(
                    "invalid entry CRC (expected 0x" + Long.toHexString(entry.crc) +
                    " but got 0x" + Long.toHexString(crc.getValue()) + ")");
            }
            return len;
        default:
            throw new ZipException("invalid compression method");
        }
    }
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > tmpbuf.length) {
                len = tmpbuf.length;
            }
            len = read(tmpbuf, 0, len);
            if (len == -1) {
                entryEOF = true;
                break;
            }
            total += len;
        }
        return total;
    }
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }
    private byte[] b = new byte[256];
    private ZipEntry readLOC() throws IOException {
        try {
            readFully(tmpbuf, 0, LOCHDR);
        } catch (EOFException e) {
            return null;
        }
        if (get32(tmpbuf, 0) != LOCSIG) {
            return null;
        }
        flag = get16(tmpbuf, LOCFLG);
        int len = get16(tmpbuf, LOCNAM);
        int blen = b.length;
        if (len > blen) {
            do
                blen = blen * 2;
            while (len > blen);
            b = new byte[blen];
        }
        readFully(b, 0, len);
        ZipEntry e = createZipEntry(((flag & EFS) != 0)
                                    ? zc.toStringUTF8(b, len)
                                    : zc.toString(b, len));
        if ((flag & 1) == 1) {
            throw new ZipException("encrypted ZIP entry not supported");
        }
        e.method = get16(tmpbuf, LOCHOW);
        e.time = get32(tmpbuf, LOCTIM);
        if ((flag & 8) == 8) {
            if (e.method != DEFLATED) {
                throw new ZipException(
                        "only DEFLATED entries can have EXT descriptor");
            }
        } else {
            e.crc = get32(tmpbuf, LOCCRC);
            e.csize = get32(tmpbuf, LOCSIZ);
            e.size = get32(tmpbuf, LOCLEN);
        }
        len = get16(tmpbuf, LOCEXT);
        if (len > 0) {
            byte[] bb = new byte[len];
            readFully(bb, 0, len);
            e.setExtra(bb);
            if (e.csize == ZIP64_MAGICVAL || e.size == ZIP64_MAGICVAL) {
                int off = 0;
                while (off + 4 < len) {
                    int sz = get16(bb, off + 2);
                    if (get16(bb, off) == ZIP64_EXTID) {
                        off += 4;
                        if (sz < 16 || (off + sz) > len ) {
                            return e;
                        }
                        e.size = get64(bb, off);
                        e.csize = get64(bb, off + 8);
                        break;
                    }
                    off += (sz + 4);
                }
            }
        }
        return e;
    }
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }
    private void readEnd(ZipEntry e) throws IOException {
        int n = inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)in).unread(buf, len - n, n);
        }
        if ((flag & 8) == 8) {
            if (inf.getBytesWritten() > ZIP64_MAGICVAL ||
                inf.getBytesRead() > ZIP64_MAGICVAL) {
                readFully(tmpbuf, 0, ZIP64_EXTHDR);
                long sig = get32(tmpbuf, 0);
                if (sig != EXTSIG) { 
                    e.crc = sig;
                    e.csize = get64(tmpbuf, ZIP64_EXTSIZ - ZIP64_EXTCRC);
                    e.size = get64(tmpbuf, ZIP64_EXTLEN - ZIP64_EXTCRC);
                    ((PushbackInputStream)in).unread(
                        tmpbuf, ZIP64_EXTHDR - ZIP64_EXTCRC - 1, ZIP64_EXTCRC);
                } else {
                    e.crc = get32(tmpbuf, ZIP64_EXTCRC);
                    e.csize = get64(tmpbuf, ZIP64_EXTSIZ);
                    e.size = get64(tmpbuf, ZIP64_EXTLEN);
                }
            } else {
                readFully(tmpbuf, 0, EXTHDR);
                long sig = get32(tmpbuf, 0);
                if (sig != EXTSIG) { 
                    e.crc = sig;
                    e.csize = get32(tmpbuf, EXTSIZ - EXTCRC);
                    e.size = get32(tmpbuf, EXTLEN - EXTCRC);
                    ((PushbackInputStream)in).unread(
                                               tmpbuf, EXTHDR - EXTCRC - 1, EXTCRC);
                } else {
                    e.crc = get32(tmpbuf, EXTCRC);
                    e.csize = get32(tmpbuf, EXTSIZ);
                    e.size = get32(tmpbuf, EXTLEN);
                }
            }
        }
        if (e.size != inf.getBytesWritten()) {
            throw new ZipException(
                "invalid entry size (expected " + e.size +
                " but got " + inf.getBytesWritten() + " bytes)");
        }
        if (e.csize != inf.getBytesRead()) {
            throw new ZipException(
                "invalid entry compressed size (expected " + e.csize +
                " but got " + inf.getBytesRead() + " bytes)");
        }
        if (e.crc != crc.getValue()) {
            throw new ZipException(
                "invalid entry CRC (expected 0x" + Long.toHexString(e.crc) +
                " but got 0x" + Long.toHexString(crc.getValue()) + ")");
        }
    }
    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }
    private static final int get16(byte b[], int off) {
        return (b[off] & 0xff) | ((b[off+1] & 0xff) << 8);
    }
    private static final long get32(byte b[], int off) {
        return (get16(b, off) | ((long)get16(b, off+2) << 16)) & 0xffffffffL;
    }
    private static final long get64(byte b[], int off) {
        return get32(b, off) | (get32(b, off+4) << 32);
    }
}
