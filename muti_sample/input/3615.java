public class RemoteToCorba implements StateFactory {
    public RemoteToCorba() {
    }
    public Object getStateToBind(Object orig, Name name, Context ctx,
        Hashtable<?,?> env) throws NamingException {
        if (orig instanceof org.omg.CORBA.Object) {
            return null;
        }
        if (orig instanceof Remote) {
            try {
                return
                    CorbaUtils.remoteToCorba((Remote)orig, ((CNCtx)ctx)._orb);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException(
                    "javax.rmi packages not available");
            }
        }
        return null; 
    }
}
