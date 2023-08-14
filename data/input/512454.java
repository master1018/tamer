public class SigTypeVariableDefinition implements ITypeVariableDefinition,
        Serializable {
    private String name;
    private IGenericDeclaration genericDeclaration;
    private List<ITypeReference> upperBounds = Uninitialized.unset();
    public SigTypeVariableDefinition(String name,
            IGenericDeclaration genericDeclaration) {
        this.name = name;
        this.genericDeclaration = genericDeclaration;
    }
    public String getName() {
        return name;
    }
    public IGenericDeclaration getGenericDeclaration() {
        return genericDeclaration;
    }
    public List<ITypeReference> getUpperBounds() {
        return upperBounds;
    }
    public void setUpperBounds(List<ITypeReference> upperBounds) {
        this.upperBounds = upperBounds;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        if (getUpperBounds().size() != 1) {
            builder.append(ModelUtil.separate(getUpperBounds(), ", "));
        } else {
            if (!ModelUtil.isJavaLangObject(getUpperBounds().get(0))) {
                builder.append(getUpperBounds().get(0));
            }
        }
        return builder.toString();
    }
}
