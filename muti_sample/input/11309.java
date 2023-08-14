class A {
    public void m(Object o) {}
}
public class NullInParamList {
    public static void main(String [] args) {
        try {
            Class [] ca = {null};
            Method m = A.class.getMethod("m", ca);
        } catch (NoSuchMethodException x) {
            return;
        }
        throw new RuntimeException("FAIL: expected NoSuchMethodException");
    }
}
