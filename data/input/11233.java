public class SerialClob implements Clob, Serializable, Cloneable {
    private char buf[];
    private final Clob clob;
    private long len;
    private final long origLen;
    public SerialClob(char ch[]) throws SerialException, SQLException {
        len = ch.length;
        buf = new char[(int)len];
        for (int i = 0; i < len ; i++){
           buf[i] = ch[i];
        }
        origLen = len;
        clob = null;
    }
    public SerialClob(Clob clob) throws SerialException, SQLException {
        if (clob == null) {
            throw new SQLException("Cannot instantiate a SerialClob " +
                "object with a null Clob object");
        }
        len = clob.length();
        this.clob = clob;
        buf = new char[(int)len];
        int read = 0;
        int offset = 0;
        try (Reader charStream = clob.getCharacterStream()) {
            if (charStream == null) {
                throw new SQLException("Invalid Clob object. The call to getCharacterStream " +
                    "returned null which cannot be serialized.");
            }
            try (InputStream asciiStream = clob.getAsciiStream()) {
                if (asciiStream == null) {
                    throw new SQLException("Invalid Clob object. The call to getAsciiStream " +
                        "returned null which cannot be serialized.");
                }
            }
            try (Reader reader = new BufferedReader(charStream)) {
                do {
                    read = reader.read(buf, offset, (int)(len - offset));
                    offset += read;
                } while (read > 0);
            }
        } catch (java.io.IOException ex) {
            throw new SerialException("SerialClob: " + ex.getMessage());
        }
        origLen = len;
    }
    public long length() throws SerialException {
        return len;
    }
    public java.io.Reader getCharacterStream() throws SerialException {
        return (java.io.Reader) new CharArrayReader(buf);
    }
    public java.io.InputStream getAsciiStream() throws SerialException, SQLException {
        if (this.clob != null) {
            return this.clob.getAsciiStream();
        } else {
            throw new SerialException("Unsupported operation. SerialClob cannot " +
                "return a the CLOB value as an ascii stream, unless instantiated " +
                "with a fully implemented Clob object.");
        }
    }
    public String getSubString(long pos, int length) throws SerialException {
        if (pos < 1 || pos > this.length()) {
            throw new SerialException("Invalid position in BLOB object set");
        }
        if ((pos-1) + length > this.length()) {
            throw new SerialException("Invalid position and substring length");
        }
        try {
            return new String(buf, (int)pos - 1, length);
        } catch (StringIndexOutOfBoundsException e) {
            throw new SerialException("StringIndexOutOfBoundsException: " +
                e.getMessage());
        }
    }
    public long position(String searchStr, long start)
        throws SerialException, SQLException {
        if (start < 1 || start > len) {
            return -1;
        }
        char pattern[] = searchStr.toCharArray();
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
    public long position(Clob searchStr, long start)
        throws SerialException, SQLException {
        return position(searchStr.getSubString(1,(int)searchStr.length()), start);
    }
    public int setString(long pos, String str) throws SerialException {
        return (setString(pos, str, 0, str.length()));
    }
    public int setString(long pos, String str, int offset, int length)
        throws SerialException {
        String temp = str.substring(offset);
        char cPattern[] = temp.toCharArray();
        if (offset < 0 || offset > str.length()) {
            throw new SerialException("Invalid offset in byte array set");
        }
        if (pos < 1 || pos > this.length()) {
            throw new SerialException("Invalid position in BLOB object set");
        }
        if ((long)(length) > origLen) {
            throw new SerialException("Buffer is not sufficient to hold the value");
        }
        if ((length + offset) > str.length()) {
            throw new SerialException("Invalid OffSet. Cannot have combined offset " +
                " and length that is greater that the Blob buffer");
        }
        int i = 0;
        pos--;  
        while ( i < length || (offset + i +1) < (str.length() - offset ) ) {
            this.buf[(int)pos + i ] = cPattern[offset + i ];
            i++;
        }
        return i;
    }
    public java.io.OutputStream setAsciiStream(long pos)
        throws SerialException, SQLException {
         if (this.clob.setAsciiStream(pos) != null) {
             return this.clob.setAsciiStream(pos);
         } else {
             throw new SerialException("Unsupported operation. SerialClob cannot " +
                "return a writable ascii stream\n unless instantiated with a Clob object " +
                "that has a setAsciiStream() implementation");
         }
    }
    public java.io.Writer setCharacterStream(long pos)
        throws SerialException, SQLException {
        if (this.clob.setCharacterStream(pos) != null) {
            return this.clob.setCharacterStream(pos);
        } else {
            throw new SerialException("Unsupported operation. SerialClob cannot " +
                "return a writable character stream\n unless instantiated with a Clob object " +
                "that has a setCharacterStream implementation");
        }
    }
    public void truncate(long length) throws SerialException {
         if (length > len) {
            throw new SerialException
               ("Length more than what can be truncated");
         } else {
              len = length;
              if (len == 0) {
                  buf = new char[] {};
              } else {
                buf = (this.getSubString(1, (int)len)).toCharArray();
              }
         }
    }
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }
    public void free() throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }
    static final long serialVersionUID = -1662519690087375313L;
}
