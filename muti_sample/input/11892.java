public class ConstructorDocImpl
        extends ExecutableMemberDocImpl implements ConstructorDoc {
    public ConstructorDocImpl(DocEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    public ConstructorDocImpl(DocEnv env, MethodSymbol sym,
                              String docComment, JCMethodDecl tree, Position.LineMap lineMap) {
        super(env, sym, docComment, tree, lineMap);
    }
    public boolean isConstructor() {
        return true;
    }
    public String name() {
        ClassSymbol c = sym.enclClass();
        String n = c.name.toString();
        for (c = c.owner.enclClass(); c != null; c = c.owner.enclClass()) {
            n = c.name.toString() + "." + n;
        }
        return n;
    }
    public String qualifiedName() {
        return sym.enclClass().getQualifiedName().toString();
    }
    public String toString() {
        return typeParametersString() + qualifiedName() + signature();
    }
}
