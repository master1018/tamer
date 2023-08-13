public class MethodDocImpl
        extends ExecutableMemberDocImpl implements MethodDoc {
    public MethodDocImpl(DocEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    public MethodDocImpl(DocEnv env, MethodSymbol sym,
                         String docComment, JCMethodDecl tree, Position.LineMap lineMap) {
        super(env, sym, docComment, tree, lineMap);
    }
    public boolean isMethod() {
        return true;
    }
    public boolean isAbstract() {
        if (containingClass().isInterface()) {
            return false;
        }
        return Modifier.isAbstract(getModifiers());
    }
    public com.sun.javadoc.Type returnType() {
        return TypeMaker.getType(env, sym.type.getReturnType(), false);
    }
    public ClassDoc overriddenClass() {
        com.sun.javadoc.Type t = overriddenType();
        return (t != null) ? t.asClassDoc() : null;
    }
    public com.sun.javadoc.Type overriddenType() {
        if ((sym.flags() & Flags.STATIC) != 0) {
            return null;
        }
        ClassSymbol origin = (ClassSymbol)sym.owner;
        for (Type t = env.types.supertype(origin.type);
             t.tag == TypeTags.CLASS;
             t = env.types.supertype(t)) {
            ClassSymbol c = (ClassSymbol)t.tsym;
            for (Scope.Entry e = c.members().lookup(sym.name); e.scope != null; e = e.next()) {
                if (sym.overrides(e.sym, origin, env.types, true)) {
                    return TypeMaker.getType(env, t);
                }
            }
        }
        return null;
    }
    public MethodDoc overriddenMethod() {
        if ((sym.flags() & Flags.STATIC) != 0) {
            return null;
        }
        ClassSymbol origin = (ClassSymbol)sym.owner;
        for (Type t = env.types.supertype(origin.type);
             t.tag == TypeTags.CLASS;
             t = env.types.supertype(t)) {
            ClassSymbol c = (ClassSymbol)t.tsym;
            for (Scope.Entry e = c.members().lookup(sym.name); e.scope != null; e = e.next()) {
                if (sym.overrides(e.sym, origin, env.types, true)) {
                    return env.getMethodDoc((MethodSymbol)e.sym);
                }
            }
        }
        return null;
    }
    public boolean overrides(MethodDoc meth) {
        MethodSymbol overridee = ((MethodDocImpl) meth).sym;
        ClassSymbol origin = (ClassSymbol) sym.owner;
        return sym.name == overridee.name &&
               sym != overridee &&
               !sym.isStatic() &&
               env.types.asSuper(origin.type, overridee.owner) != null &&
               sym.overrides(overridee, origin, env.types, false);
    }
    public String name() {
        return sym.name.toString();
    }
    public String qualifiedName() {
        return sym.enclClass().getQualifiedName() + "." + sym.name;
    }
    public String toString() {
        return sym.enclClass().getQualifiedName() +
                "." + typeParametersString() + name() + signature();
    }
}
