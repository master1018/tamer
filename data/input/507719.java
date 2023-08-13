public class HasAnnotationTest extends TestCase {
    public void testThatMethodWithAnnotationIsReportedAsBeingAnnotated() throws Exception {
        assertTrue(hasExampleAnnotation(ClassWithAnnotation.class, "testWithAnnotation"));
        assertTrue(hasExampleAnnotation(ClassWithoutAnnotation.class, "testWithAnnotation"));
    }
    public void testThatMethodWithOutAnnotationIsNotReportedAsBeingAnnotated() throws Exception {
        assertFalse(hasExampleAnnotation(ClassWithoutAnnotation.class, "testWithoutAnnotation"));
    }
    public void testThatClassAnnotatioCausesAllMethodsToBeReportedAsBeingAnnotated()
            throws Exception {
        assertTrue(hasExampleAnnotation(ClassWithAnnotation.class, "testWithoutAnnotation"));
    }
    private boolean hasExampleAnnotation(Class<? extends TestCase> aClass, String methodName)
            throws NoSuchMethodException {
        Method method = aClass.getMethod(methodName);
        TestMethod testMethod = new TestMethod(method, aClass);
        return new HasAnnotation(Example.class).apply(testMethod);
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Example {
    }
    @Example
    static class ClassWithAnnotation extends TestCase {
        @Example
        public void testWithAnnotation() {
        }
        public void testWithoutAnnotation() {
        }
    }
    static class ClassWithoutAnnotation extends TestCase {
        @Example
        public void testWithAnnotation() {
        }
        public void testWithoutAnnotation() {
        }
    }
}
