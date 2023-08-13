@TestTargetClass(EventObject.class) 
public class EventObjectTest extends junit.framework.TestCase {
    Object myObject;
    EventObject myEventObject;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "See setUp method.",
        method = "EventObject",
        args = {java.lang.Object.class}
    )
    public void test_ConstructorLjava_lang_Object() {
        try {
            new EventObject(null);
            fail ("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSource",
        args = {}
    )
    public void test_getSource() {
        assertTrue("Wrong source returned",
                myEventObject.getSource() == myObject);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertTrue("Incorrect toString returned: " + myEventObject.toString(),
                myEventObject.toString().indexOf(
                        "java.util.EventObject[source=java.lang.Object@") == 0);
    }
    protected void setUp() {
        myObject = new Object();
        myEventObject = new EventObject(myObject);
    }
    protected void tearDown() {
    }
}
