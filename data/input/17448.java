public class EnclosingConstructorTests {
    static Class<?> anonymousClass;
    static Class<?> localClass;
    static Class<?> anotherLocalClass;
    static {
        Cloneable c = new Cloneable() {}; 
        anonymousClass = c.getClass();
    }
    @ConstructorDescriptor("EnclosingConstructorTests()")
    EnclosingConstructorTests() {
        class Local {};
        Local l = new Local();
        localClass = l.getClass();
    }
    @ConstructorDescriptor("private EnclosingConstructorTests(int)")
    private EnclosingConstructorTests(int i) {
        class Local {};
        Local l = new Local();
        anotherLocalClass = l.getClass();
    }
    static int examine(Class<?> enclosedClass, String constructorSig) {
        Constructor<?> c = enclosedClass.getEnclosingConstructor();
        if (c == null && constructorSig == null)
            return 0;
        if (c != null &&
            c.getAnnotation(ConstructorDescriptor.class).value().equals(constructorSig))
            return 0; 
        else {
            System.err.println("\nUnexpected constructor value; expected:\t" + constructorSig +
                               "\ngot:\t" + c);
            return 1;
        }
    }
    public static void main(String argv[]) {
        int failures = 0;
        class StaticLocal {};
        EnclosingConstructorTests ect = new EnclosingConstructorTests();
        ect = new EnclosingConstructorTests(5);
        failures += examine(StaticLocal.class,
                            null);
        failures += examine(localClass,
                             "EnclosingConstructorTests()");
        failures += examine(anotherLocalClass,
                            "private EnclosingConstructorTests(int)");
        failures += examine(anonymousClass,
                            null);
        if (failures > 0)
            throw new RuntimeException("Test failed.");
    }
}
@Retention(RetentionPolicy.RUNTIME)
@interface ConstructorDescriptor {
    String value();
}
