public class AnnotationTypeImpl extends InterfaceTypeImpl
                                implements AnnotationType {
    AnnotationTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
    }
    public AnnotationTypeDeclaration getDeclaration() {
        return (AnnotationTypeDeclaration) super.getDeclaration();
    }
    public void accept(TypeVisitor v) {
        v.visitAnnotationType(this);
    }
}
