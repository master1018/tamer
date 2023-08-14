public final class LocateRequestMessage_1_2 extends Message_1_2
        implements LocateRequestMessage {
    private ORB orb = null;
    private ObjectKey objectKey = null;
    private TargetAddress target = null;
    LocateRequestMessage_1_2(ORB orb) {
        this.orb = orb;
    }
    LocateRequestMessage_1_2(ORB orb, int _request_id, TargetAddress _target) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPLocateRequest, 0);
        this.orb = orb;
        request_id = _request_id;
        target = _target;
    }
    public int getRequestId() {
        return this.request_id;
    }
    public ObjectKey getObjectKey() {
        if (this.objectKey == null) {
            this.objectKey = MessageBase.extractObjectKey(target, orb);
        }
        return this.objectKey;
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.target = TargetAddressHelper.read(istream);
        getObjectKey(); 
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong (this.request_id);
        nullCheck(this.target);
        TargetAddressHelper.write(ostream, this.target);
    }
    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} 
