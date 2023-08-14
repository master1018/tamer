public class DoubleSignature implements BaseType {
    private static DoubleSignature singleton = new DoubleSignature();
    private DoubleSignature(){}
    public static DoubleSignature make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){v.visitDoubleSignature(this);}
}
