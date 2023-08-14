public class JdbcRowSetImpl extends BaseRowSet implements JdbcRowSet, Joinable {
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;
    private RowSetMetaDataImpl rowsMD;
    private ResultSetMetaData resMD;
    private PropertyChangeSupport propertyChangeSupport;
    private Vector<Integer> iMatchColumns;
    private Vector<String> strMatchColumns;
    protected transient JdbcRowSetResourceBundle resBundle;
    public JdbcRowSetImpl() {
        conn = null;
        ps   = null;
        rs   = null;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        propertyChangeSupport = new PropertyChangeSupport(this);
        initParams();
        try {
            setShowDeleted(false);
        } catch(SQLException sqle) {
             System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setshowdeleted").toString() +
                                sqle.getLocalizedMessage());
        }
        try {
            setQueryTimeout(0);
        } catch(SQLException sqle) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setquerytimeout").toString() +
                                sqle.getLocalizedMessage());
        }
        try {
            setMaxRows(0);
        } catch(SQLException sqle) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setmaxrows").toString() +
                                sqle.getLocalizedMessage());
        }
        try {
            setMaxFieldSize(0);
        } catch(SQLException sqle) {
             System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setmaxfieldsize").toString() +
                                sqle.getLocalizedMessage());
        }
        try {
            setEscapeProcessing(true);
        } catch(SQLException sqle) {
             System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setescapeprocessing").toString() +
                                sqle.getLocalizedMessage());
        }
        try {
            setConcurrency(ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException sqle) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setconcurrency").toString() +
                                sqle.getLocalizedMessage());
        }
        setTypeMap(null);
        try {
            setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
        } catch(SQLException sqle){
          System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.settype").toString() +
                                sqle.getLocalizedMessage());
        }
        setReadOnly(true);
        try {
            setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch(SQLException sqle){
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.settransactionisolation").toString() +
                                sqle.getLocalizedMessage());
        }
        iMatchColumns = new Vector<Integer>(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,Integer.valueOf(-1));
        }
        strMatchColumns = new Vector<String>(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }
    public JdbcRowSetImpl(Connection con) throws SQLException {
        conn = con;
        ps = null;
        rs = null;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        propertyChangeSupport = new PropertyChangeSupport(this);
        initParams();
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        setParams();
        setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        setTypeMap(null);
        iMatchColumns = new Vector<Integer>(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,Integer.valueOf(-1));
        }
        strMatchColumns = new Vector<String>(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }
    public JdbcRowSetImpl(String url, String user, String password) throws SQLException {
        conn = null;
        ps = null;
        rs = null;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        propertyChangeSupport = new PropertyChangeSupport(this);
        initParams();
        setUsername(user);
        setPassword(password);
        setUrl(url);
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        conn = connect();
        setParams();
        setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        setTypeMap(null);
        iMatchColumns = new Vector<Integer>(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,Integer.valueOf(-1));
        }
        strMatchColumns = new Vector<String>(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }
    public JdbcRowSetImpl(ResultSet res) throws SQLException {
        conn = null;
        ps = null;
        rs = res;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
        propertyChangeSupport = new PropertyChangeSupport(this);
        initParams();
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        setParams();
        setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        setTypeMap(null);
        resMD = rs.getMetaData();
        rowsMD = new RowSetMetaDataImpl();
        initMetaData(rowsMD, resMD);
        iMatchColumns = new Vector<Integer>(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,Integer.valueOf(-1));
        }
        strMatchColumns = new Vector<String>(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }
    protected void initMetaData(RowSetMetaData md, ResultSetMetaData rsmd) throws SQLException {
        int numCols = rsmd.getColumnCount();
        md.setColumnCount(numCols);
        for (int col=1; col <= numCols; col++) {
            md.setAutoIncrement(col, rsmd.isAutoIncrement(col));
            md.setCaseSensitive(col, rsmd.isCaseSensitive(col));
            md.setCurrency(col, rsmd.isCurrency(col));
            md.setNullable(col, rsmd.isNullable(col));
            md.setSigned(col, rsmd.isSigned(col));
            md.setSearchable(col, rsmd.isSearchable(col));
            md.setColumnDisplaySize(col, rsmd.getColumnDisplaySize(col));
            md.setColumnLabel(col, rsmd.getColumnLabel(col));
            md.setColumnName(col, rsmd.getColumnName(col));
            md.setSchemaName(col, rsmd.getSchemaName(col));
            md.setPrecision(col, rsmd.getPrecision(col));
            md.setScale(col, rsmd.getScale(col));
            md.setTableName(col, rsmd.getTableName(col));
            md.setCatalogName(col, rsmd.getCatalogName(col));
            md.setColumnType(col, rsmd.getColumnType(col));
            md.setColumnTypeName(col, rsmd.getColumnTypeName(col));
        }
    }
    protected void checkState() throws SQLException {
        if (conn == null && ps == null && rs == null ) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.invalstate").toString());
        }
    }
    public void execute() throws SQLException {
        prepare();
        setProperties(ps);
        decodeParams(getParams(), ps);
        rs = ps.executeQuery();
        notifyRowSetChanged();
    }
    protected void setProperties(PreparedStatement ps) throws SQLException {
        try {
            ps.setEscapeProcessing(getEscapeProcessing());
        } catch (SQLException ex) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setescapeprocessing").toString() +
                                ex.getLocalizedMessage());
        }
        try {
            ps.setMaxFieldSize(getMaxFieldSize());
        } catch (SQLException ex) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setmaxfieldsize").toString() +
                                ex.getLocalizedMessage());
        }
        try {
            ps.setMaxRows(getMaxRows());
        } catch (SQLException ex) {
           System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setmaxrows").toString() +
                                ex.getLocalizedMessage());
        }
        try {
            ps.setQueryTimeout(getQueryTimeout());
        } catch (SQLException ex) {
           System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.setquerytimeout").toString() +
                                ex.getLocalizedMessage());
        }
    }
    protected Connection connect() throws SQLException {
        if(conn != null) {
            return conn;
        } else if (getDataSourceName() != null) {
            try {
                Context ctx = new InitialContext();
                DataSource ds = (DataSource)ctx.lookup
                    (getDataSourceName());
                if(getUsername() != null && !getUsername().equals("")) {
                     return ds.getConnection(getUsername(),getPassword());
                } else {
                     return ds.getConnection();
                }
            }
            catch (javax.naming.NamingException ex) {
                throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.connect").toString());
            }
        } else if (getUrl() != null) {
            return DriverManager.getConnection
                    (getUrl(), getUsername(), getPassword());
        }
        else {
            return null;
        }
    }
    protected PreparedStatement prepare() throws SQLException {
        conn = connect();
        try {
            Map<String, Class<?>> aMap = getTypeMap();
            if( aMap != null) {
                conn.setTypeMap(aMap);
            }
            ps = conn.prepareStatement(getCommand(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException ex) {
            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.prepare").toString() +
                                ex.getLocalizedMessage());
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
            throw new SQLException(ex.getMessage());
        }
        return ps;
    }
    private void decodeParams(Object[] params, PreparedStatement ps)
    throws SQLException {
        int arraySize;
        Object[] param = null;
        for (int i=0; i < params.length; i++) {
            if (params[i] instanceof Object[]) {
                param = (Object[])params[i];
                if (param.length == 2) {
                    if (param[0] == null) {
                        ps.setNull(i + 1, ((Integer)param[1]).intValue());
                        continue;
                    }
                    if (param[0] instanceof java.sql.Date ||
                        param[0] instanceof java.sql.Time ||
                        param[0] instanceof java.sql.Timestamp) {
                        System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.detecteddate"));
                        if (param[1] instanceof java.util.Calendar) {
                            System.err.println(resBundle.handleGetObject("jdbcrowsetimpl.detectedcalendar"));
                            ps.setDate(i + 1, (java.sql.Date)param[0],
                                       (java.util.Calendar)param[1]);
                            continue;
                        }
                        else {
                            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.paramtype").toString());
                        }
                    }
                    if (param[0] instanceof Reader) {
                        ps.setCharacterStream(i + 1, (Reader)param[0],
                                              ((Integer)param[1]).intValue());
                        continue;
                    }
                    if (param[1] instanceof Integer) {
                        ps.setObject(i + 1, param[0], ((Integer)param[1]).intValue());
                        continue;
                    }
                } else if (param.length == 3) {
                    if (param[0] == null) {
                        ps.setNull(i + 1, ((Integer)param[1]).intValue(),
                                   (String)param[2]);
                        continue;
                    }
                    if (param[0] instanceof java.io.InputStream) {
                        switch (((Integer)param[2]).intValue()) {
                        case JdbcRowSetImpl.UNICODE_STREAM_PARAM:
                            ps.setUnicodeStream(i + 1,
                                                (java.io.InputStream)param[0],
                                                ((Integer)param[1]).intValue());
                        case JdbcRowSetImpl.BINARY_STREAM_PARAM:
                            ps.setBinaryStream(i + 1,
                                               (java.io.InputStream)param[0],
                                               ((Integer)param[1]).intValue());
                        case JdbcRowSetImpl.ASCII_STREAM_PARAM:
                            ps.setAsciiStream(i + 1,
                                              (java.io.InputStream)param[0],
                                              ((Integer)param[1]).intValue());
                        default:
                            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.paramtype").toString());
                        }
                    }
                    if (param[1] instanceof Integer && param[2] instanceof Integer) {
                        ps.setObject(i + 1, param[0], ((Integer)param[1]).intValue(),
                                     ((Integer)param[2]).intValue());
                        continue;
                    }
                    throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.paramtype").toString());
                } else {
                    ps.setObject(i + 1, params[i]);
                    continue;
                }
            }  else {
               ps.setObject(i + 1, params[i]);
            }
        }
    }
    public boolean next() throws SQLException {
        checkState();
        boolean b = rs.next();
        notifyCursorMoved();
        return b;
    }
    public void close() throws SQLException {
        if (rs != null)
            rs.close();
        if (ps != null)
            ps.close();
        if (conn != null)
            conn.close();
    }
    public boolean wasNull() throws SQLException {
        checkState();
        return rs.wasNull();
    }
    public String getString(int columnIndex) throws SQLException {
        checkState();
        return rs.getString(columnIndex);
    }
    public boolean getBoolean(int columnIndex) throws SQLException {
        checkState();
        return rs.getBoolean(columnIndex);
    }
    public byte getByte(int columnIndex) throws SQLException {
        checkState();
        return rs.getByte(columnIndex);
    }
    public short getShort(int columnIndex) throws SQLException {
        checkState();
        return rs.getShort(columnIndex);
    }
    public int getInt(int columnIndex) throws SQLException {
        checkState();
        return rs.getInt(columnIndex);
    }
    public long getLong(int columnIndex) throws SQLException {
        checkState();
        return rs.getLong(columnIndex);
    }
    public float getFloat(int columnIndex) throws SQLException {
        checkState();
        return rs.getFloat(columnIndex);
    }
    public double getDouble(int columnIndex) throws SQLException {
        checkState();
        return rs.getDouble(columnIndex);
    }
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        checkState();
        return rs.getBigDecimal(columnIndex, scale);
    }
    public byte[] getBytes(int columnIndex) throws SQLException {
        checkState();
        return rs.getBytes(columnIndex);
    }
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        checkState();
        return rs.getDate(columnIndex);
    }
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        checkState();
        return rs.getTime(columnIndex);
    }
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        checkState();
        return rs.getTimestamp(columnIndex);
    }
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        checkState();
        return rs.getAsciiStream(columnIndex);
    }
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        checkState();
        return rs.getUnicodeStream(columnIndex);
    }
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        checkState();
        return rs.getBinaryStream(columnIndex);
    }
    public String getString(String columnName) throws SQLException {
        return getString(findColumn(columnName));
    }
    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(findColumn(columnName));
    }
    public byte getByte(String columnName) throws SQLException {
        return getByte(findColumn(columnName));
    }
    public short getShort(String columnName) throws SQLException {
        return getShort(findColumn(columnName));
    }
    public int getInt(String columnName) throws SQLException {
        return getInt(findColumn(columnName));
    }
    public long getLong(String columnName) throws SQLException {
        return getLong(findColumn(columnName));
    }
    public float getFloat(String columnName) throws SQLException {
        return getFloat(findColumn(columnName));
    }
    public double getDouble(String columnName) throws SQLException {
        return getDouble(findColumn(columnName));
    }
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getBigDecimal(findColumn(columnName), scale);
    }
    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(findColumn(columnName));
    }
    public java.sql.Date getDate(String columnName) throws SQLException {
        return getDate(findColumn(columnName));
    }
    public java.sql.Time getTime(String columnName) throws SQLException {
        return getTime(findColumn(columnName));
    }
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        return getTimestamp(findColumn(columnName));
    }
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        return getAsciiStream(findColumn(columnName));
    }
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        return getUnicodeStream(findColumn(columnName));
    }
    public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
        return getBinaryStream(findColumn(columnName));
    }
    public SQLWarning getWarnings() throws SQLException {
        checkState();
        return rs.getWarnings();
    }
    public void clearWarnings() throws SQLException {
        checkState();
        rs.clearWarnings();
    }
    public String getCursorName() throws SQLException {
        checkState();
        return rs.getCursorName();
    }
    public ResultSetMetaData getMetaData() throws SQLException {
        checkState();
        try {
             checkState();
        } catch(SQLException sqle) {
             prepare();
             return ps.getMetaData();
        }
        return rs.getMetaData();
    }
    public Object getObject(int columnIndex) throws SQLException {
        checkState();
        return rs.getObject(columnIndex);
    }
    public Object getObject(String columnName) throws SQLException {
        return getObject(findColumn(columnName));
    }
    public int findColumn(String columnName) throws SQLException {
        checkState();
        return rs.findColumn(columnName);
    }
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
        checkState();
        return rs.getCharacterStream(columnIndex);
    }
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        return getCharacterStream(findColumn(columnName));
    }
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        checkState();
        return rs.getBigDecimal(columnIndex);
    }
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(findColumn(columnName));
    }
    public boolean isBeforeFirst() throws SQLException {
        checkState();
        return rs.isBeforeFirst();
    }
    public boolean isAfterLast() throws SQLException {
        checkState();
        return rs.isAfterLast();
    }
    public boolean isFirst() throws SQLException {
        checkState();
        return rs.isFirst();
    }
    public boolean isLast() throws SQLException {
        checkState();
        return rs.isLast();
    }
    public void beforeFirst() throws SQLException {
        checkState();
        rs.beforeFirst();
        notifyCursorMoved();
    }
    public void afterLast() throws SQLException {
        checkState();
        rs.afterLast();
        notifyCursorMoved();
    }
    public boolean first() throws SQLException {
        checkState();
        boolean b = rs.first();
        notifyCursorMoved();
        return b;
    }
    public boolean last() throws SQLException {
        checkState();
        boolean b = rs.last();
        notifyCursorMoved();
        return b;
    }
    public int getRow() throws SQLException {
        checkState();
        return rs.getRow();
    }
    public boolean absolute(int row) throws SQLException {
        checkState();
        boolean b = rs.absolute(row);
        notifyCursorMoved();
        return b;
    }
    public boolean relative(int rows) throws SQLException {
        checkState();
        boolean b = rs.relative(rows);
        notifyCursorMoved();
        return b;
    }
    public boolean previous() throws SQLException {
        checkState();
        boolean b = rs.previous();
        notifyCursorMoved();
        return b;
    }
    public void setFetchDirection(int direction) throws SQLException {
        checkState();
        rs.setFetchDirection(direction);
    }
    public int getFetchDirection() throws SQLException {
        try {
             checkState();
        } catch(SQLException sqle) {
             super.getFetchDirection();
        }
        return rs.getFetchDirection();
    }
    public void setFetchSize(int rows) throws SQLException {
        checkState();
        rs.setFetchSize(rows);
    }
    public int getType() throws SQLException {
        try {
             checkState();
        } catch(SQLException sqle) {
            return super.getType();
        }
        if(rs == null) {
            return super.getType();
        } else {
           int rstype = rs.getType();
            return rstype;
        }
    }
    public int getConcurrency() throws SQLException {
        try {
             checkState();
        } catch(SQLException sqle) {
             super.getConcurrency();
        }
        return rs.getConcurrency();
    }
    public boolean rowUpdated() throws SQLException {
        checkState();
        return rs.rowUpdated();
    }
    public boolean rowInserted() throws SQLException {
        checkState();
        return rs.rowInserted();
    }
    public boolean rowDeleted() throws SQLException {
        checkState();
        return rs.rowDeleted();
    }
    public void updateNull(int columnIndex) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateNull(columnIndex);
    }
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateBoolean(columnIndex, x);
    }
    public void updateByte(int columnIndex, byte x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateByte(columnIndex, x);
    }
    public void updateShort(int columnIndex, short x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateShort(columnIndex, x);
    }
    public void updateInt(int columnIndex, int x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateInt(columnIndex, x);
    }
    public void updateLong(int columnIndex, long x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateLong(columnIndex, x);
    }
    public void updateFloat(int columnIndex, float x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateFloat(columnIndex, x);
    }
    public void updateDouble(int columnIndex, double x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateDouble(columnIndex, x);
    }
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateBigDecimal(columnIndex, x);
    }
    public void updateString(int columnIndex, String x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateString(columnIndex, x);
    }
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateBytes(columnIndex, x);
    }
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateDate(columnIndex, x);
    }
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateTime(columnIndex, x);
    }
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateTimestamp(columnIndex, x);
    }
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateAsciiStream(columnIndex, x, length);
    }
    public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateBinaryStream(columnIndex, x, length);
    }
    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateCharacterStream(columnIndex, x, length);
    }
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateObject(columnIndex, x, scale);
    }
    public void updateObject(int columnIndex, Object x) throws SQLException {
        checkState();
        checkTypeConcurrency();
        rs.updateObject(columnIndex, x);
    }
    public void updateNull(String columnName) throws SQLException {
        updateNull(findColumn(columnName));
    }
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        updateBoolean(findColumn(columnName), x);
    }
    public void updateByte(String columnName, byte x) throws SQLException {
        updateByte(findColumn(columnName), x);
    }
    public void updateShort(String columnName, short x) throws SQLException {
        updateShort(findColumn(columnName), x);
    }
    public void updateInt(String columnName, int x) throws SQLException {
        updateInt(findColumn(columnName), x);
    }
    public void updateLong(String columnName, long x) throws SQLException {
        updateLong(findColumn(columnName), x);
    }
    public void updateFloat(String columnName, float x) throws SQLException {
        updateFloat(findColumn(columnName), x);
    }
    public void updateDouble(String columnName, double x) throws SQLException {
        updateDouble(findColumn(columnName), x);
    }
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        updateBigDecimal(findColumn(columnName), x);
    }
    public void updateString(String columnName, String x) throws SQLException {
        updateString(findColumn(columnName), x);
    }
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        updateBytes(findColumn(columnName), x);
    }
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        updateDate(findColumn(columnName), x);
    }
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        updateTime(findColumn(columnName), x);
    }
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
        updateTimestamp(findColumn(columnName), x);
    }
    public void updateAsciiStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        updateAsciiStream(findColumn(columnName), x, length);
    }
    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        updateBinaryStream(findColumn(columnName), x, length);
    }
    public void updateCharacterStream(String columnName, java.io.Reader reader, int length) throws SQLException {
        updateCharacterStream(findColumn(columnName), reader, length);
    }
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        updateObject(findColumn(columnName), x, scale);
    }
    public void updateObject(String columnName, Object x) throws SQLException {
        updateObject(findColumn(columnName), x);
    }
    public void insertRow() throws SQLException {
        checkState();
        rs.insertRow();
        notifyRowChanged();
    }
    public void updateRow() throws SQLException {
        checkState();
        rs.updateRow();
        notifyRowChanged();
    }
    public void deleteRow() throws SQLException {
        checkState();
        rs.deleteRow();
        notifyRowChanged();
    }
    public void refreshRow() throws SQLException {
        checkState();
        rs.refreshRow();
    }
    public void cancelRowUpdates() throws SQLException {
        checkState();
        rs.cancelRowUpdates();
        notifyRowChanged();
    }
    public void moveToInsertRow() throws SQLException {
        checkState();
        rs.moveToInsertRow();
    }
    public void moveToCurrentRow() throws SQLException {
        checkState();
        rs.moveToCurrentRow();
    }
    public java.sql.Statement getStatement() throws SQLException {
        if(rs != null)
        {
           return rs.getStatement();
        } else {
           return null;
        }
    }
    public Object getObject(int i, java.util.Map<String,Class<?>> map)
        throws SQLException
    {
        checkState();
        return rs.getObject(i, map);
    }
    public Ref getRef(int i) throws SQLException {
        checkState();
        return rs.getRef(i);
    }
    public Blob getBlob(int i) throws SQLException {
        checkState();
        return rs.getBlob(i);
    }
    public Clob getClob(int i) throws SQLException {
        checkState();
        return rs.getClob(i);
    }
    public Array getArray(int i) throws SQLException {
        checkState();
        return rs.getArray(i);
    }
    public Object getObject(String colName, java.util.Map<String,Class<?>> map)
        throws SQLException
    {
        return getObject(findColumn(colName), map);
    }
    public Ref getRef(String colName) throws SQLException {
        return getRef(findColumn(colName));
    }
    public Blob getBlob(String colName) throws SQLException {
        return getBlob(findColumn(colName));
    }
    public Clob getClob(String colName) throws SQLException {
        return getClob(findColumn(colName));
    }
    public Array getArray(String colName) throws SQLException {
        return getArray(findColumn(colName));
    }
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        checkState();
        return rs.getDate(columnIndex, cal);
    }
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        return getDate(findColumn(columnName), cal);
    }
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        checkState();
        return rs.getTime(columnIndex, cal);
    }
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        return getTime(findColumn(columnName), cal);
    }
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        checkState();
        return rs.getTimestamp(columnIndex, cal);
    }
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnName), cal);
    }
    public void updateRef(int columnIndex, java.sql.Ref ref)
        throws SQLException {
        checkState();
        rs.updateRef(columnIndex, ref);
    }
    public void updateRef(String columnName, java.sql.Ref ref)
        throws SQLException {
        updateRef(findColumn(columnName), ref);
    }
    public void updateClob(int columnIndex, Clob c) throws SQLException {
        checkState();
        rs.updateClob(columnIndex, c);
    }
    public void updateClob(String columnName, Clob c) throws SQLException {
        updateClob(findColumn(columnName), c);
    }
    public void updateBlob(int columnIndex, Blob b) throws SQLException {
        checkState();
        rs.updateBlob(columnIndex, b);
    }
    public void updateBlob(String columnName, Blob b) throws SQLException {
        updateBlob(findColumn(columnName), b);
    }
    public void updateArray(int columnIndex, Array a) throws SQLException {
        checkState();
        rs.updateArray(columnIndex, a);
    }
    public void updateArray(String columnName, Array a) throws SQLException {
        updateArray(findColumn(columnName), a);
    }
    public java.net.URL getURL(int columnIndex) throws SQLException {
        checkState();
        return rs.getURL(columnIndex);
    }
    public java.net.URL getURL(String columnName) throws SQLException {
        return getURL(findColumn(columnName));
    }
    public RowSetWarning getRowSetWarnings() throws SQLException {
       return null;
    }
    public void unsetMatchColumn(int[] columnIdxes) throws SQLException {
         int i_val;
         for( int j= 0 ;j < columnIdxes.length; j++) {
            i_val = (Integer.parseInt(iMatchColumns.get(j).toString()));
            if(columnIdxes[j] != i_val) {
               throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols").toString());
            }
         }
         for( int i = 0;i < columnIdxes.length ;i++) {
            iMatchColumns.set(i,Integer.valueOf(-1));
         }
    }
    public void unsetMatchColumn(String[] columnIdxes) throws SQLException {
        for(int j = 0 ;j < columnIdxes.length; j++) {
           if( !columnIdxes[j].equals(strMatchColumns.get(j)) ){
              throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols").toString());
           }
        }
        for(int i = 0 ; i < columnIdxes.length; i++) {
           strMatchColumns.set(i,null);
        }
    }
    public String[] getMatchColumnNames() throws SQLException {
        String []str_temp = new String[strMatchColumns.size()];
        if( strMatchColumns.get(0) == null) {
           throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.setmatchcols").toString());
        }
        strMatchColumns.copyInto(str_temp);
        return str_temp;
    }
    public int[] getMatchColumnIndexes() throws SQLException {
        Integer []int_temp = new Integer[iMatchColumns.size()];
        int [] i_temp = new int[iMatchColumns.size()];
        int i_val;
        i_val = ((Integer)iMatchColumns.get(0)).intValue();
        if( i_val == -1 ) {
           throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.setmatchcols").toString());
        }
        iMatchColumns.copyInto(int_temp);
        for(int i = 0; i < int_temp.length; i++) {
           i_temp[i] = (int_temp[i]).intValue();
        }
        return i_temp;
    }
    public void setMatchColumn(int[] columnIdxes) throws SQLException {
        for(int j = 0 ; j < columnIdxes.length; j++) {
           if( columnIdxes[j] < 0 ) {
              throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols1").toString());
           }
        }
        for(int i = 0 ;i < columnIdxes.length; i++) {
           iMatchColumns.add(i,Integer.valueOf(columnIdxes[i]));
        }
    }
    public void setMatchColumn(String[] columnNames) throws SQLException {
        for(int j = 0; j < columnNames.length; j++) {
           if( columnNames[j] == null || columnNames[j].equals("")) {
              throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols2").toString());
           }
        }
        for( int i = 0; i < columnNames.length; i++) {
           strMatchColumns.add(i,columnNames[i]);
        }
    }
    public void setMatchColumn(int columnIdx) throws SQLException {
        if(columnIdx < 0) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols1").toString());
        } else {
            iMatchColumns.set(0, Integer.valueOf(columnIdx));
        }
    }
    public void setMatchColumn(String columnName) throws SQLException {
        if(columnName == null || (columnName= columnName.trim()).equals("")) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.matchcols2").toString());
        } else {
            strMatchColumns.set(0, columnName);
        }
    }
    public void unsetMatchColumn(int columnIdx) throws SQLException {
        if(! iMatchColumns.get(0).equals(Integer.valueOf(columnIdx) )  ) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.unsetmatch").toString());
        } else if(strMatchColumns.get(0) != null) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.usecolname").toString());
        } else {
               iMatchColumns.set(0, Integer.valueOf(-1));
        }
    }
    public void unsetMatchColumn(String columnName) throws SQLException {
        columnName = columnName.trim();
        if(!((strMatchColumns.get(0)).equals(columnName))) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.unsetmatch").toString());
        } else if( ((Integer)(iMatchColumns.get(0))).intValue() > 0) {
            throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.usecolid").toString());
        } else {
            strMatchColumns.set(0, null);   
        }
    }
    public DatabaseMetaData getDatabaseMetaData() throws SQLException {
        Connection con = connect();
        return con.getMetaData();
    }
    public ParameterMetaData getParameterMetaData() throws SQLException {
        prepare();
        return (ps.getParameterMetaData());
    }
    public void commit() throws SQLException {
      conn.commit();
      if( conn.getHoldability() != HOLD_CURSORS_OVER_COMMIT) {
         ResultSet oldVal = rs;
         rs = null;
      }
    }
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if(conn != null) {
           conn.setAutoCommit(autoCommit);
        } else {
           conn = connect();
           conn.setAutoCommit(autoCommit);
        }
    }
    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }
    public void rollback() throws SQLException {
        conn.rollback();
        ResultSet oldVal = rs;
        rs = null;
    }
    public void rollback(Savepoint s) throws SQLException {
        conn.rollback(s);
    }
    protected void setParams() throws SQLException {
        if(rs == null) {
           setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
           setConcurrency(ResultSet.CONCUR_UPDATABLE);
        }
        else {
            setType(rs.getType());
            setConcurrency(rs.getConcurrency());
        }
    }
    private void checkTypeConcurrency() throws SQLException {
        if(rs.getType() == TYPE_FORWARD_ONLY ||
           rs.getConcurrency() == CONCUR_READ_ONLY) {
              throw new SQLException(resBundle.handleGetObject("jdbcrowsetimpl.resnotupd").toString());
         }
    }
    protected Connection getConnection() {
       return conn;
    }
    protected void setConnection(Connection connection) {
       conn = connection;
    }
    protected PreparedStatement getPreparedStatement() {
       return ps;
    }
    protected void setPreparedStatement(PreparedStatement preparedStatement) {
       ps = preparedStatement;
    }
    protected ResultSet getResultSet() throws SQLException {
       checkState();
       return rs;
    }
    protected void setResultSet(ResultSet resultSet) {
       rs = resultSet;
    }
    public void setCommand(String command) throws SQLException {
       String oldVal;
       if (getCommand() != null) {
          if(!getCommand().equals(command)) {
             oldVal = getCommand();
             super.setCommand(command);
             ps = null;
             rs = null;
             propertyChangeSupport.firePropertyChange("command", oldVal,command);
          }
       }
       else {
          super.setCommand(command);
          propertyChangeSupport.firePropertyChange("command", null,command);
       }
    }
    public void setDataSourceName(String dsName) throws SQLException{
       String oldVal;
       if(getDataSourceName() != null) {
          if(!getDataSourceName().equals(dsName)) {
             oldVal = getDataSourceName();
             super.setDataSourceName(dsName);
             conn = null;
             ps = null;
             rs = null;
             propertyChangeSupport.firePropertyChange("dataSourceName",oldVal,dsName);
          }
       }
       else {
          super.setDataSourceName(dsName);
          propertyChangeSupport.firePropertyChange("dataSourceName",null,dsName);
       }
    }
    public void setUrl(String url) throws SQLException {
       String oldVal;
       if(getUrl() != null) {
          if(!getUrl().equals(url)) {
             oldVal = getUrl();
             super.setUrl(url);
             conn = null;
             ps = null;
             rs = null;
             propertyChangeSupport.firePropertyChange("url", oldVal, url);
          }
       }
       else {
          super.setUrl(url);
          propertyChangeSupport.firePropertyChange("url", null, url);
       }
    }
    public void setUsername(String uname) {
       String oldVal;
       if( getUsername() != null) {
          if(!getUsername().equals(uname)) {
             oldVal = getUsername();
             super.setUsername(uname);
             conn = null;
             ps = null;
             rs = null;
             propertyChangeSupport.firePropertyChange("username",oldVal,uname);
          }
       }
       else{
          super.setUsername(uname);
          propertyChangeSupport.firePropertyChange("username",null,uname);
       }
    }
    public void setPassword(String password) {
       String oldVal;
       if ( getPassword() != null) {
          if(!getPassword().equals(password)) {
             oldVal = getPassword();
             super.setPassword(password);
             conn = null;
             ps = null;
             rs = null;
             propertyChangeSupport.firePropertyChange("password",oldVal,password);
          }
       }
       else{
          super.setPassword(password);
          propertyChangeSupport.firePropertyChange("password",null,password);
       }
    }
    public void setType(int type) throws SQLException {
       int oldVal;
       try {
          oldVal = getType();
        }catch(SQLException ex) {
           oldVal = 0;
        }
       if(oldVal != type) {
           super.setType(type);
           propertyChangeSupport.firePropertyChange("type",oldVal,type);
       }
    }
    public void setConcurrency(int concur) throws SQLException {
       int oldVal;
       try {
          oldVal = getConcurrency();
        }catch(NullPointerException ex) {
           oldVal = 0;
        }
       if(oldVal != concur) {
           super.setConcurrency(concur);
           propertyChangeSupport.firePropertyChange("concurrency",oldVal,concur);
       }
    }
    public void setTransactionIsolation(int transIso) throws SQLException {
       int oldVal;
       try {
          oldVal = getTransactionIsolation();
        }catch(NullPointerException ex) {
           oldVal = 0;
        }
       if(oldVal != transIso) {
           super.setTransactionIsolation(transIso);
           propertyChangeSupport.firePropertyChange("transactionIsolation",oldVal,transIso);
       }
    }
    public void setMaxRows(int mRows) throws SQLException {
       int oldVal;
       try {
          oldVal = getMaxRows();
        }catch(NullPointerException ex) {
           oldVal = 0;
        }
       if(oldVal != mRows) {
           super.setMaxRows(mRows);
           propertyChangeSupport.firePropertyChange("maxRows",oldVal,mRows);
       }
    }
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public SQLXML getSQLXML(String colName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public RowId getRowId(String columnName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateRowId(String columnName, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public int getHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public boolean isClosed() throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNString(String columnName, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(String columnName, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public NClob getNClob(int i) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public NClob getNClob(String colName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException{
        return null;
    }
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        return false;
    }
     public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
   public void setRowId(String parameterName, RowId x) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
     public void setNString(int parameterIndex, String value) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void setNClob(String parameterName, NClob value) throws SQLException {
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException {
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public java.io.Reader getNCharacterStream(String columnName) throws SQLException {
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
     }
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateSQLXML(String columnName, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public String getNString(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public String getNString(String columnName) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
       public void updateNCharacterStream(int columnIndex,
                            java.io.Reader x,
                            long length)
                            throws SQLException {
          throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
       }
       public void updateNCharacterStream(String columnName,
                            java.io.Reader x,
                            long length)
                            throws SQLException {
          throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
       }
    public void updateNCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(int columnIndex,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(String columnLabel,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(int columnIndex,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateClob(String columnLabel,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(int columnIndex,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(String columnLabel,  Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(int columnIndex,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateNClob(String columnLabel,  Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(int columnIndex,
                           java.io.InputStream x,
                           long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBinaryStream(int columnIndex,
                            java.io.InputStream x,
                            long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(int columnIndex,
                             java.io.Reader x,
                             long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(String columnLabel,
                           java.io.InputStream x,
                           long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(int columnIndex,
                           java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateAsciiStream(String columnLabel,
                           java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBinaryStream(String columnLabel,
                            java.io.InputStream x,
                            long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBinaryStream(int columnIndex,
                            java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateBinaryStream(String columnLabel,
                            java.io.InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(String columnLabel,
                             java.io.Reader reader,
                             long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(int columnIndex,
                             java.io.Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
    public void updateCharacterStream(String columnLabel,
                             java.io.Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
  public void setURL(int parameterIndex, java.net.URL x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
  public void setNClob(int parameterIndex, Reader reader)
    throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
            public void setNClob(String parameterName, Reader reader, long length)
    throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
  public void setNClob(String parameterName, Reader reader)
    throws SQLException{
             throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
     public void setNClob(int parameterIndex, Reader reader, long length)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
     public void setNClob(int parameterIndex, NClob value) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
 public void setNString(String parameterName, String value)
         throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
 public void setNCharacterStream(String parameterName, Reader value, long length)
         throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
  public void setNCharacterStream(String parameterName, Reader value) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
      public  void setClob(String parameterName, Reader reader, long length)
      throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setClob (String parameterName, Clob x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setClob(String parameterName, Reader reader)
      throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setDate(String parameterName, java.sql.Date x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setDate(String parameterName, java.sql.Date x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setTime(String parameterName, java.sql.Time x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setTime(String parameterName, java.sql.Time x, Calendar cal)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setClob(int parameterIndex, Reader reader)
     throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setClob(int parameterIndex, Reader reader, long length)
     throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(int parameterIndex, InputStream inputStream)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
      public void setBlob(String parameterName, InputStream inputStream, long length)
         throws SQLException{
         throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
    }
   public void setBlob (String parameterName, Blob x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    public void setBlob(String parameterName, InputStream inputStream)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
  public void setObject(String parameterName, Object x, int targetSqlType, int scale)
     throws SQLException{
      throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
 }
    public void setObject(String parameterName, Object x, int targetSqlType)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setObject(String parameterName, Object x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
 public void setAsciiStream(String parameterName, java.io.InputStream x, int length)
     throws SQLException{
      throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
 }
 public void setBinaryStream(String parameterName, java.io.InputStream x,
                      int length) throws SQLException{
      throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
 }
  public void setCharacterStream(String parameterName,
                          java.io.Reader reader,
                          int length) throws SQLException{
       throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
  }
  public void setAsciiStream(String parameterName, java.io.InputStream x)
          throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setBinaryStream(String parameterName, java.io.InputStream x)
   throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setCharacterStream(String parameterName,
                         java.io.Reader reader) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setString(String parameterName, String x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setBytes(String parameterName, byte x[]) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setTimestamp(String parameterName, java.sql.Timestamp x)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setNull(String parameterName, int sqlType) throws SQLException {
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setNull (String parameterName, int sqlType, String typeName)
       throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setBoolean(String parameterName, boolean x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setByte(String parameterName, byte x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setShort(String parameterName, short x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setInt(String parameterName, int x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setLong(String parameterName, long x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setFloat(String parameterName, float x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
   public void setDouble(String parameterName, double x) throws SQLException{
        throw new SQLFeatureNotSupportedException(resBundle.handleGetObject("jdbcrowsetimpl.featnotsupp").toString());
   }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {}
    }
   static final long serialVersionUID = -3591946023893483003L;
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }
}
