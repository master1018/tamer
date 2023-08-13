public class SigAnnotationElement implements IAnnotationElement, Serializable {
    private IAnnotationField declaringField;
    private Object value;
    public IAnnotationField getDeclaringField() {
        return declaringField;
    }
    public void setDeclaringField(IAnnotationField declaringField) {
        this.declaringField = declaringField;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getDeclaringField().getName());
        builder.append(" = ");
        builder.append(getValue());
        return builder.toString();
    }
}
