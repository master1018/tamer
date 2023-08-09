public abstract class GenericDeclRepository<S extends Signature>
    extends AbstractRepository<S> {
    private TypeVariable[] typeParams; 
    protected GenericDeclRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }
    public TypeVariable[] getTypeParameters(){
        if (typeParams == null) { 
            FormalTypeParameter[] ftps = getTree().getFormalTypeParameters();
            TypeVariable[] tps = new TypeVariable[ftps.length];
            for (int i = 0; i < ftps.length; i++) {
                Reifier r = getReifier(); 
                ftps[i].accept(r); 
                tps[i] = (TypeVariable<?>) r.getResult();
            }
            typeParams = tps; 
        }
        return typeParams.clone(); 
    }
}
