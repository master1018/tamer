public abstract class MemberDeclarationImpl extends DeclarationImpl
                                            implements MemberDeclaration {
    protected MemberDeclarationImpl(AptEnv env, Symbol sym) {
        super(env, sym);
    }
    public TypeDeclaration getDeclaringType() {
        ClassSymbol c = getDeclaringClassSymbol();
        return (c == null)
            ? null
            : env.declMaker.getTypeDeclaration(c);
    }
    public Collection<TypeParameterDeclaration> getFormalTypeParameters() {
        ArrayList<TypeParameterDeclaration> res =
            new ArrayList<TypeParameterDeclaration>();
        for (Type t : sym.type.getTypeArguments()) {
            res.add(env.declMaker.getTypeParameterDeclaration(t.tsym));
        }
        return res;
    }
    public void accept(DeclarationVisitor v) {
        v.visitMemberDeclaration(this);
    }
    private ClassSymbol getDeclaringClassSymbol() {
        return sym.owner.enclClass();
    }
    protected static String typeParamsToString(AptEnv env, Symbol sym) {
        if (sym.type.getTypeArguments().isEmpty()) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for (Type t : sym.type.getTypeArguments()) {
            Type.TypeVar tv = (Type.TypeVar) t;
            s.append(s.length() == 0 ? "<" : ", ")
             .append(TypeParameterDeclarationImpl.toString(env, tv));
        }
        s.append(">");
        return s.toString();
    }
}
