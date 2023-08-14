public final class FragmentMessage_1_2 extends Message_1_2
        implements FragmentMessage {
    FragmentMessage_1_2() {}
    FragmentMessage_1_2(int _request_id) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPFragment, 0);
        this.message_type = GIOPFragment;
        request_id = _request_id;
    }
    FragmentMessage_1_2(Message_1_1 msg12) {
        this.magic = msg12.magic;
        this.GIOP_version = msg12.GIOP_version;
        this.flags = msg12.flags;
        this.message_type = GIOPFragment;
        this.message_size = 0;
        switch (msg12.message_type) {
        case GIOPRequest :
            this.request_id = ((RequestMessage) msg12).getRequestId();
            break;
        case GIOPReply :
            this.request_id = ((ReplyMessage) msg12).getRequestId();
            break;
        case GIOPLocateRequest :
            this.request_id = ((LocateRequestMessage) msg12).getRequestId();
            break;
        case GIOPLocateReply :
            this.request_id = ((LocateReplyMessage) msg12).getRequestId();
            break;
        case GIOPFragment :
            this.request_id = ((FragmentMessage) msg12).getRequestId();
            break;
        }
    }
    public int getRequestId() {
        return this.request_id;
    }
    public int getHeaderLength() {
        return GIOPMessageHeaderLength + 4;
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
    }
    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} 
