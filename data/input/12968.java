public class EnclosingClassTest {
    static void info(Class<?> c, Class<?> encClass, String desc) {
        if (!"".equals(desc))
            System.out.println(desc + ":");
        System.out.println(c);
        System.out.println("\tis enclosed by:\t\t" + encClass);
        System.out.println("\thas simple name:\t`" +
                           c.getSimpleName() + "'");
        System.out.println("\thas canonical name:\t`" +
                           c.getCanonicalName() + "'");
    }
    static void match(String actual, String expected) {
        assert((actual == null && expected == null) || actual.equals(expected));
        System.out.println("\t`" +
                           actual + "' matches expected `" +
                           expected + "'");
    }
    static void check(Class<?> c, Class<?> enc,
                      String encName, String encNameExpected,
                      String simpleName, String simpleNameExpected,
                      String canonicalName, String canonicalNameExpected) {
        match(encName, encNameExpected);
        match(simpleName, simpleNameExpected);
        match(canonicalName, canonicalNameExpected);
    }
    static void testClass(Class<?> c, TestMe annotation, Field f) {
        if (Void.class.equals(c))
            return;
        Class<?> encClass = c.getEnclosingClass();
        c.getEnclosingMethod(); 
        c.getEnclosingConstructor(); 
        info(c, encClass, annotation.desc());
        check(c, encClass,
              ""+encClass, annotation.encl(),
              c.getSimpleName(), annotation.simple(),
              c.getCanonicalName(),
              annotation.hasCanonical() ? annotation.canonical() : null);
        if (void.class.equals(c))
            return;
        Class<?> array = java.lang.reflect.Array.newInstance(c, 0).getClass();
        check(array, array.getEnclosingClass(),
              "", "",
              array.getSimpleName(), annotation.simple()+"[]",
              array.getCanonicalName(),
              annotation.hasCanonical() ? annotation.canonical()+"[]" : null);
    }
    static void test(Object tests) {
        for (Field f : tests.getClass().getFields()) {
            TestMe annotation = f.getAnnotation(TestMe.class);
            if (annotation != null) {
                try {
                    testClass((Class<?>)f.get(tests), annotation, f);
                } catch (AssertionError ex) {
                    System.err.println("Error in " +
                                       tests.getClass().getName() +
                                       "." + f.getName());
                    throw ex;
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    public static void main(String[] args) {
        test(new EnclosingClass());
        test(new pkg1.EnclosingClass());
        test(new pkg1.pkg2.EnclosingClass());
    }
}
