public abstract class BaseParameterAnnotations extends BaseAttribute {
    private final AnnotationsList parameterAnnotations;
    private final int byteLength;
    public BaseParameterAnnotations(String attributeName,
            AnnotationsList parameterAnnotations, int byteLength) {
        super(attributeName);
        try {
            if (parameterAnnotations.isMutable()) {
                throw new MutabilityException(
                        "parameterAnnotations.isMutable()");
            }
        } catch (NullPointerException ex) {
            throw new NullPointerException("parameterAnnotations == null");
        }
        this.parameterAnnotations = parameterAnnotations;
        this.byteLength = byteLength;
    }
    public final int byteLength() {
        return byteLength + 6;
    }
    public final AnnotationsList getParameterAnnotations() {
        return parameterAnnotations;
    }
}
