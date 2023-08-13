public abstract class DeclarationImpl implements Declaration {
    protected final AptEnv env;
    public final Symbol sym;
    protected static final DeclarationFilter identityFilter =
            new DeclarationFilter();
    protected DeclarationImpl(AptEnv env, Symbol sym) {
        this.env = env;
        this.sym = sym;
    }
    public boolean equals(Object obj) {
        if (obj instanceof DeclarationImpl) {
            DeclarationImpl that = (DeclarationImpl) obj;
            return sym == that.sym && env == that.env;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return sym.hashCode() + env.hashCode();
    }
    public String getDocComment() {
        Env<AttrContext> enterEnv = getEnterEnv();
        if (enterEnv == null)
            return null;
        JCTree tree = TreeInfo.declarationFor(sym, enterEnv.tree);
        return enterEnv.toplevel.docComments.get(tree);
    }
    public Collection<AnnotationMirror> getAnnotationMirrors() {
        Collection<AnnotationMirror> res =
            new ArrayList<AnnotationMirror>();
        for (Attribute.Compound a : sym.getAnnotationMirrors()) {
            res.add(env.declMaker.getAnnotationMirror(a, this));
        }
        return res;
    }
    public <A extends Annotation> A getAnnotation(Class<A> annoType) {
        return getAnnotation(annoType, sym);
    }
    protected <A extends Annotation> A getAnnotation(Class<A> annoType,
                                                     Symbol annotated) {
        if (!annoType.isAnnotation()) {
            throw new IllegalArgumentException(
                                "Not an annotation type: " + annoType);
        }
        String name = annoType.getName();
        for (Attribute.Compound attr : annotated.getAnnotationMirrors()) {
            if (name.equals(attr.type.tsym.flatName().toString())) {
                return AnnotationProxyMaker.generateAnnotation(env, attr,
                                                               annoType);
            }
        }
        return null;
    }
    private EnumSet<Modifier> modifiers = null;
    public Collection<Modifier> getModifiers() {
        if (modifiers == null) {
            modifiers = EnumSet.noneOf(Modifier.class);
            long flags = AptEnv.getFlags(sym);
            if (0 != (flags & Flags.PUBLIC))       modifiers.add(PUBLIC);
            if (0 != (flags & Flags.PROTECTED))    modifiers.add(PROTECTED);
            if (0 != (flags & Flags.PRIVATE))      modifiers.add(PRIVATE);
            if (0 != (flags & Flags.ABSTRACT))     modifiers.add(ABSTRACT);
            if (0 != (flags & Flags.STATIC))       modifiers.add(STATIC);
            if (0 != (flags & Flags.FINAL))        modifiers.add(FINAL);
            if (0 != (flags & Flags.TRANSIENT))    modifiers.add(TRANSIENT);
            if (0 != (flags & Flags.VOLATILE))     modifiers.add(VOLATILE);
            if (0 != (flags & Flags.SYNCHRONIZED)) modifiers.add(SYNCHRONIZED);
            if (0 != (flags & Flags.NATIVE))       modifiers.add(NATIVE);
            if (0 != (flags & Flags.STRICTFP))     modifiers.add(STRICTFP);
        }
        return modifiers;
    }
    public String getSimpleName() {
        return sym.name.toString();
    }
    public SourcePosition getPosition() {
        Env<AttrContext> enterEnv = getEnterEnv();
        if (enterEnv == null)
            return null;
        JCTree.JCCompilationUnit toplevel = enterEnv.toplevel;
        JavaFileObject sourcefile = toplevel.sourcefile;
        if (sourcefile == null)
            return null;
        int pos = TreeInfo.positionFor(sym, toplevel);
        return new SourcePositionImpl(sourcefile, pos, toplevel.lineMap);
    }
    public void accept(DeclarationVisitor v) {
        v.visitDeclaration(this);
    }
    private Collection<Symbol> members = null;  
    protected Collection<Symbol> getMembers(boolean cache) {
        if (members != null) {
            return members;
        }
        LinkedList<Symbol> res = new LinkedList<Symbol>();
        for (Scope.Entry e = sym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null && !unwanted(e.sym)) {
                res.addFirst(e.sym);
            }
        }
        return cache ? (members = res) : res;
    }
    private static boolean unwanted(Symbol s) {
        return AptEnv.hasFlag(s, Flags.SYNTHETIC) ||
               (s.kind == TYP &&
                !DeclarationMaker.isJavaIdentifier(s.name.toString()));
    }
    private Env<AttrContext> getEnterEnv() {
        TypeSymbol ts = (sym.kind != PCK)
                        ? sym.enclClass()
                        : (PackageSymbol) sym;
        return (ts != null)
                ? env.enter.getEnv(ts)
                : null;
    }
}
