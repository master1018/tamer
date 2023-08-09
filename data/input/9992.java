public class SerialBlob implements Blob, Serializable, Cloneable {
    private byte buf[];
    private Blob blob;
    private long len;
    private long origLen;
    public SerialBlob(byte[] b) throws SerialException, SQLException {
        len = b.length;
        buf = new byte[(int)len];
        for(int i = 0; i < len; i++) {
           buf[i] = b[i];
        }
        origLen = len;
    }
    public SerialBlob (Blob blob) throws SerialException, SQLException {
        if (blob == null) {
            throw new SQLException("Cannot instantiate a SerialBlob " +
                 "object with a null Blob object");
        }
        len = blob.length();
        buf = blob.getBytes(1, (int)len );
        this.blob = blob;
        origLen = len;
    }
    public byte[] getBytes(long pos, int length) throws SerialException {
        if (length > len) {
            length = (int)len;
        }
        if (pos < 1 || len - pos < 0 ) {
            throw new SerialException("Invalid arguments: position cannot be "
                    + "less than 1 or greater than the length of the SerialBlob");
        }
        pos--; 
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = this.buf[(int)pos];
            pos++;
        }
        return b;
    }
    public long length() throws SerialException {
        return len;
    }
    public java.io.InputStream getBinaryStream() throws SerialException {
         InputStream stream = new ByteArrayInputStream(buf);
         return (java.io.InputStream)stream;
    }
    public long position(byte[] pattern, long start)
                throws SerialException, SQLException {
        if (start < 1 || start > len) {
            return -1;
        }
        int pos = (int)start-1; 
        int i = 0;
        long patlen = pattern.length;
        while (pos < len) {
            if (pattern[i] == buf[pos]) {
                if (i + 1 == patlen) {
                    return (pos + 1) - (patlen - 1);
                }
                i++; pos++; 
            } else if (pattern[i] != buf[pos]) {
                pos++; 
            }
        }
        return -1; 
    }
    public long position(Blob pattern, long start)
       throws SerialException, SQLException {
        return position(pattern.getBytes(1, (int)(pattern.length())), start);
    }
    public int setBytes(long pos, byte[] bytes)
        throws SerialException, SQLException {
        return (setBytes(pos, bytes, 0, bytes.length));
    }
    public int setBytes(long pos, byte[] bytes, int offset, int length)
        throws SerialException, SQLException {
        if (offset < 0 || offset > bytes.length) {
            throw new SerialException("Invalid offset in byte array set");
        }
        if (pos < 1 || pos > this.length()) {
            throw new SerialException("Invalid position in BLOB object set");
        }
        if ((long)(length) > origLen) {
            throw new SerialException("Buffer is not sufficient to hold the value");
        }
        if ((length + offset) > bytes.length) {
            throw new SerialException("Invalid OffSet. Cannot have combined offset " +
                "and length that is greater that the Blob buffer");
        }
        int i = 0;
        pos--; 
        while ( i < length || (offset + i +1) < (bytes.length-offset) ) {
            this.buf[(int)pos + i] = bytes[offset + i ];
            i++;
        }
        return i;
    }
    public java.io.OutputStream setBinaryStream(long pos)
        throws SerialException, SQLException {
        if (this.blob.setBinaryStream(pos) != null) {
            return this.blob.setBinaryStream(pos);
        } else {
            throw new SerialException("Unsupported operation. SerialBlob cannot " +
                "return a writable binary stream, unless instantiated with a Blob object " +
                "that provides a setBinaryStream() implementation");
        }
    }
    public void truncate(long length) throws SerialException {
         if (length > len) {
            throw new SerialException
               ("Length more than what can be truncated");
         } else if((int)length == 0) {
              buf = new byte[0];
              len = length;
         } else {
              len = length;
              buf = this.getBytes(1, (int)len);
         }
    }
    public InputStream getBinaryStream(long pos,long length) throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }
    public void free() throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }
    static final long serialVersionUID = -8144641928112860441L;
}
