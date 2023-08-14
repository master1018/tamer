public class WebRowSetXmlWriter implements XmlWriter, Serializable {
    private java.io.Writer writer;
    private java.util.Stack stack;
    private  JdbcRowSetResourceBundle resBundle;
    public WebRowSetXmlWriter() {
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public void writeXML(WebRowSet caller, java.io.Writer wrt)
    throws SQLException {
        stack = new java.util.Stack();
        writer = wrt;
        writeRowSet(caller);
    }
    public void writeXML(WebRowSet caller, java.io.OutputStream oStream)
    throws SQLException {
        stack = new java.util.Stack();
        writer = new OutputStreamWriter(oStream);
        writeRowSet(caller);
    }
    private void writeRowSet(WebRowSet caller) throws SQLException {
        try {
            startHeader();
            writeProperties(caller);
            writeMetaData(caller);
            writeData(caller);
            endHeader();
        } catch (java.io.IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("wrsxmlwriter.ioex").toString(), ex.getMessage()));
        }
    }
    private void startHeader() throws java.io.IOException {
        setTag("webRowSet");
        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<webRowSet xmlns=\"http:
        writer.write("xsi:schemaLocation=\"http:
    }
    private void endHeader() throws java.io.IOException {
        endTag("webRowSet");
    }
    private void writeProperties(WebRowSet caller) throws java.io.IOException {
        beginSection("properties");
        try {
            propString("command", processSpecialCharacters(caller.getCommand()));
            propInteger("concurrency", caller.getConcurrency());
            propString("datasource", caller.getDataSourceName());
            propBoolean("escape-processing",
                    caller.getEscapeProcessing());
            try {
                propInteger("fetch-direction", caller.getFetchDirection());
            } catch(SQLException sqle) {
            }
            propInteger("fetch-size", caller.getFetchSize());
            propInteger("isolation-level",
                    caller.getTransactionIsolation());
            beginSection("key-columns");
            int[] kc = caller.getKeyColumns();
            for (int i = 0; kc != null && i < kc.length; i++)
                propInteger("column", kc[i]);
            endSection("key-columns");
            beginSection("map");
            java.util.Map typeMap = caller.getTypeMap();
            if (typeMap != null) {
                Iterator i = typeMap.keySet().iterator();
                Class c;
                String type;
                while (i.hasNext()) {
                    type = (String)i.next();
                    c = (Class)typeMap.get(type);
                    propString("type", type);
                    propString("class", c.getName());
                }
            }
            endSection("map");
            propInteger("max-field-size", caller.getMaxFieldSize());
            propInteger("max-rows", caller.getMaxRows());
            propInteger("query-timeout", caller.getQueryTimeout());
            propBoolean("read-only", caller.isReadOnly());
            int itype = caller.getType();
            String strType = "";
            if(itype == 1003) {
                strType = "ResultSet.TYPE_FORWARD_ONLY";
            } else if(itype == 1004) {
                strType = "ResultSet.TYPE_SCROLL_INSENSITIVE";
            } else if(itype == 1005) {
                strType = "ResultSet.TYPE_SCROLL_SENSITIVE";
            }
            propString("rowset-type", strType);
            propBoolean("show-deleted", caller.getShowDeleted());
            propString("table-name", caller.getTableName());
            propString("url", caller.getUrl());
            beginSection("sync-provider");
            String strProviderInstance = (caller.getSyncProvider()).toString();
            String strProvider = strProviderInstance.substring(0, (caller.getSyncProvider()).toString().indexOf("@"));
            propString("sync-provider-name", strProvider);
            propString("sync-provider-vendor", "Oracle Corporation");
            propString("sync-provider-version", "1.0");
            propInteger("sync-provider-grade", caller.getSyncProvider().getProviderGrade());
            propInteger("data-source-lock", caller.getSyncProvider().getDataSourceLock());
            endSection("sync-provider");
        } catch (SQLException ex) {
            throw new java.io.IOException(MessageFormat.format(resBundle.handleGetObject("wrsxmlwriter.sqlex").toString(), ex.getMessage()));
        }
        endSection("properties");
    }
    private void writeMetaData(WebRowSet caller) throws java.io.IOException {
        int columnCount;
        beginSection("metadata");
        try {
            ResultSetMetaData rsmd = caller.getMetaData();
            columnCount = rsmd.getColumnCount();
            propInteger("column-count", columnCount);
            for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                beginSection("column-definition");
                propInteger("column-index", colIndex);
                propBoolean("auto-increment", rsmd.isAutoIncrement(colIndex));
                propBoolean("case-sensitive", rsmd.isCaseSensitive(colIndex));
                propBoolean("currency", rsmd.isCurrency(colIndex));
                propInteger("nullable", rsmd.isNullable(colIndex));
                propBoolean("signed", rsmd.isSigned(colIndex));
                propBoolean("searchable", rsmd.isSearchable(colIndex));
                propInteger("column-display-size",rsmd.getColumnDisplaySize(colIndex));
                propString("column-label", rsmd.getColumnLabel(colIndex));
                propString("column-name", rsmd.getColumnName(colIndex));
                propString("schema-name", rsmd.getSchemaName(colIndex));
                propInteger("column-precision", rsmd.getPrecision(colIndex));
                propInteger("column-scale", rsmd.getScale(colIndex));
                propString("table-name", rsmd.getTableName(colIndex));
                propString("catalog-name", rsmd.getCatalogName(colIndex));
                propInteger("column-type", rsmd.getColumnType(colIndex));
                propString("column-type-name", rsmd.getColumnTypeName(colIndex));
                endSection("column-definition");
            }
        } catch (SQLException ex) {
            throw new java.io.IOException(MessageFormat.format(resBundle.handleGetObject("wrsxmlwriter.sqlex").toString(), ex.getMessage()));
        }
        endSection("metadata");
    }
    private void writeData(WebRowSet caller) throws java.io.IOException {
        ResultSet rs;
        try {
            ResultSetMetaData rsmd = caller.getMetaData();
            int columnCount = rsmd.getColumnCount();
            int i;
            beginSection("data");
            caller.beforeFirst();
            caller.setShowDeleted(true);
            while (caller.next()) {
                if (caller.rowDeleted() && caller.rowInserted()) {
                    beginSection("modifyRow");
                } else if (caller.rowDeleted()) {
                    beginSection("deleteRow");
                } else if (caller.rowInserted()) {
                    beginSection("insertRow");
                } else {
                    beginSection("currentRow");
                }
                for (i = 1; i <= columnCount; i++) {
                    if (caller.columnUpdated(i)) {
                        rs = caller.getOriginalRow();
                        rs.next();
                        beginTag("columnValue");
                        writeValue(i, (RowSet)rs);
                        endTag("columnValue");
                        beginTag("updateRow");
                        writeValue(i, caller);
                        endTag("updateRow");
                    } else {
                        beginTag("columnValue");
                        writeValue(i, caller);
                        endTag("columnValue");
                    }
                }
                endSection(); 
            }
            endSection("data");
        } catch (SQLException ex) {
            throw new java.io.IOException(MessageFormat.format(resBundle.handleGetObject("wrsxmlwriter.sqlex").toString(), ex.getMessage()));
        }
    }
    private void writeValue(int idx, RowSet caller) throws java.io.IOException {
        try {
            int type = caller.getMetaData().getColumnType(idx);
            switch (type) {
                case java.sql.Types.BIT:
                case java.sql.Types.BOOLEAN:
                    boolean b = caller.getBoolean(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeBoolean(b);
                    break;
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
                    short s = caller.getShort(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeShort(s);
                    break;
                case java.sql.Types.INTEGER:
                    int i = caller.getInt(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeInteger(i);
                    break;
                case java.sql.Types.BIGINT:
                    long l = caller.getLong(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeLong(l);
                    break;
                case java.sql.Types.REAL:
                case java.sql.Types.FLOAT:
                    float f = caller.getFloat(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeFloat(f);
                    break;
                case java.sql.Types.DOUBLE:
                    double d = caller.getDouble(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeDouble(d);
                    break;
                case java.sql.Types.NUMERIC:
                case java.sql.Types.DECIMAL:
                    writeBigDecimal(caller.getBigDecimal(idx));
                    break;
                case java.sql.Types.BINARY:
                case java.sql.Types.VARBINARY:
                case java.sql.Types.LONGVARBINARY:
                    break;
                case java.sql.Types.DATE:
                    java.sql.Date date = caller.getDate(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeLong(date.getTime());
                    break;
                case java.sql.Types.TIME:
                    java.sql.Time time = caller.getTime(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeLong(time.getTime());
                    break;
                case java.sql.Types.TIMESTAMP:
                    java.sql.Timestamp ts = caller.getTimestamp(idx);
                    if (caller.wasNull())
                        writeNull();
                    else
                        writeLong(ts.getTime());
                    break;
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    writeStringData(caller.getString(idx));
                    break;
                default:
                    System.out.println(resBundle.handleGetObject("wsrxmlwriter.notproper").toString());
            }
        } catch (SQLException ex) {
            throw new java.io.IOException(resBundle.handleGetObject("wrsxmlwriter.failedwrite").toString()+ ex.getMessage());
        }
    }
    private void beginSection(String tag) throws java.io.IOException {
        setTag(tag);
        writeIndent(stack.size());
        writer.write("<" + tag + ">\n");
    }
    private void endSection(String tag) throws java.io.IOException {
        writeIndent(stack.size());
        String beginTag = getTag();
        if(beginTag.indexOf("webRowSet") != -1) {
            beginTag ="webRowSet";
        }
        if (tag.equals(beginTag) ) {
            writer.write("</" + beginTag + ">\n");
        } else {
            ;
        }
        writer.flush();
    }
    private void endSection() throws java.io.IOException {
        writeIndent(stack.size());
        String beginTag = getTag();
        writer.write("</" + beginTag + ">\n");
        writer.flush();
    }
    private void beginTag(String tag) throws java.io.IOException {
        setTag(tag);
        writeIndent(stack.size());
        writer.write("<" + tag + ">");
    }
    private void endTag(String tag) throws java.io.IOException {
        String beginTag = getTag();
        if (tag.equals(beginTag)) {
            writer.write("</" + beginTag + ">\n");
        } else {
            ;
        }
        writer.flush();
    }
    private void emptyTag(String tag) throws java.io.IOException {
        writer.write("<" + tag + "/>");
    }
    private void setTag(String tag) {
        stack.push(tag);
    }
    private String getTag() {
        return (String)stack.pop();
    }
    private void writeNull() throws java.io.IOException {
        emptyTag("null");
    }
    private void writeStringData(String s) throws java.io.IOException {
        if (s == null) {
            writeNull();
        } else if (s.equals("")) {
            writeEmptyString();
        } else {
            s = processSpecialCharacters(s);
            writer.write(s);
        }
    }
    private void writeString(String s) throws java.io.IOException {
        if (s != null) {
            writer.write(s);
        } else  {
            writeNull();
        }
    }
    private void writeShort(short s) throws java.io.IOException {
        writer.write(Short.toString(s));
    }
    private void writeLong(long l) throws java.io.IOException {
        writer.write(Long.toString(l));
    }
    private void writeInteger(int i) throws java.io.IOException {
        writer.write(Integer.toString(i));
    }
    private void writeBoolean(boolean b) throws java.io.IOException {
        writer.write(Boolean.valueOf(b).toString());
    }
    private void writeFloat(float f) throws java.io.IOException {
        writer.write(Float.toString(f));
    }
    private void writeDouble(double d) throws java.io.IOException {
        writer.write(Double.toString(d));
    }
    private void writeBigDecimal(java.math.BigDecimal bd) throws java.io.IOException {
        if (bd != null)
            writer.write(bd.toString());
        else
            emptyTag("null");
    }
    private void writeIndent(int tabs) throws java.io.IOException {
        for (int i = 1; i < tabs; i++) {
            writer.write("  ");
        }
    }
    private void propString(String tag, String s) throws java.io.IOException {
        beginTag(tag);
        writeString(s);
        endTag(tag);
    }
    private void propInteger(String tag, int i) throws java.io.IOException {
        beginTag(tag);
        writeInteger(i);
        endTag(tag);
    }
    private void propBoolean(String tag, boolean b) throws java.io.IOException {
        beginTag(tag);
        writeBoolean(b);
        endTag(tag);
    }
    private void writeEmptyString() throws java.io.IOException {
        emptyTag("emptyString");
    }
    public boolean writeData(RowSetInternal caller) {
        return false;
    }
    private String processSpecialCharacters(String s) {
        if(s == null) {
            return null;
        }
        char []charStr = s.toCharArray();
        String specialStr = "";
        for(int i = 0; i < charStr.length; i++) {
            if(charStr[i] == '&') {
                specialStr = specialStr.concat("&amp;");
            } else if(charStr[i] == '<') {
                specialStr = specialStr.concat("&lt;");
            } else if(charStr[i] == '>') {
                specialStr = specialStr.concat("&gt;");
            } else if(charStr[i] == '\'') {
                specialStr = specialStr.concat("&apos;");
            } else if(charStr[i] == '\"') {
                specialStr = specialStr.concat("&quot;");
            } else {
                specialStr = specialStr.concat(String.valueOf(charStr[i]));
            }
        }
        s = specialStr;
        return s;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID = 7163134986189677641L;
}
