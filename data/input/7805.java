public class PerfDataBufferPrologue extends AbstractPerfDataBufferPrologue {
    private static final int SUPPORTED_MAJOR_VERSION = 1;
    private static final int SUPPORTED_MINOR_VERSION = 0;
    final static int PERFDATA_PROLOG_USED_OFFSET=8;
    final static int PERFDATA_PROLOG_USED_SIZE=4;              
    final static int PERFDATA_PROLOG_OVERFLOW_OFFSET=12;
    final static int PERFDATA_PROLOG_OVERFLOW_SIZE=4;          
    final static int PERFDATA_PROLOG_MODTIMESTAMP_OFFSET=16;
    final static int PERFDATA_PROLOG_MODTIMESTAMP_SIZE=8;      
    final static int PERFDATA_PROLOG_SIZE=24;  
    final static String PERFDATA_BUFFER_SIZE_NAME  = "sun.perfdata.size";
    final static String PERFDATA_BUFFER_USED_NAME  = "sun.perfdata.used";
    final static String PERFDATA_OVERFLOW_NAME     = "sun.perfdata.overflow";
    final static String PERFDATA_MODTIMESTAMP_NAME = "sun.perfdata.timestamp";
    public PerfDataBufferPrologue(ByteBuffer byteBuffer)
           throws MonitorException  {
        super(byteBuffer);
        assert ((getMajorVersion() == 1) && (getMinorVersion() == 0));
    }
    public boolean supportsAccessible() {
        return false;
    }
    public boolean isAccessible() {
        return true;
    }
    public int getUsed() {
        byteBuffer.position(PERFDATA_PROLOG_USED_OFFSET);
        return byteBuffer.getInt();
    }
    public int getBufferSize() {
        return byteBuffer.capacity();
    }
    public int getOverflow() {
        byteBuffer.position(PERFDATA_PROLOG_OVERFLOW_OFFSET);
        return byteBuffer.getInt();
    }
    public long getModificationTimeStamp() {
        byteBuffer.position(PERFDATA_PROLOG_MODTIMESTAMP_OFFSET);
        return byteBuffer.getLong();
    }
    public int getSize() {
        return PERFDATA_PROLOG_SIZE;  
    }
    public IntBuffer usedBuffer() {
        byteBuffer.position(PERFDATA_PROLOG_USED_OFFSET);
        IntBuffer ib = byteBuffer.asIntBuffer();
        ib.limit(1);
        return ib;
    }
    public IntBuffer sizeBuffer() {
        IntBuffer ib = IntBuffer.allocate(1);
        ib.put(byteBuffer.capacity());
        return ib;
    }
    public IntBuffer overflowBuffer() {
        byteBuffer.position(PERFDATA_PROLOG_OVERFLOW_OFFSET);
        IntBuffer ib = byteBuffer.asIntBuffer();
        ib.limit(1);
        return ib;
    }
    public LongBuffer modificationTimeStampBuffer() {
        byteBuffer.position(PERFDATA_PROLOG_MODTIMESTAMP_OFFSET);
        LongBuffer lb = byteBuffer.asLongBuffer();
        lb.limit(1);
        return lb;
    }
}
