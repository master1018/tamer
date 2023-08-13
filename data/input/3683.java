public class SQLInputImpl implements SQLInput {
    private boolean lastValueWasNull;
    private int idx;
    private Object attrib[];
    private Map map;
    public SQLInputImpl(Object[] attributes, Map<String,Class<?>> map)
        throws SQLException
    {
        if ((attributes == null) || (map == null)) {
            throw new SQLException("Cannot instantiate a SQLInputImpl " +
            "object with null parameters");
        }
        attrib = attributes;
        idx = -1;
        this.map = map;
    }
    private Object getNextAttribute() throws SQLException {
        if (++idx >= attrib.length) {
            throw new SQLException("SQLInputImpl exception: Invalid read " +
                                   "position");
        } else {
            return attrib[idx];
        }
    }
    public String readString() throws SQLException {
        String attrib = (String)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public boolean readBoolean() throws SQLException {
        Boolean attrib = (Boolean)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return false;
        } else {
            lastValueWasNull = false;
            return attrib.booleanValue();
        }
    }
    public byte readByte() throws SQLException {
        Byte attrib = (Byte)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (byte)0;
        } else {
            lastValueWasNull = false;
            return attrib.byteValue();
        }
    }
    public short readShort() throws SQLException {
        Short attrib = (Short)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (short)0;
        } else {
            lastValueWasNull = false;
            return attrib.shortValue();
        }
    }
    public int readInt() throws SQLException {
        Integer attrib = (Integer)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (int)0;
        } else {
            lastValueWasNull = false;
            return attrib.intValue();
        }
    }
    public long readLong() throws SQLException {
        Long attrib = (Long)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (long)0;
        } else {
            lastValueWasNull = false;
            return attrib.longValue();
        }
    }
    public float readFloat() throws SQLException {
        Float attrib = (Float)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (float)0;
        } else {
            lastValueWasNull = false;
            return attrib.floatValue();
        }
    }
    public double readDouble() throws SQLException {
        Double attrib = (Double)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return (double)0;
        } else {
            lastValueWasNull = false;
            return attrib.doubleValue();
        }
    }
    public java.math.BigDecimal readBigDecimal() throws SQLException {
        java.math.BigDecimal attrib = (java.math.BigDecimal)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public byte[] readBytes() throws SQLException {
        byte[] attrib = (byte[])getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.sql.Date readDate() throws SQLException {
        java.sql.Date attrib = (java.sql.Date)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.sql.Time readTime() throws SQLException {
        java.sql.Time attrib = (java.sql.Time)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.sql.Timestamp readTimestamp() throws SQLException {
        java.sql.Timestamp attrib = (java.sql.Timestamp)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.io.Reader readCharacterStream() throws SQLException {
        java.io.Reader attrib = (java.io.Reader)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.io.InputStream readAsciiStream() throws SQLException {
        java.io.InputStream attrib = (java.io.InputStream)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public java.io.InputStream readBinaryStream() throws SQLException {
        java.io.InputStream attrib = (java.io.InputStream)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public Object readObject() throws SQLException {
        Object attrib = (Object)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            if (attrib instanceof Struct) {
                Struct s = (Struct)attrib;
                Class c = (Class)map.get(s.getSQLTypeName());
                if (c != null) {
                    SQLData obj = null;
                    try {
                        obj = (SQLData)c.newInstance();
                    } catch (java.lang.InstantiationException ex) {
                        throw new SQLException("Unable to instantiate: " +
                                               ex.getMessage());
                    } catch (java.lang.IllegalAccessException ex) {
                        throw new SQLException("Unable to instantiate: " +
                                               ex.getMessage());
                    }
                    Object attribs[] = s.getAttributes(map);
                    SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                    obj.readSQL(sqlInput, s.getSQLTypeName());
                    return (Object)obj;
                }
            }
            return (Object)attrib;
        }
    }
    public Ref readRef() throws SQLException {
        Ref attrib = (Ref)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public Blob readBlob() throws SQLException {
        Blob attrib = (Blob)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public Clob readClob() throws SQLException {
        Clob attrib = (Clob)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public Array readArray() throws SQLException {
        Array attrib = (Array)getNextAttribute();
        if (attrib == null) {
            lastValueWasNull = true;
            return null;
        } else {
            lastValueWasNull = false;
            return attrib;
        }
    }
    public boolean wasNull() throws SQLException {
        return lastValueWasNull;
    }
    public java.net.URL readURL() throws SQLException {
        throw new SQLException("Operation not supported");
    }
     public NClob readNClob() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
    public String readNString() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
    public SQLXML readSQLXML() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
    public RowId readRowId() throws SQLException {
        throw new UnsupportedOperationException("Operation not supported");
    }
}
