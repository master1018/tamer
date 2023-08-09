public class CorbaContactInfoListIteratorImpl
    implements
        CorbaContactInfoListIterator
{
    protected ORB orb;
    protected CorbaContactInfoList contactInfoList;
    protected CorbaContactInfo successContactInfo;
    protected CorbaContactInfo failureContactInfo;
    protected RuntimeException failureException;
    protected Iterator effectiveTargetIORIterator;
    protected CorbaContactInfo previousContactInfo;
    protected boolean isAddrDispositionRetry;
    protected IIOPPrimaryToContactInfo primaryToContactInfo;
    protected ContactInfo primaryContactInfo;
    protected List listOfContactInfos;
    public CorbaContactInfoListIteratorImpl(
        ORB orb,
        CorbaContactInfoList corbaContactInfoList,
        ContactInfo primaryContactInfo,
        List listOfContactInfos)
    {
        this.orb = orb;
        this.contactInfoList = corbaContactInfoList;
        this.primaryContactInfo = primaryContactInfo;
        if (listOfContactInfos != null) {
            this.effectiveTargetIORIterator = listOfContactInfos.iterator();
        }
        this.listOfContactInfos = listOfContactInfos;
        this.previousContactInfo = null;
        this.isAddrDispositionRetry = false;
        this.successContactInfo = null;
        this.failureContactInfo = null;
        this.failureException = null;
        primaryToContactInfo = orb.getORBData().getIIOPPrimaryToContactInfo();
    }
    public boolean hasNext()
    {
        if (isAddrDispositionRetry) {
            return true;
        }
        boolean result;
        if (primaryToContactInfo != null) {
            result = primaryToContactInfo.hasNext(primaryContactInfo,
                                                  previousContactInfo,
                                                  listOfContactInfos);
        } else {
            result = effectiveTargetIORIterator.hasNext();
        }
        return result;
    }
    public Object next()
    {
        if (isAddrDispositionRetry) {
            isAddrDispositionRetry = false;
            return previousContactInfo;
        }
        if (primaryToContactInfo != null) {
            previousContactInfo = (CorbaContactInfo)
                primaryToContactInfo.next(primaryContactInfo,
                                          previousContactInfo,
                                          listOfContactInfos);
        } else {
            previousContactInfo = (CorbaContactInfo)
                effectiveTargetIORIterator.next();
        }
        return previousContactInfo;
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    public ContactInfoList getContactInfoList()
    {
        return contactInfoList;
    }
    public void reportSuccess(ContactInfo contactInfo)
    {
        this.successContactInfo = (CorbaContactInfo)contactInfo;
    }
    public boolean reportException(ContactInfo contactInfo,
                                   RuntimeException ex)
    {
        this.failureContactInfo = (CorbaContactInfo)contactInfo;
        this.failureException = ex;
        if (ex instanceof COMM_FAILURE) {
            SystemException se = (SystemException) ex;
            if (se.completed == CompletionStatus.COMPLETED_NO) {
                if (hasNext()) {
                    return true;
                }
                if (contactInfoList.getEffectiveTargetIOR() !=
                    contactInfoList.getTargetIOR())
                {
                    updateEffectiveTargetIOR(contactInfoList.getTargetIOR());
                    return true;
                }
            }
        }
        return false;
    }
    public RuntimeException getFailureException()
    {
        if (failureException == null) {
            return
                ORBUtilSystemException.get( orb,
                                            CORBALogDomains.RPC_TRANSPORT )
                    .invalidContactInfoListIteratorFailureException();
        } else {
            return failureException;
        }
    }
    public void reportAddrDispositionRetry(CorbaContactInfo contactInfo,
                                           short disposition)
    {
        previousContactInfo.setAddressingDisposition(disposition);
        isAddrDispositionRetry = true;
    }
    public void reportRedirect(CorbaContactInfo contactInfo,
                               IOR forwardedIOR)
    {
        updateEffectiveTargetIOR(forwardedIOR);
    }
    public void updateEffectiveTargetIOR(IOR newIOR)
    {
        contactInfoList.setEffectiveTargetIOR(newIOR);
        ((CorbaInvocationInfo)orb.getInvocationInfo())
            .setContactInfoListIterator(contactInfoList.iterator());
    }
}
