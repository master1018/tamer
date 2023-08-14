public class TypeParameterDeclarationImpl extends DeclarationImpl
                                          implements TypeParameterDeclaration
{
    protected TypeSymbol sym;
    TypeParameterDeclarationImpl(AptEnv env, TypeSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return toString(env, (Type.TypeVar) sym.type);
    }
    public Collection<ReferenceType> getBounds() {
        ArrayList<ReferenceType> res = new ArrayList<ReferenceType>();
        for (Type t : env.jctypes.getBounds((Type.TypeVar) sym.type)) {
            res.add((ReferenceType) env.typeMaker.getType(t));
        }
        return res;
    }
    public Declaration getOwner() {
        Symbol owner = sym.owner;
        return ((owner.kind & Kinds.TYP) != 0)
               ? env.declMaker.getTypeDeclaration((ClassSymbol) owner)
               : env.declMaker.getExecutableDeclaration((MethodSymbol) owner);
    }
    public void accept(DeclarationVisitor v) {
        v.visitTypeParameterDeclaration(this);
    }
    static String toString(AptEnv env, Type.TypeVar tv) {
        StringBuilder s = new StringBuilder();
        s.append(tv);
        boolean first = true;
        for (Type bound : getExtendsBounds(env, tv)) {
            s.append(first ? " extends " : " & ");
            s.append(env.typeMaker.typeToString(bound));
            first = false;
        }
        return s.toString();
    }
    private static Iterable<Type> getExtendsBounds(AptEnv env,
                                                   Type.TypeVar tv) {
        return (tv.getUpperBound().tsym == env.symtab.objectType.tsym)
               ? com.sun.tools.javac.util.List.<Type>nil()
               : env.jctypes.getBounds(tv);
    }
}
