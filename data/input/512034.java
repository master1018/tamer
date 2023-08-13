public class HasClassAnnotationTest extends TestCase {
    public void testShouldTellIfParentClassHasSpecifiedClassification()
            throws NoSuchMethodException {
        assertTrue(classHasAnnotation(SmokeTestExample.class, Smoke.class));
    }
    public void testShouldTellIfParentClassDoesNotHaveSpecifiedClassification()
            throws NoSuchMethodException {
        assertFalse(classHasAnnotation(NonSmokeTestExample.class, Smoke.class));
    }
    private boolean classHasAnnotation(
            Class<? extends TestCase> aClass,
            Class<Smoke> expectedClassification) throws NoSuchMethodException {
        Method method = aClass.getMethod("testSomeTest");
        TestMethod testMethod = new TestMethod(method, aClass);
        return new HasClassAnnotation(expectedClassification).apply(testMethod);
    }
    @Smoke
    static class SmokeTestExample extends TestCase {
        public void testSomeTest() {
        }
    }
    static class NonSmokeTestExample extends TestCase {
        public void testSomeTest() {
        }
    }
}
