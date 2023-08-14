public class StubDelegateImpl implements javax.rmi.CORBA.StubDelegate
{
    static UtilSystemException wrapper = UtilSystemException.get(
        CORBALogDomains.RMIIIOP ) ;
    private StubIORImpl ior ;
    public StubIORImpl getIOR()
    {
        return ior ;
    }
    public StubDelegateImpl()
    {
        ior = null ;
    }
    private void init (javax.rmi.CORBA.Stub self)
    {
        if (ior == null)
            ior = new StubIORImpl( self ) ;
    }
    public int hashCode(javax.rmi.CORBA.Stub self)
    {
        init(self);
        return ior.hashCode() ;
    }
    public boolean equals(javax.rmi.CORBA.Stub self, java.lang.Object obj)
    {
        if (self == obj) {
            return true;
        }
        if (!(obj instanceof javax.rmi.CORBA.Stub)) {
            return false;
        }
        javax.rmi.CORBA.Stub other = (javax.rmi.CORBA.Stub) obj;
        if (other.hashCode() != self.hashCode()) {
            return false;
        }
        return self.toString().equals( other.toString() ) ;
    }
    public boolean equals( Object obj )
    {
        if (this == obj)
            return true ;
        if (!(obj instanceof StubDelegateImpl))
            return false ;
        StubDelegateImpl other = (StubDelegateImpl)obj ;
        if (ior == null)
            return ior == other.ior ;
        else
            return ior.equals( other.ior ) ;
    }
    public String toString(javax.rmi.CORBA.Stub self)
    {
        if (ior == null)
            return null ;
        else
            return ior.toString() ;
    }
    public void connect(javax.rmi.CORBA.Stub self, ORB orb)
        throws RemoteException
    {
        ior = StubConnectImpl.connect( ior, self, self, orb ) ;
    }
    public void readObject(javax.rmi.CORBA.Stub self,
        java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        if (ior == null)
            ior = new StubIORImpl() ;
        ior.doRead( stream ) ;
    }
    public void writeObject(javax.rmi.CORBA.Stub self,
        java.io.ObjectOutputStream stream) throws IOException
    {
        init(self);
        ior.doWrite( stream ) ;
    }
}
