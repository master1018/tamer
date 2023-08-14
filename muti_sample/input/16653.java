public class IsSynthetic {
    static class NestedClass {
    }
    static int test(Class<?> clazz, boolean expected) {
        if (clazz.isSynthetic() == expected)
            return 0;
        else {
            System.err.println("Unexpected synthetic status for " +
                               clazz.getName() + " expected: " + expected +
                               " got: " + (!expected));
            return 1;
        }
    }
    public static void main(String argv[]) {
        int failures = 0;
        class LocalClass {}
        Cloneable clone = new Cloneable() {};
        failures += test(IsSynthetic.class,             false);
        failures += test(java.lang.String.class,        false);
        failures += test(LocalClass.class,              false);
        failures += test(NestedClass.class,             false);
        failures += test(clone.getClass(),              false);
        for(Constructor c: Tricky.class.getDeclaredConstructors()) {
            Class<?>[] paramTypes = c.getParameterTypes();
            if (paramTypes.length > 0) {
                System.out.println("Testing class that should be synthetic.");
                for(Class paramType: paramTypes) {
                    failures += test(paramType, true);
                }
            }
        }
        if (failures != 0)
            throw new RuntimeException("Test failed with " + failures  + " failures.");
    }
}
class Tricky {
    private Tricky() {}
    public static class Nested {
        Tricky t = new Tricky();
    }
}
