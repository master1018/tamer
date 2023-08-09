public abstract class AbstractPerfDataBufferPrologue {
    protected ByteBuffer byteBuffer;
    final static int PERFDATA_PROLOG_OFFSET=0;
    final static int PERFDATA_PROLOG_MAGIC_OFFSET=0;
    final static int PERFDATA_PROLOG_BYTEORDER_OFFSET=4;
    final static int PERFDATA_PROLOG_BYTEORDER_SIZE=1;         
    final static int PERFDATA_PROLOG_MAJOR_OFFSET=5;
    final static int PERFDATA_PROLOG_MAJOR_SIZE=1;             
    final static int PERFDATA_PROLOG_MINOR_OFFSET=6;
    final static int PERFDATA_PROLOG_MINOR_SIZE=1;             
    final static int PERFDATA_PROLOG_RESERVEDB1_OFFSET=7;
    final static int PERFDATA_PROLOG_RESERVEDB1_SIZE=1;        
    final static int PERFDATA_PROLOG_SIZE=8;   
    final static byte PERFDATA_BIG_ENDIAN=0;
    final static byte PERFDATA_LITTLE_ENDIAN=1;
    final static int  PERFDATA_MAGIC = 0xcafec0c0;
    public final static String PERFDATA_MAJOR_NAME =
            "sun.perfdata.majorVersion";
    public final static String PERFDATA_MINOR_NAME =
            "sun.perfdata.minorVersion";
    public AbstractPerfDataBufferPrologue(ByteBuffer byteBuffer)
           throws MonitorException  {
        this.byteBuffer = byteBuffer.duplicate();
        if (getMagic() != PERFDATA_MAGIC) {
            throw new MonitorVersionException(
                    "Bad Magic: " + Integer.toHexString(getMagic()));
        }
        this.byteBuffer.order(getByteOrder());
    }
    public int getMagic() {
        ByteOrder order = byteBuffer.order();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.position(PERFDATA_PROLOG_MAGIC_OFFSET);
        int magic = byteBuffer.getInt();
        byteBuffer.order(order);
        return magic;
    }
    public ByteOrder getByteOrder() {
        byteBuffer.position(PERFDATA_PROLOG_BYTEORDER_OFFSET);
        byte byte_order = byteBuffer.get();
        if (byte_order == PERFDATA_BIG_ENDIAN) {
            return ByteOrder.BIG_ENDIAN;
        } else {
            return ByteOrder.LITTLE_ENDIAN;
        }
    }
    public int getMajorVersion() {
        byteBuffer.position(PERFDATA_PROLOG_MAJOR_OFFSET);
        return (int)byteBuffer.get();
    }
    public int getMinorVersion() {
        byteBuffer.position(PERFDATA_PROLOG_MINOR_OFFSET);
        return (int)byteBuffer.get();
    }
    public abstract boolean isAccessible();
    public abstract boolean supportsAccessible();
    public int getSize() {
        return PERFDATA_PROLOG_SIZE;  
    }
    public IntBuffer majorVersionBuffer() {
        int[] holder = new int[1];
        holder[0] = getMajorVersion();
        IntBuffer ib = IntBuffer.wrap(holder);
        ib.limit(1);
        return ib;
      }
    public IntBuffer minorVersionBuffer() {
        int[] holder = new int[1];
        holder[0] = getMinorVersion();
        IntBuffer ib = IntBuffer.wrap(holder);
        ib.limit(1);
        return ib;
    }
    public static int getMagic(ByteBuffer bb) {
        int position = bb.position();
        ByteOrder order = bb.order();
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.position(PERFDATA_PROLOG_MAGIC_OFFSET);
        int magic = bb.getInt();
        bb.order(order);
        bb.position(position);
        return magic;
    }
    public static int getMajorVersion(ByteBuffer bb) {
        int position = bb.position();
        bb.position(PERFDATA_PROLOG_MAJOR_OFFSET);
        int major = (int) bb.get();
        bb.position(position);
        return major;
    }
    public static int getMinorVersion(ByteBuffer bb) {
        int position = bb.position();
        bb.position(PERFDATA_PROLOG_MINOR_OFFSET);
        int minor = (int)bb.get();
        bb.position(position);
        return minor;
    }
    public static ByteOrder getByteOrder(ByteBuffer bb) {
        int position = bb.position();
        bb.position(PERFDATA_PROLOG_BYTEORDER_OFFSET);
        ByteOrder order = (bb.get() == PERFDATA_BIG_ENDIAN)
                          ? ByteOrder.BIG_ENDIAN
                          : ByteOrder.LITTLE_ENDIAN;
        bb.position(position);
        return order;
    }
}
