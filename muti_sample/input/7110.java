public class InitialLdapContext extends InitialDirContext implements LdapContext {
    private static final String
        BIND_CONTROLS_PROPERTY = "java.naming.ldap.control.connect";
    public InitialLdapContext() throws NamingException {
        super(null);
    }
    public InitialLdapContext(Hashtable<?,?> environment,
                              Control[] connCtls)
            throws NamingException {
        super(true); 
        Hashtable env = (environment == null)
            ? new Hashtable(11)
            : (Hashtable)environment.clone();
        if (connCtls != null) {
            Control[] copy = new Control[connCtls.length];
            System.arraycopy(connCtls, 0, copy, 0, connCtls.length);
            env.put(BIND_CONTROLS_PROPERTY, copy);
        }
        env.put("java.naming.ldap.version", "3");
        init(env);
    }
    private LdapContext getDefaultLdapInitCtx() throws NamingException{
        Context answer = getDefaultInitCtx();
        if (!(answer instanceof LdapContext)) {
            if (answer == null) {
                throw new NoInitialContextException();
            } else {
                throw new NotContextException(
                    "Not an instance of LdapContext");
            }
        }
        return (LdapContext)answer;
    }
    public ExtendedResponse extendedOperation(ExtendedRequest request)
            throws NamingException {
        return getDefaultLdapInitCtx().extendedOperation(request);
    }
    public LdapContext newInstance(Control[] reqCtls)
        throws NamingException {
            return getDefaultLdapInitCtx().newInstance(reqCtls);
    }
    public void reconnect(Control[] connCtls) throws NamingException {
        getDefaultLdapInitCtx().reconnect(connCtls);
    }
    public Control[] getConnectControls() throws NamingException {
        return getDefaultLdapInitCtx().getConnectControls();
    }
    public void setRequestControls(Control[] requestControls)
        throws NamingException {
            getDefaultLdapInitCtx().setRequestControls(requestControls);
    }
    public Control[] getRequestControls() throws NamingException {
        return getDefaultLdapInitCtx().getRequestControls();
    }
    public Control[] getResponseControls() throws NamingException {
        return getDefaultLdapInitCtx().getResponseControls();
    }
}
