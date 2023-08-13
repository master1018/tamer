public class AnnotationTypeUsage {
    @AnnotationType(optional="Field Annotation", required=1994)
    @AnnotationTypeUndocumented(optional="Field Annotation", required=1994)
    public int field;
    @AnnotationType(optional="Constructor Annotation", required=1994)
    @AnnotationTypeUndocumented(optional="Constructor Annotation", required=1994)
    public AnnotationTypeUsage() {}
    public AnnotationTypeUsage(
        @AnnotationType(optional="Constructor Param Annotation", required=1994) int documented,
        @AnnotationTypeUndocumented(optional="Constructor Param Annotation", required=1994) int undocmented) {}
    @AnnotationType(optional="Method Annotation", required=1994)
    @AnnotationTypeUndocumented(optional="Method Annotation", required=1994)
    public void method() {}
    public void methodWithParams(
        @AnnotationType(optional="Parameter Annotation", required=1994) int documented,
        @AnnotationTypeUndocumented(optional="Parameter Annotation", required=1994) int undocmented) {}
}
