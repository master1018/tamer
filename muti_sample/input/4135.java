class ClassAttributeValueExp extends AttributeValueExp {
    private static final long oldSerialVersionUID = -2212731951078526753L;
    private static final long newSerialVersionUID = -1081892073854801359L;
    private static final long serialVersionUID;
    static {
        boolean compat = false;
        try {
            GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
            String form = AccessController.doPrivileged(act);
            compat = (form != null && form.equals("1.0"));
        } catch (Exception e) {
        }
        if (compat)
            serialVersionUID = oldSerialVersionUID;
        else
            serialVersionUID = newSerialVersionUID;
    }
    private String attr;
    public ClassAttributeValueExp() {
        super("Class");
        attr = "Class";
    }
    public ValueExp apply(ObjectName name)
            throws BadStringOperationException, BadBinaryOpValueExpException,
                   BadAttributeValueExpException, InvalidApplicationException {
        Object result = getValue(name);
        if  (result instanceof String) {
            return new StringValueExp((String)result);
        } else {
            throw new BadAttributeValueExpException(result);
        }
    }
    public String toString()  {
        return attr;
    }
    protected Object getValue(ObjectName name) {
        try {
            MBeanServer server = QueryEval.getMBeanServer();
            return server.getObjectInstance(name).getClassName();
        } catch (Exception re) {
            return null;
        }
    }
}
