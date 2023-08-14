public abstract class AbstractScope<D extends GenericDeclaration>
    implements Scope {
    private D recvr; 
    private Scope enclosingScope; 
    protected AbstractScope(D decl){ recvr = decl;}
    protected D getRecvr() {return recvr;}
    protected abstract Scope computeEnclosingScope();
    protected Scope getEnclosingScope(){
        if (enclosingScope == null) {enclosingScope = computeEnclosingScope();}
        return enclosingScope;
    }
    public TypeVariable<?> lookup(String name) {
        TypeVariable[] tas = getRecvr().getTypeParameters();
        for (TypeVariable tv : tas) {
            if (tv.getName().equals(name)) {return tv;}
        }
        return getEnclosingScope().lookup(name);
    }
}
