public class SerialRef implements Ref, Serializable, Cloneable {
    private String baseTypeName;
    private Object object;
    private Ref reference;
    public SerialRef(Ref ref) throws SerialException, SQLException {
        if (ref == null) {
            throw new SQLException("Cannot instantiate a SerialRef object " +
                "with a null Ref object");
        }
        reference = ref;
        object = ref;
        if (ref.getBaseTypeName() == null) {
            throw new SQLException("Cannot instantiate a SerialRef object " +
                "that returns a null base type name");
        } else {
            baseTypeName = ref.getBaseTypeName();
        }
    }
    public String getBaseTypeName() throws SerialException {
        return baseTypeName;
    }
    public Object getObject(java.util.Map<String,Class<?>> map)
        throws SerialException
    {
        map = new Hashtable(map);
        if (object != null) {
            return map.get(object);
        } else {
            throw new SerialException("The object is not set");
        }
    }
    public Object getObject() throws SerialException {
        if (reference != null) {
            try {
                return reference.getObject();
            } catch (SQLException e) {
                throw new SerialException("SQLException: " + e.getMessage());
            }
        }
        if (object != null) {
            return object;
        }
        throw new SerialException("The object is not set");
    }
    public void setObject(Object obj) throws SerialException {
        try {
            reference.setObject(obj);
        } catch (SQLException e) {
            throw new SerialException("SQLException: " + e.getMessage());
        }
        object = obj;
    }
    static final long serialVersionUID = -4727123500609662274L;
}
