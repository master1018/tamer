public class TypeDeclarationImpl extends MemberDeclarationImpl
                                 implements TypeDeclaration {
    public ClassSymbol sym;
     protected TypeDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return toString(env, sym);
    }
    public PackageDeclaration getPackage() {
        return env.declMaker.getPackageDeclaration(sym.packge());
    }
    public String getQualifiedName() {
        return sym.toString();
    }
    public Collection<InterfaceType> getSuperinterfaces() {
        return env.typeMaker.getTypes(env.jctypes.interfaces(sym.type),
                                      InterfaceType.class);
    }
    public Collection<FieldDeclaration> getFields() {
        ArrayList<FieldDeclaration> res = new ArrayList<FieldDeclaration>();
        for (Symbol s : getMembers(true)) {
            if (s.kind == Kinds.VAR) {
                res.add(env.declMaker.getFieldDeclaration((VarSymbol) s));
            }
        }
        return res;
    }
    public Collection<? extends MethodDeclaration> getMethods() {
        ArrayList<MethodDeclaration> res = new ArrayList<MethodDeclaration>();
        for (Symbol s : getMembers(true)) {
            if (s.kind == Kinds.MTH && !s.isConstructor() &&
                !env.names.clinit.equals(s.name) ) { 
                MethodSymbol m = (MethodSymbol) s;
                res.add((MethodDeclaration)
                        env.declMaker.getExecutableDeclaration(m));
            }
        }
        return res;
    }
    public Collection<TypeDeclaration> getNestedTypes() {
        ArrayList<TypeDeclaration> res = new ArrayList<TypeDeclaration>();
        for (Symbol s : getMembers(true)) {
            if (s.kind == Kinds.TYP) {
                res.add(env.declMaker.getTypeDeclaration((ClassSymbol) s));
            }
        }
        return res;
    }
    public void accept(DeclarationVisitor v) {
        v.visitTypeDeclaration(this);
    }
    static String toString(AptEnv env, ClassSymbol c) {
        StringBuilder sb = new StringBuilder();
        if (c.isInner()) {
            ClassSymbol enclosing = c.owner.enclClass();
            sb.append(toString(env, enclosing))
              .append('.')
              .append(c.name);
        } else {
            sb.append(c);
        }
        sb.append(typeParamsToString(env, c));
        return sb.toString();
    }
}
