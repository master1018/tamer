public class SharedCDRContactInfoImpl
    extends
        CorbaContactInfoBase
{
    private static int requestId = 0;
    protected ORBUtilSystemException wrapper;
    public SharedCDRContactInfoImpl(
        ORB orb,
        CorbaContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition)
    {
        this.orb = orb;
        this.contactInfoList = contactInfoList;
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }
    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        return new SharedCDRClientRequestDispatcherImpl();
    }
    public boolean isConnectionBased()
    {
        return false;
    }
    public boolean shouldCacheConnection()
    {
        return false;
    }
    public String getConnectionCacheType()
    {
        throw getWrapper().methodShouldNotBeCalled();
    }
    public Connection createConnection()
    {
        throw getWrapper().methodShouldNotBeCalled();
    }
    public MessageMediator createMessageMediator(Broker broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay)
    {
        if (connection != null) {
            throw new RuntimeException("connection is not null");
        }
        CorbaMessageMediator messageMediator =
            new CorbaMessageMediatorImpl(
                (ORB) broker,
                contactInfo,
                null, 
                GIOPVersion.chooseRequestVersion( (ORB)broker,
                     effectiveTargetIOR),
                effectiveTargetIOR,
                requestId++, 
                getAddressingDisposition(),
                methodName,
                isOneWay);
        return messageMediator;
    }
    public OutputObject createOutputObject(MessageMediator messageMediator)
    {
        CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
            messageMediator;
        OutputObject outputObject =
            new CDROutputObject(orb, messageMediator,
                                corbaMessageMediator.getRequestHeader(),
                                corbaMessageMediator.getStreamFormatVersion(),
                                BufferManagerFactory.GROW);
        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }
    public String getMonitoringName()
    {
        throw getWrapper().methodShouldNotBeCalled();
    }
    public String toString()
    {
        return
            "SharedCDRContactInfoImpl["
            + "]";
    }
    protected ORBUtilSystemException getWrapper()
    {
        if (wrapper == null) {
            wrapper = ORBUtilSystemException.get( orb,
                          CORBALogDomains.RPC_TRANSPORT ) ;
        }
        return wrapper;
    }
}
