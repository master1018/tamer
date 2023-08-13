public class FloatSignature implements BaseType {
    private static FloatSignature singleton = new FloatSignature();
    private FloatSignature(){}
    public static FloatSignature make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){v.visitFloatSignature(this);}
}
