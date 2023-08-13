@TestTargetClass(ByteOrder.class)
public class ByteOrderTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ByteOrderTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertEquals(ByteOrder.BIG_ENDIAN.toString(), "BIG_ENDIAN");
        assertEquals(ByteOrder.LITTLE_ENDIAN.toString(), "LITTLE_ENDIAN");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "nativeOrder",
        args = {}
    )
    public void testNativeOrder() {
        ByteOrder o = ByteOrder.nativeOrder();
        assertTrue(o == ByteOrder.BIG_ENDIAN || o == ByteOrder.LITTLE_ENDIAN);
    }
}
