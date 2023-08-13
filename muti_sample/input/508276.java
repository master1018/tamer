public class SigMethod extends SigExecutableMember implements IMethod,
        Serializable {
    private ITypeReference returnType = Uninitialized.unset();
    public SigMethod(String name) {
        super(name);
    }
    public ITypeReference getReturnType() {
        return returnType;
    }
    public void setReturnType(ITypeReference returnType) {
        this.returnType = returnType;
    }
    @Override
    public String toString() {
        return SigMethod.toString(this);
    }
    public static String toString(IMethod method) {
        StringBuilder builder = new StringBuilder();
        builder.append(Modifier.toString(method.getModifiers()));
        builder.append(method.getReturnType());
        builder.append(" ");
        if (method.getTypeParameters() != null
                && !method.getTypeParameters().isEmpty()) {
            builder.append("<");
            builder
                    .append(ModelUtil
                            .separate(method.getTypeParameters(), ", "));
            builder.append("> ");
        }
        builder.append(method.getName());
        builder.append("(");
        builder.append(method.getParameters().isEmpty() ? "" : ModelUtil
                .separate(method.getParameters(), ", "));
        builder.append(")");
        if (method.getExceptions() != null
                && !method.getExceptions().isEmpty()) {
            builder.append(" throws ");
            builder.append(ModelUtil.separate(method.getExceptions(), " "));
        }
        return builder.toString();
    }
}
