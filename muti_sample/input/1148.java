public class EnclosingMethodTests {
    static Class<?> anonymousClass;
    static {
        Cloneable c = new Cloneable() {}; 
        anonymousClass = c.getClass();
    }
    EnclosingMethodTests() {}
    @MethodDescriptor("java.lang.Class EnclosingMethodTests.getLocalClass(Object o)")
    Class getLocalClass(Object o) {
        class Local {};
        Local l = new Local();
        return l.getClass();
    }
    static int examine(Class enclosedClass, String methodSig) {
        Method m = enclosedClass.getEnclosingMethod();
        if (m == null && methodSig == null)
            return 0;
        if (m != null &&
            m.getAnnotation(MethodDescriptor.class).value().equals(methodSig))
            return 0; 
        else {
            System.err.println("\nUnexpected method value; expected:\t" + methodSig +
                               "\ngot:\t" + m);
            return 1;
        }
    }
    @MethodDescriptor("public static void EnclosingMethodTests.main(java.lang.String[])")
    public static void main(String argv[]) {
        int failures = 0;
        class StaticLocal {};
        failures += examine(StaticLocal.class,
                            "public static void EnclosingMethodTests.main(java.lang.String[])");
        failures += examine( (new EnclosingMethodTests()).getLocalClass(null),
                             "java.lang.Class EnclosingMethodTests.getLocalClass(Object o)");
        failures += examine(EnclosingMethodTests.class, null);
        failures += examine(anonymousClass, null);
        if (failures > 0)
            throw new RuntimeException("Test failed.");
    }
}
@Retention(RetentionPolicy.RUNTIME)
@interface MethodDescriptor {
    String value();
}
