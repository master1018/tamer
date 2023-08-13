public class LongSignature implements BaseType {
    private static LongSignature singleton = new LongSignature();
    private LongSignature(){}
    public static LongSignature make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){v.visitLongSignature(this);}
}
