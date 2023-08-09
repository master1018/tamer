public class EnumConstantDeclarationImpl extends FieldDeclarationImpl
                                         implements EnumConstantDeclaration {
    EnumConstantDeclarationImpl(AptEnv env, VarSymbol sym) {
        super(env, sym);
    }
    public EnumDeclaration getDeclaringType() {
        return (EnumDeclaration) super.getDeclaringType();
    }
    public void accept(DeclarationVisitor v) {
        v.visitEnumConstantDeclaration(this);
    }
}
