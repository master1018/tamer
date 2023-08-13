public class ClassDeclarationImpl extends TypeDeclarationImpl
                                  implements ClassDeclaration {
    ClassDeclarationImpl(AptEnv env, ClassSymbol sym) {
        super(env, sym);
    }
    public <A extends Annotation> A getAnnotation(Class<A> annoType) {
        boolean inherited = annoType.isAnnotationPresent(Inherited.class);
        for (Type t = sym.type;
             t.tsym != env.symtab.objectType.tsym && !t.isErroneous();
             t = env.jctypes.supertype(t)) {
            A result = getAnnotation(annoType, t.tsym);
            if (result != null || !inherited) {
                return result;
            }
        }
        return null;
    }
    public ClassType getSuperclass() {
        if (sym == env.symtab.objectType.tsym) {
            return null;
        }
        Type t = env.jctypes.supertype(sym.type);
        return (ClassType) env.typeMaker.getType(t);
    }
    public Collection<ConstructorDeclaration> getConstructors() {
        ArrayList<ConstructorDeclaration> res =
            new ArrayList<ConstructorDeclaration>();
        for (Symbol s : getMembers(true)) {
            if (s.isConstructor()) {
                MethodSymbol m = (MethodSymbol) s;
                res.add((ConstructorDeclaration)
                        env.declMaker.getExecutableDeclaration(m));
            }
        }
        return res;
    }
    public Collection<MethodDeclaration> getMethods() {
        return identityFilter.filter(super.getMethods(),
                                     MethodDeclaration.class);
    }
    public void accept(DeclarationVisitor v) {
        v.visitClassDeclaration(this);
    }
}
