public abstract class StubConnectImpl
{
    static UtilSystemException wrapper = UtilSystemException.get(
        CORBALogDomains.RMIIIOP ) ;
    public static StubIORImpl connect( StubIORImpl ior, org.omg.CORBA.Object proxy,
        org.omg.CORBA.portable.ObjectImpl stub, ORB orb ) throws RemoteException
    {
        Delegate del = null ;
        try {
            try {
                del = StubAdapter.getDelegate( stub );
                if (del.orb(stub) != orb)
                    throw wrapper.connectWrongOrb() ;
            } catch (org.omg.CORBA.BAD_OPERATION err) {
                if (ior == null) {
                    Tie tie = (javax.rmi.CORBA.Tie) Utility.getAndForgetTie(proxy);
                    if (tie == null)
                        throw wrapper.connectNoTie() ;
                    ORB existingOrb = orb ;
                    try {
                        existingOrb = tie.orb();
                    } catch (BAD_OPERATION exc) {
                        tie.orb(orb);
                    } catch (BAD_INV_ORDER exc) {
                        tie.orb(orb);
                    }
                    if (existingOrb != orb)
                        throw wrapper.connectTieWrongOrb() ;
                    del = StubAdapter.getDelegate( tie ) ;
                    ObjectImpl objref = new CORBAObjectImpl() ;
                    objref._set_delegate( del ) ;
                    ior = new StubIORImpl( objref ) ;
                } else {
                    del = ior.getDelegate( orb ) ;
                }
                StubAdapter.setDelegate( stub, del ) ;
            }
        } catch (SystemException exc) {
            throw new RemoteException("CORBA SystemException", exc );
        }
        return ior ;
    }
}
