public class ReferenceWrapper
        extends UnicastRemoteObject
        implements RemoteReference
{
    protected Reference wrappee;        
    public ReferenceWrapper(Reference wrappee)
            throws NamingException, RemoteException
    {
        this.wrappee = wrappee;
    }
    public Reference getReference() throws RemoteException {
        return wrappee;
    }
    private static final long serialVersionUID = 6078186197417641456L;
}
