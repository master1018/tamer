public class CachedRowSetWriter implements TransactionalWriter, Serializable {
    private transient Connection con;
    private String selectCmd;
    private String updateCmd;
    private String updateWhere;
    private String deleteCmd;
    private String deleteWhere;
    private String insertCmd;
    private int[] keyCols;
    private Object[] params;
    private CachedRowSetReader reader;
    private ResultSetMetaData callerMd;
    private int callerColumnCount;
    private CachedRowSetImpl crsResolve;
    private ArrayList status;
    private int iChangedValsInDbAndCRS;
    private int iChangedValsinDbOnly ;
    private JdbcRowSetResourceBundle resBundle;
    public CachedRowSetWriter() {
       try {
               resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
       } catch(IOException ioe) {
               throw new RuntimeException(ioe);
       }
    }
    public boolean writeData(RowSetInternal caller) throws SQLException {
        boolean conflict = false;
        boolean showDel = false;
        PreparedStatement pstmtIns = null;
        iChangedValsInDbAndCRS = 0;
        iChangedValsinDbOnly = 0;
        CachedRowSetImpl crs = (CachedRowSetImpl)caller;
        this.crsResolve = new CachedRowSetImpl();;
        con = reader.connect(caller);
        if (con == null) {
            throw new SQLException(resBundle.handleGetObject("crswriter.connect").toString());
        }
        initSQLStatements(crs);
        int iColCount;
        RowSetMetaDataImpl rsmdWrite = (RowSetMetaDataImpl)crs.getMetaData();
        RowSetMetaDataImpl rsmdResolv = new RowSetMetaDataImpl();
        iColCount = rsmdWrite.getColumnCount();
        int sz= crs.size()+1;
        status = new ArrayList(sz);
        status.add(0,null);
        rsmdResolv.setColumnCount(iColCount);
        for(int i =1; i <= iColCount; i++) {
            rsmdResolv.setColumnType(i, rsmdWrite.getColumnType(i));
            rsmdResolv.setColumnName(i, rsmdWrite.getColumnName(i));
            rsmdResolv.setNullable(i, ResultSetMetaData.columnNullableUnknown);
        }
        this.crsResolve.setMetaData(rsmdResolv);
        if (callerColumnCount < 1) {
            if (reader.getCloseConnection() == true)
                    con.close();
            return true;
        }
        showDel = crs.getShowDeleted();
        crs.setShowDeleted(true);
        crs.beforeFirst();
        int rows =1;
        while (crs.next()) {
            if (crs.rowDeleted()) {
                if (conflict = (deleteOriginalRow(crs, this.crsResolve)) == true) {
                       status.add(rows, Integer.valueOf(SyncResolver.DELETE_ROW_CONFLICT));
                } else {
                       status.add(rows, Integer.valueOf(SyncResolver.NO_ROW_CONFLICT));
                }
           } else if (crs.rowInserted()) {
                pstmtIns = con.prepareStatement(insertCmd);
                if ( (conflict = insertNewRow(crs, pstmtIns, this.crsResolve)) == true) {
                          status.add(rows, Integer.valueOf(SyncResolver.INSERT_ROW_CONFLICT));
                } else {
                       status.add(rows, Integer.valueOf(SyncResolver.NO_ROW_CONFLICT));
                }
            } else  if (crs.rowUpdated()) {
                       if ( conflict = (updateOriginalRow(crs)) == true) {
                             status.add(rows, Integer.valueOf(SyncResolver.UPDATE_ROW_CONFLICT));
               } else {
                      status.add(rows, Integer.valueOf(SyncResolver.NO_ROW_CONFLICT));
               }
            } else {
                int icolCount = crs.getMetaData().getColumnCount();
                status.add(rows, Integer.valueOf(SyncResolver.NO_ROW_CONFLICT));
                this.crsResolve.moveToInsertRow();
                for(int cols=0;cols<iColCount;cols++) {
                   this.crsResolve.updateNull(cols+1);
                } 
                this.crsResolve.insertRow();
                this.crsResolve.moveToCurrentRow();
                } 
         rows++;
      } 
        if(pstmtIns!=null)
        pstmtIns.close();
        crs.setShowDeleted(showDel);
      boolean boolConf = false;
      for (int j=1;j<status.size();j++){
          if(! ((status.get(j)).equals(Integer.valueOf(SyncResolver.NO_ROW_CONFLICT)))) {
              boolConf = true;
             break;
          }
      }
        crs.beforeFirst();
        this.crsResolve.beforeFirst();
    if(boolConf) {
        SyncProviderException spe = new SyncProviderException(status.size() - 1+resBundle.handleGetObject("crswriter.conflictsno").toString());
         SyncResolverImpl syncResImpl = (SyncResolverImpl) spe.getSyncResolver();
         syncResImpl.setCachedRowSet(crs);
         syncResImpl.setCachedRowSetResolver(this.crsResolve);
         syncResImpl.setStatus(status);
         syncResImpl.setCachedRowSetWriter(this);
        throw spe;
    } else {
         return true;
    }
  } 
    private boolean updateOriginalRow(CachedRowSet crs)
        throws SQLException {
        PreparedStatement pstmt;
        int i = 0;
        int idx = 0;
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();
        try {
            updateWhere = buildWhereClause(updateWhere, origVals);
            String tempselectCmd = selectCmd.toLowerCase();
            int idxWhere = tempselectCmd.indexOf("where");
            if(idxWhere != -1)
            {
               String tempSelect = selectCmd.substring(0,idxWhere);
               selectCmd = tempSelect;
            }
            pstmt = con.prepareStatement(selectCmd + updateWhere,
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (i = 0; i < keyCols.length; i++) {
                if (params[i] != null) {
                    pstmt.setObject(++idx, params[i]);
                } else {
                    continue;
                }
            }
            try {
                pstmt.setMaxRows(crs.getMaxRows());
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) {
            }
            ResultSet rs = null;
            rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rs.next()) {
                if (rs.next()) {
                   return true;
                }
                rs.first();
                int colsNotChanged = 0;
                Vector cols = new Vector();
                String updateExec = updateCmd;
                Object orig;
                Object curr;
                Object rsval;
                boolean boolNull = true;
                Object objVal = null;
                boolean first = true;
                boolean flag = true;
          this.crsResolve.moveToInsertRow();
          for (i = 1; i <= callerColumnCount; i++) {
                orig = origVals.getObject(i);
                curr = crs.getObject(i);
                rsval = rs.getObject(i);
                Map map = (crs.getTypeMap() == null)?con.getTypeMap():crs.getTypeMap();
                if (rsval instanceof Struct) {
                    Struct s = (Struct)rsval;
                    Class c = null;
                    c = (Class)map.get(s.getSQLTypeName());
                    if (c != null) {
                        SQLData obj = null;
                        try {
                            obj = (SQLData)c.newInstance();
                        } catch (java.lang.InstantiationException ex) {
                            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                            ex.getMessage()));
                        } catch (java.lang.IllegalAccessException ex) {
                            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.unableins").toString(),
                            ex.getMessage()));
                        }
                        Object attribs[] = s.getAttributes(map);
                        SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                        obj.readSQL(sqlInput, s.getSQLTypeName());
                        rsval = obj;
                    }
                } else if (rsval instanceof SQLData) {
                    rsval = new SerialStruct((SQLData)rsval, map);
                } else if (rsval instanceof Blob) {
                    rsval = new SerialBlob((Blob)rsval);
                } else if (rsval instanceof Clob) {
                    rsval = new SerialClob((Clob)rsval);
                } else if (rsval instanceof java.sql.Array) {
                    rsval = new SerialArray((java.sql.Array)rsval, map);
                }
                boolNull = true;
                if(rsval == null && orig != null) {
                    iChangedValsinDbOnly++;
                     boolNull = false;
                     objVal = rsval;
                }
                else if(rsval != null && (!rsval.equals(orig)))
                {
                    iChangedValsinDbOnly++;
                     boolNull = false;
                     objVal = rsval;
                } else if (  (orig == null || curr == null) ) {
                        if (first == false || flag == false) {
                          updateExec += ", ";
                         }
                        updateExec += crs.getMetaData().getColumnName(i);
                        cols.add(Integer.valueOf(i));
                        updateExec += " = ? ";
                        first = false;
                }  else if (orig.equals(curr)) {
                       colsNotChanged++;
                } else if(orig.equals(curr) == false) {
                         if(crs.columnUpdated(i)) {
                             if(rsval.equals(orig)) {
                                 if (flag == false || first == false) {
                                    updateExec += ", ";
                                 }
                                updateExec += crs.getMetaData().getColumnName(i);
                                cols.add(Integer.valueOf(i));
                                updateExec += " = ? ";
                                flag = false;
                             } else {
                               boolNull= false;
                               objVal = rsval;
                               iChangedValsInDbAndCRS++;
                             }
                         }
                  }
                    if(!boolNull) {
                        this.crsResolve.updateObject(i,objVal);
                                 } else {
                                      this.crsResolve.updateNull(i);
                                 }
                } 
                rs.close();
                pstmt.close();
               this.crsResolve.insertRow();
                   this.crsResolve.moveToCurrentRow();
                if ( (first == false && cols.size() == 0)  ||
                     colsNotChanged == callerColumnCount ) {
                    return false;
                }
                if(iChangedValsInDbAndCRS != 0 || iChangedValsinDbOnly != 0) {
                   return true;
                }
                updateExec += updateWhere;
                pstmt = con.prepareStatement(updateExec);
                for (i = 0; i < cols.size(); i++) {
                    Object obj = crs.getObject(((Integer)cols.get(i)).intValue());
                    if (obj != null)
                        pstmt.setObject(i + 1, obj);
                    else
                        pstmt.setNull(i + 1,crs.getMetaData().getColumnType(i + 1));
                }
                idx = i;
                for (i = 0; i < keyCols.length; i++) {
                    if (params[i] != null) {
                        pstmt.setObject(++idx, params[i]);
                    } else {
                        continue;
                    }
                }
                i = pstmt.executeUpdate();
                 return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.crsResolve.moveToInsertRow();
            for(i = 1; i <= callerColumnCount; i++) {
               this.crsResolve.updateNull(i);
            }
            this.crsResolve.insertRow();
            this.crsResolve.moveToCurrentRow();
            return true;
        }
    }
    private boolean insertNewRow(CachedRowSet crs,
        PreparedStatement pstmt, CachedRowSetImpl crsRes) throws SQLException {
        int i = 0;
        int icolCount = crs.getMetaData().getColumnCount();
        boolean returnVal = false;
        PreparedStatement pstmtSel = con.prepareStatement(selectCmd,
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs, rs2 = null;
        DatabaseMetaData dbmd = con.getMetaData();
        rs = pstmtSel.executeQuery();
        String table = crs.getTableName();
        rs2 = dbmd.getPrimaryKeys(null, null, table);
        String [] primaryKeys = new String[icolCount];
        int k = 0;
        while(rs2.next()) {
            String pkcolname = rs2.getString("COLUMN_NAME");
            primaryKeys[k] = pkcolname;
            k++;
        }
        if(rs.next()) {
            for(int j=0;j<primaryKeys.length;j++) {
                if(primaryKeys[j] != null) {
                    if(crs.getObject(primaryKeys[j]) == null){
                        break;
                    }
                    String crsPK = (crs.getObject(primaryKeys[j])).toString();
                    String rsPK = (rs.getObject(primaryKeys[j])).toString();
                    if(crsPK.equals(rsPK)) {
                        returnVal = true;
                        this.crsResolve.moveToInsertRow();
                        for(i = 1; i <= icolCount; i++) {
                            String colname = (rs.getMetaData()).getColumnName(i);
                            if(colname.equals(primaryKeys[j]))
                                this.crsResolve.updateObject(i,rsPK);
                            else
                                this.crsResolve.updateNull(i);
                        }
                        this.crsResolve.insertRow();
                        this.crsResolve.moveToCurrentRow();
                    }
                }
            }
        }
        if(returnVal)
            return returnVal;
        try {
            for (i = 1; i <= icolCount; i++) {
                Object obj = crs.getObject(i);
                if (obj != null) {
                    pstmt.setObject(i, obj);
                } else {
                    pstmt.setNull(i,crs.getMetaData().getColumnType(i));
                }
            }
             i = pstmt.executeUpdate();
             return false;
        } catch (SQLException ex) {
            this.crsResolve.moveToInsertRow();
            for(i = 1; i <= icolCount; i++) {
               this.crsResolve.updateNull(i);
            }
            this.crsResolve.insertRow();
            this.crsResolve.moveToCurrentRow();
            return true;
        }
    }
    private boolean deleteOriginalRow(CachedRowSet crs, CachedRowSetImpl crsRes) throws SQLException {
        PreparedStatement pstmt;
        int i;
        int idx = 0;
        String strSelect;
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();
        deleteWhere = buildWhereClause(deleteWhere, origVals);
        pstmt = con.prepareStatement(selectCmd + deleteWhere,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        for (i = 0; i < keyCols.length; i++) {
            if (params[i] != null) {
                pstmt.setObject(++idx, params[i]);
            } else {
                continue;
            }
        }
        try {
            pstmt.setMaxRows(crs.getMaxRows());
            pstmt.setMaxFieldSize(crs.getMaxFieldSize());
            pstmt.setEscapeProcessing(crs.getEscapeProcessing());
            pstmt.setQueryTimeout(crs.getQueryTimeout());
        } catch (Exception ex) {
            ;
        }
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() == true) {
            if (rs.next()) {
                return true;
            }
            rs.first();
            boolean boolChanged = false;
            crsRes.moveToInsertRow();
            for (i = 1; i <= crs.getMetaData().getColumnCount(); i++) {
                Object original = origVals.getObject(i);
                Object changed = rs.getObject(i);
                if(original != null && changed != null ) {
                  if(! (original.toString()).equals(changed.toString()) ) {
                      boolChanged = true;
                      crsRes.updateObject(i,origVals.getObject(i));
                  }
                } else {
                   crsRes.updateNull(i);
               }
            }
           crsRes.insertRow();
           crsRes.moveToCurrentRow();
           if(boolChanged) {
               return true;
            } else {
            }
            String cmd = deleteCmd + deleteWhere;
            pstmt = con.prepareStatement(cmd);
            idx = 0;
            for (i = 0; i < keyCols.length; i++) {
                if (params[i] != null) {
                    pstmt.setObject(++idx, params[i]);
                } else {
                    continue;
                }
            }
            if (pstmt.executeUpdate() != 1) {
                return true;
            }
            pstmt.close();
        } else {
            return true;
        }
        return false;
    }
    public void setReader(CachedRowSetReader reader) throws SQLException {
        this.reader = reader;
    }
    public CachedRowSetReader getReader() throws SQLException {
        return reader;
    }
    private void initSQLStatements(CachedRowSet caller) throws SQLException {
        int i;
        callerMd = caller.getMetaData();
        callerColumnCount = callerMd.getColumnCount();
        if (callerColumnCount < 1)
            return;
        String table = caller.getTableName();
        if (table == null) {
            table = callerMd.getTableName(1);
            if (table == null || table.length() == 0) {
                throw new SQLException(resBundle.handleGetObject("crswriter.tname").toString());
            }
        }
        String catalog = callerMd.getCatalogName(1);
            String schema = callerMd.getSchemaName(1);
        DatabaseMetaData dbmd = con.getMetaData();
        selectCmd = "SELECT ";
        for (i=1; i <= callerColumnCount; i++) {
            selectCmd += callerMd.getColumnName(i);
            if ( i <  callerMd.getColumnCount() )
                selectCmd += ", ";
            else
                selectCmd += " ";
        }
        selectCmd += "FROM " + buildTableName(dbmd, catalog, schema, table);
        updateCmd = "UPDATE " + buildTableName(dbmd, catalog, schema, table);
        String tempupdCmd = updateCmd.toLowerCase();
        int idxupWhere = tempupdCmd.indexOf("where");
        if(idxupWhere != -1)
        {
           updateCmd = updateCmd.substring(0,idxupWhere);
        }
        updateCmd += "SET ";
        insertCmd = "INSERT INTO " + buildTableName(dbmd, catalog, schema, table);
        insertCmd += "(";
        for (i=1; i <= callerColumnCount; i++) {
            insertCmd += callerMd.getColumnName(i);
            if ( i <  callerMd.getColumnCount() )
                insertCmd += ", ";
            else
                insertCmd += ") VALUES (";
        }
        for (i=1; i <= callerColumnCount; i++) {
            insertCmd += "?";
            if (i < callerColumnCount)
                insertCmd += ", ";
            else
                insertCmd += ")";
        }
        deleteCmd = "DELETE FROM " + buildTableName(dbmd, catalog, schema, table);
        buildKeyDesc(caller);
    }
    private String buildTableName(DatabaseMetaData dbmd,
        String catalog, String schema, String table) throws SQLException {
        String cmd = "";
        catalog = catalog.trim();
        schema = schema.trim();
        table = table.trim();
        if (dbmd.isCatalogAtStart() == true) {
            if (catalog != null && catalog.length() > 0) {
                cmd += catalog + dbmd.getCatalogSeparator();
            }
            if (schema != null && schema.length() > 0) {
                cmd += schema + ".";
            }
            cmd += table;
        } else {
            if (schema != null && schema.length() > 0) {
                cmd += schema + ".";
            }
            cmd += table;
            if (catalog != null && catalog.length() > 0) {
                cmd += dbmd.getCatalogSeparator() + catalog;
            }
        }
        cmd += " ";
        return cmd;
    }
    private void buildKeyDesc(CachedRowSet crs) throws SQLException {
        keyCols = crs.getKeyColumns();
        ResultSetMetaData resultsetmd = crs.getMetaData();
        if (keyCols == null || keyCols.length == 0) {
            ArrayList<Integer> listKeys = new ArrayList<Integer>();
            for (int i = 0; i < callerColumnCount; i++ ) {
                if(resultsetmd.getColumnType(i+1) != java.sql.Types.CLOB &&
                        resultsetmd.getColumnType(i+1) != java.sql.Types.STRUCT &&
                        resultsetmd.getColumnType(i+1) != java.sql.Types.SQLXML &&
                        resultsetmd.getColumnType(i+1) != java.sql.Types.BLOB &&
                        resultsetmd.getColumnType(i+1) != java.sql.Types.ARRAY &&
                        resultsetmd.getColumnType(i+1) != java.sql.Types.OTHER )
                    listKeys.add(i+1);
            }
            keyCols = new int[listKeys.size()];
            for (int i = 0; i < listKeys.size(); i++ )
                keyCols[i] = listKeys.get(i);
        }
        params = new Object[keyCols.length];
    }
    private String buildWhereClause(String whereClause,
                                    ResultSet rs) throws SQLException {
        whereClause = "WHERE ";
        for (int i = 0; i < keyCols.length; i++) {
            if (i > 0) {
                    whereClause += "AND ";
            }
            whereClause += callerMd.getColumnName(keyCols[i]);
            params[i] = rs.getObject(keyCols[i]);
            if (rs.wasNull() == true) {
                whereClause += " IS NULL ";
            } else {
                whereClause += " = ? ";
            }
        }
        return whereClause;
    }
    void updateResolvedConflictToDB(CachedRowSet crs, Connection con) throws SQLException {
          PreparedStatement pStmt  ;
          String strWhere = "WHERE " ;
          String strExec =" ";
          String strUpdate = "UPDATE ";
          int icolCount = crs.getMetaData().getColumnCount();
          int keyColumns[] = crs.getKeyColumns();
          Object param[];
          String strSet="";
        strWhere = buildWhereClause(strWhere, crs);
        if (keyColumns == null || keyColumns.length == 0) {
            keyColumns = new int[icolCount];
            for (int i = 0; i < keyColumns.length; ) {
                keyColumns[i] = ++i;
            }
          }
          param = new Object[keyColumns.length];
         strUpdate = "UPDATE " + buildTableName(con.getMetaData(),
                            crs.getMetaData().getCatalogName(1),
                           crs.getMetaData().getSchemaName(1),
                           crs.getTableName());
         strUpdate += "SET ";
        boolean first = true;
        for (int i=1; i<=icolCount;i++) {
           if (crs.columnUpdated(i)) {
                  if (first == false) {
                    strSet += ", ";
                  }
                 strSet += crs.getMetaData().getColumnName(i);
                 strSet += " = ? ";
                 first = false;
         } 
      } 
         strUpdate += strSet;
         strWhere = "WHERE ";
        for (int i = 0; i < keyColumns.length; i++) {
            if (i > 0) {
                    strWhere += "AND ";
            }
            strWhere += crs.getMetaData().getColumnName(keyColumns[i]);
            param[i] = crs.getObject(keyColumns[i]);
            if (crs.wasNull() == true) {
                strWhere += " IS NULL ";
            } else {
                strWhere += " = ? ";
            }
        }
          strUpdate += strWhere;
        pStmt = con.prepareStatement(strUpdate);
        int idx =0;
          for (int i = 0; i < icolCount; i++) {
             if(crs.columnUpdated(i+1)) {
              Object obj = crs.getObject(i+1);
              if (obj != null) {
                  pStmt.setObject(++idx, obj);
              } else {
                  pStmt.setNull(i + 1,crs.getMetaData().getColumnType(i + 1));
             } 
           } 
        } 
          for (int i = 0; i < keyColumns.length; i++) {
              if (param[i] != null) {
                  pStmt.setObject(++idx, param[i]);
              }
          }
        int id = pStmt.executeUpdate();
      }
    public void commit() throws SQLException {
        con.commit();
        if (reader.getCloseConnection() == true) {
            con.close();
        }
    }
     public void commit(CachedRowSetImpl crs, boolean updateRowset) throws SQLException {
        con.commit();
        if(updateRowset) {
          if(crs.getCommand() != null)
            crs.execute(con);
        }
        if (reader.getCloseConnection() == true) {
            con.close();
        }
    }
    public void rollback() throws SQLException {
        con.rollback();
        if (reader.getCloseConnection() == true) {
            con.close();
        }
    }
    public void rollback(Savepoint s) throws SQLException {
        con.rollback(s);
        if (reader.getCloseConnection() == true) {
            con.close();
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID =-8506030970299413976L;
}
