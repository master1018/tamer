@TestTargetClass(AccessibleObject.class) 
public class AccessibleObjectTest extends junit.framework.TestCase {
    public class TestClass {
        public Object aField;
        @InheritedRuntime
        public void annotatedMethod(){}
    }
    public class SubTestClass extends TestClass{
        @AnnotationRuntime0
        @AnnotationRuntime1
        @AnnotationClass0
        @AnnotationSource0
        public void annotatedMethod(){}
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.METHOD})
    static @interface AnnotationRuntime0 {
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.METHOD})
    static @interface AnnotationRuntime1 {
    }
    @Retention(RetentionPolicy.CLASS)
    @Target( { ElementType.METHOD})
    static @interface AnnotationClass0 {
    }
    @Retention(RetentionPolicy.SOURCE)
    @Target( {ElementType.METHOD})
    static @interface AnnotationSource0 {
    }
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.METHOD})
    static @interface InheritedRuntime {
    }
    private static class MyAccessibleObject extends AccessibleObject{
        public MyAccessibleObject() {
            super();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "The only thing I can do",
        method = "AccessibleObject",
        args = {}
    )
    public void test_Constructor() {
        assertNotNull(new MyAccessibleObject());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAccessible",
        args = {}
    )
    public void test_isAccessible() {
        try {
            AccessibleObject ao = TestClass.class.getField("aField");
            ao.setAccessible(true);
            assertTrue("Returned false to isAccessible", ao.isAccessible());
            ao.setAccessible(false);
            assertTrue("Returned true to isAccessible", !ao.isAccessible());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SecurityExeption is tested in tests.security.permissions.JavaLangReflectAccessibleObjectTest",
        method = "setAccessible",
        args = {java.lang.reflect.AccessibleObject[].class, boolean.class}
    )
    public void test_setAccessible$Ljava_lang_reflect_AccessibleObjectZ() {
        try {
            AccessibleObject ao = TestClass.class.getField("aField");
            AccessibleObject[] aoa = new AccessibleObject[] { ao };
            AccessibleObject.setAccessible(aoa, true);
            assertTrue("Returned false to isAccessible", ao.isAccessible());
            AccessibleObject.setAccessible(aoa, false);
            assertTrue("Returned true to isAccessible", !ao.isAccessible());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SecurityExeption is tested in tests.security.permissions.JavaLangReflectAccessibleObjectTest",
        method = "setAccessible",
        args = {boolean.class}
    )
    public void test_setAccessible() throws Exception {
        AccessibleObject ao = TestClass.class.getField("aField");
        ao.setAccessible(true);
        assertTrue("Returned false to isAccessible", ao.isAccessible());
        ao.setAccessible(false);
        assertFalse("Returned true to isAccessible", ao.isAccessible());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnnotation",
        args = {java.lang.Class.class}
    )
    public void test_getAnnotation() throws Exception{
        AccessibleObject ao = SubTestClass.class.getMethod("annotatedMethod");
        boolean npeThrown = false;
        try {
          ao.getAnnotation(null);
          fail("NPE expected");
        } catch (NullPointerException e) {
            npeThrown = true;
        }
        assertTrue("NPE expected", npeThrown);
        InheritedRuntime ir = ao.getAnnotation(InheritedRuntime.class);
        assertNull("Inherited Annotations should have no effect", ir);
        AnnotationRuntime0 rt0 = ao.getAnnotation(AnnotationRuntime0.class);
        assertNotNull("AnnotationRuntime0 instance expected", rt0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnnotations",
        args = {}
    )
     public void test_getAnnotations() throws Exception {
        AccessibleObject ao = SubTestClass.class.getMethod("annotatedMethod");
        Annotation[] annotations = ao.getAnnotations();
        assertEquals(2, annotations.length);
        Set<Class<?>> ignoreOrder = new HashSet<Class<?>>();
        ignoreOrder.add(annotations[0].annotationType());
        ignoreOrder.add(annotations[1].annotationType());
        assertTrue("Missing @AnnotationRuntime0", 
                ignoreOrder.contains(AnnotationRuntime0.class));
        assertTrue("Missing @AnnotationRuntime1",
                ignoreOrder.contains(AnnotationRuntime1.class));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDeclaredAnnotations",
        args = {}
    )
     public void test_getDeclaredAnnotations() throws Exception {
        AccessibleObject ao = SubTestClass.class.getMethod("annotatedMethod");
        Annotation[] annotations = ao.getDeclaredAnnotations();
        assertEquals(2, annotations.length);
        Set<Class<?>> ignoreOrder = new HashSet<Class<?>>();
        ignoreOrder.add(annotations[0].annotationType());
        ignoreOrder.add(annotations[1].annotationType());
        assertTrue("Missing @AnnotationRuntime0", 
                ignoreOrder.contains(AnnotationRuntime0.class));
        assertTrue("Missing @AnnotationRuntime1",
                ignoreOrder.contains(AnnotationRuntime1.class));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAnnotationPresent",
        args = {java.lang.Class.class}
    )
    public void test_isAnnotationPresent() throws Exception {
        AccessibleObject ao = SubTestClass.class.getMethod("annotatedMethod");
        assertTrue("Missing @AnnotationRuntime0",
                ao.isAnnotationPresent(AnnotationRuntime0.class));
        assertFalse("AnnotationSource0 should not be visible at runtime",
                ao.isAnnotationPresent(AnnotationSource0.class));
        boolean npeThrown = false;
        try {
          ao.isAnnotationPresent(null);
          fail("NPE expected");
        } catch (NullPointerException e) {
            npeThrown = true;
        }
        assertTrue("NPE expected", npeThrown);
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
