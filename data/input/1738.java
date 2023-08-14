public class SerialStruct implements Struct, Serializable, Cloneable {
    private String SQLTypeName;
    private Object attribs[];
     public SerialStruct(Struct in, Map<String,Class<?>> map)
         throws SerialException
     {
        try {
        SQLTypeName = in.getSQLTypeName();
        System.out.println("SQLTypeName: " + SQLTypeName);
        attribs = in.getAttributes(map);
        mapToSerial(map);
        } catch (SQLException e) {
            throw new SerialException(e.getMessage());
        }
    }
    public SerialStruct(SQLData in, Map<String,Class<?>> map)
        throws SerialException
    {
        try {
        SQLTypeName = in.getSQLTypeName();
        Vector tmp = new Vector();
        in.writeSQL(new SQLOutputImpl(tmp, map));
        attribs = tmp.toArray();
        } catch (SQLException e) {
            throw new SerialException(e.getMessage());
        }
    }
    public String getSQLTypeName() throws SerialException {
        return SQLTypeName;
    }
    public Object[]  getAttributes() throws SerialException {
        return attribs;
    }
    public Object[] getAttributes(Map<String,Class<?>> map)
        throws SerialException
    {
       return attribs;
    }
    private void mapToSerial(Map map) throws SerialException {
        try {
        for (int i = 0; i < attribs.length; i++) {
            if (attribs[i] instanceof Struct) {
                attribs[i] = new SerialStruct((Struct)attribs[i], map);
            } else if (attribs[i] instanceof SQLData) {
                attribs[i] = new SerialStruct((SQLData)attribs[i], map);
            } else if (attribs[i] instanceof Blob) {
                attribs[i] = new SerialBlob((Blob)attribs[i]);
            } else if (attribs[i] instanceof Clob) {
                attribs[i] = new SerialClob((Clob)attribs[i]);
            } else if (attribs[i] instanceof Ref) {
                attribs[i] = new SerialRef((Ref)attribs[i]);
            } else if (attribs[i] instanceof java.sql.Array) {
                attribs[i] = new SerialArray((java.sql.Array)attribs[i], map);
            }
        }
        } catch (SQLException e) {
            throw new SerialException(e.getMessage());
        }
        return;
    }
    static final long serialVersionUID = -8322445504027483372L;
}
