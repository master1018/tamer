@TestTargetClass(FileLock.class)
public class FileLockTest extends TestCase {
    private FileChannel readWriteChannel;
    private MockFileLock mockLock;
    class MockFileLock extends FileLock {
        boolean isValid = true;
        protected MockFileLock(FileChannel channel, long position, long size,
                boolean shared) {
            super(channel, position, size, shared);
        }
        public boolean isValid() {
            return isValid;
        }
        public void release() throws IOException {
            isValid = false;
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        File tempFile = File.createTempFile("testing", "tmp");
        tempFile.deleteOnExit();
        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
        readWriteChannel = randomAccessFile.getChannel();
        mockLock = new MockFileLock(readWriteChannel, 10, 100, false);
    }
	protected void tearDown() throws IOException {
	    if (readWriteChannel != null) {
	        readWriteChannel.close();
	    }
	}
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FileLock",
        args = {java.nio.channels.FileChannel.class, long.class, long.class, boolean.class}
    )
    public void test_Constructor_Ljava_nio_channels_FileChannelJJZ() {
        FileLock fileLock1 = new MockFileLock(null, 0, 0, false);
        assertNull(fileLock1.channel());
        try {
            new MockFileLock(readWriteChannel, -1, 0, false);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            new MockFileLock(readWriteChannel, 0, -1, false);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            new MockFileLock(readWriteChannel, Long.MAX_VALUE, 1, false);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "channel",
        args = {}
    )
    public void test_channel() {
        assertSame(readWriteChannel, mockLock.channel());
        FileLock lock = new MockFileLock(null, 0, 10, true);
        assertNull(lock.channel());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void test_position() {
        FileLock fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true);
        assertEquals(20, fileLock1.position());
        final long position = ((long) Integer.MAX_VALUE + 1);
        FileLock fileLock2 = new MockFileLock(readWriteChannel, position, 100,
                true);
        assertEquals(position, fileLock2.position());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        FileLock fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true);
        assertEquals(100, fileLock1.size());
        final long position = 0x0FFFFFFFFFFFFFFFL;
        final long size = ((long) Integer.MAX_VALUE + 1);
        FileLock fileLock2 = new MockFileLock(readWriteChannel, position, size,
                true);
        assertEquals(size, fileLock2.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "[check with false shared parameter]",
        method = "isShared",
        args = {}
    )
    public void test_isShared() {
        assertFalse(mockLock.isShared());
        FileLock lock = new MockFileLock(null, 0, 10, true);
        assertTrue(lock.isShared());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "overlaps",
        args = {long.class, long.class}
    )
    public void test_overlaps_JJ() {
        assertTrue(mockLock.overlaps(0, 11));
        assertFalse(mockLock.overlaps(0, 10));
        assertTrue(mockLock.overlaps(100, 110));
        assertTrue(mockLock.overlaps(99, 110));
        assertFalse(mockLock.overlaps(-1, 10));
        assertTrue(mockLock.overlaps(1, 120));
        assertTrue(mockLock.overlaps(20, 50));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )
    public void test_isValid() throws IOException {
        FileLock fileLock = readWriteChannel.lock();
        assertTrue(fileLock.isValid());
        fileLock.release();
        assertFalse(fileLock.isValid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "release",
        args = {}
    )
    public void test_release() throws Exception {
        File file = File.createTempFile("test", "tmp");
        file.deleteOnExit();
        FileOutputStream fout = new FileOutputStream(file);
        FileChannel fileChannel = fout.getChannel();
        FileLock fileLock = fileChannel.lock();
        fileChannel.close();
        try {
            fileLock.release();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        fout = new FileOutputStream(file);
        fileChannel = fout.getChannel();
        fileLock = fileChannel.lock();
        fileLock.release();
        fileChannel.close();
        try {
            fileLock.release();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
        File file = File.createTempFile("test", "tmp");
        file.deleteOnExit();
        FileOutputStream fout = new FileOutputStream(file);
        FileChannel fileChannel = fout.getChannel();
        FileLock fileLock = fileChannel.lock();
        assertTrue(fileLock.toString().length() > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression test",
        method = "release",
        args = {}
    )
    public void testFileLock() throws Exception {
        String fileName = File.createTempFile("test", "tmp").getAbsolutePath();
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");   
        FileLock lock = raf.getChannel().tryLock();
        raf.write("file lock test".getBytes()); 
        lock.release();   
        raf.close();
      }
}
