    value = MappedByteBuffer.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "isLoaded",
            args = {}
        )
    }
)
public class MappedByteBufferTest extends DirectByteBufferTest {
    File tmpFile;
    FileChannel fc;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "A regression test for failing to correctly set capacity",
        method = "asIntBuffer",
        args = {}
    )
    public void test_asIntBuffer() {
        int len = buf.capacity();
        assertEquals("Got wrong number of bytes", BUFFER_LENGTH, len); 
        for (int i = 0; i < BUFFER_LENGTH - 20; i++) {
            byte b = buf.get();
            assertEquals("Got wrong byte value", (byte) i, b); 
        }
        IntBuffer ibuffer = buf.asIntBuffer();
        for (int i = BUFFER_LENGTH - 20; i < BUFFER_LENGTH; i+=4) {
            int val = ibuffer.get();
            int res = i * 16777216 + (i + 1) * 65536 + (i + 2) * 256 + (i + 3);
            assertEquals("Got wrong int value", res, val); 
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "force",
        args = {}
    )
    public void test_force() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelRead = fileInputStream.getChannel();
        MappedByteBuffer mmbRead = fileChannelRead.map(MapMode.READ_ONLY, 0,
                fileChannelRead.size());
        mmbRead.force();
        FileInputStream inputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelR = inputStream.getChannel();
        MappedByteBuffer resultRead = fileChannelR.map(MapMode.READ_ONLY, 0,
                fileChannelR.size());
        assertEquals(
                "Invoking force() should have no effect when this buffer was not mapped in read/write mode",
                mmbRead, resultRead);
        RandomAccessFile randomFile = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelReadWrite = randomFile.getChannel();
        MappedByteBuffer mmbReadWrite = fileChannelReadWrite.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelReadWrite.size());
        mmbReadWrite.put((byte) 'o');
        mmbReadWrite.force();
        RandomAccessFile random = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelRW = random.getChannel();
        MappedByteBuffer resultReadWrite = fileChannelRW.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelRW.size());
        assertFalse(mmbReadWrite.equals(resultReadWrite));
        fileChannelRead.close();
        fileChannelR.close();
        fileChannelReadWrite.close();
        fileChannelRW.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "load",
        args = {}
    )
    public void test_load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        FileChannel fileChannelRead = fileInputStream.getChannel();
        MappedByteBuffer mmbRead = fileChannelRead.map(MapMode.READ_ONLY, 0,
                fileChannelRead.size());
        assertEquals(mmbRead, mmbRead.load());
        RandomAccessFile randomFile = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannelReadWrite = randomFile.getChannel();
        MappedByteBuffer mmbReadWrite = fileChannelReadWrite.map(
                FileChannel.MapMode.READ_WRITE, 0, fileChannelReadWrite.size());
        assertEquals(mmbReadWrite, mmbReadWrite.load());
        fileChannelRead.close();
        fileChannelReadWrite.close();
    }
    protected void setUp() throws IOException {
        tmpFile = File.createTempFile("MappedByteBufferTest", ".tmp");  
        tmpFile.createNewFile();
        tmpFile.deleteOnExit();
        fillTempFile();
        RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
        fc = raf.getChannel();
        capacity = (int) fc.size();
        buf = fc.map(FileChannel.MapMode.READ_WRITE, 0, capacity);
        baseBuf = buf;
    }
    protected void tearDown() throws IOException {
        fc.close();
    }
    private void fillTempFile() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        FileChannel fileChannel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_LENGTH);
        loadTestData1(byteBuffer);
        byteBuffer.clear();
        fileChannel.write(byteBuffer);
        fileChannel.close();
        fileOutputStream.close();
    }
}