public class CorbaContactInfoListImpl
    implements
        CorbaContactInfoList
{
    protected ORB orb;
    protected LocalClientRequestDispatcher LocalClientRequestDispatcher;
    protected IOR targetIOR;
    protected IOR effectiveTargetIOR;
    protected List effectiveTargetIORContactInfoList;
    protected ContactInfo primaryContactInfo;
    public CorbaContactInfoListImpl(ORB orb)
    {
        this.orb = orb;
    }
    public CorbaContactInfoListImpl(ORB orb, IOR targetIOR)
    {
        this(orb);
        setTargetIOR(targetIOR);
    }
    public synchronized Iterator iterator()
    {
        createContactInfoList();
        return new CorbaContactInfoListIteratorImpl(
            orb, this, primaryContactInfo,
            effectiveTargetIORContactInfoList);
    }
    public synchronized void setTargetIOR(IOR targetIOR)
    {
        this.targetIOR = targetIOR;
        setEffectiveTargetIOR(targetIOR);
    }
    public synchronized IOR getTargetIOR()
    {
        return targetIOR;
    }
    public synchronized void setEffectiveTargetIOR(IOR effectiveTargetIOR)
    {
        this.effectiveTargetIOR = effectiveTargetIOR;
        effectiveTargetIORContactInfoList = null;
        if (primaryContactInfo != null &&
            orb.getORBData().getIIOPPrimaryToContactInfo() != null)
        {
            orb.getORBData().getIIOPPrimaryToContactInfo()
                .reset(primaryContactInfo);
        }
        primaryContactInfo = null;
        setLocalSubcontract();
    }
    public synchronized IOR getEffectiveTargetIOR()
    {
        return effectiveTargetIOR;
    }
    public synchronized LocalClientRequestDispatcher getLocalClientRequestDispatcher()
    {
        return LocalClientRequestDispatcher;
    }
    public synchronized int hashCode()
    {
        return targetIOR.hashCode();
    }
    protected void createContactInfoList()
    {
        if (effectiveTargetIORContactInfoList != null) {
            return;
        }
        effectiveTargetIORContactInfoList = new ArrayList();
        IIOPProfile iiopProfile = effectiveTargetIOR.getProfile();
        String hostname =
            ((IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate())
                .getPrimaryAddress().getHost().toLowerCase();
        int    port     =
            ((IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate())
                .getPrimaryAddress().getPort();
        primaryContactInfo =
            createContactInfo(SocketInfo.IIOP_CLEAR_TEXT, hostname, port);
        if (iiopProfile.isLocal()) {
            ContactInfo contactInfo = new SharedCDRContactInfoImpl(
                orb, this, effectiveTargetIOR,
                orb.getORBData().getGIOPAddressDisposition());
            effectiveTargetIORContactInfoList.add(contactInfo);
        } else {
            addRemoteContactInfos(effectiveTargetIOR,
                                  effectiveTargetIORContactInfoList);
        }
    }
    protected void addRemoteContactInfos(
        IOR  effectiveTargetIOR,
        List effectiveTargetIORContactInfoList)
    {
        ContactInfo contactInfo;
        List socketInfos = orb.getORBData()
            .getIORToSocketInfo().getSocketInfo(effectiveTargetIOR);
        Iterator iterator = socketInfos.iterator();
        while (iterator.hasNext()) {
            SocketInfo socketInfo = (SocketInfo) iterator.next();
            String type = socketInfo.getType();
            String host = socketInfo.getHost().toLowerCase();
            int    port = socketInfo.getPort();
            contactInfo = createContactInfo(type, host, port);
            effectiveTargetIORContactInfoList.add(contactInfo);
        }
    }
    protected ContactInfo createContactInfo(String type,
                                            String hostname, int port)
    {
        return new SocketOrChannelContactInfoImpl(
            orb, this,
            effectiveTargetIOR,
            orb.getORBData().getGIOPAddressDisposition(),
            type, hostname, port);
    }
    protected void setLocalSubcontract()
    {
        if (!effectiveTargetIOR.getProfile().isLocal()) {
            LocalClientRequestDispatcher = new NotLocalLocalCRDImpl();
            return;
        }
        int scid = effectiveTargetIOR.getProfile().getObjectKeyTemplate().
            getSubcontractId() ;
        LocalClientRequestDispatcherFactory lcsf = orb.getRequestDispatcherRegistry().getLocalClientRequestDispatcherFactory( scid ) ;
        LocalClientRequestDispatcher = lcsf.create( scid, effectiveTargetIOR ) ;
    }
}
