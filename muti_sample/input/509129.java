@TestTargetClass(InheritableThreadLocal.class) 
public class InheritableThreadLocalTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InheritableThreadLocal",
        args = {}
    )
    public void test_Constructor() {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>();
        assertNull(itl.get());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initialValue",
        args = {}
    )
    public void test_initialValue() {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "initial";
            }
        };
        assertEquals("initial", itl.get());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "childValue",
        args = {java.lang.Object.class}
    )
    public void test_childValue() {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "initial";
            }            
            @Override
            protected String childValue(String parentValue) {
                return "childValue";
            }
        };
        assertEquals("initial", itl.get());        
    }  
}
