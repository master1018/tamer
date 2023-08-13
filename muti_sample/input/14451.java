public class AnnotationTypeElementDeclarationImpl extends MethodDeclarationImpl
                                  implements AnnotationTypeElementDeclaration {
    AnnotationTypeElementDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    public AnnotationTypeDeclaration getDeclaringType() {
        return (AnnotationTypeDeclaration) super.getDeclaringType();
    }
    public AnnotationValue getDefaultValue() {
        return (sym.defaultValue == null)
               ? null
               : new AnnotationValueImpl(env, sym.defaultValue, null);
    }
    public void accept(DeclarationVisitor v) {
        v.visitAnnotationTypeElementDeclaration(this);
    }
}
