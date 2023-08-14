public class SigConstructor extends SigExecutableMember implements
        IConstructor, Serializable {
    public SigConstructor(String name) {
        super(name);
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" ");
        builder.append(Modifier.toString(getModifiers()));
        if (getTypeParameters() != null && !getTypeParameters().isEmpty()) {
            builder.append("<");
            builder.append(ModelUtil.separate(getTypeParameters(), ", "));
            builder.append("> ");
        }
        builder.append(getName());
        builder.append("(");
        builder.append(getParameters().isEmpty() ? "" : ModelUtil.separate(
                getParameters(), ", "));
        builder.append(")");
        if (getExceptions() != null && !getExceptions().isEmpty()) {
            builder.append(" throws ");
            builder.append(ModelUtil.separate(getExceptions(), " "));
        }
        return builder.toString();
    }
}
