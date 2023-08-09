public class ZipInputStream extends InflaterInputStream implements ZipConstants {
    static final int DEFLATED = 8;
    static final int STORED = 0;
    static final int ZIPDataDescriptorFlag = 8;
    static final int ZIPLocalHeaderVersionNeeded = 20;
    private boolean entriesEnd = false;
    private boolean hasDD = false;
    private int entryIn = 0;
    private int inRead, lastRead = 0;
    ZipEntry currentEntry;
    private final byte[] hdrBuf = new byte[LOCHDR - LOCVER];
    private final CRC32 crc = new CRC32();
    private byte[] nameBuf = new byte[256];
    private char[] charBuf = new char[256];
    public ZipInputStream(InputStream stream) {
        super(new PushbackInputStream(stream, BUF_SIZE), new Inflater(true));
        if (stream == null) {
            throw new NullPointerException();
        }
    }
    @Override
    public void close() throws IOException {
        if (!closed) {
            closeEntry(); 
            super.close();
        }
    }
    public void closeEntry() throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (currentEntry == null) {
            return;
        }
        if (currentEntry instanceof java.util.jar.JarEntry) {
            Attributes temp = ((JarEntry) currentEntry).getAttributes();
            if (temp != null && temp.containsKey("hidden")) { 
                return;
            }
        }
        Exception failure = null;
        try {
            skip(Long.MAX_VALUE);
        } catch (Exception e) {
            failure = e;
        }
        int inB, out;
        if (currentEntry.compressionMethod == DEFLATED) {
            inB = inf.getTotalIn();
            out = inf.getTotalOut();
        } else {
            inB = inRead;
            out = inRead;
        }
        int diff = entryIn - inB;
        if (diff != 0) {
            ((PushbackInputStream) in).unread(buf, len - diff, diff);
        }
        try {
            readAndVerifyDataDescriptor(inB, out);
        } catch (Exception e) {
            if (failure == null) { 
                failure = e;
            }
        }
        inf.reset();
        lastRead = inRead = entryIn = len = 0;
        crc.reset();
        currentEntry = null;
        if (failure != null) {
            if (failure instanceof IOException) {
                throw (IOException) failure;
            } else if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            AssertionError error = new AssertionError();
            error.initCause(failure);
            throw error;
        }
    }
    private void readAndVerifyDataDescriptor(int inB, int out) throws IOException {
        if (hasDD) {
            in.read(hdrBuf, 0, EXTHDR);
            if (getLong(hdrBuf, 0) != EXTSIG) {
                throw new ZipException(Messages.getString("archive.1F")); 
            }
            currentEntry.crc = getLong(hdrBuf, EXTCRC);
            currentEntry.compressedSize = getLong(hdrBuf, EXTSIZ);
            currentEntry.size = getLong(hdrBuf, EXTLEN);
        }
        if (currentEntry.crc != crc.getValue()) {
            throw new ZipException(Messages.getString("archive.20")); 
        }
        if (currentEntry.compressedSize != inB || currentEntry.size != out) {
            throw new ZipException(Messages.getString("archive.21")); 
        }
    }
    public ZipEntry getNextEntry() throws IOException {
        closeEntry();
        if (entriesEnd) {
            return null;
        }
        int x = 0, count = 0;
        while (count != 4) {
            count += x = in.read(hdrBuf, count, 4 - count);
            if (x == -1) {
                return null;
            }
        }
        long hdr = getLong(hdrBuf, 0);
        if (hdr == CENSIG) {
            entriesEnd = true;
            return null;
        }
        if (hdr != LOCSIG) {
            return null;
        }
        count = 0;
        while (count != (LOCHDR - LOCVER)) {
            count += x = in.read(hdrBuf, count, (LOCHDR - LOCVER) - count);
            if (x == -1) {
                throw new EOFException();
            }
        }
        int version = getShort(hdrBuf, 0) & 0xff;
        if (version > ZIPLocalHeaderVersionNeeded) {
            throw new ZipException(Messages.getString("archive.22")); 
        }
        int flags = getShort(hdrBuf, LOCFLG - LOCVER);
        hasDD = ((flags & ZIPDataDescriptorFlag) == ZIPDataDescriptorFlag);
        int cetime = getShort(hdrBuf, LOCTIM - LOCVER);
        int cemodDate = getShort(hdrBuf, LOCTIM - LOCVER + 2);
        int cecompressionMethod = getShort(hdrBuf, LOCHOW - LOCVER);
        long cecrc = 0, cecompressedSize = 0, cesize = -1;
        if (!hasDD) {
            cecrc = getLong(hdrBuf, LOCCRC - LOCVER);
            cecompressedSize = getLong(hdrBuf, LOCSIZ - LOCVER);
            cesize = getLong(hdrBuf, LOCLEN - LOCVER);
        }
        int flen = getShort(hdrBuf, LOCNAM - LOCVER);
        if (flen == 0) {
            throw new ZipException(Messages.getString("archive.23")); 
        }
        int elen = getShort(hdrBuf, LOCEXT - LOCVER);
        count = 0;
        if (flen > nameBuf.length) {
            nameBuf = new byte[flen];
            charBuf = new char[flen];
        }
        while (count != flen) {
            count += x = in.read(nameBuf, count, flen - count);
            if (x == -1) {
                throw new EOFException();
            }
        }
        currentEntry = createZipEntry(Util.convertUTF8WithBuf(nameBuf, charBuf,
                0, flen));
        currentEntry.time = cetime;
        currentEntry.modDate = cemodDate;
        currentEntry.setMethod(cecompressionMethod);
        if (cesize != -1) {
            currentEntry.setCrc(cecrc);
            currentEntry.setSize(cesize);
            currentEntry.setCompressedSize(cecompressedSize);
        }
        if (elen > 0) {
            count = 0;
            byte[] e = new byte[elen];
            while (count != elen) {
                count += x = in.read(e, count, elen - count);
                if (x == -1) {
                    throw new EOFException();
                }
            }
            currentEntry.setExtra(e);
        }
        return currentEntry;
    }
    @Override
    public int read(byte[] buffer, int start, int length) throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (inf.finished() || currentEntry == null) {
            return -1;
        }
        if (start > buffer.length || length < 0 || start < 0
                || buffer.length - start < length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (currentEntry.compressionMethod == STORED) {
            int csize = (int) currentEntry.size;
            if (inRead >= csize) {
                return -1;
            }
            if (lastRead >= len) {
                lastRead = 0;
                if ((len = in.read(buf)) == -1) {
                    eof = true;
                    return -1;
                }
                entryIn += len;
            }
            int toRead = length > (len - lastRead) ? len - lastRead : length;
            if ((csize - inRead) < toRead) {
                toRead = csize - inRead;
            }
            System.arraycopy(buf, lastRead, buffer, start, toRead);
            lastRead += toRead;
            inRead += toRead;
            crc.update(buffer, start, toRead);
            return toRead;
        }
        if (inf.needsInput()) {
            fill();
            if (len > 0) {
                entryIn += len;
            }
        }
        int read;
        try {
            read = inf.inflate(buffer, start, length);
        } catch (DataFormatException e) {
            throw new ZipException(e.getMessage());
        }
        if (read == 0 && inf.finished()) {
            return -1;
        }
        crc.update(buffer, start, read);
        return read;
    }
    @Override
    public long skip(long value) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        long skipped = 0;
        byte[] b = new byte[(int)Math.min(value, 2048L)];
        while (skipped != value) {
            long rem = value - skipped;
            int x = read(b, 0, (int) (b.length > rem ? rem : b.length));
            if (x == -1) {
                return skipped;
            }
            skipped += x;
        }
        return skipped;
    }
    @Override
    public int available() throws IOException {
        if (closed) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        return (currentEntry == null || inRead < currentEntry.size) ? 1 : 0;
    }
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }
    private int getShort(byte[] buffer, int off) {
        return (buffer[off] & 0xFF) | ((buffer[off + 1] & 0xFF) << 8);
    }
    private long getLong(byte[] buffer, int off) {
        long l = 0;
        l |= (buffer[off] & 0xFF);
        l |= (buffer[off + 1] & 0xFF) << 8;
        l |= (buffer[off + 2] & 0xFF) << 16;
        l |= ((long) (buffer[off + 3] & 0xFF)) << 24;
        return l;
    }
}
