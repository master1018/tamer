public class EntropyServiceTest extends AndroidTestCase {
    public void testInitialWrite() throws Exception {
        File dir = getContext().getDir("testInitialWrite", Context.MODE_PRIVATE);
        File file = File.createTempFile("testInitialWrite", "dat", dir);
        file.deleteOnExit();
        assertEquals(0, FileUtils.readTextFile(file, 0, null).length());
        new EntropyService("/dev/null", file.getCanonicalPath());
        assertTrue(FileUtils.readTextFile(file, 0, null).length() > 0);
    }
}
