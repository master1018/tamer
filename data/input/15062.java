class FieldDeclarationImpl extends MemberDeclarationImpl
                                  implements FieldDeclaration {
    protected VarSymbol sym;
    FieldDeclarationImpl(AptEnv env, VarSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return getSimpleName();
    }
    public TypeMirror getType() {
        return env.typeMaker.getType(sym.type);
    }
    public Object getConstantValue() {
        Object val = sym.getConstValue();
        return Constants.decodeConstant(val, sym.type);
    }
    public String getConstantExpression() {
        Object val = getConstantValue();
        if (val == null) {
            return null;
        }
        Constants.Formatter fmtr = Constants.getFormatter();
        fmtr.append(val);
        return fmtr.toString();
    }
    public void accept(DeclarationVisitor v) {
        v.visitFieldDeclaration(this);
    }
}
