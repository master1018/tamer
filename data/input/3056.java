public class NegotiatorImpl extends Negotiator {
    private static final boolean DEBUG =
        java.security.AccessController.doPrivileged(
              new sun.security.action.GetBooleanAction("sun.security.krb5.debug"));
    private GSSContext context;
    private byte[] oneToken;
    private void init(HttpCallerInfo hci) throws GSSException {
        final Oid oid;
        if (hci.scheme.equalsIgnoreCase("Kerberos")) {
            oid = GSSUtil.GSS_KRB5_MECH_OID;
        } else {
            String pref = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<String>() {
                        public String run() {
                            return System.getProperty(
                                "http.auth.preference",
                                "spnego");
                        }
                    });
            if (pref.equalsIgnoreCase("kerberos")) {
                oid = GSSUtil.GSS_KRB5_MECH_OID;
            } else {
                oid = GSSUtil.GSS_SPNEGO_MECH_OID;
            }
        }
        GSSManagerImpl manager = new GSSManagerImpl(
                new HttpCaller(hci));
        String peerName = "HTTP@" + hci.host.toLowerCase();
        GSSName serverName = manager.createName(peerName,
                GSSName.NT_HOSTBASED_SERVICE);
        context = manager.createContext(serverName,
                                        oid,
                                        null,
                                        GSSContext.DEFAULT_LIFETIME);
        if (context instanceof ExtendedGSSContext) {
            ((ExtendedGSSContext)context).requestDelegPolicy(true);
        }
        oneToken = context.initSecContext(new byte[0], 0, 0);
    }
    public NegotiatorImpl(HttpCallerInfo hci) throws IOException {
        try {
            init(hci);
        } catch (GSSException e) {
            if (DEBUG) {
                System.out.println("Negotiate support not initiated, will " +
                        "fallback to other scheme if allowed. Reason:");
                e.printStackTrace();
            }
            IOException ioe = new IOException("Negotiate support not initiated");
            ioe.initCause(e);
            throw ioe;
        }
    }
    @Override
    public byte[] firstToken() {
        return oneToken;
    }
    @Override
    public byte[] nextToken(byte[] token) throws IOException {
        try {
            return context.initSecContext(token, 0, token.length);
        } catch (GSSException e) {
            if (DEBUG) {
                System.out.println("Negotiate support cannot continue. Reason:");
                e.printStackTrace();
            }
            IOException ioe = new IOException("Negotiate support cannot continue");
            ioe.initCause(e);
            throw ioe;
        }
    }
}
