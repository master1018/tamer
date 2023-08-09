public class ClassRepository extends GenericDeclRepository<ClassSignature> {
    private Type superclass; 
    private Type[] superInterfaces; 
    private ClassRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }
    protected ClassSignature parse(String s) {
        return SignatureParser.make().parseClassSig(s);
    }
    public static ClassRepository make(String rawSig, GenericsFactory f) {
        return new ClassRepository(rawSig, f);
    }
    public Type getSuperclass(){
        if (superclass == null) { 
            Reifier r = getReifier(); 
            getTree().getSuperclass().accept(r);
            superclass = r.getResult();
            }
        return superclass; 
    }
    public Type[] getSuperInterfaces(){
        if (superInterfaces == null) { 
            TypeTree[] ts  = getTree().getSuperInterfaces();
            Type[] sis = new Type[ts.length];
            for (int i = 0; i < ts.length; i++) {
                Reifier r = getReifier(); 
                ts[i].accept(r);
                sis[i] = r.getResult();
            }
            superInterfaces = sis; 
        }
        return superInterfaces.clone(); 
    }
}
