public class SigField extends SigAnnotatableElement implements IField,
        Serializable {
    private String name;
    private ITypeReference type = Uninitialized.unset();
    private Set<Modifier> modifiers = Uninitialized.unset();
    public SigField(String name) {
        this.name = name;
        modifiers = Collections.emptySet();
    }
    public String getName() {
        return name;
    }
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
    public ITypeReference getType() {
        return type;
    }
    public void setType(ITypeReference type) {
        this.type = type;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getAnnotations() != null && !getAnnotations().isEmpty()) {
            builder.append(super.toString());
            builder.append("\n");
        }
        builder.append(Modifier.toString(getModifiers()));
        builder.append(getType().toString());
        builder.append(" ");
        builder.append(getName());
        return builder.toString();
    }
}
