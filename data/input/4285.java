public class FieldRepository extends AbstractRepository<TypeSignature> {
    private Type genericType; 
    protected FieldRepository(String rawSig, GenericsFactory f) {
      super(rawSig, f);
    }
    protected TypeSignature parse(String s) {
        return SignatureParser.make().parseTypeSig(s);
    }
    public static FieldRepository make(String rawSig,
                                             GenericsFactory f) {
        return new FieldRepository(rawSig, f);
    }
    public Type getGenericType(){
        if (genericType == null) { 
            Reifier r = getReifier(); 
            getTree().accept(r); 
            genericType = r.getResult();
        }
        return genericType; 
    }
}
