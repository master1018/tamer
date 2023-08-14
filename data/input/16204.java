public class MethodDeclarationImpl extends ExecutableDeclarationImpl
                                   implements MethodDeclaration {
    MethodDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    public TypeMirror getReturnType() {
        return env.typeMaker.getType(sym.type.getReturnType());
    }
    public void accept(DeclarationVisitor v) {
        v.visitMethodDeclaration(this);
    }
}
