class Prologue {
    private final static byte PERFDATA_BIG_ENDIAN    = 0;
    private final static byte PERFDATA_LITTLE_ENDIAN = 1;
    private final static int  PERFDATA_MAGIC         = 0xcafec0c0;
    private class PrologueFieldOffset {
        private final static int SIZEOF_BYTE = 1;
        private final static int SIZEOF_INT  = 4;
        private final static int SIZEOF_LONG = 8;
        private final static int MAGIC_SIZE            = SIZEOF_INT;
        private final static int BYTE_ORDER_SIZE       = SIZEOF_BYTE;
        private final static int MAJOR_SIZE            = SIZEOF_BYTE;
        private final static int MINOR_SIZE            = SIZEOF_BYTE;
        private final static int ACCESSIBLE_SIZE       = SIZEOF_BYTE;
        private final static int USED_SIZE             = SIZEOF_INT;
        private final static int OVERFLOW_SIZE         = SIZEOF_INT;
        private final static int MOD_TIMESTAMP_SIZE    = SIZEOF_LONG;
        private final static int ENTRY_OFFSET_SIZE     = SIZEOF_INT;
        private final static int NUM_ENTRIES_SIZE      = SIZEOF_INT;
        final static int MAGIC          = 0;
        final static int BYTE_ORDER     = MAGIC + MAGIC_SIZE;
        final static int MAJOR_VERSION  = BYTE_ORDER + BYTE_ORDER_SIZE;
        final static int MINOR_VERSION  = MAJOR_VERSION + MAJOR_SIZE;
        final static int ACCESSIBLE     = MINOR_VERSION + MINOR_SIZE;
        final static int USED           = ACCESSIBLE + ACCESSIBLE_SIZE;
        final static int OVERFLOW       = USED + USED_SIZE;
        final static int MOD_TIMESTAMP  = OVERFLOW + OVERFLOW_SIZE;
        final static int ENTRY_OFFSET   = MOD_TIMESTAMP + MOD_TIMESTAMP_SIZE;
        final static int NUM_ENTRIES    = ENTRY_OFFSET + ENTRY_OFFSET_SIZE;
        final static int PROLOGUE_2_0_SIZE = NUM_ENTRIES + NUM_ENTRIES_SIZE;
    }
    private ByteBuffer header;
    private int magic;
    Prologue(ByteBuffer b) {
        this.header = b.duplicate();
        header.order(ByteOrder.BIG_ENDIAN);
        header.position(PrologueFieldOffset.MAGIC);
        magic = header.getInt();
        if (magic != PERFDATA_MAGIC) {
            throw new InstrumentationException("Bad Magic: " +
                                               Integer.toHexString(getMagic()));
        }
        header.order(getByteOrder());
        int major = getMajorVersion();
        int minor = getMinorVersion();
        if (major < 2) {
            throw new InstrumentationException("Unsupported version: " +
                                               major + "." + minor);
        }
        header.limit(PrologueFieldOffset.PROLOGUE_2_0_SIZE);
    }
    public int getMagic() {
        return magic;
    }
    public int getMajorVersion() {
        header.position(PrologueFieldOffset.MAJOR_VERSION);
        return (int)header.get();
    }
    public int getMinorVersion() {
        header.position(PrologueFieldOffset.MINOR_VERSION);
        return (int)header.get();
    }
    public ByteOrder getByteOrder() {
        header.position(PrologueFieldOffset.BYTE_ORDER);
        byte byte_order = header.get();
        if (byte_order == PERFDATA_BIG_ENDIAN) {
            return ByteOrder.BIG_ENDIAN;
        }
        else {
            return ByteOrder.LITTLE_ENDIAN;
        }
    }
    public int getEntryOffset() {
        header.position(PrologueFieldOffset.ENTRY_OFFSET);
        return header.getInt();
    }
    public int getUsed() {
        header.position(PrologueFieldOffset.USED);
        return header.getInt();
    }
    public int getOverflow() {
        header.position(PrologueFieldOffset.OVERFLOW);
        return header.getInt();
    }
    public long getModificationTimeStamp() {
        header.position(PrologueFieldOffset.MOD_TIMESTAMP);
        return header.getLong();
    }
    public int getNumEntries() {
        header.position(PrologueFieldOffset.NUM_ENTRIES);
        return header.getInt();
    }
    public boolean isAccessible() {
        header.position(PrologueFieldOffset.ACCESSIBLE);
        byte b = header.get();
        return (b == 0 ? false : true);
    }
}
