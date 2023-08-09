public class SigParameter extends SigAnnotatableElement implements IParameter,
        Serializable {
    private ITypeReference type;
    public SigParameter(ITypeReference type) {
        this.type = type;
    }
    public ITypeReference getType() {
        return type;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" ");
        builder.append(getType().toString());
        return builder.toString();
    }
}
