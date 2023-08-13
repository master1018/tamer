public final class CancelRequestMessage_1_0 extends Message_1_0
        implements CancelRequestMessage {
    private int request_id = (int) 0;
    CancelRequestMessage_1_0() {}
    CancelRequestMessage_1_0(int _request_id) {
        super(Message.GIOPBigMagic, false, Message.GIOPCancelRequest,
              CANCEL_REQ_MSG_SIZE);
        request_id = _request_id;
    }
    public int getRequestId() {
        return this.request_id;
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
