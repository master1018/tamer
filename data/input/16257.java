public class CharSignature implements BaseType {
    private static CharSignature singleton = new CharSignature();
    private CharSignature(){}
    public static CharSignature make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){
        v.visitCharSignature(this);
    }
}
