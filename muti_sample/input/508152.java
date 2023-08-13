public class MemoryFileProviderTest extends AndroidTestCase {
    @MediumTest
    public void testRead() throws Exception {
        ContentResolver resolver = getContext().getContentResolver();
        Uri uri = Uri.parse("content:
        byte[] buf = new byte[MemoryFileProvider.TEST_BLOB.length];
        InputStream in = resolver.openInputStream(uri);
        assertNotNull(in);
        int count = in.read(buf);
        assertEquals(buf.length, count);
        assertEquals(-1, in.read());
        in.close();
        assertTrue(Arrays.equals(MemoryFileProvider.TEST_BLOB, buf));
    }
    @MediumTest
    public void testClose() throws Exception {
        ContentResolver resolver = getContext().getContentResolver();
        for (int i = 0; i < 1025; i++) {
            Uri uri = Uri.parse("content:
            InputStream in = resolver.openInputStream(uri);
            assertNotNull("Failed to open stream number " + i, in);
            assertEquals(1000000, in.skip(1000000));
            byte[] buf = new byte[MemoryFileProvider.TEST_BLOB.length];
            int count = in.read(buf);
            assertEquals(buf.length, count);
            assertTrue(Arrays.equals(MemoryFileProvider.TEST_BLOB, buf));
            in.close();
        }
    }
    @MediumTest
    public void testFile() throws Exception {
        ContentResolver resolver = getContext().getContentResolver();
        Uri uri = Uri.parse("content:
        byte[] buf = new byte[MemoryFileProvider.TEST_BLOB.length];
        InputStream in = resolver.openInputStream(uri);
        assertNotNull(in);
        int count = in.read(buf);
        assertEquals(buf.length, count);
        assertEquals(-1, in.read());
        in.close();
        assertTrue(Arrays.equals(MemoryFileProvider.TEST_BLOB, buf));
    }
}
