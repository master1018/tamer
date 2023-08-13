final class NTLMClient implements SaslClient {
    private static final String NTLM_VERSION =
            "com.sun.security.sasl.ntlm.version";
    private static final String NTLM_RANDOM =
            "com.sun.security.sasl.ntlm.random";
    private final static String NTLM_DOMAIN =
            "com.sun.security.sasl.ntlm.domain";
    private final static String NTLM_HOSTNAME =
            "com.sun.security.sasl.ntlm.hostname";
    private final Client client;
    private final String mech;
    private final Random random;
    private int step = 0;   
    NTLMClient(String mech, String authzid, String protocol, String serverName,
            Map props, CallbackHandler cbh) throws SaslException {
        this.mech = mech;
        String version = null;
        Random rtmp = null;
        String hostname = null;
        if (props != null) {
            String qop = (String)props.get(Sasl.QOP);
            if (qop != null && !qop.equals("auth")) {
                throw new SaslException("NTLM only support auth");
            }
            version = (String)props.get(NTLM_VERSION);
            rtmp = (Random)props.get(NTLM_RANDOM);
            hostname = (String)props.get(NTLM_HOSTNAME);
        }
        this.random = rtmp != null ? rtmp : new Random();
        if (version == null) {
            version = System.getProperty("ntlm.version");
        }
        RealmCallback dcb = (serverName != null && !serverName.isEmpty())?
            new RealmCallback("Realm: ", serverName) :
            new RealmCallback("Realm: ");
        NameCallback ncb = (authzid != null && !authzid.isEmpty()) ?
            new NameCallback("User name: ", authzid) :
            new NameCallback("User name: ");
        PasswordCallback pcb =
            new PasswordCallback("Password: ", false);
        try {
            cbh.handle(new Callback[] {dcb, ncb, pcb});
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("NTLM: Cannot perform callback to " +
                "acquire realm, username or password", e);
        } catch (IOException e) {
            throw new SaslException(
                "NTLM: Error acquiring realm, username or password", e);
        }
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                hostname = "localhost";
            }
        }
        try {
            client = new Client(version, hostname,
                    ncb.getName(),
                    dcb.getText(),
                    pcb.getPassword());
        } catch (NTLMException ne) {
            throw new SaslException(
                    "NTLM: Invalid version string: " + version, ne);
        }
    }
    @Override
    public String getMechanismName() {
        return mech;
    }
    @Override
    public boolean isComplete() {
        return step >= 2;
    }
    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len)
            throws SaslException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len)
            throws SaslException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public Object getNegotiatedProperty(String propName) {
        if (propName.equals(Sasl.QOP)) {
            return "auth";
        } else if (propName.equals(NTLM_DOMAIN)) {
            return client.getDomain();
        } else {
            return null;
        }
    }
    @Override
    public void dispose() throws SaslException {
        client.dispose();
    }
    @Override
    public boolean hasInitialResponse() {
        return true;
    }
    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
        step++;
        if (step == 1) {
            return client.type1();
        } else {
            try {
                byte[] nonce = new byte[8];
                random.nextBytes(nonce);
                return client.type3(challenge, nonce);
            } catch (NTLMException ex) {
                throw new SaslException("Type3 creation failed", ex);
            }
        }
    }
}
