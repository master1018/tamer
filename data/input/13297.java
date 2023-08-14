public class JSJavaObjArray extends JSJavaArray {
    public JSJavaObjArray(ObjArray array, JSJavaFactory fac) {
        super(array, fac);
    }
    public final ObjArray getObjArray() {
        return (ObjArray) getArray();
    }
}
