public abstract class ExecutableDeclarationImpl extends MemberDeclarationImpl
                                             implements ExecutableDeclaration {
    public MethodSymbol sym;
    protected ExecutableDeclarationImpl(AptEnv env, MethodSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return sym.toString();
    }
    public boolean isVarArgs() {
        return AptEnv.hasFlag(sym, Flags.VARARGS);
    }
    public Collection<ParameterDeclaration> getParameters() {
        Collection<ParameterDeclaration> res =
            new ArrayList<ParameterDeclaration>();
        for (VarSymbol param : sym.params())
            res.add(env.declMaker.getParameterDeclaration(param));
        return res;
    }
    public Collection<ReferenceType> getThrownTypes() {
        ArrayList<ReferenceType> res = new ArrayList<ReferenceType>();
        for (Type t : sym.type.getThrownTypes()) {
            res.add((ReferenceType) env.typeMaker.getType(t));
        }
        return res;
    }
}
