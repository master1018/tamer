public class Test7002666 {
    public static void main(String[] args) {
        for (int i = 0; i < 25000; i++) {
            Object[] a = test(Test7002666.class, new Test7002666());
            if (a[0] != null) {
                System.err.println(a[0]);
                throw new InternalError(a[0].toString());
            }
        }
    }
    public static Object[] test(Class c, Object o) {
        Object[] a = (Object[])java.lang.reflect.Array.newInstance(c, 1);
        return a;
    }
}
