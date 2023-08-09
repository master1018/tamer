final class NTLMServer implements SaslServer {
    private final static String NTLM_VERSION =
            "com.sun.security.sasl.ntlm.version";
    private final static String NTLM_DOMAIN =
            "com.sun.security.sasl.ntlm.domain";
    private final static String NTLM_HOSTNAME =
            "com.sun.security.sasl.ntlm.hostname";
    private static final String NTLM_RANDOM =
            "com.sun.security.sasl.ntlm.random";
    private final Random random;
    private final Server server;
    private byte[] nonce;
    private int step = 0;
    private String authzId;
    private final String mech;
    private String hostname;
    NTLMServer(String mech, String protocol, String serverName,
            Map props, final CallbackHandler cbh) throws SaslException {
        this.mech = mech;
        String version = null;
        String domain = null;
        Random rtmp = null;
        if (props != null) {
            domain = (String) props.get(NTLM_DOMAIN);
            version = (String)props.get(NTLM_VERSION);
            rtmp = (Random)props.get(NTLM_RANDOM);
        }
        random = rtmp != null ? rtmp : new Random();
        if (version == null) {
            version = System.getProperty("ntlm.version");
        }
        if (domain == null) {
            domain = serverName;
        }
        if (domain == null) {
            throw new NullPointerException("Domain must be provided as"
                    + " the serverName argument or in props");
        }
        try {
            server = new Server(version, domain) {
                public char[] getPassword(String ntdomain, String username) {
                    try {
                        RealmCallback rcb = new RealmCallback(
                                "Domain: ", ntdomain);
                        NameCallback ncb = new NameCallback(
                                "Name: ", username);
                        PasswordCallback pcb = new PasswordCallback(
                                "Password: ", false);
                        cbh.handle(new Callback[] { rcb, ncb, pcb });
                        char[] passwd = pcb.getPassword();
                        pcb.clearPassword();
                        return passwd;
                    } catch (IOException ioe) {
                        return null;
                    } catch (UnsupportedCallbackException uce) {
                        return null;
                    }
                }
            };
        } catch (NTLMException ne) {
            throw new SaslException(
                    "NTLM: Invalid version string: " + version, ne);
        }
        nonce = new byte[8];
    }
    @Override
    public String getMechanismName() {
        return mech;
    }
    @Override
    public byte[] evaluateResponse(byte[] response) throws SaslException {
        try {
            step++;
            if (step == 1) {
                random.nextBytes(nonce);
                return server.type2(response, nonce);
            } else {
                String[] out = server.verify(response, nonce);
                authzId = out[0];
                hostname = out[1];
                return null;
            }
        } catch (GeneralSecurityException ex) {
            throw new SaslException("", ex);
        }
    }
    @Override
    public boolean isComplete() {
        return step >= 2;
    }
    @Override
    public String getAuthorizationID() {
        return authzId;
    }
    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len)
            throws SaslException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len)
            throws SaslException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public Object getNegotiatedProperty(String propName) {
        if (propName.equals(Sasl.QOP)) {
            return "auth";
        } else if (propName.equals(NTLM_HOSTNAME)) {
            return hostname;
        } else {
            return null;
        }
    }
    @Override
    public void dispose() throws SaslException {
        return;
    }
}
