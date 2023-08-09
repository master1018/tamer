public final class LocateReplyMessage_1_0 extends Message_1_0
        implements LocateReplyMessage {
    private ORB orb = null;
    private int request_id = (int) 0;
    private int locate_status = (int) 0;
    private IOR ior = null;
    LocateReplyMessage_1_0(ORB orb) {
        this.orb = orb;
    }
    LocateReplyMessage_1_0(ORB orb, int _request_id,
            int _locate_status, IOR _ior) {
        super(Message.GIOPBigMagic, false, Message.GIOPLocateReply, 0);
        this.orb = orb;
        request_id = _request_id;
        locate_status = _locate_status;
        ior = _ior;
    }
    public int getRequestId() {
        return this.request_id;
    }
    public int getReplyStatus() {
        return this.locate_status;
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
        this.locate_status = istream.read_long();
        isValidReplyStatus(this.locate_status); 
        if (this.locate_status == OBJECT_FORWARD) {
            CDRInputStream cdr = (CDRInputStream) istream;
            this.ior = IORFactories.makeIOR( cdr ) ;
        }
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        ostream.write_long(this.locate_status);
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
