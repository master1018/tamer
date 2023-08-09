public abstract class CorbaContactInfoBase
    implements
        CorbaContactInfo
{
    protected ORB orb;
    protected CorbaContactInfoList contactInfoList;
    protected IOR effectiveTargetIOR;
    protected short addressingDisposition;
    protected OutboundConnectionCache connectionCache;
    public Broker getBroker()
    {
        return orb;
    }
    public ContactInfoList getContactInfoList()
    {
        return contactInfoList;
    }
    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        int scid =
            getEffectiveProfile().getObjectKeyTemplate().getSubcontractId() ;
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
        return scr.getClientRequestDispatcher( scid ) ;
    }
    public void setConnectionCache(OutboundConnectionCache connectionCache)
    {
        this.connectionCache = connectionCache;
    }
    public OutboundConnectionCache getConnectionCache()
    {
        return connectionCache;
    }
    public MessageMediator createMessageMediator(Broker broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay)
    {
        CorbaMessageMediator messageMediator =
            new CorbaMessageMediatorImpl(
                (ORB) broker,
                contactInfo,
                connection,
                GIOPVersion.chooseRequestVersion( (ORB)broker,
                     effectiveTargetIOR),
                effectiveTargetIOR,
                ((CorbaConnection)connection).getNextRequestId(),
                getAddressingDisposition(),
                methodName,
                isOneWay);
        return messageMediator;
    }
    public MessageMediator createMessageMediator(Broker broker,Connection conn)
    {
        ORB orb = (ORB) broker;
        CorbaConnection connection = (CorbaConnection) conn;
        if (orb.transportDebugFlag) {
            if (connection.shouldReadGiopHeaderOnly()) {
                dprint(
                ".createMessageMediator: waiting for message header on connection: "
                + connection);
            } else {
                dprint(
                ".createMessageMediator: waiting for message on connection: "
                + connection);
            }
        }
        Message msg = null;
        if (connection.shouldReadGiopHeaderOnly()) {
            msg = MessageBase.readGIOPHeader(orb, connection);
        } else {
            msg = MessageBase.readGIOPMessage(orb, connection);
        }
        ByteBuffer byteBuffer = msg.getByteBuffer();
        msg.setByteBuffer(null);
        CorbaMessageMediator messageMediator =
            new CorbaMessageMediatorImpl(orb, connection, msg, byteBuffer);
        return messageMediator;
    }
    public MessageMediator finishCreatingMessageMediator(Broker broker,
                               Connection conn, MessageMediator messageMediator)
    {
        ORB orb = (ORB) broker;
        CorbaConnection connection = (CorbaConnection) conn;
        CorbaMessageMediator corbaMessageMediator =
                      (CorbaMessageMediator)messageMediator;
        if (orb.transportDebugFlag) {
            dprint(
            ".finishCreatingMessageMediator: waiting for message body on connection: "
                + connection);
        }
        Message msg = corbaMessageMediator.getDispatchHeader();
        msg.setByteBuffer(corbaMessageMediator.getDispatchBuffer());
        msg = MessageBase.readGIOPBody(orb, connection, msg);
        ByteBuffer byteBuffer = msg.getByteBuffer();
        msg.setByteBuffer(null);
        corbaMessageMediator.setDispatchHeader(msg);
        corbaMessageMediator.setDispatchBuffer(byteBuffer);
        return corbaMessageMediator;
    }
    public OutputObject createOutputObject(MessageMediator messageMediator)
    {
        CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
            messageMediator;
        OutputObject outputObject =
            new CDROutputObject(orb, messageMediator,
                                corbaMessageMediator.getRequestHeader(),
                                corbaMessageMediator.getStreamFormatVersion());
        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }
    public InputObject createInputObject(Broker broker,
                                         MessageMediator messageMediator)
    {
        CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
            messageMediator;
        return new CDRInputObject((ORB)broker,
                                  (CorbaConnection)messageMediator.getConnection(),
                                  corbaMessageMediator.getDispatchBuffer(),
                                  corbaMessageMediator.getDispatchHeader());
    }
    public short getAddressingDisposition()
    {
        return addressingDisposition;
    }
    public void setAddressingDisposition(short addressingDisposition)
    {
        this.addressingDisposition = addressingDisposition;
    }
    public IOR getTargetIOR()
    {
        return  contactInfoList.getTargetIOR();
    }
    public IOR getEffectiveTargetIOR()
    {
        return effectiveTargetIOR ;
    }
    public IIOPProfile getEffectiveProfile()
    {
        return effectiveTargetIOR.getProfile();
    }
    public String toString()
    {
        return
            "CorbaContactInfoBase["
            + "]";
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaContactInfoBase", msg);
    }
}
