public abstract class ProgramElementDocImpl
        extends DocImpl implements ProgramElementDoc {
    private final Symbol sym;
    JCTree tree = null;
    Position.LineMap lineMap = null;
    private int modifiers = -1;
    protected ProgramElementDocImpl(DocEnv env, Symbol sym,
                                    String doc, JCTree tree, Position.LineMap lineMap) {
        super(env, doc);
        this.sym = sym;
        this.tree = tree;
        this.lineMap = lineMap;
    }
    void setTree(JCTree tree) {
        this.tree = tree;
    }
    protected abstract ClassSymbol getContainingClass();
    abstract protected long getFlags();
    protected int getModifiers() {
        if (modifiers == -1) {
            modifiers = DocEnv.translateModifiers(getFlags());
        }
        return modifiers;
    }
    public ClassDoc containingClass() {
        if (getContainingClass() == null) {
            return null;
        }
        return env.getClassDoc(getContainingClass());
    }
    public PackageDoc containingPackage() {
        return env.getPackageDoc(getContainingClass().packge());
    }
    public int modifierSpecifier() {
        int modifiers = getModifiers();
        if (isMethod() && containingClass().isInterface())
            return modifiers & ~Modifier.ABSTRACT;
        return modifiers;
    }
    public String modifiers() {
        int modifiers = getModifiers();
        if (isAnnotationTypeElement() ||
                (isMethod() && containingClass().isInterface())) {
            return Modifier.toString(modifiers & ~Modifier.ABSTRACT);
        } else {
            return Modifier.toString(modifiers);
        }
    }
    public AnnotationDesc[] annotations() {
        AnnotationDesc res[] = new AnnotationDesc[sym.getAnnotationMirrors().length()];
        int i = 0;
        for (Attribute.Compound a : sym.getAnnotationMirrors()) {
            res[i++] = new AnnotationDescImpl(env, a);
        }
        return res;
    }
    public boolean isPublic() {
        int modifiers = getModifiers();
        return Modifier.isPublic(modifiers);
    }
    public boolean isProtected() {
        int modifiers = getModifiers();
        return Modifier.isProtected(modifiers);
    }
    public boolean isPrivate() {
        int modifiers = getModifiers();
        return Modifier.isPrivate(modifiers);
    }
    public boolean isPackagePrivate() {
        return !(isPublic() || isPrivate() || isProtected());
    }
    public boolean isStatic() {
        int modifiers = getModifiers();
        return Modifier.isStatic(modifiers);
    }
    public boolean isFinal() {
        int modifiers = getModifiers();
        return Modifier.isFinal(modifiers);
    }
    CollationKey generateKey() {
        String k = name();
        return env.doclocale.collator.getCollationKey(k);
    }
}
