public class ZipOutputStream extends DeflaterOutputStream implements
        ZipConstants {
    public static final int DEFLATED = 8;
    public static final int STORED = 0;
    static final int ZIPDataDescriptorFlag = 8;
    static final int ZIPLocalHeaderVersionNeeded = 20;
    private String comment;
    private final Vector<String> entries = new Vector<String>();
    private int compressMethod = DEFLATED;
    private int compressLevel = Deflater.DEFAULT_COMPRESSION;
    private ByteArrayOutputStream cDir = new ByteArrayOutputStream();
    private ZipEntry currentEntry;
    private final CRC32 crc = new CRC32();
    private int offset = 0, curOffset = 0, nameLength;
    private byte[] nameBytes;
    public ZipOutputStream(OutputStream p1) {
        super(p1, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    }
    @Override
    public void close() throws IOException {
        if (out != null) {
            finish();
            out.close();
            out = null;
        }
    }
    public void closeEntry() throws IOException {
        if (cDir == null) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (currentEntry == null) {
            return;
        }
        if (currentEntry.getMethod() == DEFLATED) {
            super.finish();
        }
        if (currentEntry.getMethod() == STORED) {
            if (crc.getValue() != currentEntry.crc) {
                throw new ZipException(Messages.getString("archive.20")); 
            }
            if (currentEntry.size != crc.tbytes) {
                throw new ZipException(Messages.getString("archive.21")); 
            }
        }
        curOffset = LOCHDR;
        if (currentEntry.getMethod() != STORED) {
            curOffset += EXTHDR;
            writeLong(out, EXTSIG);
            writeLong(out, currentEntry.crc = crc.getValue());
            writeLong(out, currentEntry.compressedSize = def.getTotalOut());
            writeLong(out, currentEntry.size = def.getTotalIn());
        }
        writeLong(cDir, CENSIG);
        writeShort(cDir, ZIPLocalHeaderVersionNeeded); 
        writeShort(cDir, ZIPLocalHeaderVersionNeeded); 
        writeShort(cDir, currentEntry.getMethod() == STORED ? 0
                : ZIPDataDescriptorFlag);
        writeShort(cDir, currentEntry.getMethod());
        writeShort(cDir, currentEntry.time);
        writeShort(cDir, currentEntry.modDate);
        writeLong(cDir, crc.getValue());
        if (currentEntry.getMethod() == DEFLATED) {
            curOffset += writeLong(cDir, def.getTotalOut());
            writeLong(cDir, def.getTotalIn());
        } else {
            curOffset += writeLong(cDir, crc.tbytes);
            writeLong(cDir, crc.tbytes);
        }
        curOffset += writeShort(cDir, nameLength);
        if (currentEntry.extra != null) {
            curOffset += writeShort(cDir, currentEntry.extra.length);
        } else {
            writeShort(cDir, 0);
        }
        String c;
        if ((c = currentEntry.getComment()) != null) {
            writeShort(cDir, c.length());
        } else {
            writeShort(cDir, 0);
        }
        writeShort(cDir, 0); 
        writeShort(cDir, 0); 
        writeLong(cDir, 0); 
        writeLong(cDir, offset);
        cDir.write(nameBytes);
        nameBytes = null;
        if (currentEntry.extra != null) {
            cDir.write(currentEntry.extra);
        }
        offset += curOffset;
        if (c != null) {
            cDir.write(c.getBytes());
        }
        currentEntry = null;
        crc.reset();
        def.reset();
        done = false;
    }
    @Override
    public void finish() throws IOException {
        if (out == null) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (cDir == null) {
            return;
        }
        if (entries.size() == 0) {
            throw new ZipException(Messages.getString("archive.28")); 
        }
        if (currentEntry != null) {
            closeEntry();
        }
        int cdirSize = cDir.size();
        writeLong(cDir, ENDSIG);
        writeShort(cDir, 0); 
        writeShort(cDir, 0); 
        writeShort(cDir, entries.size()); 
        writeShort(cDir, entries.size()); 
        writeLong(cDir, cdirSize); 
        writeLong(cDir, offset); 
        if (comment != null) {
            writeShort(cDir, comment.length());
            cDir.write(comment.getBytes());
        } else {
            writeShort(cDir, 0);
        }
        out.write(cDir.toByteArray());
        cDir = null;
    }
    public void putNextEntry(ZipEntry ze) throws java.io.IOException {
        if (currentEntry != null) {
            closeEntry();
        }
        if (ze.getMethod() == STORED
                || (compressMethod == STORED && ze.getMethod() == -1)) {
            if (ze.crc == -1) {
                throw new ZipException(Messages.getString("archive.20")); 
            }
            if (ze.size == -1 && ze.compressedSize == -1) {
                throw new ZipException(Messages.getString("archive.21")); 
            }
            if (ze.size != ze.compressedSize && ze.compressedSize != -1
                    && ze.size != -1) {
                throw new ZipException(Messages.getString("archive.21")); 
            }
        }
        if (cDir == null) {
            throw new IOException(Messages.getString("archive.1E")); 
        }
        if (entries.contains(ze.name)) {
            throw new ZipException(Messages.getString("archive.29", ze.name)); 
        }
        nameLength = utf8Count(ze.name);
        if (nameLength > 0xffff) {
            throw new IllegalArgumentException(Messages.getString(
                    "archive.2A", ze.name)); 
        }
        def.setLevel(compressLevel);
        currentEntry = ze;
        entries.add(currentEntry.name);
        if (currentEntry.getMethod() == -1) {
            currentEntry.setMethod(compressMethod);
        }
        writeLong(out, LOCSIG); 
        writeShort(out, ZIPLocalHeaderVersionNeeded); 
        writeShort(out, currentEntry.getMethod() == STORED ? 0
                : ZIPDataDescriptorFlag);
        writeShort(out, currentEntry.getMethod());
        if (currentEntry.getTime() == -1) {
            currentEntry.setTime(System.currentTimeMillis());
        }
        writeShort(out, currentEntry.time);
        writeShort(out, currentEntry.modDate);
        if (currentEntry.getMethod() == STORED) {
            if (currentEntry.size == -1) {
                currentEntry.size = currentEntry.compressedSize;
            } else if (currentEntry.compressedSize == -1) {
                currentEntry.compressedSize = currentEntry.size;
            }
            writeLong(out, currentEntry.crc);
            writeLong(out, currentEntry.size);
            writeLong(out, currentEntry.size);
        } else {
            writeLong(out, 0);
            writeLong(out, 0);
            writeLong(out, 0);
        }
        writeShort(out, nameLength);
        if (currentEntry.extra != null) {
            writeShort(out, currentEntry.extra.length);
        } else {
            writeShort(out, 0);
        }
        nameBytes = toUTF8Bytes(currentEntry.name, nameLength);
        out.write(nameBytes);
        if (currentEntry.extra != null) {
            out.write(currentEntry.extra);
        }
    }
    public void setComment(String comment) {
        if (comment.length() > 0xFFFF) {
            throw new IllegalArgumentException(Messages.getString("archive.2B")); 
        }
        this.comment = comment;
    }
    public void setLevel(int level) {
        if (level < Deflater.DEFAULT_COMPRESSION
                || level > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException();
        }
        compressLevel = level;
    }
    public void setMethod(int method) {
        if (method != STORED && method != DEFLATED) {
            throw new IllegalArgumentException();
        }
        compressMethod = method;
    }
    private long writeLong(OutputStream os, long i) throws java.io.IOException {
        os.write((int) (i & 0xFF));
        os.write((int) (i >> 8) & 0xFF);
        os.write((int) (i >> 16) & 0xFF);
        os.write((int) (i >> 24) & 0xFF);
        return i;
    }
    private int writeShort(OutputStream os, int i) throws java.io.IOException {
        os.write(i & 0xFF);
        os.write((i >> 8) & 0xFF);
        return i;
    }
    @Override
    public void write(byte[] buffer, int off, int nbytes)
            throws java.io.IOException {
        if ((off < 0 || (nbytes < 0) || off > buffer.length)
                || (buffer.length - off < nbytes)) {
            throw new IndexOutOfBoundsException();
        }
        if (currentEntry == null) {
            throw new ZipException(Messages.getString("archive.2C")); 
        }
        if (currentEntry.getMethod() == STORED) {
            out.write(buffer, off, nbytes);
        } else {
            super.write(buffer, off, nbytes);
        }
        crc.update(buffer, off, nbytes);
    }
    static int utf8Count(String value) {
        int total = 0;
        for (int i = value.length(); --i >= 0;) {
            char ch = value.charAt(i);
            if (ch < 0x80) {
                total++;
            } else if (ch < 0x800) {
                total += 2;
            } else {
                total += 3;
            }
        }
        return total;
    }
    static byte[] toUTF8Bytes(String value, int length) {
        byte[] result = new byte[length];
        int pos = result.length;
        for (int i = value.length(); --i >= 0;) {
            char ch = value.charAt(i);
            if (ch < 0x80) {
                result[--pos] = (byte) ch;
            } else if (ch < 0x800) {
                result[--pos] = (byte) (0x80 | (ch & 0x3f));
                result[--pos] = (byte) (0xc0 | (ch >> 6));
            } else {
                result[--pos] = (byte) (0x80 | (ch & 0x3f));
                result[--pos] = (byte) (0x80 | ((ch >> 6) & 0x3f));
                result[--pos] = (byte) (0xe0 | (ch >> 12));
            }
        }
        return result;
    }
}
