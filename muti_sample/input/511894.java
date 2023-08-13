public class AssignableFromTest extends TestCase {
    private AssignableFrom assignableFrom;
    protected void setUp() throws Exception {
        super.setUp();
        assignableFrom = new AssignableFrom(Animal.class);
    }
    public void testSelfIsAssignable() throws Exception {
        assertTrue(assignableFrom.apply(testMethodFor(Animal.class)));
    }
    public void testSubclassesAreAssignable() throws Exception {
        assertTrue(assignableFrom.apply(testMethodFor(Mammal.class)));
        assertTrue(assignableFrom.apply(testMethodFor(Human.class)));
    }
    public void testNotAssignable() throws Exception {
        assertFalse(assignableFrom.apply(testMethodFor(Pencil.class)));
    }
    public void testImplementorsAreAssignable() throws Exception {
        assignableFrom = new AssignableFrom(WritingInstrument.class);
        assertTrue(assignableFrom.apply(testMethodFor(Pencil.class)));
        assertTrue(assignableFrom.apply(testMethodFor(Pen.class)));
    }
    private TestMethod testMethodFor(Class<? extends TestCase> aClass)
            throws NoSuchMethodException {
        Method method = aClass.getMethod("testX");
        return new TestMethod(method, aClass);
    }
    private class Animal extends TestCase {
        public void testX() {
        }
    }
    private class Mammal extends Animal {
        public void testX() {
        }
    }
    private class Human extends Mammal {
        public void testX() {
        }
    }
    private interface WritingInstrument {
    }
    private class Pencil extends TestCase implements WritingInstrument {
        public void testX() {
        }
    }
    private class Pen extends TestCase implements WritingInstrument {
        public void testX() {
        }
    }
}
