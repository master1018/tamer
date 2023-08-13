public class TestGroovy extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    public static interface AdtTestInterface {
        public boolean acceptDrag(String xmlName);
        public Rectangle acceptDrop(String xmlName);
        public Object returnGroovyObject();
        public boolean testGroovyObject(Object o);
    }
    @SuppressWarnings("unchecked")
    private AdtTestInterface loadScript(String filename)
        throws CompilationFailedException, ClassCastException,
               InstantiationException, IllegalAccessException,
               FileNotFoundException {
        InputStream myGroovyStream = getClass().getResourceAsStream(filename);
        if (myGroovyStream == null) {
            throw new FileNotFoundException(filename);
        }
        ClassLoader cl = getClass().getClassLoader();
        GroovyClassLoader gcl = new GroovyClassLoader(cl);
        Class gClass = gcl.parseClass(myGroovyStream, filename);
        return (AdtTestInterface) gClass.newInstance();
    }
    public void testMissingScript() throws Exception {
        try {
            @SuppressWarnings("unused")
            AdtTestInterface instance = loadScript("not_an_existing_script.groovy");
            fail("loadScript should not succeed, FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            assertEquals("not_an_existing_script.groovy", e.getMessage());
            return; 
        }
        fail("Script failed to throw an exception on missing groovy file.");
    }
    public void testInvalidInterface() throws Exception {
        try {
            @SuppressWarnings("unused")
            AdtTestInterface instance = loadScript("invalid_interface.groovy");
            fail("loadScript should not succeed, ClassCastException expected.");
        } catch(ClassCastException e) {
            assertNotNull(e.getMessage());
            return; 
        }
        fail("Script failed to throw a ClassCastException.");
    }
    public void testCompilationError() throws Exception {
        try {
            @SuppressWarnings("unused")
            AdtTestInterface instance = loadScript("compile_error.groovy");
            fail("loadScript should not succeed, CompilationFailedException expected.");
        } catch (CompilationFailedException e) {
            assertNotNull(e.getMessage());
            return; 
        }
        fail("Script failed to throw a compilation error.");
    }
    public void testSimpleMethods() throws Exception {
        AdtTestInterface instance = loadScript("simple_test.groovy");
        assertTrue(instance.acceptDrag("LinearLayout"));
        assertFalse(instance.acceptDrag("RelativeLayout"));
        assertNull(instance.acceptDrop("none"));
        Rectangle r = instance.acceptDrop("LinearLayout");
        assertNotNull(r);
        assertEquals(new Rectangle(1, 2, 3, 4), r);
    }
    public void testCallback() throws Exception {
        AdtTestInterface instance = loadScript("simple_test.groovy");
        Object o = instance.returnGroovyObject();
        assertNotNull(o);
        assertTrue(instance.testGroovyObject(o));
    }
}
