public abstract class AbstractAnnotationValueVisitor6<R, P>
    implements AnnotationValueVisitor<R, P> {
    protected AbstractAnnotationValueVisitor6() {}
    public final R visit(AnnotationValue av, P p) {
        return av.accept(this, p);
    }
    public final R visit(AnnotationValue av) {
        return av.accept(this, null);
    }
    public R visitUnknown(AnnotationValue av, P p) {
        throw new UnknownAnnotationValueException(av, p);
    }
}
