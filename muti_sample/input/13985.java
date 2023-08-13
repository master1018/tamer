public class ConstructorRepository
    extends GenericDeclRepository<MethodTypeSignature> {
    private Type[] paramTypes; 
    private Type[] exceptionTypes; 
    protected ConstructorRepository(String rawSig, GenericsFactory f) {
      super(rawSig, f);
    }
    protected MethodTypeSignature parse(String s) {
        return SignatureParser.make().parseMethodSig(s);
    }
    public static ConstructorRepository make(String rawSig,
                                             GenericsFactory f) {
        return new ConstructorRepository(rawSig, f);
    }
    public Type[] getParameterTypes(){
        if (paramTypes == null) { 
            TypeSignature[] pts = getTree().getParameterTypes();
            Type[] ps = new Type[pts.length];
            for (int i = 0; i < pts.length; i++) {
                Reifier r = getReifier(); 
                pts[i].accept(r); 
                ps[i] = r.getResult();
            }
            paramTypes = ps; 
        }
        return paramTypes.clone(); 
    }
    public Type[] getExceptionTypes(){
        if (exceptionTypes == null) { 
            FieldTypeSignature[] ets = getTree().getExceptionTypes();
            Type[] es = new Type[ets.length];
            for (int i = 0; i < ets.length; i++) {
                Reifier r = getReifier(); 
                ets[i].accept(r); 
                es[i] = r.getResult();
            }
            exceptionTypes = es; 
        }
        return exceptionTypes.clone(); 
    }
}
