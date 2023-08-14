public class StringValueExp implements ValueExp   {
    private static final long serialVersionUID = -3256390509806284044L;
    private String val;
    public StringValueExp() {
    }
    public StringValueExp(String val) {
        this.val = val;
    }
    public String getValue()  {
        return val;
    }
    public String toString()  {
        return "'" + val.replace("'", "''") + "'";
    }
    @Deprecated
    public void setMBeanServer(MBeanServer s)  { }
    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        return this;
    }
 }
