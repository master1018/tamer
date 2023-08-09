public class AnnotationTypeDocImpl
        extends ClassDocImpl implements AnnotationTypeDoc {
    AnnotationTypeDocImpl(DocEnv env, ClassSymbol sym) {
        this(env, sym, null, null, null);
    }
    AnnotationTypeDocImpl(DocEnv env, ClassSymbol sym,
                          String doc, JCClassDecl tree, Position.LineMap lineMap) {
        super(env, sym, doc, tree, lineMap);
    }
    public boolean isAnnotationType() {
        return !isInterface();
    }
    public boolean isInterface() {
        return env.legacyDoclet;
    }
    public MethodDoc[] methods(boolean filter) {
        return env.legacyDoclet
                ? (MethodDoc[])elements()
                : new MethodDoc[0];
    }
    public AnnotationTypeElementDoc[] elements() {
        Names names = tsym.name.table.names;
        List<AnnotationTypeElementDoc> elements = List.nil();
        for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null && e.sym.kind == Kinds.MTH) {
                MethodSymbol s = (MethodSymbol)e.sym;
                elements = elements.prepend(env.getAnnotationTypeElementDoc(s));
            }
        }
        return
            elements.toArray(new AnnotationTypeElementDoc[elements.length()]);
    }
}
