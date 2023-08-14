public class SerialArray implements Array, Serializable, Cloneable {
    private Object[] elements;
    private int baseType;
    private String baseTypeName;
    private int len;
     public SerialArray(Array array, Map<String,Class<?>> map)
         throws SerialException, SQLException
     {
        if ((array == null) || (map == null)) {
            throw new SQLException("Cannot instantiate a SerialArray " +
            "object with null parameters");
        }
        if ((elements = (Object[])array.getArray()) == null) {
             throw new SQLException("Invalid Array object. Calls to Array.getArray() " +
                 "return null value which cannot be serialized");
         }
        elements = (Object[])array.getArray(map);
        baseType = array.getBaseType();
        baseTypeName = array.getBaseTypeName();
        len = elements.length;
        switch (baseType) {
            case java.sql.Types.STRUCT:
                for (int i = 0; i < len; i++) {
                    elements[i] = new SerialStruct((Struct)elements[i], map);
                }
            break;
            case java.sql.Types.ARRAY:
                for (int i = 0; i < len; i++) {
                    elements[i] = new SerialArray((Array)elements[i], map);
                }
            break;
            case java.sql.Types.BLOB:
            for (int i = 0; i < len; i++) {
                elements[i] = new SerialBlob((Blob)elements[i]);
            }
            break;
            case java.sql.Types.CLOB:
                for (int i = 0; i < len; i++) {
                    elements[i] = new SerialClob((Clob)elements[i]);
                }
            break;
            case java.sql.Types.DATALINK:
                for (int i = 0; i < len; i++) {
                    elements[i] = new SerialDatalink((URL)elements[i]);
                }
            break;
            case java.sql.Types.JAVA_OBJECT:
                for (int i = 0; i < len; i++) {
                elements[i] = new SerialJavaObject((Object)elements[i]);
            }
        default:
            ;
        }
  }
    public void free() throws SQLException {
         throw new SQLFeatureNotSupportedException("Feature not supported");
    }
     public SerialArray(Array array) throws SerialException, SQLException {
         if (array == null) {
             throw new SQLException("Cannot instantiate a SerialArray " +
                 "object with a null Array object");
         }
         if ((elements = (Object[])array.getArray()) == null) {
             throw new SQLException("Invalid Array object. Calls to Array.getArray() " +
                 "return null value which cannot be serialized");
         }
         baseType = array.getBaseType();
         baseTypeName = array.getBaseTypeName();
         len = elements.length;
        switch (baseType) {
        case java.sql.Types.BLOB:
            for (int i = 0; i < len; i++) {
                elements[i] = new SerialBlob((Blob)elements[i]);
            }
            break;
        case java.sql.Types.CLOB:
            for (int i = 0; i < len; i++) {
                elements[i] = new SerialClob((Clob)elements[i]);
            }
            break;
        case java.sql.Types.DATALINK:
            for (int i = 0; i < len; i++) {
                elements[i] = new SerialDatalink((URL)elements[i]);
            }
            break;
        case java.sql.Types.JAVA_OBJECT:
            for (int i = 0; i < len; i++) {
                elements[i] = new SerialJavaObject((Object)elements[i]);
            }
        default:
            ;
        }
    }
    public Object getArray() throws SerialException {
        Object dst = new Object[len];
        System.arraycopy((Object)elements, 0, dst, 0, len);
        return dst;
    }
    public Object getArray(Map<String, Class<?>> map) throws SerialException {
        Object dst[] = new Object[len];
        System.arraycopy((Object)elements, 0, dst, 0, len);
        return dst;
    }
    public Object getArray(long index, int count) throws SerialException {
        Object dst = new Object[count];
        System.arraycopy((Object)elements, (int)index, dst, 0, count);
        return dst;
    }
    public Object getArray(long index, int count, Map<String,Class<?>> map)
        throws SerialException
    {
        Object dst = new Object[count];
        System.arraycopy((Object)elements, (int)index, dst, 0, count);
        return dst;
    }
    public int getBaseType() throws SerialException {
        return baseType;
    }
    public String getBaseTypeName() throws SerialException {
        return baseTypeName;
    }
    public ResultSet getResultSet(long index, int count) throws SerialException {
        throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet(Map<String, Class<?>> map)
        throws SerialException
    {
        throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet() throws SerialException {
        throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet(long index, int count,
                                  Map<String,Class<?>> map)
        throws SerialException
    {
        throw new UnsupportedOperationException();
    }
    static final long serialVersionUID = -8466174297270688520L;
}
