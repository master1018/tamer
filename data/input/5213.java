class InstanceOfQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -1081892073854801359L;
    private StringValueExp classNameValue;
    public InstanceOfQueryExp(StringValueExp classNameValue) {
        if (classNameValue == null) {
            throw new IllegalArgumentException("Null class name.");
        }
        this.classNameValue = classNameValue;
    }
    public StringValueExp getClassNameValue()  {
        return classNameValue;
    }
    public boolean apply(ObjectName name)
        throws BadStringOperationException,
        BadBinaryOpValueExpException,
        BadAttributeValueExpException,
        InvalidApplicationException {
        final StringValueExp val;
        try {
            val = (StringValueExp) classNameValue.apply(name);
        } catch (ClassCastException x) {
            final BadStringOperationException y =
                    new BadStringOperationException(x.toString());
            y.initCause(x);
            throw y;
        }
        try {
            return getMBeanServer().isInstanceOf(name, val.getValue());
        } catch (InstanceNotFoundException infe) {
            return false;
        }
    }
    public String toString() {
       return "InstanceOf " + classNameValue.toString();
   }
}
