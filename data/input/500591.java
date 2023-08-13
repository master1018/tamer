@TestTargetClass(VirtualMachineError.class) 
public class VirtualMachineErrorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "VirtualMachineError",
        args = {}
    )
    public void test_Constructor() {
        VirtualMachineError e = new VirtualMachineError() {};
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "VirtualMachineError",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        VirtualMachineError e = new VirtualMachineError("fixture") {};
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
