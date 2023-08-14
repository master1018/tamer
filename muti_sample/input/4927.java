public class ClassTypeSignature implements FieldTypeSignature {
    private List<SimpleClassTypeSignature> path;
    private ClassTypeSignature(List<SimpleClassTypeSignature> p) {
        path = p;
    }
    public static ClassTypeSignature make(List<SimpleClassTypeSignature> p) {
        return new ClassTypeSignature(p);
    }
    public List<SimpleClassTypeSignature> getPath(){return path;}
    public void accept(TypeTreeVisitor<?> v){v.visitClassTypeSignature(this);}
}
