public class AnnotationTypeDeclarationImpl extends InterfaceDeclarationImpl
                                           implements AnnotationTypeDeclaration
{
    AnnotationTypeDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }
    public Collection<AnnotationTypeElementDeclaration> getMethods() {
        return identityFilter.filter(super.getMethods(),
                                     AnnotationTypeElementDeclaration.class);
    }
    public void accept(DeclarationVisitor v) {
        v.visitAnnotationTypeDeclaration(this);
    }
}
