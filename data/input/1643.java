public class SocketFactoryAcceptorImpl
    extends
        SocketOrChannelAcceptorImpl
{
    public SocketFactoryAcceptorImpl(ORB orb, int port,
                                     String name, String type)
    {
        super(orb, port, name, type);
    }
    public boolean initialize()
    {
        if (initialized) {
            return false;
        }
        if (orb.transportDebugFlag) {
            dprint("initialize: " + this);
        }
        try {
            serverSocket = orb.getORBData()
                .getLegacySocketFactory().createServerSocket(type, port);
            internalInitialize();
        } catch (Throwable t) {
            throw wrapper.createListenerFailed( t, Integer.toString(port) ) ;
        }
        initialized = true;
        return true;
    }
    protected String toStringName()
    {
        return "SocketFactoryAcceptorImpl";
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint(toStringName(), msg);
    }
}
