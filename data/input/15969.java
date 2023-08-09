public class JSJavaTypeArray extends JSJavaArray {
    public JSJavaTypeArray(TypeArray array, JSJavaFactory fac) {
        super(array, fac);
    }
    public final TypeArray getTypeArray() {
        return (TypeArray) getArray();
    }
}
