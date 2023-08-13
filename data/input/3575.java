public class ClassSignature implements Signature {
    private FormalTypeParameter[] formalTypeParams;
    private ClassTypeSignature superclass;
    private ClassTypeSignature[] superInterfaces;
    private ClassSignature(FormalTypeParameter[] ftps,
                                      ClassTypeSignature sc,
                                      ClassTypeSignature[] sis) {
        formalTypeParams = ftps;
        superclass = sc;
        superInterfaces = sis;
    }
    public static ClassSignature make(FormalTypeParameter[] ftps,
                                      ClassTypeSignature sc,
                                      ClassTypeSignature[] sis) {
        return new ClassSignature(ftps, sc, sis);
    }
    public FormalTypeParameter[] getFormalTypeParameters(){
        return formalTypeParams;
    }
    public ClassTypeSignature getSuperclass(){return superclass;}
    public ClassTypeSignature[] getSuperInterfaces(){return superInterfaces;}
    public void accept(Visitor v){v.visitClassSignature(this);}
}
