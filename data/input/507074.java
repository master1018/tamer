public class ZipEntry implements ZipConstants, Cloneable {
    String name, comment;
    long compressedSize = -1, crc = -1, size = -1;
    int compressionMethod = -1, time = -1, modDate = -1;
    byte[] extra;
    int nameLen = -1;
    long mLocalHeaderRelOffset = -1;
    public static final int DEFLATED = 8;
    public static final int STORED = 0;
    public ZipEntry(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.length() > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }
    public String getComment() {
        return comment;
    }
    public long getCompressedSize() {
        return compressedSize;
    }
    public long getCrc() {
        return crc;
    }
    public byte[] getExtra() {
        return extra;
    }
    public int getMethod() {
        return compressionMethod;
    }
    public String getName() {
        return name;
    }
    public long getSize() {
        return size;
    }
    public long getTime() {
        if (time != -1) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(1980 + ((modDate >> 9) & 0x7f), ((modDate >> 5) & 0xf) - 1,
                    modDate & 0x1f, (time >> 11) & 0x1f, (time >> 5) & 0x3f,
                    (time & 0x1f) << 1);
            return cal.getTime().getTime();
        }
        return -1;
    }
    public boolean isDirectory() {
        return name.charAt(name.length() - 1) == '/';
    }
    public void setComment(String string) {
        if (string == null || string.length() <= 0xFFFF) {
            comment = string;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public void setCompressedSize(long value) {
        compressedSize = value;
    }
    public void setCrc(long value) {
        if (value >= 0 && value <= 0xFFFFFFFFL) {
            crc = value;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public void setExtra(byte[] data) {
        if (data == null || data.length <= 0xFFFF) {
            extra = data;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public void setMethod(int value) {
        if (value != STORED && value != DEFLATED) {
            throw new IllegalArgumentException();
        }
        compressionMethod = value;
    }
    public void setSize(long value) {
        if (value >= 0 && value <= 0xFFFFFFFFL) {
            size = value;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public void setTime(long value) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(value));
        int year = cal.get(Calendar.YEAR);
        if (year < 1980) {
            modDate = 0x21;
            time = 0;
        } else {
            modDate = cal.get(Calendar.DATE);
            modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
            modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
            time = cal.get(Calendar.SECOND) >> 1;
            time = (cal.get(Calendar.MINUTE) << 5) | time;
            time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
        }
    }
    @Override
    public String toString() {
        return name;
    }
    public ZipEntry(ZipEntry ze) {
        name = ze.name;
        comment = ze.comment;
        time = ze.time;
        size = ze.size;
        compressedSize = ze.compressedSize;
        crc = ze.crc;
        compressionMethod = ze.compressionMethod;
        modDate = ze.modDate;
        extra = ze.extra;
        nameLen = ze.nameLen;
        mLocalHeaderRelOffset = ze.mLocalHeaderRelOffset;
    }
    @Override
    public Object clone() {
        return new ZipEntry(this);
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    ZipEntry(LittleEndianReader ler, InputStream in) throws IOException {
        byte[] hdrBuf = ler.hdrBuf;
        myReadFully(in, hdrBuf);
        long sig = (hdrBuf[0] & 0xff) | ((hdrBuf[1] & 0xff) << 8) |
            ((hdrBuf[2] & 0xff) << 16) | ((hdrBuf[3] << 24) & 0xffffffffL);
        if (sig != CENSIG) {
             throw new ZipException("Central Directory Entry not found");
        }
        compressionMethod = (hdrBuf[10] & 0xff) | ((hdrBuf[11] & 0xff) << 8);
        time = (hdrBuf[12] & 0xff) | ((hdrBuf[13] & 0xff) << 8);
        modDate = (hdrBuf[14] & 0xff) | ((hdrBuf[15] & 0xff) << 8);
        crc = (hdrBuf[16] & 0xff) | ((hdrBuf[17] & 0xff) << 8)
                | ((hdrBuf[18] & 0xff) << 16)
                | ((hdrBuf[19] << 24) & 0xffffffffL);
        compressedSize = (hdrBuf[20] & 0xff) | ((hdrBuf[21] & 0xff) << 8)
                | ((hdrBuf[22] & 0xff) << 16)
                | ((hdrBuf[23] << 24) & 0xffffffffL);
        size = (hdrBuf[24] & 0xff) | ((hdrBuf[25] & 0xff) << 8)
                | ((hdrBuf[26] & 0xff) << 16)
                | ((hdrBuf[27] << 24) & 0xffffffffL);
        nameLen = (hdrBuf[28] & 0xff) | ((hdrBuf[29] & 0xff) << 8);
        int extraLen = (hdrBuf[30] & 0xff) | ((hdrBuf[31] & 0xff) << 8);
        int commentLen = (hdrBuf[32] & 0xff) | ((hdrBuf[33] & 0xff) << 8);
        mLocalHeaderRelOffset = (hdrBuf[42] & 0xff) | ((hdrBuf[43] & 0xff) << 8)
                | ((hdrBuf[44] & 0xff) << 16)
                | ((hdrBuf[45] << 24) & 0xffffffffL);
        byte[] nameBytes = new byte[nameLen];
        myReadFully(in, nameBytes);
        byte[] commentBytes = null;
        if (commentLen > 0) {
            commentBytes = new byte[commentLen];
            myReadFully(in, commentBytes);
        }
        if (extraLen > 0) {
            extra = new byte[extraLen];
            myReadFully(in, extra);
        }
        try {
            name = new String(nameBytes, "ISO-8859-1");
            if (commentBytes != null) {
                comment = new String(commentBytes, "ISO-8859-1");
            } else {
                comment = null;
            }
        } catch (UnsupportedEncodingException uee) {
            throw new InternalError(uee.getMessage());
        }
    }
    private void myReadFully(InputStream in, byte[] b) throws IOException {
        int len = b.length;
        int off = 0;
        while (len > 0) {
            int count = in.read(b, off, len);
            if (count <= 0) {
                throw new EOFException();
            }
            off += count;
            len -= count;
        }
    }
    static long readIntLE(RandomAccessFile raf) throws IOException {
        int b0 = raf.read();
        int b1 = raf.read();
        int b2 = raf.read();
        int b3 = raf.read();
        if (b3 < 0) {
            throw new EOFException("in ZipEntry.readIntLE(RandomAccessFile)");
        }
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24); 
    }
    static class LittleEndianReader {
        private byte[] b = new byte[4];
        byte[] hdrBuf = new byte[CENHDR];
        int readShortLE(InputStream in) throws IOException {
            if (in.read(b, 0, 2) == 2) {
                return (b[0] & 0XFF) | ((b[1] & 0XFF) << 8);
            } else {
                throw new EOFException("in ZipEntry.readShortLE(InputStream)");
            }
        }
        long readIntLE(InputStream in) throws IOException {
            if (in.read(b, 0, 4) == 4) {
                return (   ((b[0] & 0XFF))
                         | ((b[1] & 0XFF) << 8)
                         | ((b[2] & 0XFF) << 16)
                         | ((b[3] & 0XFF) << 24))
                       & 0XFFFFFFFFL; 
            } else {
                throw new EOFException("in ZipEntry.readIntLE(InputStream)");
            }
        }
    }
}
