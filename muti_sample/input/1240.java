public class MethodRepository extends ConstructorRepository {
    private Type returnType; 
    private MethodRepository(String rawSig, GenericsFactory f) {
      super(rawSig, f);
    }
    public static MethodRepository make(String rawSig, GenericsFactory f) {
        return new MethodRepository(rawSig, f);
    }
    public Type getReturnType() {
        if (returnType == null) { 
            Reifier r = getReifier(); 
            getTree().getReturnType().accept(r);
            returnType = r.getResult();
            }
        return returnType; 
    }
}
