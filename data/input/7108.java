class QualifiedAttributeValueExp extends AttributeValueExp   {
    private static final long serialVersionUID = 8832517277410933254L;
    private String className;
    @Deprecated
    public QualifiedAttributeValueExp() {
    }
    public QualifiedAttributeValueExp(String className, String attr) {
        super(attr);
        this.className = className;
    }
    public String getAttrClassName()  {
        return className;
    }
    @Override
    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        try {
            MBeanServer server = QueryEval.getMBeanServer();
            String v = server.getObjectInstance(name).getClassName();
            if (v.equals(className)) {
                return super.apply(name);
            }
            throw new InvalidApplicationException("Class name is " + v +
                                                  ", should be " + className);
        } catch (Exception e) {
            throw new InvalidApplicationException("Qualified attribute: " + e);
        }
    }
    @Override
    public String toString()  {
        if (className != null) {
            return className + "." + super.toString();
        } else {
            return super.toString();
        }
    }
}
