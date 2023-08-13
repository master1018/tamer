public class EnumDeclarationImpl extends ClassDeclarationImpl
                                 implements EnumDeclaration {
    EnumDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }
    public Collection<EnumConstantDeclaration> getEnumConstants() {
        return identityFilter.filter(getFields(),
                                     EnumConstantDeclaration.class);
    }
    public void accept(DeclarationVisitor v) {
        v.visitEnumDeclaration(this);
    }
}
