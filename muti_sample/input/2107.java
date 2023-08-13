public class Message_1_1
        extends com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase {
    final static int UPPER_THREE_BYTES_OF_INT_MASK = 0xFF;
    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.get( CORBALogDomains.RPC_PROTOCOL ) ;
    int magic = (int) 0;
    GIOPVersion GIOP_version = null;
    byte flags = (byte) 0;
    byte message_type = (byte) 0;
    int message_size = (int) 0;
    Message_1_1() {
    }
    Message_1_1(int _magic, GIOPVersion _GIOP_version, byte _flags,
            byte _message_type, int _message_size) {
        magic = _magic;
        GIOP_version = _GIOP_version;
        flags = _flags;
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
        return ((this.flags & LITTLE_ENDIAN_BIT) == LITTLE_ENDIAN_BIT);
    }
    public boolean moreFragmentsToFollow() {
        return ( (this.flags & MORE_FRAGMENTS_BIT) == MORE_FRAGMENTS_BIT );
    }
    public void setThreadPoolToUse(int poolToUse) {
        int tmpFlags = poolToUse << 2;
        tmpFlags &= UPPER_THREE_BYTES_OF_INT_MASK;
        tmpFlags |= flags;
        flags = (byte)tmpFlags;
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
        switch (this.message_type) {
        case GIOPCancelRequest :
        case GIOPCloseConnection :
        case GIOPMessageError :
            throw wrapper.fragmentationDisallowed(
                CompletionStatus.COMPLETED_MAYBE);
        case GIOPLocateRequest :
        case GIOPLocateReply :
            if (this.GIOP_version.equals(GIOPVersion.V1_1)) {
                throw wrapper.fragmentationDisallowed(
                    CompletionStatus.COMPLETED_MAYBE);
            }
            break;
        }
        if (this.GIOP_version.equals(GIOPVersion.V1_1)) {
            return new FragmentMessage_1_1(this);
        } else if (this.GIOP_version.equals(GIOPVersion.V1_2)) {
            return new FragmentMessage_1_2(this);
        }
        throw wrapper.giopVersionError( CompletionStatus.COMPLETED_MAYBE);
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_long(this.magic);
        nullCheck(this.GIOP_version);
        this.GIOP_version.write(ostream);
        ostream.write_octet(this.flags);
        ostream.write_octet(this.message_type);
        ostream.write_ulong(this.message_size);
    }
} 
