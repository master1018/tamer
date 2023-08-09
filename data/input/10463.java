public class AttrContext {
    Scope scope = null;
    int staticLevel = 0;
    boolean isSelfCall = false;
    boolean selectSuper = false;
    boolean varArgs = false;
    List<Type> tvars = List.nil();
    Lint lint;
    Symbol enclVar = null;
    AttrContext dup(Scope scope) {
        AttrContext info = new AttrContext();
        info.scope = scope;
        info.staticLevel = staticLevel;
        info.isSelfCall = isSelfCall;
        info.selectSuper = selectSuper;
        info.varArgs = varArgs;
        info.tvars = tvars;
        info.lint = lint;
        info.enclVar = enclVar;
        return info;
    }
    AttrContext dup() {
        return dup(scope);
    }
    public Iterable<Symbol> getLocalElements() {
        if (scope == null)
            return List.nil();
        return scope.getElements();
    }
    public String toString() {
        return "AttrContext[" + scope.toString() + "]";
    }
}
