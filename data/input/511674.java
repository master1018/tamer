public class SigAnnotationField extends SigField implements IAnnotationField,
        Serializable {
    private Object defaultValue = Uninitialized.unset();
    public SigAnnotationField(String name) {
        super(name);
    }
    public Object getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
