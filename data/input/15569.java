public class InterfaceDeclarationImpl extends TypeDeclarationImpl
                                      implements InterfaceDeclaration {
    InterfaceDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }
    public void accept(DeclarationVisitor v) {
        v.visitInterfaceDeclaration(this);
    }
}
