public final class AttRuntimeVisibleParameterAnnotations
        extends BaseParameterAnnotations {
    public static final String ATTRIBUTE_NAME =
        "RuntimeVisibleParameterAnnotations";
    public AttRuntimeVisibleParameterAnnotations(
            AnnotationsList annotations, int byteLength) {
        super(ATTRIBUTE_NAME, annotations, byteLength);
    }
}
