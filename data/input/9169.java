public class JoinRowSetImpl extends WebRowSetImpl implements JoinRowSet {
    private Vector<CachedRowSetImpl> vecRowSetsInJOIN;
    private CachedRowSetImpl crsInternal;
    private Vector<Integer> vecJoinType;
    private Vector<String> vecTableNames;
    private int iMatchKey;
    private String strMatchKey ;
    boolean[] supportedJOINs;
    private WebRowSet wrs;
    public JoinRowSetImpl() throws SQLException {
        vecRowSetsInJOIN = new Vector<CachedRowSetImpl>();
        crsInternal = new CachedRowSetImpl();
        vecJoinType = new Vector<Integer>();
        vecTableNames = new Vector<String>();
        iMatchKey = -1;
        strMatchKey = null;
        supportedJOINs =
              new boolean[] {false, true, false, false, false};
       try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public void addRowSet(Joinable rowset) throws SQLException {
        boolean boolColId, boolColName;
        boolColId = false;
        boolColName = false;
        CachedRowSetImpl cRowset;
        if(!(rowset instanceof RowSet)) {
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notinstance").toString());
        }
        if(rowset instanceof JdbcRowSetImpl ) {
            cRowset = new CachedRowSetImpl();
            cRowset.populate((RowSet)rowset);
            if(cRowset.size() == 0){
                throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.emptyrowset").toString());
            }
            try {
                int matchColumnCount = 0;
                for(int i=0; i< rowset.getMatchColumnIndexes().length; i++) {
                    if(rowset.getMatchColumnIndexes()[i] != -1)
                        ++ matchColumnCount;
                    else
                        break;
                }
                int[] pCol = new int[matchColumnCount];
                for(int i=0; i<matchColumnCount; i++)
                   pCol[i] = rowset.getMatchColumnIndexes()[i];
                cRowset.setMatchColumn(pCol);
            } catch(SQLException sqle) {
            }
        } else {
             cRowset = (CachedRowSetImpl)rowset;
             if(cRowset.size() == 0){
                 throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.emptyrowset").toString());
             }
        }
        try {
             iMatchKey = (cRowset.getMatchColumnIndexes())[0];
        } catch(SQLException sqle) {
             boolColId = true;
        }
        try {
             strMatchKey = (cRowset.getMatchColumnNames())[0];
        } catch(SQLException sqle) {
           boolColName = true;
        }
        if(boolColId && boolColName) {
           throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.matchnotset").toString());
        } else {
           if(boolColId){
              ArrayList<Integer> indices = new ArrayList<>();
              for(int i=0;i<cRowset.getMatchColumnNames().length;i++) {
                  if( (strMatchKey = (cRowset.getMatchColumnNames())[i]) != null) {
                      iMatchKey = cRowset.findColumn(strMatchKey);
                      indices.add(iMatchKey);
                  }
                  else
                      break;
              }
              int[] indexes = new int[indices.size()];
              for(int i=0; i<indices.size();i++)
                  indexes[i] = ((Integer)indices.get(i)).intValue();
              cRowset.setMatchColumn(indexes);
           } else {
           }
        }
        initJOIN(cRowset);
    }
    public void addRowSet(RowSet rowset, int columnIdx) throws SQLException {
        ((CachedRowSetImpl)rowset).setMatchColumn(columnIdx);
        addRowSet((Joinable)rowset);
    }
    public void addRowSet(RowSet rowset, String columnName) throws SQLException {
        ((CachedRowSetImpl)rowset).setMatchColumn(columnName);
        addRowSet((Joinable)rowset);
    }
    public void addRowSet(RowSet[] rowset,
                          int[] columnIdx) throws SQLException {
     if(rowset.length != columnIdx.length) {
        throw new SQLException
             (resBundle.handleGetObject("joinrowsetimpl.numnotequal").toString());
     } else {
        for(int i=0; i< rowset.length; i++) {
           ((CachedRowSetImpl)rowset[i]).setMatchColumn(columnIdx[i]);
           addRowSet((Joinable)rowset[i]);
        } 
     } 
   }
    public void addRowSet(RowSet[] rowset,
                          String[] columnName) throws SQLException {
     if(rowset.length != columnName.length) {
        throw new SQLException
                 (resBundle.handleGetObject("joinrowsetimpl.numnotequal").toString());
     } else {
        for(int i=0; i< rowset.length; i++) {
           ((CachedRowSetImpl)rowset[i]).setMatchColumn(columnName[i]);
           addRowSet((Joinable)rowset[i]);
        } 
     } 
    }
    public Collection getRowSets() throws SQLException {
        return vecRowSetsInJOIN;
    }
    public String[] getRowSetNames() throws SQLException {
        Object [] arr = vecTableNames.toArray();
        String []strArr = new String[arr.length];
        for( int i = 0;i < arr.length; i++) {
           strArr[i] = arr[i].toString();
        }
        return strArr;
    }
    public CachedRowSet toCachedRowSet() throws SQLException {
        return crsInternal;
    }
    public boolean supportsCrossJoin() {
        return supportedJOINs[JoinRowSet.CROSS_JOIN];
    }
    public boolean supportsInnerJoin() {
        return supportedJOINs[JoinRowSet.INNER_JOIN];
    }
    public boolean supportsLeftOuterJoin() {
        return supportedJOINs[JoinRowSet.LEFT_OUTER_JOIN];
    }
    public boolean supportsRightOuterJoin() {
        return supportedJOINs[JoinRowSet.RIGHT_OUTER_JOIN];
    }
    public boolean supportsFullJoin() {
        return supportedJOINs[JoinRowSet.FULL_JOIN];
    }
    public void setJoinType(int type) throws SQLException {
       if (type >= JoinRowSet.CROSS_JOIN && type <= JoinRowSet.FULL_JOIN) {
           if (type != JoinRowSet.INNER_JOIN) {
               throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notsupported").toString());
           } else {
              Integer Intgr = Integer.valueOf(JoinRowSet.INNER_JOIN);
              vecJoinType.add(Intgr);
           }
       } else {
          throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.notdefined").toString());
       }  
    }
    private boolean checkforMatchColumn(Joinable rs) throws SQLException {
        int[] i = rs.getMatchColumnIndexes();
        if (i.length <= 0) {
            return false;
        }
        return true;
    }
    private void initJOIN(CachedRowSet rowset) throws SQLException {
        try {
            CachedRowSetImpl cRowset = (CachedRowSetImpl)rowset;
            CachedRowSetImpl crsTemp = new CachedRowSetImpl();
            RowSetMetaDataImpl rsmd = new RowSetMetaDataImpl();
            if (vecRowSetsInJOIN.isEmpty() ) {
                crsInternal = (CachedRowSetImpl)rowset.createCopy();
                crsInternal.setMetaData((RowSetMetaDataImpl)cRowset.getMetaData());
                vecRowSetsInJOIN.add(cRowset);
            } else {
                if( (vecRowSetsInJOIN.size() - vecJoinType.size() ) == 2 ) {
                        setJoinType(JoinRowSet.INNER_JOIN);
                } else if( (vecRowSetsInJOIN.size() - vecJoinType.size() ) == 1  ) {
                }
                vecTableNames.add(crsInternal.getTableName());
                vecTableNames.add(cRowset.getTableName());
                int rowCount2 = cRowset.size();
                int rowCount1 = crsInternal.size();
                int matchColumnCount = 0;
                for(int i=0; i< crsInternal.getMatchColumnIndexes().length; i++) {
                    if(crsInternal.getMatchColumnIndexes()[i] != -1)
                        ++ matchColumnCount;
                    else
                        break;
                }
                rsmd.setColumnCount
                    (crsInternal.getMetaData().getColumnCount() +
                     cRowset.getMetaData().getColumnCount() - matchColumnCount);
                crsTemp.setMetaData(rsmd);
                crsInternal.beforeFirst();
                cRowset.beforeFirst();
                for (int i = 1 ; i <= rowCount1 ; i++) {
                  if(crsInternal.isAfterLast() ) {
                    break;
                  }
                  if(crsInternal.next()) {
                    cRowset.beforeFirst();
                    for(int j = 1 ; j <= rowCount2 ; j++) {
                         if( cRowset.isAfterLast()) {
                            break;
                         }
                         if(cRowset.next()) {
                             boolean match = true;
                             for(int k=0; k<matchColumnCount; k++) {
                                 if (!crsInternal.getObject( crsInternal.getMatchColumnIndexes()[k]).equals
                                         (cRowset.getObject(cRowset.getMatchColumnIndexes()[k]))) {
                                     match = false;
                                     break;
                                 }
                             }
                             if (match) {
                                int p;
                                int colc = 0;   
                                crsTemp.moveToInsertRow();
                            for( p=1;
                                p<=crsInternal.getMetaData().getColumnCount();p++) {
                                match = false;
                                for(int k=0; k<matchColumnCount; k++) {
                                 if (p == crsInternal.getMatchColumnIndexes()[k] ) {
                                     match = true;
                                     break;
                                 }
                                }
                                    if ( !match ) {
                                    crsTemp.updateObject(++colc, crsInternal.getObject(p));
                                    rsmd.setColumnName
                                        (colc, crsInternal.getMetaData().getColumnName(p));
                                    rsmd.setTableName(colc, crsInternal.getTableName());
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setAutoIncrement(p, crsInternal.getMetaData().isAutoIncrement(p));
                                    rsmd.setCaseSensitive(p, crsInternal.getMetaData().isCaseSensitive(p));
                                    rsmd.setCatalogName(p, crsInternal.getMetaData().getCatalogName(p));
                                    rsmd.setColumnDisplaySize(p, crsInternal.getMetaData().getColumnDisplaySize(p));
                                    rsmd.setColumnLabel(p, crsInternal.getMetaData().getColumnLabel(p));
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setColumnTypeName(p, crsInternal.getMetaData().getColumnTypeName(p));
                                    rsmd.setCurrency(p,crsInternal.getMetaData().isCurrency(p) );
                                    rsmd.setNullable(p, crsInternal.getMetaData().isNullable(p));
                                    rsmd.setPrecision(p, crsInternal.getMetaData().getPrecision(p));
                                    rsmd.setScale(p, crsInternal.getMetaData().getScale(p));
                                    rsmd.setSchemaName(p, crsInternal.getMetaData().getSchemaName(p));
                                    rsmd.setSearchable(p, crsInternal.getMetaData().isSearchable(p));
                                    rsmd.setSigned(p, crsInternal.getMetaData().isSigned(p));
                                } else {
                                    crsTemp.updateObject(++colc, crsInternal.getObject(p));
                                    rsmd.setColumnName(colc, crsInternal.getMetaData().getColumnName(p));
                                    rsmd.setTableName
                                        (colc, crsInternal.getTableName()+
                                         "#"+
                                         cRowset.getTableName());
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setAutoIncrement(p, crsInternal.getMetaData().isAutoIncrement(p));
                                    rsmd.setCaseSensitive(p, crsInternal.getMetaData().isCaseSensitive(p));
                                    rsmd.setCatalogName(p, crsInternal.getMetaData().getCatalogName(p));
                                    rsmd.setColumnDisplaySize(p, crsInternal.getMetaData().getColumnDisplaySize(p));
                                    rsmd.setColumnLabel(p, crsInternal.getMetaData().getColumnLabel(p));
                                    rsmd.setColumnType(p, crsInternal.getMetaData().getColumnType(p));
                                    rsmd.setColumnTypeName(p, crsInternal.getMetaData().getColumnTypeName(p));
                                    rsmd.setCurrency(p,crsInternal.getMetaData().isCurrency(p) );
                                    rsmd.setNullable(p, crsInternal.getMetaData().isNullable(p));
                                    rsmd.setPrecision(p, crsInternal.getMetaData().getPrecision(p));
                                    rsmd.setScale(p, crsInternal.getMetaData().getScale(p));
                                    rsmd.setSchemaName(p, crsInternal.getMetaData().getSchemaName(p));
                                    rsmd.setSearchable(p, crsInternal.getMetaData().isSearchable(p));
                                    rsmd.setSigned(p, crsInternal.getMetaData().isSigned(p));
                                } 
                            } 
                            for(int q=1;
                                q<= cRowset.getMetaData().getColumnCount();q++) {
                                match = false;
                                for(int k=0; k<matchColumnCount; k++) {
                                 if (q == cRowset.getMatchColumnIndexes()[k] ) {
                                     match = true;
                                     break;
                                 }
                                }
                                    if ( !match ) {
                                    crsTemp.updateObject(++colc, cRowset.getObject(q));
                                    rsmd.setColumnName
                                        (colc, cRowset.getMetaData().getColumnName(q));
                                    rsmd.setTableName(colc, cRowset.getTableName());
                                    rsmd.setColumnType(p+q-1, cRowset.getMetaData().getColumnType(q));
                                    rsmd.setAutoIncrement(p+q-1, cRowset.getMetaData().isAutoIncrement(q));
                                    rsmd.setCaseSensitive(p+q-1, cRowset.getMetaData().isCaseSensitive(q));
                                    rsmd.setCatalogName(p+q-1, cRowset.getMetaData().getCatalogName(q));
                                    rsmd.setColumnDisplaySize(p+q-1, cRowset.getMetaData().getColumnDisplaySize(q));
                                    rsmd.setColumnLabel(p+q-1, cRowset.getMetaData().getColumnLabel(q));
                                    rsmd.setColumnType(p+q-1, cRowset.getMetaData().getColumnType(q));
                                    rsmd.setColumnTypeName(p+q-1, cRowset.getMetaData().getColumnTypeName(q));
                                    rsmd.setCurrency(p+q-1,cRowset.getMetaData().isCurrency(q) );
                                    rsmd.setNullable(p+q-1, cRowset.getMetaData().isNullable(q));
                                    rsmd.setPrecision(p+q-1, cRowset.getMetaData().getPrecision(q));
                                    rsmd.setScale(p+q-1, cRowset.getMetaData().getScale(q));
                                    rsmd.setSchemaName(p+q-1, cRowset.getMetaData().getSchemaName(q));
                                    rsmd.setSearchable(p+q-1, cRowset.getMetaData().isSearchable(q));
                                    rsmd.setSigned(p+q-1, cRowset.getMetaData().isSigned(q));
                                }
                                else {
                                    --p;
                                }
                            }
                            crsTemp.insertRow();
                            crsTemp.moveToCurrentRow();
                        } else {
                        } 
                         }
                    } 
                   }
                } 
                crsTemp.setMetaData(rsmd);
                crsTemp.setOriginal();
                int[] pCol = new int[matchColumnCount];
                for(int i=0; i<matchColumnCount; i++)
                   pCol[i] = crsInternal.getMatchColumnIndexes()[i];
                crsInternal = (CachedRowSetImpl)crsTemp.createCopy();
                crsInternal.setMatchColumn(pCol);
                crsInternal.setMetaData(rsmd);
                vecRowSetsInJOIN.add(cRowset);
            } 
        } catch(SQLException sqle) {
            sqle.printStackTrace();
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.initerror").toString() + sqle);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(resBundle.handleGetObject("joinrowsetimpl.genericerr").toString() + e);
        }
    }
    public String getWhereClause() throws SQLException {
       String strWhereClause = "Select ";
       String whereClause;
       String tabName= "";
       String strTabName = "";
       int sz,cols;
       int j;
       CachedRowSetImpl crs;
       sz = vecRowSetsInJOIN.size();
       for(int i=0;i<sz; i++) {
          crs = (CachedRowSetImpl)vecRowSetsInJOIN.get(i);
          cols = crs.getMetaData().getColumnCount();
          tabName = tabName.concat(crs.getTableName());
          strTabName = strTabName.concat(tabName+", ");
          j = 1;
          while(j<cols) {
            strWhereClause = strWhereClause.concat
                (tabName+"."+crs.getMetaData().getColumnName(j++));
            strWhereClause = strWhereClause.concat(", ");
          } 
        } 
        strWhereClause = strWhereClause.substring
             (0, strWhereClause.lastIndexOf(","));
        strWhereClause = strWhereClause.concat(" from ");
        strWhereClause = strWhereClause.concat(strTabName);
        strWhereClause = strWhereClause.substring
             (0, strWhereClause.lastIndexOf(","));
        strWhereClause = strWhereClause.concat(" where ");
         for(int i=0;i<sz; i++) {
             strWhereClause = strWhereClause.concat(
               ((CachedRowSetImpl)vecRowSetsInJOIN.get(i)).getMatchColumnNames()[0]);
             if(i%2!=0) {
               strWhereClause = strWhereClause.concat("=");
             }  else {
               strWhereClause = strWhereClause.concat(" and");
             }
          strWhereClause = strWhereClause.concat(" ");
         }
        return strWhereClause;
    }
    public boolean next() throws SQLException {
        return crsInternal.next();
    }
    public void close() throws SQLException {
        crsInternal.close();
    }
    public boolean wasNull() throws SQLException {
        return crsInternal.wasNull();
    }
    public String getString(int columnIndex) throws SQLException {
        return crsInternal.getString(columnIndex);
    }
    public boolean getBoolean(int columnIndex) throws SQLException {
        return crsInternal.getBoolean(columnIndex);
    }
    public byte getByte(int columnIndex) throws SQLException {
        return crsInternal.getByte(columnIndex);
    }
    public short getShort(int columnIndex) throws SQLException {
        return crsInternal.getShort(columnIndex);
    }
    public int getInt(int columnIndex) throws SQLException {
        return crsInternal.getInt(columnIndex);
    }
    public long getLong(int columnIndex) throws SQLException {
        return crsInternal.getLong(columnIndex);
    }
    public float getFloat(int columnIndex) throws SQLException {
        return crsInternal.getFloat(columnIndex);
    }
    public double getDouble(int columnIndex) throws SQLException {
        return crsInternal.getDouble(columnIndex);
    }
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return crsInternal.getBigDecimal(columnIndex);
    }
    public byte[] getBytes(int columnIndex) throws SQLException {
        return crsInternal.getBytes(columnIndex);
    }
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        return crsInternal.getDate(columnIndex);
    }
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return crsInternal.getTime(columnIndex);
    }
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        return crsInternal.getTimestamp(columnIndex);
    }
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        return crsInternal.getAsciiStream(columnIndex);
    }
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return crsInternal.getUnicodeStream(columnIndex);
    }
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        return crsInternal.getBinaryStream(columnIndex);
    }
    public String getString(String columnName) throws SQLException {
        return crsInternal.getString(columnName);
    }
    public boolean getBoolean(String columnName) throws SQLException {
        return crsInternal.getBoolean(columnName);
    }
    public byte getByte(String columnName) throws SQLException {
        return crsInternal.getByte(columnName);
    }
    public short getShort(String columnName) throws SQLException {
        return crsInternal.getShort(columnName);
    }
    public int getInt(String columnName) throws SQLException {
        return crsInternal.getInt(columnName);
    }
    public long getLong(String columnName) throws SQLException {
        return crsInternal.getLong(columnName);
    }
    public float getFloat(String columnName) throws SQLException {
        return crsInternal.getFloat(columnName);
    }
    public double getDouble(String columnName) throws SQLException {
        return crsInternal.getDouble(columnName);
    }
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return crsInternal.getBigDecimal(columnName);
    }
    public byte[] getBytes(String columnName) throws SQLException {
        return crsInternal.getBytes(columnName);
    }
    public java.sql.Date getDate(String columnName) throws SQLException {
        return crsInternal.getDate(columnName);
    }
    public java.sql.Time getTime(String columnName) throws SQLException {
        return crsInternal.getTime(columnName);
    }
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        return crsInternal.getTimestamp(columnName);
    }
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        return crsInternal.getAsciiStream(columnName);
    }
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        return crsInternal.getUnicodeStream(columnName);
    }
    public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
        return crsInternal.getBinaryStream(columnName);
    }
    public SQLWarning getWarnings() {
        return crsInternal.getWarnings();
    }
     public void clearWarnings() {
        crsInternal.clearWarnings();
    }
    public String getCursorName() throws SQLException {
        return crsInternal.getCursorName();
    }
    public ResultSetMetaData getMetaData() throws SQLException {
        return crsInternal.getMetaData();
    }
    public Object getObject(int columnIndex) throws SQLException {
        return crsInternal.getObject(columnIndex);
    }
    public Object getObject(int columnIndex,
                            java.util.Map<String,Class<?>> map)
    throws SQLException {
        return crsInternal.getObject(columnIndex, map);
    }
    public Object getObject(String columnName) throws SQLException {
        return crsInternal.getObject(columnName);
    }
    public Object getObject(String columnName,
                            java.util.Map<String,Class<?>> map)
        throws SQLException {
        return crsInternal.getObject(columnName, map);
    }
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
        return crsInternal.getCharacterStream(columnIndex);
    }
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        return crsInternal.getCharacterStream(columnName);
    }
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
       return crsInternal.getBigDecimal(columnIndex);
    }
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
       return crsInternal.getBigDecimal(columnName);
    }
    public int size() {
        return crsInternal.size();
    }
    public boolean isBeforeFirst() throws SQLException {
        return crsInternal.isBeforeFirst();
    }
    public boolean isAfterLast() throws SQLException {
        return crsInternal.isAfterLast();
    }
    public boolean isFirst() throws SQLException {
        return crsInternal.isFirst();
    }
    public boolean isLast() throws SQLException {
        return crsInternal.isLast();
    }
    public void beforeFirst() throws SQLException {
        crsInternal.beforeFirst();
    }
    public void afterLast() throws SQLException {
        crsInternal.afterLast();
    }
    public boolean first() throws SQLException {
        return crsInternal.first();
    }
    public boolean last() throws SQLException {
        return crsInternal.last();
    }
    public int getRow() throws SQLException {
        return crsInternal.getRow();
    }
    public boolean absolute(int row) throws SQLException {
        return crsInternal.absolute(row);
    }
    public boolean relative(int rows) throws SQLException {
        return crsInternal.relative(rows);
    }
    public boolean previous() throws SQLException {
        return crsInternal.previous();
    }
    public int findColumn(String columnName) throws SQLException {
        return crsInternal.findColumn(columnName);
    }
    public boolean rowUpdated() throws SQLException {
        return crsInternal.rowUpdated();
    }
    public boolean columnUpdated(int indexColumn) throws SQLException {
        return crsInternal.columnUpdated(indexColumn);
    }
    public boolean rowInserted() throws SQLException {
        return crsInternal.rowInserted();
    }
    public boolean rowDeleted() throws SQLException {
        return crsInternal.rowDeleted();
    }
    public void updateNull(int columnIndex) throws SQLException {
        crsInternal.updateNull(columnIndex);
    }
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        crsInternal.updateBoolean(columnIndex, x);
    }
    public void updateByte(int columnIndex, byte x) throws SQLException {
        crsInternal.updateByte(columnIndex, x);
    }
    public void updateShort(int columnIndex, short x) throws SQLException {
        crsInternal.updateShort(columnIndex, x);
    }
    public void updateInt(int columnIndex, int x) throws SQLException {
        crsInternal.updateInt(columnIndex, x);
    }
    public void updateLong(int columnIndex, long x) throws SQLException {
        crsInternal.updateLong(columnIndex, x);
    }
    public void updateFloat(int columnIndex, float x) throws SQLException {
        crsInternal.updateFloat(columnIndex, x);
    }
    public void updateDouble(int columnIndex, double x) throws SQLException {
        crsInternal.updateDouble(columnIndex, x);
    }
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        crsInternal.updateBigDecimal(columnIndex, x);
    }
    public void updateString(int columnIndex, String x) throws SQLException {
        crsInternal.updateString(columnIndex, x);
    }
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        crsInternal.updateBytes(columnIndex, x);
    }
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        crsInternal.updateDate(columnIndex, x);
    }
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        crsInternal.updateTime(columnIndex, x);
    }
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
        crsInternal.updateTimestamp(columnIndex, x);
    }
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        crsInternal.updateAsciiStream(columnIndex, x, length);
    }
    public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        crsInternal.updateBinaryStream(columnIndex, x, length);
    }
    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
        crsInternal.updateCharacterStream(columnIndex, x, length);
    }
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        crsInternal.updateObject(columnIndex, x, scale);
    }
    public void updateObject(int columnIndex, Object x) throws SQLException {
        crsInternal.updateObject(columnIndex, x);
    }
    public void updateNull(String columnName) throws SQLException {
        crsInternal.updateNull(columnName);
    }
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        crsInternal.updateBoolean(columnName, x);
    }
    public void updateByte(String columnName, byte x) throws SQLException {
        crsInternal.updateByte(columnName, x);
    }
    public void updateShort(String columnName, short x) throws SQLException {
        crsInternal.updateShort(columnName, x);
    }
    public void updateInt(String columnName, int x) throws SQLException {
        crsInternal.updateInt(columnName, x);
    }
    public void updateLong(String columnName, long x) throws SQLException {
        crsInternal.updateLong(columnName, x);
    }
    public void updateFloat(String columnName, float x) throws SQLException {
        crsInternal.updateFloat(columnName, x);
    }
    public void updateDouble(String columnName, double x) throws SQLException {
        crsInternal.updateDouble(columnName, x);
    }
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        crsInternal.updateBigDecimal(columnName, x);
    }
    public void updateString(String columnName, String x) throws SQLException {
        crsInternal.updateString(columnName, x);
    }
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        crsInternal.updateBytes(columnName, x);
    }
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        crsInternal.updateDate(columnName, x);
    }
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        crsInternal.updateTime(columnName, x);
    }
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
        crsInternal.updateTimestamp(columnName, x);
    }
    public void updateAsciiStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        crsInternal.updateAsciiStream(columnName, x, length);
    }
    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        crsInternal.updateBinaryStream(columnName, x, length);
    }
    public void updateCharacterStream(String columnName, java.io.Reader x, int length) throws SQLException {
        crsInternal.updateCharacterStream(columnName, x, length);
    }
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        crsInternal.updateObject(columnName, x, scale);
    }
    public void updateObject(String columnName, Object x) throws SQLException {
        crsInternal.updateObject(columnName, x);
    }
    public void insertRow() throws SQLException {
        crsInternal.insertRow();
    }
    public void updateRow() throws SQLException {
        crsInternal.updateRow();
    }
    public void deleteRow() throws SQLException {
        crsInternal.deleteRow();
    }
    public void refreshRow() throws SQLException {
        crsInternal.refreshRow();
    }
    public void cancelRowUpdates() throws SQLException {
        crsInternal.cancelRowUpdates();
    }
    public void moveToInsertRow() throws SQLException {
        crsInternal.moveToInsertRow();
    }
    public void moveToCurrentRow() throws SQLException {
        crsInternal.moveToCurrentRow();
    }
    public Statement getStatement() throws SQLException {
        return crsInternal.getStatement();
    }
    public Ref getRef(int columnIndex) throws SQLException {
        return crsInternal.getRef(columnIndex);
    }
    public Blob getBlob(int columnIndex) throws SQLException {
        return crsInternal.getBlob(columnIndex);
    }
    public Clob getClob(int columnIndex) throws SQLException {
        return crsInternal.getClob(columnIndex);
    }
     public Array getArray(int columnIndex) throws SQLException {
        return crsInternal.getArray(columnIndex);
    }
    public Ref getRef(String columnName) throws SQLException {
        return crsInternal.getRef(columnName);
    }
    public Blob getBlob(String columnName) throws SQLException {
        return crsInternal.getBlob(columnName);
    }
    public Clob getClob(String columnName) throws SQLException {
        return crsInternal.getClob(columnName);
    }
    public Array getArray(String columnName) throws SQLException {
        return crsInternal.getArray(columnName);
    }
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return crsInternal.getDate(columnIndex, cal);
    }
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        return crsInternal.getDate(columnName, cal);
    }
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return crsInternal.getTime(columnIndex, cal);
    }
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        return crsInternal.getTime(columnName, cal);
    }
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return crsInternal.getTimestamp(columnIndex, cal);
    }
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return crsInternal.getTimestamp(columnName, cal);
    }
    public void setMetaData(RowSetMetaData md) throws SQLException {
        crsInternal.setMetaData(md);
    }
    public ResultSet getOriginal() throws SQLException {
        return crsInternal.getOriginal();
    }
    public ResultSet getOriginalRow() throws SQLException {
        return crsInternal.getOriginalRow();
    }
    public void setOriginalRow() throws SQLException {
        crsInternal.setOriginalRow();
    }
    public int[] getKeyColumns() throws SQLException {
        return crsInternal.getKeyColumns();
    }
    public void setKeyColumns(int[] cols) throws SQLException {
        crsInternal.setKeyColumns(cols);
    }
    public void updateRef(int columnIndex, java.sql.Ref ref) throws SQLException {
        crsInternal.updateRef(columnIndex, ref);
    }
    public void updateRef(String columnName, java.sql.Ref ref) throws SQLException {
        crsInternal.updateRef(columnName, ref);
    }
    public void updateClob(int columnIndex, Clob c) throws SQLException {
        crsInternal.updateClob(columnIndex, c);
    }
    public void updateClob(String columnName, Clob c) throws SQLException {
        crsInternal.updateClob(columnName, c);
    }
    public void updateBlob(int columnIndex, Blob b) throws SQLException {
         crsInternal.updateBlob(columnIndex, b);
    }
    public void updateBlob(String columnName, Blob b) throws SQLException {
         crsInternal.updateBlob(columnName, b);
    }
    public void updateArray(int columnIndex, Array a) throws SQLException {
         crsInternal.updateArray(columnIndex, a);
    }
    public void updateArray(String columnName, Array a) throws SQLException {
         crsInternal.updateArray(columnName, a);
    }
    public void execute() throws SQLException {
        crsInternal.execute();
    }
    public void execute(Connection conn) throws SQLException {
        crsInternal.execute(conn);
    }
    public java.net.URL getURL(int columnIndex) throws SQLException {
        return crsInternal.getURL(columnIndex);
    }
    public java.net.URL getURL(String columnName) throws SQLException {
        return crsInternal.getURL(columnName);
    }
    public void writeXml(ResultSet rs, java.io.Writer writer)
        throws SQLException {
             wrs = new WebRowSetImpl();
             wrs.populate(rs);
             wrs.writeXml(writer);
    }
    public void writeXml(java.io.Writer writer) throws SQLException {
        createWebRowSet().writeXml(writer);
}
    public void readXml(java.io.Reader reader) throws SQLException {
        wrs = new WebRowSetImpl();
        wrs.readXml(reader);
        crsInternal = (CachedRowSetImpl)wrs;
    }
    public void readXml(java.io.InputStream iStream) throws SQLException, IOException {
         wrs = new WebRowSetImpl();
         wrs.readXml(iStream);
         crsInternal = (CachedRowSetImpl)wrs;
    }
    public void writeXml(java.io.OutputStream oStream) throws SQLException, IOException {
         createWebRowSet().writeXml(oStream);
    }
    public void writeXml(ResultSet rs, java.io.OutputStream oStream) throws SQLException, IOException {
             wrs = new WebRowSetImpl();
             wrs.populate(rs);
             wrs.writeXml(oStream);
    }
    private WebRowSet createWebRowSet() throws SQLException {
       if(wrs != null) {
           return wrs;
       } else {
         wrs = new WebRowSetImpl();
          crsInternal.beforeFirst();
          wrs.populate(crsInternal);
          return wrs;
       }
    }
    public int getJoinType() throws SQLException {
        if (vecJoinType == null) {
            this.setJoinType(JoinRowSet.INNER_JOIN);
        }
        Integer i = (Integer)(vecJoinType.get(vecJoinType.size()-1));
        return i.intValue();
    }
    public void addRowSetListener(RowSetListener listener) {
        crsInternal.addRowSetListener(listener);
    }
     public void removeRowSetListener(RowSetListener listener) {
        crsInternal.removeRowSetListener(listener);
    }
     public Collection<?> toCollection() throws SQLException {
        return crsInternal.toCollection();
    }
    public Collection<?> toCollection(int column) throws SQLException {
        return crsInternal.toCollection(column);
    }
    public Collection<?> toCollection(String column) throws SQLException {
        return crsInternal.toCollection(column);
    }
     public CachedRowSet createCopySchema() throws SQLException {
         return crsInternal.createCopySchema();
     }
     public void setSyncProvider(String providerStr) throws SQLException {
         crsInternal.setSyncProvider(providerStr);
     }
     public void acceptChanges() throws SyncProviderException {
         crsInternal.acceptChanges();
     }
     public SyncProvider getSyncProvider() throws SQLException {
        return crsInternal.getSyncProvider();
     }
     private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
     }
     static final long serialVersionUID = -5590501621560008453L;
}
