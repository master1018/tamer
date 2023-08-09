public class SigAnnotation implements IAnnotation, Serializable {
    private Set<IAnnotationElement> elements;
    private IClassReference type;
    public SigAnnotation() {
        elements = Collections.emptySet();
    }
    public IClassReference getType() {
        return type;
    }
    public void setType(IClassReference type) {
        this.type = type;
    }
    public Set<IAnnotationElement> getElements() {
        return elements;
    }
    public void setElements(Set<IAnnotationElement> elements) {
        this.elements = elements;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("@");
        builder.append(getType());
        if (!getElements().isEmpty()) {
            builder.append("{");
            for (IAnnotationElement element : getElements()) {
                builder.append("\n");
                builder.append(element.toString());
            }
            builder.append("}");
        }
        return builder.toString();
    }
}
