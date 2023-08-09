public class HasMethodAnnotationTest extends TestCase {
    public void testMethodWithSpecifiedAttribute() throws Exception {
        assertTrue(methodHasAnnotation(AnnotatedMethodExample.class,
                "testThatIsAnnotated", Smoke.class));
    }
    public void testMethodWithoutSpecifiedAttribute() throws Exception {
        assertFalse(methodHasAnnotation(AnnotatedMethodExample.class,
                "testThatIsNotAnnotated", Smoke.class));
    }
    private boolean methodHasAnnotation(Class<? extends TestCase> aClass,
            String methodName,
            Class<? extends Annotation> expectedClassification
    ) throws NoSuchMethodException {
        Method method = aClass.getMethod(methodName);
        TestMethod testMethod = new TestMethod(method, aClass);
        return new HasMethodAnnotation(expectedClassification).apply(testMethod);
    }
    static class AnnotatedMethodExample extends TestCase {
        @Smoke
        public void testThatIsAnnotated() {
        }
        public void testThatIsNotAnnotated() {
        }
    }
}
