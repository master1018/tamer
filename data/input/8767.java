public final class LocateReplyMessage_1_1 extends Message_1_1
        implements LocateReplyMessage {
    private ORB orb = null;
    private int request_id = (int) 0;
    private int reply_status = (int) 0;
    private IOR ior = null;
    LocateReplyMessage_1_1(ORB orb) {
        this.orb = orb;
    }
    LocateReplyMessage_1_1(ORB orb, int _request_id,
            int _reply_status, IOR _ior) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_1, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPLocateReply, 0);
        this.orb = orb;
        request_id = _request_id;
        reply_status = _reply_status;
        ior = _ior;
    }
    public int getRequestId() {
        return this.request_id;
    }
    public int getReplyStatus() {
        return this.reply_status;
    }
    public short getAddrDisposition() {
        return KeyAddr.value;
    }
    public SystemException getSystemException(String message) {
        return null; 
    }
    public IOR getIOR() {
        return this.ior;
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.reply_status = istream.read_long();
        isValidReplyStatus(this.reply_status); 
        if (this.reply_status == OBJECT_FORWARD) {
            CDRInputStream cdr = (CDRInputStream) istream;
            this.ior = IORFactories.makeIOR( cdr ) ;
        }
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        ostream.write_long(this.reply_status);
    }
    public static void isValidReplyStatus(int replyStatus) {
        switch (replyStatus) {
        case UNKNOWN_OBJECT :
        case OBJECT_HERE :
        case OBJECT_FORWARD :
            break;
        default :
            ORBUtilSystemException localWrapper = ORBUtilSystemException.get(
                CORBALogDomains.RPC_PROTOCOL ) ;
            throw localWrapper.illegalReplyStatus( CompletionStatus.COMPLETED_MAYBE);
        }
    }
    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} 
