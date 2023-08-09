public class CachedRowSetReader implements RowSetReader, Serializable {
    private int writerCalls = 0;
    private boolean userCon = false;
    private int startPosition;
    private JdbcRowSetResourceBundle resBundle;
    public CachedRowSetReader() {
        try {
                resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public void readData(RowSetInternal caller) throws SQLException
    {
        Connection con = null;
        try {
            CachedRowSet crs = (CachedRowSet)caller;
            if(crs.getPageSize() == 0 && crs.size() >0 ) {
               crs.close();
            }
            writerCalls = 0;
            userCon = false;
            con = this.connect(caller);
            if (con == null || crs.getCommand() == null)
                throw new SQLException(resBundle.handleGetObject("crsreader.connecterr").toString());
            try {
                con.setTransactionIsolation(crs.getTransactionIsolation());
            } catch (Exception ex) {
                ;
            }
            PreparedStatement pstmt = con.prepareStatement(crs.getCommand());
            decodeParams(caller.getParams(), pstmt);
            try {
                pstmt.setMaxRows(crs.getMaxRows());
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) {
                throw new SQLException(ex.getMessage());
            }
            if(crs.getCommand().toLowerCase().indexOf("select") != -1) {
                ResultSet rs = pstmt.executeQuery();
               if(crs.getPageSize() == 0){
                      crs.populate(rs);
               }
               else {
                       pstmt = con.prepareStatement(crs.getCommand(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
                       decodeParams(caller.getParams(), pstmt);
                       try {
                               pstmt.setMaxRows(crs.getMaxRows());
                               pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                               pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                               pstmt.setQueryTimeout(crs.getQueryTimeout());
                           } catch (Exception ex) {
                            throw new SQLException(ex.getMessage());
                          }
                       rs = pstmt.executeQuery();
                       crs.populate(rs,startPosition);
               }
                rs.close();
            } else  {
                pstmt.executeUpdate();
            }
            pstmt.close();
            try {
                con.commit();
            } catch (SQLException ex) {
                ;
            }
            if (getCloseConnection() == true)
                con.close();
        }
        catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (con != null && getCloseConnection() == true) {
                    try {
                        if (!con.getAutoCommit()) {
                            con.rollback();
                        }
                    } catch (Exception dummy) {
                    }
                    con.close();
                    con = null;
                }
            } catch (SQLException e) {
            }
        }
    }
    public boolean reset() throws SQLException {
        writerCalls++;
        return writerCalls == 1;
    }
    public Connection connect(RowSetInternal caller) throws SQLException {
        if (caller.getConnection() != null) {
            userCon = true;
            return caller.getConnection();
        }
        else if (((RowSet)caller).getDataSourceName() != null) {
            try {
                Context ctx = new InitialContext();
                DataSource ds = (DataSource)ctx.lookup
                    (((RowSet)caller).getDataSourceName());
                if(((RowSet)caller).getUsername() != null) {
                     return ds.getConnection(((RowSet)caller).getUsername(),
                                            ((RowSet)caller).getPassword());
                } else {
                     return ds.getConnection();
                }
            }
            catch (javax.naming.NamingException ex) {
                SQLException sqlEx = new SQLException(resBundle.handleGetObject("crsreader.connect").toString());
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        } else if (((RowSet)caller).getUrl() != null) {
            return DriverManager.getConnection(((RowSet)caller).getUrl(),
                                               ((RowSet)caller).getUsername(),
                                               ((RowSet)caller).getPassword());
        }
        else {
            return null;
        }
    }
    private void decodeParams(Object[] params,
                              PreparedStatement pstmt) throws SQLException {
        int arraySize;
        Object[] param = null;
        for (int i=0; i < params.length; i++) {
            if (params[i] instanceof Object[]) {
                param = (Object[])params[i];
                if (param.length == 2) {
                    if (param[0] == null) {
                        pstmt.setNull(i + 1, ((Integer)param[1]).intValue());
                        continue;
                    }
                    if (param[0] instanceof java.sql.Date ||
                        param[0] instanceof java.sql.Time ||
                        param[0] instanceof java.sql.Timestamp) {
                        System.err.println(resBundle.handleGetObject("crsreader.datedetected").toString());
                        if (param[1] instanceof java.util.Calendar) {
                            System.err.println(resBundle.handleGetObject("crsreader.caldetected").toString());
                            pstmt.setDate(i + 1, (java.sql.Date)param[0],
                                       (java.util.Calendar)param[1]);
                            continue;
                        }
                        else {
                            throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
                        }
                    }
                    if (param[0] instanceof Reader) {
                        pstmt.setCharacterStream(i + 1, (Reader)param[0],
                                              ((Integer)param[1]).intValue());
                        continue;
                    }
                    if (param[1] instanceof Integer) {
                        pstmt.setObject(i + 1, param[0], ((Integer)param[1]).intValue());
                        continue;
                    }
                } else if (param.length == 3) {
                    if (param[0] == null) {
                        pstmt.setNull(i + 1, ((Integer)param[1]).intValue(),
                                   (String)param[2]);
                        continue;
                    }
                    if (param[0] instanceof java.io.InputStream) {
                        switch (((Integer)param[2]).intValue()) {
                        case CachedRowSetImpl.UNICODE_STREAM_PARAM:
                            pstmt.setUnicodeStream(i + 1,
                                                (java.io.InputStream)param[0],
                                                ((Integer)param[1]).intValue());
                        case CachedRowSetImpl.BINARY_STREAM_PARAM:
                            pstmt.setBinaryStream(i + 1,
                                               (java.io.InputStream)param[0],
                                               ((Integer)param[1]).intValue());
                        case CachedRowSetImpl.ASCII_STREAM_PARAM:
                            pstmt.setAsciiStream(i + 1,
                                              (java.io.InputStream)param[0],
                                              ((Integer)param[1]).intValue());
                        default:
                            throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
                        }
                    }
                    if (param[1] instanceof Integer && param[2] instanceof Integer) {
                        pstmt.setObject(i + 1, param[0], ((Integer)param[1]).intValue(),
                                     ((Integer)param[2]).intValue());
                        continue;
                    }
                    throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
                } else {
                    pstmt.setObject(i + 1, params[i]);
                    continue;
                }
            }  else {
               pstmt.setObject(i + 1, params[i]);
            }
        }
    }
    protected boolean getCloseConnection() {
        if (userCon == true)
            return false;
        return true;
    }
    public void setStartPosition(int pos){
        startPosition = pos;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID =5049738185801363801L;
}
