public class DefaultAccessibility {
    private DefaultAccessibility() {
        super();
    }
    private static int f = 42;
    public static void main(String... args) throws Exception {
        Class<?> daClass = (new DefaultAccessibility()).getClass();
        int elementCount = 0;
        for(Constructor<?> ctor : daClass.getDeclaredConstructors()) {
            elementCount++;
            if (ctor.isAccessible())
                throw new RuntimeException("Unexpected accessibility for constructor " +
                                           ctor);
        }
        for(Method method : daClass.getDeclaredMethods()) {
            elementCount++;
            if (method.isAccessible())
                throw new RuntimeException("Unexpected accessibility for method " +
                                           method);
        }
        for(Field field : daClass.getDeclaredFields()) {
            elementCount++;
            if (field.isAccessible())
                throw new RuntimeException("Unexpected accessibility for field " +
                                           field);
        }
        if (elementCount < 3)
            throw new RuntimeException("Expected at least three members; only found " +
                                       elementCount);
    }
}
