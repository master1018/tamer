@TestTargetClass(ObjectStreamClass.class) 
public class ObjectStreamClassTest extends junit.framework.TestCase {
    static class DummyClass implements Serializable {
        private static final long serialVersionUID = 999999999999999L;
        long bam = 999L;
        int ham = 9999;
        public static long getUID() {
            return serialVersionUID;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "forClass",
        args = {}
    )      
    public void test_forClass() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertTrue("forClass returned an object: " + osc.forClass(), osc
                .forClass().equals(DummyClass.class));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getField",
        args = {java.lang.String.class}
    )     
    public void test_getFieldLjava_lang_String() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertEquals("getField did not return correct field", 'J', osc.getField("bam")
                .getTypeCode());
        assertNull("getField did not null for non-existent field", osc
                .getField("wham"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFields",
        args = {}
    )     
    public void test_getFields() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        ObjectStreamField[] osfArray = osc.getFields();
        assertTrue(
                "Array of fields should be of length 2 but is instead of length: "
                        + osfArray.length, osfArray.length == 2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )    
    public void test_getName() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertTrue("getName returned incorrect name: " + osc.getName(), osc
                .getName().equals(
                        "tests.api.java.io.ObjectStreamClassTest$DummyClass"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSerialVersionUID",
        args = {}
    )    
    public void test_getSerialVersionUID() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertTrue("getSerialversionUID returned incorrect uid: "
                + osc.getSerialVersionUID() + " instead of "
                + DummyClass.getUID(), osc.getSerialVersionUID() == DummyClass
                .getUID());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lookup",
        args = {java.lang.Class.class}
    )        
    public void test_lookupLjava_lang_Class() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertTrue("lookup returned wrong class: " + osc.getName(), osc
                .getName().equals(
                        "tests.api.java.io.ObjectStreamClassTest$DummyClass"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )    
    public void test_toString() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        String oscString = osc.toString();
        assertTrue("toString returned incorrect string: " + osc.toString(),
                oscString.indexOf("serialVersionUID") >= 0
                        && oscString.indexOf("999999999999999L") >= 0);
        ;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lookup",
        args = {java.lang.Class.class}
    )    
    public void testSerialization() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(ObjectStreamClass.class);
        assertEquals(0, osc.getFields().length);
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void testFooSerialVersionUid() {
        assertEquals(-5887964677443030867L, Foo.serialVersionUID());
    }
    static abstract class Foo implements Cloneable, Serializable {
        private final String name = "foo";
        static final long now;
        static {
            now = System.currentTimeMillis();
        }
        Foo() {}
        protected Foo(int ignored) {}
        synchronized static int foo() { return 0; }
        static int bar() { return 0; }
        abstract void tee();
        protected native synchronized boolean bob();
        protected synchronized void tim() {}
        static long serialVersionUID() {
            return ObjectStreamClass.lookup(Foo.class).getSerialVersionUID();
        }
    }
    public static void main(String[] args) {
        System.out.println(Foo.serialVersionUID());
    }
}
