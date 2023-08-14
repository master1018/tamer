public class ConstructorDeclarationImpl extends ExecutableDeclarationImpl
                                        implements ConstructorDeclaration {
    ConstructorDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    public String getSimpleName() {
        return sym.enclClass().name.toString();
    }
    public void accept(DeclarationVisitor v) {
        v.visitConstructorDeclaration(this);
    }
}
