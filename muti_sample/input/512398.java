public final class ImplForVariable<D extends GenericDeclaration> 
        implements TypeVariable<D> {
    private ImplForVariable<D> formalVar;
    private final GenericDeclaration declOfVarUser;
    private final String name;
    private D genericDeclaration;
    private ListOfTypes bounds;
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TypeVariable)) {
            return false;
        }
        TypeVariable<?> that = (TypeVariable<?>) o;
        return getName().equals(that.getName()) && 
                getGenericDeclaration().equals(that.getGenericDeclaration()); 
    }
    @Override 
    public int hashCode() {
        return 31 * getName().hashCode() + getGenericDeclaration().hashCode();
    }
    ImplForVariable(D genericDecl, String name, ListOfTypes bounds) {
        this.genericDeclaration = genericDecl;
        this.name = name;
        this.bounds = bounds;
        this.formalVar = this;
        this.declOfVarUser = null;
    }
    ImplForVariable(D genericDecl, String name) {
        this.name = name;
        this.declOfVarUser = genericDecl;
    }
    static TypeVariable findFormalVar(GenericDeclaration layer, String name) {
        TypeVariable[] formalVars = layer.getTypeParameters();
        for (TypeVariable var : formalVars) {
            if (name.equals(var.getName())) {
                return var;
            }
        }
        return null;
    }
    static GenericDeclaration nextLayer(GenericDeclaration decl) {
            if (decl instanceof Class) {
                Class cl = (Class)decl;
                decl = cl.getEnclosingMethod(); 
                if (decl != null) {
                    return decl;
                }
                decl = cl.getEnclosingConstructor(); 
                if (decl != null) {
                    return decl;
                }
                return cl.getEnclosingClass();
            } else if (decl instanceof Method) {
                return ((Method)decl).getDeclaringClass();
            } else if (decl instanceof Constructor) {
                return ((Constructor)decl).getDeclaringClass();
            }
            throw new RuntimeException("unknown GenericDeclaration2: " 
                    + decl.toString());
    }
    void resolve() {
        if (formalVar == null) {
            GenericDeclaration curLayer = declOfVarUser;
            TypeVariable var = null;
            do {
                var = findFormalVar(curLayer, name);
                if (var != null) break;
                else {
                    curLayer = nextLayer(curLayer);
                    if (curLayer == null) break; 
                }
            } while (true);
            formalVar = (ImplForVariable<D>)var;
            this.genericDeclaration = formalVar.genericDeclaration;
            this.bounds = formalVar.bounds;
        }
    }
    public Type[] getBounds() {
        resolve();
        return bounds.getResolvedTypes().clone();
    }
    public D getGenericDeclaration() {
        resolve();
        return genericDeclaration;
    }
    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return name;
    }
}
