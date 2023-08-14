public class ParameterDeclarationImpl extends DeclarationImpl
                                      implements ParameterDeclaration
{
    protected VarSymbol sym;
    ParameterDeclarationImpl(AptEnv env, VarSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return getType() + " " + sym.name;
    }
    public boolean equals(Object obj) {
        if (obj instanceof ParameterDeclarationImpl) {
            ParameterDeclarationImpl that = (ParameterDeclarationImpl) obj;
            return sym.owner == that.sym.owner &&
                   sym.name == that.sym.name &&
                   env == that.env;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return sym.owner.hashCode() + sym.name.hashCode() + env.hashCode();
    }
    public TypeMirror getType() {
        return env.typeMaker.getType(sym.type);
    }
    public void accept(DeclarationVisitor v) {
        v.visitParameterDeclaration(this);
    }
}
