public class SocketFactoryContactInfoImpl
    extends
        SocketOrChannelContactInfoImpl
{
    protected ORBUtilSystemException wrapper;
    protected SocketInfo socketInfo;
    public SocketFactoryContactInfoImpl()
    {
    }
    public SocketFactoryContactInfoImpl(
        ORB orb,
        CorbaContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition,
        SocketInfo cookie)
    {
        super(orb, contactInfoList);
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_TRANSPORT ) ;
        socketInfo =
            orb.getORBData().getLegacySocketFactory()
                .getEndPointInfo(orb, effectiveTargetIOR, cookie);
        socketType = socketInfo.getType();
        hostname = socketInfo.getHost();
        port = socketInfo.getPort();
    }
    public Connection createConnection()
    {
        Connection connection =
            new SocketFactoryConnectionImpl(
                orb, this,
                orb.getORBData().connectionSocketUseSelectThreadToWait(),
                orb.getORBData().connectionSocketUseWorkerThreadForEvent());
        return connection;
    }
    public String toString()
    {
        return
            "SocketFactoryContactInfoImpl["
            + socketType + " "
            + hostname + " "
            + port
            + "]";
    }
}
