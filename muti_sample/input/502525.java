    value = FileChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "FileChannel",
            args = {}
        )
    }
)
public class FileChannelTest extends TestCase {
    private static final int CAPACITY = 100;
    private static final int LIMITED_CAPACITY = 2;
    private static final int TIME_OUT = 10000;
    private static final String CONTENT = "MYTESTSTRING needs to be a little long";
    private static final byte[] TEST_BYTES;
    static {
        try {
            TEST_BYTES = "test".getBytes("iso8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
    private static final int CONTENT_LENGTH = CONTENT.length();
    private static final byte[] CONTENT_AS_BYTES = CONTENT.getBytes();
    private static final int CONTENT_AS_BYTES_LENGTH = CONTENT_AS_BYTES.length;
    private FileChannel readOnlyFileChannel;
    private FileChannel writeOnlyFileChannel;
    private FileChannel readWriteFileChannel;
    private File fileOfReadOnlyFileChannel;
    private File fileOfWriteOnlyFileChannel;
    private File fileOfReadWriteFileChannel;
    private ReadableByteChannel readByteChannel;
    private WritableByteChannel writableByteChannel;
    private DatagramChannel datagramChannelSender;
    private DatagramChannel datagramChannelReceiver;
    private ServerSocketChannel serverSocketChannel;
    private SocketChannel socketChannelSender;
    private SocketChannel socketChannelReceiver;
    private Pipe pipe;
    private FileInputStream fis;
    private FileLock fileLock;
    protected void setUp() throws Exception {
        fileOfReadOnlyFileChannel = File.createTempFile(
                "File_of_readOnlyFileChannel", "tmp");
        fileOfReadOnlyFileChannel.deleteOnExit();
        fileOfWriteOnlyFileChannel = File.createTempFile(
                "File_of_writeOnlyFileChannel", "tmp");
        fileOfWriteOnlyFileChannel.deleteOnExit();
        fileOfReadWriteFileChannel = File.createTempFile(
                "File_of_readWriteFileChannel", "tmp");
        fileOfReadWriteFileChannel.deleteOnExit();
        fis = null;
        fileLock = null;
        readOnlyFileChannel = new FileInputStream(fileOfReadOnlyFileChannel)
                .getChannel();
        writeOnlyFileChannel = new FileOutputStream(fileOfWriteOnlyFileChannel)
                .getChannel();
        readWriteFileChannel = new RandomAccessFile(fileOfReadWriteFileChannel,
                "rw").getChannel();
    }
    protected void tearDown() {
        if (null != readOnlyFileChannel) {
            try {
                readOnlyFileChannel.close();
            } catch (IOException e) {
            }
        }
        if (null != writeOnlyFileChannel) {
            try {
                writeOnlyFileChannel.close();
            } catch (IOException e) {
            }
        }
        if (null != readWriteFileChannel) {
            try {
                readWriteFileChannel.close();
            } catch (IOException e) {
            }
        }
        if (null != fis) {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
        if (null != fileLock) {
            try {
                fileLock.release();
            } catch (IOException e) {
            }
        }
        if (null != fileOfReadOnlyFileChannel) {
            fileOfReadOnlyFileChannel.delete();
        }
        if (null != fileOfWriteOnlyFileChannel) {
            fileOfWriteOnlyFileChannel.delete();
        }
        if (null != fileOfReadWriteFileChannel) {
            fileOfReadWriteFileChannel.delete();
        }
        if (null != datagramChannelSender) {
            try {
                datagramChannelSender.close();
            } catch (IOException e) {
            }
        }
        if (null != datagramChannelReceiver) {
            try {
                datagramChannelReceiver.close();
            } catch (IOException e) {
            }
        }
        if (null != serverSocketChannel) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
            }
        }
        if (null != socketChannelSender) {
            try {
                socketChannelSender.close();
            } catch (IOException e) {
            }
        }
        if (null != socketChannelReceiver) {
            try {
                socketChannelReceiver.close();
            } catch (IOException e) {
            }
        }
        if (null != pipe) {
            if (null != pipe.source()) {
                try {
                    pipe.source().close();
                } catch (IOException e) {
                }
            }
            if (null != pipe.sink()) {
                try {
                    pipe.sink().close();
                } catch (IOException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "force",
        args = {boolean.class}
    )
    public void test_forceZ() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.force(true);
        byte[] readBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(readBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, readBuffer));
        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.force(false);
        readBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(readBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, readBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "force",
        args = {boolean.class}
    )
    public void test_forceZ_closed() throws Exception {
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.force(true);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            writeOnlyFileChannel.force(false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "force",
        args = {boolean.class}
    )
    public void test_forceZ_ReadOnlyChannel() throws Exception {
        readOnlyFileChannel.force(true);
        readOnlyFileChannel.force(false);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void test_position_Init() throws Exception {
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(0, writeOnlyFileChannel.position());
        assertEquals(0, readWriteFileChannel.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void test_position_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        assertEquals(0, readOnlyFileChannel.position());
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH, readOnlyFileChannel.position());
    }
    private void writeDataToFile(File file) throws FileNotFoundException,
            IOException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(CONTENT_AS_BYTES);
        } finally {
            fos.close();
        }
    }
    private void writeLargeDataToFile(File file, int size) 
            throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[size];
        try {
            fos.write(buf);
        } finally {
            fos.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void test_position_WriteOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_LENGTH, writeOnlyFileChannel.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    public void test_position_ReadWrite() throws Exception {
        writeDataToFile(fileOfReadWriteFileChannel);
        assertEquals(0, readWriteFileChannel.position());
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readWriteFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH, readWriteFileChannel.position());
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        readWriteFileChannel.write(writeBuffer);
        assertEquals(CONTENT_LENGTH * 2, readWriteFileChannel.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "position",
        args = {}
    )
    public void test_position_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "position",
        args = {long.class}
    )
    public void test_positionJ_Closed() throws Exception {
        final long POSITION = 100;
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.position(POSITION);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.position(POSITION);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.position(POSITION);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "position",
        args = {long.class}
    )
    public void test_positionJ_Negative() throws Exception {
        final long NEGATIVE_POSITION = -1;
        try {
            readOnlyFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {long.class}
    )
    public void test_positionJ_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        final int POSITION = 4;
        readOnlyFileChannel.position(POSITION);
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        int count = readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH - POSITION, count);
        readBuffer.flip();
        int i = POSITION;
        while (readBuffer.hasRemaining()) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
            i++;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {long.class}
    )
    public void test_positionJ_WriteOnly() throws Exception {
        writeDataToFile(fileOfWriteOnlyFileChannel);
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        final int POSITION = 4;
        writeOnlyFileChannel.position(POSITION);
        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.close();
        byte[] result = new byte[POSITION + CONTENT_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(result);
        byte[] expectedResult = new byte[POSITION + CONTENT_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, POSITION,
                CONTENT_LENGTH);
        assertTrue(Arrays.equals(expectedResult, result));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies zero size.",
        method = "size",
        args = {}
    )
    public void test_size_Init() throws Exception {
        assertEquals(0, readOnlyFileChannel.size());
        assertEquals(0, writeOnlyFileChannel.size());
        assertEquals(0, readWriteFileChannel.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "size",
        args = {}
    )
    public void test_size() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        assertEquals(fileOfReadOnlyFileChannel.length(), readOnlyFileChannel
                .size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "size",
        args = {}
    )
    public void test_size_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "truncate",
        args = {long.class}
    )
    public void test_truncateJ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.truncate(0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.truncate(0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.truncate(-1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "truncate",
        args = {long.class}
    )
    public void test_truncateJ_IllegalArgument() throws Exception {
        try {
            readOnlyFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException.",
        method = "truncate",
        args = {long.class}
    )
    public void test_truncateJ_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        try {
            readOnlyFileChannel.truncate(readOnlyFileChannel.size());
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
        try {
            readOnlyFileChannel.truncate(0);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "truncate",
        args = {long.class}
    )
    public void test_truncateJ() throws Exception {
        writeDataToFile(fileOfReadWriteFileChannel);
        int truncateLength = CONTENT_LENGTH + 2;
        assertEquals(readWriteFileChannel, readWriteFileChannel
                .truncate(truncateLength));
        assertEquals(CONTENT_LENGTH, fileOfReadWriteFileChannel.length());
        truncateLength = CONTENT_LENGTH;
        assertEquals(readWriteFileChannel, readWriteFileChannel
                .truncate(truncateLength));
        assertEquals(CONTENT_LENGTH, fileOfReadWriteFileChannel.length());
        truncateLength = CONTENT_LENGTH / 2;
        assertEquals(readWriteFileChannel, readWriteFileChannel
                .truncate(truncateLength));
        assertEquals(truncateLength, fileOfReadWriteFileChannel.length());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lock",
        args = {}
    )
    public void test_lock() throws Exception {
        MockFileChannel mockFileChannel = new MockFileChannel();
        mockFileChannel.lock();
        assertTrue(mockFileChannel.isLockCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.lock(-1, 0, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_IllegalArgument() throws Exception {
        try {
            writeOnlyFileChannel.lock(0, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.lock(-1, 0, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.lock(-1, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.lock(Long.MAX_VALUE, 1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.lock(0, 10, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        try {
            readOnlyFileChannel.lock(-1, 0, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonReadableChannelException.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_NonReadable() throws Exception {
        try {
            writeOnlyFileChannel.lock(0, 10, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
        try {
            writeOnlyFileChannel.lock(-1, 0, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies shared channel.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_Shared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = readOnlyFileChannel.lock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertSame(readOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that unshared channel.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_NotShared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies lock method with Long max value as a size.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_Long_MAX_VALUE() throws Exception {
        final long POSITION = 0;
        final long SIZE = Long.MAX_VALUE;
        fileLock = readOnlyFileChannel.lock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
        assertSame(readOnlyFileChannel, fileLock.channel());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies OverlappingFileLockException.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_Overlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        try {
            writeOnlyFileChannel.lock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that not overlaping regions can be locked.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_NotOverlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        FileLock fileLock1 = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock1.isValid());
        FileLock fileLock2 = writeOnlyFileChannel.lock(POSITION + SIZE, SIZE,
                false);
        assertTrue(fileLock2.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies functionality after release method.",
        method = "lock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_lockJJZ_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.lock(0, 10, false);
        fileLock.release();
        fileLock = writeOnlyFileChannel.lock(0, 10, false);
        assertTrue(fileLock.isValid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "tryLock",
        args = {}
    )
    public void test_tryLock() throws Exception {
        MockFileChannel mockFileChannel = new MockFileChannel();
        mockFileChannel.tryLock();
        assertTrue(mockFileChannel.isTryLockCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.tryLock(-1, 0, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_IllegalArgument() throws Exception {
        try {
            writeOnlyFileChannel.tryLock(0, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.tryLock(-1, 0, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.tryLock(-1, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.tryLock(Long.MAX_VALUE, 1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        long tooBig = ((long) Integer.MAX_VALUE) + 1;
        try {
            readWriteFileChannel.tryLock(tooBig, 1, false);
            fail("should throw IOException");
        } catch (IOException e) {
        }
        try {
            readWriteFileChannel.tryLock(0, tooBig, false);
            fail("should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException.",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        try {
            readOnlyFileChannel.tryLock(-1, 0, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonReadableChannelException.",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_NonReadable() throws Exception {
        try {
            writeOnlyFileChannel.tryLock(0, 10, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
        try {
            writeOnlyFileChannel.tryLock(-1, 0, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_Shared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = readOnlyFileChannel.tryLock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertSame(readOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_NotShared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.tryLock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_Long_MAX_VALUE() throws Exception {
        final long POSITION = 0;
        final long SIZE = Long.MAX_VALUE;
        fileLock = readOnlyFileChannel.tryLock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
        assertSame(readOnlyFileChannel, fileLock.channel());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies OverlappingFileLockException.",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_Overlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        try {
            writeOnlyFileChannel.lock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_NotOverlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        FileLock fileLock1 = writeOnlyFileChannel
                .tryLock(POSITION, SIZE, false);
        assertTrue(fileLock1.isValid());
        FileLock fileLock2 = writeOnlyFileChannel.tryLock(POSITION + SIZE,
                SIZE, false);
        assertTrue(fileLock2.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "tryLock",
        args = {long.class, long.class, boolean.class}
    )
    public void test_tryLockJJZ_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.tryLock(0, 10, false);
        fileLock.release();
        fileLock = writeOnlyFileChannel.tryLock(0, 10, false);
        assertTrue(fileLock.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer_Null() throws Exception {
        ByteBuffer readBuffer = null;
        try {
            readOnlyFileChannel.read(readBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer_Closed() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readBuffer = null;
        try {
            readOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonReadableChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer_WriteOnly() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
        readBuffer = null;
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer_EmptyFile() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer_LimitedCapacity() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer readBuffer = ByteBuffer.allocate(LIMITED_CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(LIMITED_CAPACITY, result);
        assertEquals(LIMITED_CAPACITY, readBuffer.position());
        readBuffer.flip();
        for (int i = 0; i < LIMITED_CAPACITY; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_readLByteBuffer() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_AS_BYTES_LENGTH);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffer.position());
        readBuffer.flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_Null() throws Exception {
        ByteBuffer readBuffer = null;
        try {
            readOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_Closed() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer, 0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer, 0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonReadableChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_WriteOnly() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            writeOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer, 0);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_Emptyfile() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer, 0);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_Postion_BeyondFileLimit()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer,
                CONTENT_AS_BYTES.length);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IOException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ_Postion_As_Long() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffer, Long.MAX_VALUE);
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_readLByteBufferJ() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        final int BUFFER_POSITION = 1;
        readBuffer.position(BUFFER_POSITION);
        final int POSITION = 2;
        int result = readOnlyFileChannel.read(readBuffer, POSITION);
        assertEquals(CONTENT_AS_BYTES_LENGTH - POSITION, result);
        assertEquals(BUFFER_POSITION + result, readBuffer.position());
        readBuffer.flip();
        readBuffer.position(BUFFER_POSITION);
        for (int i = POSITION; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_read$LByteBuffer_Regression() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        long readCount = readOnlyFileChannel.read(readBuffers);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readCount);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffers[0].position());
        assertEquals(0, readBuffers[1].position());
        readBuffers[0].flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[0].get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_read$LByteBuffer() throws Exception {
        FileChannel mockChannel = new MockFileChannel();
        ByteBuffer[] buffers = new ByteBuffer[2];
        mockChannel.read(buffers);
        assertTrue(((MockFileChannel)mockChannel).isReadCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_Null() throws Exception {
        try {
            readOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_Closed() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readBuffers[0] = null;
        try {
            readOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_WriteOnly() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
        readBuffers[0] = null;
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
        }
    }
    private void doTestForIOOBException(FileChannel channel, 
            ByteBuffer[] buffer) throws IOException{
        try {
            channel.read(buffer, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            channel.read(buffer, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            channel.read(buffer, 0, 3);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            channel.read(buffer, 1, 2);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            channel.read(buffer, 2, 1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            channel.read(buffer, 3, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_IndexOutOfBound() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        ByteBuffer[] readBuffersNull = new ByteBuffer[2];
        doTestForIOOBException(readOnlyFileChannel, readBuffers);
        doTestForIOOBException(readWriteFileChannel, readBuffers);
        doTestForIOOBException(writeOnlyFileChannel, readBuffers);
        doTestForIOOBException(readOnlyFileChannel, readBuffersNull);
        doTestForIOOBException(readWriteFileChannel, readBuffersNull);
        doTestForIOOBException(writeOnlyFileChannel, readBuffersNull);
        try {
            readOnlyFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
        }
        readOnlyFileChannel.close();
        doTestForIOOBException(readOnlyFileChannel, readBuffers);
        doTestForIOOBException(readOnlyFileChannel, readBuffersNull);
        readWriteFileChannel.close();
        doTestForIOOBException(readWriteFileChannel, readBuffers);
        doTestForIOOBException(readWriteFileChannel, readBuffersNull);
        writeOnlyFileChannel.close();
        doTestForIOOBException(writeOnlyFileChannel, readBuffers);
        doTestForIOOBException(writeOnlyFileChannel, readBuffersNull);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_EmptyFile() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 0, 2);
        assertEquals(-1, result);
        assertEquals(0, readBuffers[0].position());
        assertEquals(0, readBuffers[1].position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffers, 0, 2);
        } catch (NullPointerException e) {
        }
        writeDataToFile(fileOfReadOnlyFileChannel);
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffers, 0, 2);
        } catch (NullPointerException e) {
        }
        long result = readOnlyFileChannel.read(readBuffers, 0, 1);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_EmptyFile_EmptyBuffers()
            throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        long result = readOnlyFileChannel.read(readBuffers, 0, 0);
        assertEquals(0, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_Length_Zero() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(LIMITED_CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(LIMITED_CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 1, 0);
        assertEquals(0, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII_LimitedCapacity() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(LIMITED_CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(LIMITED_CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 1, 1);
        assertEquals(LIMITED_CAPACITY, result);
        assertEquals(0, readBuffers[0].position());
        assertEquals(LIMITED_CAPACITY, readBuffers[1].position());
        readBuffers[1].flip();
        for (int i = 0; i < LIMITED_CAPACITY; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[1].get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_read$LByteBufferII() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readOnlyFileChannel.read(
                readBuffers, 1, 1));
        assertEquals(0, readBuffers[0].position());
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffers[1].position());
        readBuffers[1].flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[1].get());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "isOpen",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "close",
            args = {}
        )
    })
    public void test_isOpen() throws Exception {
        File logFile = File.createTempFile("out", "tmp");
        logFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(logFile, true);
        FileChannel channel = out.getChannel();
        out.write(1);
        assertTrue("Assert 0: Channel is not open", channel.isOpen());
        out.close();
        assertFalse("Assert 0: Channel is still open", channel.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "position",
        args = {}
    )
    @AndroidOnly("Fails on RI. See comment below")
    public void test_position_append() throws Exception {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile);
        byte[] b = new byte[10];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }
        fos.write(b);
        fos.flush();
        fos.close();
        FileOutputStream f = new FileOutputStream(tmpfile, true);
        assertEquals(10, f.getChannel().position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonReadableChannelException, NonWritableChannelException , ClosedChannelException, IllegalArgumentException, IOException. ",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_AbnormalMode() throws IOException {
        try {
            writeOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
        }
        try {
            writeOnlyFileChannel.map(MapMode.READ_WRITE, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
        }
        try {
            writeOnlyFileChannel.map(MapMode.PRIVATE, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, CONTENT_LENGTH);
            fail("should throw NonWritableChannelException .");
        } catch (NonWritableChannelException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.PRIVATE, 0, CONTENT_LENGTH);
            fail("should throw NonWritableChannelException .");
        } catch (NonWritableChannelException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, -1, CONTENT_LENGTH);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH + 1);
            fail("should throw IOException.");
        } catch (IOException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 2, CONTENT_LENGTH - 1);
            fail("should throw IOException.");
        } catch (IOException ex) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 2, CONTENT_LENGTH - 1);
            fail("should throw IOException.");
        } catch (IOException ex) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_ReadOnly_CloseChannel() throws IOException {
        assertEquals(0, readWriteFileChannel.size());
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.READ_ONLY,
                0, CONTENT_LENGTH);
        assertEquals(CONTENT_LENGTH, readWriteFileChannel.size());
        readOnlyFileChannel.close();
        assertEquals(CONTENT_LENGTH, mapped.limit());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_Private_CloseChannel() throws IOException {
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 0,
                CONTENT_LENGTH);
        readWriteFileChannel.close();
        mapped.put(TEST_BYTES);
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals("test".length(), mapped.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_ReadOnly() throws IOException {
        MappedByteBuffer mapped = null;
        writeDataToFile(fileOfReadOnlyFileChannel);
        mapped = readOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH);
        try {
            mapped.put(TEST_BYTES);
            fail("should throw ReadOnlyBufferException.");
        } catch (ReadOnlyBufferException ex) {
        }
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals(CONTENT_LENGTH, mapped.capacity());
        assertEquals(0, mapped.position());
        writeDataToFile(fileOfReadWriteFileChannel);
        mapped = readWriteFileChannel.map(MapMode.READ_ONLY, 0, CONTENT
                .length());
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals(CONTENT_LENGTH, mapped.capacity());
        assertEquals(0, mapped.position());
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(0, readWriteFileChannel.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_ReadOnly_NonZeroPosition() throws IOException {
        this.writeDataToFile(fileOfReadOnlyFileChannel);
        MappedByteBuffer mapped = readOnlyFileChannel.map(MapMode.READ_ONLY,
                10, CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT_LENGTH - 10, mapped.capacity());
        assertEquals(0, mapped.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_Private() throws IOException {
        this.writeDataToFile(fileOfReadWriteFileChannel);
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 0,
                CONTENT_LENGTH);
        assertEquals(CONTENT_LENGTH, mapped.limit());
        ByteBuffer returnByPut = mapped.put(TEST_BYTES);
        assertSame(returnByPut, mapped);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        mapped.force();
        readWriteFileChannel.read(checkBuffer);
        assertEquals(CONTENT, new String(checkBuffer.array(), "iso8859-1"));
        try {
            mapped.put(("test" + CONTENT).getBytes("iso8859-1"));
            fail("should throw BufferOverflowException.");
        } catch (BufferOverflowException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_Private_NonZeroPosition() throws IOException {
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 10,
                CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT_LENGTH - 10, mapped.capacity());
        assertEquals(0, mapped.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_ReadWrite() throws IOException {
        MappedByteBuffer mapped = null;
        writeDataToFile(fileOfReadWriteFileChannel);
        mapped = readWriteFileChannel.map(MapMode.READ_WRITE, 0, CONTENT
                .length());
        ByteBuffer returnByPut = mapped.put(TEST_BYTES);
        assertSame(returnByPut, mapped);
        String checkString = "test" + CONTENT.substring(4);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        mapped.force();
        readWriteFileChannel.position(0);
        readWriteFileChannel.read(checkBuffer);
        assertEquals(checkString, new String(checkBuffer.array(), "iso8859-1"));
        try {
            mapped.put(("test" + CONTENT).getBytes("iso8859-1"));
            fail("should throw BufferOverflowException.");
        } catch (BufferOverflowException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    public void test_map_ReadWrite_NonZeroPosition() throws IOException {
        writeDataToFile(fileOfReadWriteFileChannel);
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.READ_WRITE,
                10, CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT.length() - 10, mapped.capacity());
        assertEquals(0, mapped.position());
        mapped.put(TEST_BYTES);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readWriteFileChannel.read(checkBuffer);
        String expected = CONTENT.substring(0, 10) + "test"
                + CONTENT.substring(10 + "test".length());
        assertEquals(expected, new String(checkBuffer.array(), "iso8859-1"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "map",
        args = {java.nio.channels.FileChannel.MapMode.class, long.class, long.class}
    )
    @AndroidOnly("Platform.class is harmony specific")
    public void test_map_LargePosition() throws IOException {
        int[] sizes = {
            4096, 
            65536, 
            Platform.getFileSystem().getAllocGranularity() 
        };
        final int CONTENT_LEN = 10;
        for (int i = 0; i < sizes.length; ++i) {
            if (i > 0 ) {
                fileOfReadOnlyFileChannel = File.createTempFile(
                        "File_of_readOnlyFileChannel", "tmp");
                fileOfReadOnlyFileChannel.deleteOnExit();
                readOnlyFileChannel = new FileInputStream(
                        fileOfReadOnlyFileChannel).getChannel();
            }
            writeLargeDataToFile(fileOfReadOnlyFileChannel, sizes[i] + 
                    2 * CONTENT_LEN);
            MappedByteBuffer mapped = readOnlyFileChannel.map(MapMode.READ_ONLY,
                    sizes[i], CONTENT_LEN);
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (capacity)", CONTENT_LEN, mapped.capacity());
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (limit)", CONTENT_LEN, mapped.limit());
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (position)", 0, mapped.position());
            assertEquals(0, readOnlyFileChannel.position());
            readOnlyFileChannel.close();
            fileOfReadOnlyFileChannel.delete();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLByteBuffer_Null() throws Exception {
        ByteBuffer writeBuffer = null;
        try {
            writeOnlyFileChannel.write(writeBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLByteBuffer_Closed() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeBuffer = null;
        try {
            readWriteFileChannel.read(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLByteBuffer_ReadOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        writeBuffer = null;
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLByteBuffer() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        int result = writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLByteBuffer_NonZeroPosition() throws Exception {
        final int pos = 5;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffer.position(pos);
        int result = writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();
        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, fileOfWriteOnlyFileChannel
                .length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH - pos];
        fis.read(inputBuffer);
        String test = CONTENT.substring(pos);
        assertTrue(Arrays.equals(test.getBytes(), inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_Null() throws Exception {
        ByteBuffer writeBuffer = null;
        try {
            readOnlyFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_Closed() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer, 0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer, 0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_ReadOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffer, 10);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer, 10);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        writeBuffer = null;
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IOException.",
        method = "read",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_Postion_As_Long() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(TEST_BYTES);
        try {
            writeOnlyFileChannel.write(writeBuffer, Long.MAX_VALUE);
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ() throws Exception {
        writeDataToFile(fileOfWriteOnlyFileChannel);
        final int POSITION = 4;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        int result = writeOnlyFileChannel.write(writeBuffer, POSITION);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();
        assertEquals(POSITION + CONTENT_AS_BYTES_LENGTH,
                fileOfWriteOnlyFileChannel.length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[POSITION + CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        byte[] expectedResult = new byte[POSITION + CONTENT_AS_BYTES_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, POSITION,
                CONTENT_AS_BYTES_LENGTH);
        assertTrue(Arrays.equals(expectedResult, inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class, long.class}
    )
    public void test_writeLByteBufferJ_NonZeroPosition() throws Exception {
        final int pos = 5;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffer.position(pos);
        int result = writeOnlyFileChannel.write(writeBuffer, pos);
        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH - pos];
        fis.skip(pos);
        fis.read(inputBuffer);
        String test = CONTENT.substring(pos);
        assertTrue(Arrays.equals(test.getBytes(), inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer_Closed() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer_ReadOnly() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffers);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer_EmptyBuffers() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        try {
            writeOnlyFileChannel.write(writeBuffers);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffers[1] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        long result = writeOnlyFileChannel.write(writeBuffers);
        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[0].position());
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[1].position());
        writeOnlyFileChannel.close();
        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, fileOfWriteOnlyFileChannel
                .length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        byte[] expectedResult = new byte[CONTENT_AS_BYTES_LENGTH * 2];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0,
                CONTENT_AS_BYTES_LENGTH);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult,
                CONTENT_AS_BYTES_LENGTH, CONTENT_AS_BYTES_LENGTH);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_Null() throws Exception {
        ByteBuffer[] writeBuffers = null;
        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_Closed() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NonWritableChannelException, IndexOutOfBoundsException, NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_ReadOnly() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        writeBuffers = new ByteBuffer[2];
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
        }
        readOnlyFileChannel.close();
        writeBuffers = null;
        try {
            readOnlyFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_IndexOutOfBound() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        writeBuffers[1] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        try {
            writeOnlyFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffers[1] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        long result = writeOnlyFileChannel.write(writeBuffers, 0, 2);
        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[0].position());
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[1].position());
        writeOnlyFileChannel.close();
        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, fileOfWriteOnlyFileChannel
                .length());
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        byte[] expectedResult = new byte[CONTENT_AS_BYTES_LENGTH * 2];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0,
                CONTENT_AS_BYTES_LENGTH);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult,
                CONTENT_AS_BYTES_LENGTH, CONTENT_AS_BYTES_LENGTH);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, inputBuffer));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_Closed()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_SourceClosed()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        readByteChannel.close();
        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_IllegalArgument()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 10, -1);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.transferFrom(readByteChannel, -1, 10);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_NonWritable()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_SourceNonReadable()
            throws Exception {
        try {
            readWriteFileChannel.transferFrom(writeOnlyFileChannel, 0, 0);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException e) {
        }
        readWriteFileChannel.transferFrom(writeOnlyFileChannel, 10, 10);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_PositionBeyondSize()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        final int READONLYFILECHANNELPOSITION = 2;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);
        final int POSITION = CONTENT_AS_BYTES_LENGTH * 2;
        final int LENGTH = 5;
        long result = writeOnlyFileChannel.transferFrom(readOnlyFileChannel,
                POSITION, LENGTH);
        assertEquals(0, result);
        assertEquals(0, writeOnlyFileChannel.position());
        assertEquals(READONLYFILECHANNELPOSITION, readOnlyFileChannel
                .position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_FileChannel()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        final int READONLYFILECHANNELPOSITION = 2;
        final int WRITEONLYFILECHANNELPOSITION = 4;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);
        final int POSITION = 3;
        final int LENGTH = 5;
        long result = writeOnlyFileChannel.transferFrom(readOnlyFileChannel,
                POSITION, LENGTH);
        assertEquals(LENGTH, result);
        assertEquals(WRITEONLYFILECHANNELPOSITION, writeOnlyFileChannel
                .position());
        assertEquals(READONLYFILECHANNELPOSITION + LENGTH, readOnlyFileChannel
                .position());
        writeOnlyFileChannel.close();
        final int EXPECTED_LENGTH = POSITION + LENGTH;
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultContent = new byte[EXPECTED_LENGTH];
        fis.read(resultContent);
        byte[] expectedContent = new byte[EXPECTED_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedContent, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, READONLYFILECHANNELPOSITION,
                expectedContent, POSITION, LENGTH);
        assertTrue(Arrays.equals(expectedContent, resultContent));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_DatagramChannel()
            throws Exception {
        datagramChannelReceiver = DatagramChannel.open();
        datagramChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);
        datagramChannelReceiver.connect(datagramChannelSender.socket()
                .getLocalSocketAddress());
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        datagramChannelSender.send(writeBuffer, datagramChannelReceiver
                .socket().getLocalSocketAddress());
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);
        long result = writeOnlyFileChannel.transferFrom(
                datagramChannelReceiver, 0, CONTENT_AS_BYTES_LENGTH);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, writeOnlyFileChannel.position());
        writeOnlyFileChannel.close();
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());
        byte[] resultContent = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(resultContent);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, resultContent));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_SocketChannel()
            throws Exception {
        socketChannelReceiver = SocketChannel.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        socketChannelReceiver.socket().setSoTimeout(TIME_OUT);
        socketChannelReceiver.connect(serverSocketChannel.socket()
                .getLocalSocketAddress());
        serverSocketChannel.socket().setSoTimeout(TIME_OUT);
        socketChannelSender = serverSocketChannel.accept();
        socketChannelSender.socket().setSoTimeout(TIME_OUT);
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        socketChannelSender.write(writeBuffer);
        long result = readWriteFileChannel.transferFrom(socketChannelReceiver,
                0, CONTENT_AS_BYTES_LENGTH);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, readWriteFileChannel.position());
        readWriteFileChannel.close();
        fis = new FileInputStream(fileOfReadWriteFileChannel);
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfReadWriteFileChannel
                .length());
        byte[] resultContent = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(resultContent);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, resultContent));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferFrom",
        args = {java.nio.channels.ReadableByteChannel.class, long.class, long.class}
    )
    public void test_transferFromLReadableByteChannelJJ_Pipe() throws Exception {
        writeDataToFile(fileOfWriteOnlyFileChannel);
        pipe = Pipe.open();
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        pipe.sink().write(writeBuffer);
        final int OFFSET = 2;
        final int LENGTH = 4;
        long result = writeOnlyFileChannel.transferFrom(pipe.source(), OFFSET,
                LENGTH);
        assertEquals(LENGTH, result);
        writeOnlyFileChannel.close();
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultBytes = new byte[OFFSET + LENGTH];
        fis.read(resultBytes);
        byte[] expectedBytes = new byte[OFFSET + LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedBytes, 0, OFFSET);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedBytes, OFFSET, LENGTH);
        assertTrue(Arrays.equals(expectedBytes, resultBytes));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_Null() throws Exception {
        writableByteChannel = null;
        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        readOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferTo(-1, 0, writableByteChannel);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_Closed() throws Exception {
        writableByteChannel = DatagramChannel.open();
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(0, -1, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_SourceClosed()
            throws Exception {
        writableByteChannel = DatagramChannel.open();
        writableByteChannel.close();
        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(0, -1, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_IllegalArgument()
            throws Exception {
        writableByteChannel = DatagramChannel.open();
        try {
            readOnlyFileChannel.transferTo(10, -1, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
        }
        try {
            readWriteFileChannel.transferTo(-1, 10, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_NonReadable()
            throws Exception {
        writableByteChannel = DatagramChannel.open();
        try {
            writeOnlyFileChannel.transferTo(-1, 10, writableByteChannel);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_TargetNonWritable()
            throws Exception {
        try {
            readWriteFileChannel.transferTo(0, 0, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(10, 10, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(-1, 10, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
        try {
            readWriteFileChannel.transferTo(0, -1, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_PositionBeyondSize()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        final int WRITEONLYFILECHANNELPOSITION = 2;
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);
        final int POSITION = CONTENT_AS_BYTES_LENGTH * 2;
        final int LENGTH = 5;
        long result = readOnlyFileChannel.transferTo(POSITION, LENGTH,
                writeOnlyFileChannel);
        assertEquals(0, result);
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(WRITEONLYFILECHANNELPOSITION, writeOnlyFileChannel
                .position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_FileChannel()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        final int READONLYFILECHANNELPOSITION = 2;
        final int WRITEONLYFILECHANNELPOSITION = 4;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);
        final int POSITION = 3;
        final int LENGTH = 5;
        long result = readOnlyFileChannel.transferTo(POSITION, LENGTH,
                writeOnlyFileChannel);
        assertEquals(LENGTH, result);
        assertEquals(READONLYFILECHANNELPOSITION, readOnlyFileChannel
                .position());
        assertEquals(WRITEONLYFILECHANNELPOSITION + LENGTH,
                writeOnlyFileChannel.position());
        writeOnlyFileChannel.close();
        final int EXPECTED_LENGTH = WRITEONLYFILECHANNELPOSITION + LENGTH;
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultContent = new byte[EXPECTED_LENGTH];
        fis.read(resultContent);
        byte[] expectedContent = new byte[EXPECTED_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedContent, 0,
                WRITEONLYFILECHANNELPOSITION);
        System.arraycopy(CONTENT_AS_BYTES, POSITION, expectedContent,
                WRITEONLYFILECHANNELPOSITION, LENGTH);
        assertTrue(Arrays.equals(expectedContent, resultContent));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_SocketChannel()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        socketChannelReceiver = SocketChannel.open();
        socketChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        socketChannelReceiver.socket().setSoTimeout(TIME_OUT);
        socketChannelReceiver.connect(serverSocketChannel.socket()
                .getLocalSocketAddress());
        serverSocketChannel.socket().setSoTimeout(TIME_OUT);
        socketChannelSender = serverSocketChannel.accept();
        socketChannelSender.socket().setSoTimeout(TIME_OUT);
        final int POSITION = 10;
        readOnlyFileChannel.position(POSITION);
        final int OFFSET = 2;
        long result = readOnlyFileChannel.transferTo(OFFSET,
                CONTENT_AS_BYTES_LENGTH * 2, socketChannelSender);
        final int LENGTH = CONTENT_AS_BYTES_LENGTH - OFFSET;
        assertEquals(LENGTH, result);
        assertEquals(POSITION, readOnlyFileChannel.position());
        readOnlyFileChannel.close();
        socketChannelSender.close();
        ByteBuffer readBuffer = ByteBuffer.allocate(LENGTH + 1);
        int totalRead = 0;
        int countRead = 0;
        long beginTime = System.currentTimeMillis();
        while ((countRead = socketChannelReceiver.read(readBuffer)) != -1) {
            totalRead += countRead;
            if (System.currentTimeMillis() - beginTime > TIME_OUT) {
                break;
            }
        }
        assertEquals(LENGTH, totalRead);
        readBuffer.flip();
        for (int i = OFFSET; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_DatagramChannel()
            throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        datagramChannelReceiver = DatagramChannel.open();
        datagramChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        datagramChannelSender.connect(datagramChannelReceiver.socket()
                .getLocalSocketAddress());
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);
        datagramChannelReceiver.connect(datagramChannelSender.socket()
                .getLocalSocketAddress());
        long result = readOnlyFileChannel.transferTo(0,
                CONTENT_AS_BYTES_LENGTH, datagramChannelSender);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, readOnlyFileChannel.position());
        readOnlyFileChannel.close();
        datagramChannelSender.close();
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_AS_BYTES_LENGTH);
        long beginTime = System.currentTimeMillis();
        int totalRead = 0;
        while (totalRead < CONTENT_AS_BYTES_LENGTH) {
            totalRead += datagramChannelReceiver.read(readBuffer);
            if (System.currentTimeMillis() - beginTime > TIME_OUT) {
                break;
            }
        }
        assertEquals(CONTENT_AS_BYTES_LENGTH, totalRead);
        readBuffer.flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "transferTo",
        args = {long.class, long.class, java.nio.channels.WritableByteChannel.class}
    )
    public void test_transferToJJLWritableByteChannel_Pipe() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        pipe = Pipe.open();
        final int OFFSET = 2;
        final int LENGTH = 4;
        long result = readOnlyFileChannel.transferTo(OFFSET, LENGTH, pipe
                .sink());
        assertEquals(LENGTH, result);
        assertEquals(0, readOnlyFileChannel.position());
        readOnlyFileChannel.close();
        ByteBuffer readBuffer = ByteBuffer.allocate(LENGTH);
        result = pipe.source().read(readBuffer);
        assertEquals(LENGTH, result);
        readBuffer.flip();
        for (int i = OFFSET; i < OFFSET + LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }
    public void test_transferTo_couldDelete() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        readOnlyFileChannel.transferTo(0 , 2, writeOnlyFileChannel);
        readOnlyFileChannel.close();
        writeOnlyFileChannel.close();
        boolean rDel = fileOfReadOnlyFileChannel.delete();
        boolean wDel = fileOfWriteOnlyFileChannel.delete();
        assertTrue("File " + readOnlyFileChannel + " exists", rDel);
        assertTrue("File " + writeOnlyFileChannel + " exists", wDel);
    }
    public void test_transferFrom_couldDelete() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);
        writeOnlyFileChannel.transferFrom(readOnlyFileChannel, 0 , 2);
        readOnlyFileChannel.close();
        writeOnlyFileChannel.close();
        boolean rDel = fileOfReadOnlyFileChannel.delete();
        boolean wDel = fileOfWriteOnlyFileChannel.delete();
        assertTrue("File " + readOnlyFileChannel + " exists", rDel);
        assertTrue("File " + writeOnlyFileChannel + " exists", wDel);
    }
    private class MockFileChannel extends FileChannel {
        private boolean isLockCalled = false;
        private boolean isTryLockCalled = false;
        private boolean isReadCalled = false;
        private boolean isWriteCalled = false;
        public void force(boolean arg0) throws IOException {
        }
        public FileLock lock(long position, long size, boolean shared)
                throws IOException {
            if (0 == position && Long.MAX_VALUE == size && false == shared) {
                isLockCalled = true;
            }
            return null;
        }
        public MappedByteBuffer map(MapMode arg0, long arg1, long arg2)
                throws IOException {
            return null;
        }
        public long position() throws IOException {
            return 0;
        }
        public FileChannel position(long arg0) throws IOException {
            return null;
        }
        public int read(ByteBuffer arg0) throws IOException {
            return 0;
        }
        public int read(ByteBuffer arg0, long arg1) throws IOException {
            return 0;
        }
        public long read(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            if (0 == offset && length == srcs.length) {
                isReadCalled = true;
            }
            return 0;
        }
        public long size() throws IOException {
            return 0;
        }
        public long transferFrom(ReadableByteChannel arg0, long arg1, long arg2)
                throws IOException {
            return 0;
        }
        public long transferTo(long arg0, long arg1, WritableByteChannel arg2)
                throws IOException {
            return 0;
        }
        public FileChannel truncate(long arg0) throws IOException {
            return null;
        }
        public FileLock tryLock(long position, long size, boolean shared)
                throws IOException {
            if (0 == position && Long.MAX_VALUE == size && false == shared) {
                isTryLockCalled = true;
            }
            return null;
        }
        public int write(ByteBuffer arg0) throws IOException {
            return 0;
        }
        public int write(ByteBuffer arg0, long arg1) throws IOException {
            return 0;
        }
        public long write(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            if(0 == offset && length == srcs.length){
                isWriteCalled = true;
            }
            return 0;
        }
        protected void implCloseChannel() throws IOException {
        }
    }
}
