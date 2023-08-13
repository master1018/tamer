public class SQLOutputImpl implements SQLOutput {
    private Vector attribs;
    private Map map;
    public SQLOutputImpl(Vector<?> attributes, Map<String,?> map)
        throws SQLException
    {
        if ((attributes == null) || (map == null)) {
            throw new SQLException("Cannot instantiate a SQLOutputImpl " +
            "instance with null parameters");
        }
        this.attribs = attributes;
        this.map = map;
    }
    public void writeString(String x) throws SQLException {
        attribs.add(x);
    }
    public void writeBoolean(boolean x) throws SQLException {
        attribs.add(Boolean.valueOf(x));
    }
    public void writeByte(byte x) throws SQLException {
        attribs.add(Byte.valueOf(x));
    }
    public void writeShort(short x) throws SQLException {
        attribs.add(Short.valueOf(x));
    }
    public void writeInt(int x) throws SQLException {
        attribs.add(Integer.valueOf(x));
    }
    public void writeLong(long x) throws SQLException {
        attribs.add(Long.valueOf(x));
    }
    public void writeFloat(float x) throws SQLException {
        attribs.add(new Float(x));
    }
    public void writeDouble(double x) throws SQLException{
        attribs.add(new Double(x));
    }
    public void writeBigDecimal(java.math.BigDecimal x) throws SQLException{
        attribs.add(x);
    }
    public void writeBytes(byte[] x) throws SQLException {
        attribs.add(x);
    }
    public void writeDate(java.sql.Date x) throws SQLException {
        attribs.add(x);
    }
    public void writeTime(java.sql.Time x) throws SQLException {
        attribs.add(x);
    }
    public void writeTimestamp(java.sql.Timestamp x) throws SQLException {
        attribs.add(x);
    }
    public void writeCharacterStream(java.io.Reader x) throws SQLException {
         BufferedReader bufReader = new BufferedReader(x);
         try {
             int i;
             while( (i = bufReader.read()) != -1 ) {
                char ch = (char)i;
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(ch);
                String str = new String(strBuf);
                String strLine = bufReader.readLine();
                writeString(str.concat(strLine));
             }
         } catch(IOException ioe) {
         }
    }
    public void writeAsciiStream(java.io.InputStream x) throws SQLException {
         BufferedReader bufReader = new BufferedReader(new InputStreamReader(x));
         try {
               int i;
               while( (i=bufReader.read()) != -1 ) {
                char ch = (char)i;
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(ch);
                String str = new String(strBuf);
                String strLine = bufReader.readLine();
                writeString(str.concat(strLine));
            }
          }catch(IOException ioe) {
            throw new SQLException(ioe.getMessage());
        }
    }
    public void writeBinaryStream(java.io.InputStream x) throws SQLException {
         BufferedReader bufReader = new BufferedReader(new InputStreamReader(x));
         try {
               int i;
             while( (i=bufReader.read()) != -1 ) {
                char ch = (char)i;
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(ch);
                String str = new String(strBuf);
                String strLine = bufReader.readLine();
                writeString(str.concat(strLine));
             }
        } catch(IOException ioe) {
            throw new SQLException(ioe.getMessage());
        }
    }
    public void writeObject(SQLData x) throws SQLException {
        if (x == null) {
            attribs.add(x);
            return;
        }
        attribs.add(new SerialStruct((SQLData)x, map));
    }
    public void writeRef(Ref x) throws SQLException {
        if (x == null) {
            attribs.add(x);
            return;
        }
        attribs.add(new SerialRef(x));
    }
    public void writeBlob(Blob x) throws SQLException {
        if (x == null) {
            attribs.add(x);
            return;
        }
        attribs.add(new SerialBlob(x));
    }
    public void writeClob(Clob x) throws SQLException {
        if (x == null) {
            attribs.add(x);
            return;
        }
        attribs.add(new SerialClob(x));
    }
    public void writeStruct(Struct x) throws SQLException {
        SerialStruct s = new SerialStruct(x,map);;
        attribs.add(s);
    }
    public void writeArray(Array x) throws SQLException {
        if (x == null) {
            attribs.add(x);
            return;
        }
        attribs.add(new SerialArray(x, map));
    }
    public void writeURL(java.net.URL url) throws SQLException {
        if (url == null) {
            attribs.add(url);
            return;
        }
        attribs.add(new SerialDatalink(url));
    }
   public void writeNString(String x) throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
   public void writeNClob(NClob x) throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
   public void writeRowId(RowId x) throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
   public void writeSQLXML(SQLXML x) throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
}
