public class JSJavaString extends JSJavaInstance {
    public JSJavaString(Instance instance, JSJavaFactory fac) {
        super(instance, fac);
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("String (address=");
        buf.append(getOop().getHandle());
        buf.append(", value=");
        buf.append("'");
        buf.append(getString());
        buf.append('\'');
        buf.append(')');
        return buf.toString();
    }
    protected Object getFieldValue(String name) {
        if (name.equals("stringValue")) {
            return getString();
        } else {
            return super.getFieldValue(name);
        }
    }
    protected String[] getFieldNames() {
        String[] fields = super.getFieldNames();
        String[] res = new String[fields.length + 1];
        System.arraycopy(fields, 0, res, 0, fields.length);
        res[fields.length] = "stringValue";
        return res;
    }
    protected boolean hasField(String name) {
        if (name.equals("stringValue")) {
            return true;
        } else {
            return super.hasField(name);
        }
    }
    private String getString() {
        return OopUtilities.stringOopToString(getOop());
    }
}
