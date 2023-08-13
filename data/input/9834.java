public final class LocateReplyMessage_1_2 extends Message_1_2
        implements LocateReplyMessage {
    private ORB orb = null;
    private ORBUtilSystemException wrapper = null ;
    private int reply_status = (int) 0;
    private IOR ior = null;
    private String exClassName = null;
    private int minorCode = (int) 0;
    private CompletionStatus completionStatus = null;
    private short addrDisposition = KeyAddr.value; 
    LocateReplyMessage_1_2(ORB orb) {
        this.orb = orb;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
    }
    LocateReplyMessage_1_2(ORB orb, int _request_id,
            int _reply_status, IOR _ior) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPLocateReply, 0);
        this.orb = orb;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
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
        return this.addrDisposition;
    }
    public SystemException getSystemException(String message) {
        return MessageBase.getSystemException(
            exClassName, minorCode, completionStatus, message, wrapper);
    }
    public IOR getIOR() {
        return this.ior;
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.reply_status = istream.read_long();
        isValidReplyStatus(this.reply_status); 
        if (this.reply_status == LOC_SYSTEM_EXCEPTION) {
            String reposId = istream.read_string();
            this.exClassName = ORBUtility.classNameOf(reposId);
            this.minorCode = istream.read_long();
            int status = istream.read_long();
            switch (status) {
            case CompletionStatus._COMPLETED_YES:
                this.completionStatus = CompletionStatus.COMPLETED_YES;
                break;
            case CompletionStatus._COMPLETED_NO:
                this.completionStatus = CompletionStatus.COMPLETED_NO;
                break;
            case CompletionStatus._COMPLETED_MAYBE:
                this.completionStatus = CompletionStatus.COMPLETED_MAYBE;
                break;
            default:
                throw wrapper.badCompletionStatusInLocateReply(
                    CompletionStatus.COMPLETED_MAYBE, new Integer(status) );
            }
        } else if ( (this.reply_status == OBJECT_FORWARD) ||
                (this.reply_status == OBJECT_FORWARD_PERM) ){
            CDRInputStream cdr = (CDRInputStream) istream;
            this.ior = IORFactories.makeIOR( cdr ) ;
        }  else if (this.reply_status == LOC_NEEDS_ADDRESSING_MODE) {
            this.addrDisposition = AddressingDispositionHelper.read(istream);
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
        case OBJECT_FORWARD_PERM :
        case LOC_SYSTEM_EXCEPTION :
        case LOC_NEEDS_ADDRESSING_MODE :
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
