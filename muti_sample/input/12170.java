public class SocketFactoryContactInfoListIteratorImpl
    extends CorbaContactInfoListIteratorImpl
{
    private SocketInfo socketInfoCookie;
    public SocketFactoryContactInfoListIteratorImpl(
        ORB orb,
        CorbaContactInfoList corbaContactInfoList)
    {
        super(orb, corbaContactInfoList, null, null);
    }
    public boolean hasNext()
    {
        return true;
    }
    public Object next()
    {
        if (contactInfoList.getEffectiveTargetIOR().getProfile().isLocal()){
            return new SharedCDRContactInfoImpl(
                orb, contactInfoList,
                contactInfoList.getEffectiveTargetIOR(),
                orb.getORBData().getGIOPAddressDisposition());
        } else {
            return new SocketFactoryContactInfoImpl(
                orb, contactInfoList,
                contactInfoList.getEffectiveTargetIOR(),
                orb.getORBData().getGIOPAddressDisposition(),
                socketInfoCookie);
        }
    }
    public boolean reportException(ContactInfo contactInfo,
                                   RuntimeException ex)
    {
        this.failureContactInfo = (CorbaContactInfo)contactInfo;
        this.failureException = ex;
        if (ex instanceof org.omg.CORBA.COMM_FAILURE) {
            if (ex.getCause() instanceof GetEndPointInfoAgainException) {
                socketInfoCookie =
                    ((GetEndPointInfoAgainException) ex.getCause())
                    .getEndPointInfo();
                return true;
            }
            SystemException se = (SystemException) ex;
            if (se.completed == CompletionStatus.COMPLETED_NO) {
                if (contactInfoList.getEffectiveTargetIOR() !=
                    contactInfoList.getTargetIOR())
                {
                    contactInfoList.setEffectiveTargetIOR(
                        contactInfoList.getTargetIOR());
                    return true;
                }
            }
        }
        return false;
    }
}
