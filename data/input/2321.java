public class Message_1_0
        extends com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase {
    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.get( CORBALogDomains.RPC_PROTOCOL ) ;
    int magic = (int) 0;
    GIOPVersion GIOP_version = null;
    boolean byte_order = false;
    byte message_type = (byte) 0;
    int message_size = (int) 0;
    Message_1_0() {
    }
    Message_1_0(int _magic, boolean _byte_order, byte _message_type,
            int _message_size) {
        magic = _magic;
        GIOP_version = GIOPVersion.V1_0;
        byte_order = _byte_order;
        message_type = _message_type;
        message_size = _message_size;
    }
    public GIOPVersion getGIOPVersion() {
        return this.GIOP_version;
    }
    public int getType() {
        return this.message_type;
    }
    public int getSize() {
            return this.message_size;
    }
    public boolean isLittleEndian() {
        return this.byte_order;
    }
    public boolean moreFragmentsToFollow() {
        return false;
    }
    public void setSize(ByteBuffer byteBuffer, int size) {
            this.message_size = size;
            int patch = size - GIOPMessageHeaderLength;
        if (!isLittleEndian()) {
            byteBuffer.put(8,  (byte)((patch >>> 24) & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(11, (byte)((patch >>> 0)  & 0xFF));
        } else {
            byteBuffer.put(8,  (byte)((patch >>> 0)  & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(11, (byte)((patch >>> 24) & 0xFF));
        }
    }
    public FragmentMessage createFragmentMessage() {
        throw wrapper.fragmentationDisallowed(
            CompletionStatus.COMPLETED_MAYBE);
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_long(this.magic);
        nullCheck(this.GIOP_version);
        this.GIOP_version.write(ostream);
        ostream.write_boolean(this.byte_order);
        ostream.write_octet(this.message_type);
        ostream.write_ulong(this.message_size);
    }
} 
