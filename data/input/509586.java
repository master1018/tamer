public class FileTest extends TestCase {
    @SmallTest
    public void testFile() throws Exception {
        File file = File.createTempFile(String.valueOf(System.currentTimeMillis()), null, null);
        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
}
