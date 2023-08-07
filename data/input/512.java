public class PropertyTag extends ComponentTagSupport {
    private static final long serialVersionUID = -4233662078530349600L;
    protected String value;
    protected Object rawvalue;
    protected String defaultValue;
    protected String format;
    protected String escape;
    public Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
        return new Property(stack);
    }
    protected void populateParams() {
        super.populateParams();
        Property p = (Property) component;
        p.setDefault(defaultValue);
        p.setRawvalue(rawvalue);
        p.setValue(value);
        p.setFormat(format);
        p.setEscape(escape);
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setRawvalue(Object rawvalue) {
        this.rawvalue = rawvalue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public void setEscape(String escape) {
        this.escape = escape;
    }
}
