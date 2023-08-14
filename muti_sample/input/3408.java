public class ByteSignature implements BaseType {
    private static ByteSignature singleton = new ByteSignature();
    private ByteSignature(){}
    public static ByteSignature make() {return singleton;}
    public void accept(TypeTreeVisitor<?> v){
        v.visitByteSignature(this);
    }
}
