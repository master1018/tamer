@TestTargetClass(java.nio.channels.FileChannel.MapMode.class)
public class MapModeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies fields.",
        method = "!Constants",
        args = {}
    )    
    public void test_PRIVATE_READONLY_READWRITE() {
        assertNotNull(FileChannel.MapMode.PRIVATE);
        assertNotNull(FileChannel.MapMode.READ_ONLY);
        assertNotNull(FileChannel.MapMode.READ_WRITE);
        assertFalse(FileChannel.MapMode.PRIVATE
                .equals(FileChannel.MapMode.READ_ONLY));
        assertFalse(FileChannel.MapMode.PRIVATE
                .equals(FileChannel.MapMode.READ_WRITE));
        assertFalse(FileChannel.MapMode.READ_ONLY
                .equals(FileChannel.MapMode.READ_WRITE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertNotNull(FileChannel.MapMode.PRIVATE.toString());
        assertNotNull(FileChannel.MapMode.READ_ONLY.toString());
        assertNotNull(FileChannel.MapMode.READ_WRITE.toString());
    }
}
