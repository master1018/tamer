public abstract class SigAnnotatableElement implements IAnnotatableElement,
        Serializable {
    private Set<IAnnotation> annotations;
    public SigAnnotatableElement() {
        annotations = Collections.emptySet();
    }
    public Set<IAnnotation> getAnnotations() {
        return annotations;
    }
    public void setAnnotations(Set<IAnnotation> annotations) {
        this.annotations = annotations;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (IAnnotation annotation : getAnnotations()) {
            builder.append(annotation);
        }
        return builder.toString();
    }
}
