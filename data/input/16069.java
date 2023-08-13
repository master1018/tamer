public class CorbaInvocationInfo implements ClientInvocationInfo
{
    private boolean isRetryInvocation;
    private int entryCount;
    private ORB orb;
    private Iterator contactInfoListIterator;
    private ClientRequestDispatcher clientRequestDispatcher;
    private MessageMediator messageMediator;
    private CorbaInvocationInfo()
    {
    }
    public CorbaInvocationInfo(ORB orb)
    {
        this.orb = orb;
        isRetryInvocation = false;
        entryCount = 0;
    }
    public Iterator getContactInfoListIterator()
    {
        return contactInfoListIterator;
    }
    public void setContactInfoListIterator(Iterator contactInfoListIterator)
    {
        this.contactInfoListIterator = contactInfoListIterator;
    }
    public boolean isRetryInvocation()
    {
        return isRetryInvocation;
    }
    public void setIsRetryInvocation(boolean isRetryInvocation)
    {
        this.isRetryInvocation = isRetryInvocation;
    }
    public int getEntryCount()
    {
        return entryCount;
    }
    public void incrementEntryCount()
    {
        entryCount++;
    }
    public void decrementEntryCount()
    {
        entryCount--;
    }
    public void setClientRequestDispatcher(ClientRequestDispatcher clientRequestDispatcher)
    {
        this.clientRequestDispatcher = clientRequestDispatcher;
    }
    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        return clientRequestDispatcher;
    }
    public void setMessageMediator(MessageMediator messageMediator)
    {
        this.messageMediator = messageMediator;
    }
    public MessageMediator getMessageMediator()
    {
        return messageMediator;
    }
}
