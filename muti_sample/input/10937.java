public class ArrayTypeSignature implements FieldTypeSignature {
    private TypeSignature componentType;
    private ArrayTypeSignature(TypeSignature ct) {componentType = ct;}
    public static ArrayTypeSignature make(TypeSignature ct) {
        return new ArrayTypeSignature(ct);
    }
    public TypeSignature getComponentType(){return componentType;}
    public void accept(TypeTreeVisitor<?> v){
        v.visitArrayTypeSignature(this);
    }
}
