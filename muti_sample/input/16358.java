public class WebRowSetImpl extends CachedRowSetImpl implements WebRowSet {
    private WebRowSetXmlReader xmlReader;
    private WebRowSetXmlWriter xmlWriter;
    private int curPosBfrWrite;
    private SyncProvider provider;
    public WebRowSetImpl() throws SQLException {
        super();
        xmlReader = new WebRowSetXmlReader();
        xmlWriter = new WebRowSetXmlWriter();
    }
    public WebRowSetImpl(Hashtable env) throws SQLException {
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if ( env == null) {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.nullhash").toString());
        }
        String providerName =
            (String)env.get(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER);
        provider = (SyncProvider)SyncFactory.getInstance(providerName);
    }
    public void writeXml(ResultSet rs, java.io.Writer writer)
        throws SQLException {
            this.populate(rs);
            curPosBfrWrite = this.getRow();
            this.writeXml(writer);
    }
    public void writeXml(java.io.Writer writer) throws SQLException {
        if (xmlWriter != null) {
            curPosBfrWrite = this.getRow();
            xmlWriter.writeXML(this, writer);
        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidwr").toString());
        }
    }
    public void readXml(java.io.Reader reader) throws SQLException {
        try {
             if (reader != null) {
                xmlReader.readXML(this, reader);
                if(curPosBfrWrite == 0) {
                   this.beforeFirst();
                }
                else {
                   this.absolute(curPosBfrWrite);
                }
            } else {
                throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidrd").toString());
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }
    public void readXml(java.io.InputStream iStream) throws SQLException, IOException {
        if (iStream != null) {
            xmlReader.readXML(this, iStream);
                if(curPosBfrWrite == 0) {
                   this.beforeFirst();
                }
                else {
                   this.absolute(curPosBfrWrite);
                }
        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidrd").toString());
        }
    }
    public void writeXml(java.io.OutputStream oStream) throws SQLException, IOException {
        if (xmlWriter != null) {
            curPosBfrWrite = this.getRow();
            xmlWriter.writeXML(this, oStream);
        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidwr").toString());
        }
    }
    public void writeXml(ResultSet rs, java.io.OutputStream oStream) throws SQLException, IOException {
            this.populate(rs);
            curPosBfrWrite = this.getRow();
            this.writeXml(oStream);
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID = -8771775154092422943L;
}
