public class NTLMTest {
    private static final String MECH = "NTLM";
    private static final String REALM = "REALM";
    private static final String PROTOCOL = "jmx";
    private static final byte[] EMPTY = new byte[0];
    private static final String USER1 = "dummy";
    private static final char[] PASS1 = "bogus".toCharArray();
    private static final String USER2 = "foo";
    private static final char[] PASS2 = "bar".toCharArray();
    private static final Map<String,char[]> maps =
            new HashMap<String,char[]>();
    static {
        maps.put(USER1, PASS1);
        maps.put(USER2, PASS2);
    }
    static char[] getPass(String d, String u) {
        if (!d.equals(REALM)) return null;
        return maps.get(u);
    }
    public static void main(String[] args) throws Exception {
        checkAuthOnly();
        checkClientNameOverride();
        checkServerDomainOverride();
        checkClientDomainOverride();
        checkVersions();
        checkClientHostname();
    }
    static void checkVersions() throws Exception {
        checkVersion(null, null);
        checkVersion("LM/NTLM", null);
        checkVersion("LM", null);
        checkVersion("NTLM", null);
        checkVersion("NTLM2", null);
        checkVersion("LMv2/NTLMv2", null);
        checkVersion("LMv2", null);
        checkVersion("NTLMv2", null);
        checkVersion(null, "LMv2");
        checkVersion("LM/NTLM", "LM");
        checkVersion("LM", "LM");
        checkVersion("NTLM", "LM");
        checkVersion("NTLM2", "NTLM2");
        checkVersion("LMv2/NTLMv2", "LMv2");
        checkVersion("LMv2", "LMv2");
        checkVersion("NTLMv2", "LMv2");
        try {
            checkVersion("LM/NTLM", "LMv2");
            throw new Exception("Should not succeed");
        } catch (SaslException se) {
        }
        try {
            checkVersion("LMv2/NTLMv2", "LM");
            throw new Exception("Should not succeed");
        } catch (SaslException se) {
        }
    }
    private static void checkVersion(String vc, String vs) throws Exception {
        Map<String,Object> pc = new HashMap<>();
        pc.put("com.sun.security.sasl.ntlm.version", vc);
        Map<String,Object> ps = new HashMap<>();
        ps.put("com.sun.security.sasl.ntlm.version", vs);
        SaslClient clnt = Sasl.createSaslClient(
                new String[]{MECH}, USER1, PROTOCOL, null, pc,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                NameCallback ncb = (NameCallback)cb;
                                ncb.setName(ncb.getDefaultName());
                            } else if (cb instanceof PasswordCallback) {
                                ((PasswordCallback)cb).setPassword(PASS1);
                            }
                        }
                    }
                });
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, REALM, ps,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        String domain = null, name = null;
                        PasswordCallback pcb = null;
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                name = ((NameCallback)cb).getDefaultName();
                            } else if (cb instanceof RealmCallback) {
                                domain = ((RealmCallback)cb).getDefaultText();
                            } else if (cb instanceof PasswordCallback) {
                                pcb = (PasswordCallback)cb;
                            }
                        }
                        if (pcb != null) {
                            pcb.setPassword(getPass(domain, name));
                        }
                    }
                });
        handshake(clnt, srv);
    }
    private static void checkClientHostname() throws Exception {
        Map<String,Object> pc = new HashMap<>();
        pc.put("com.sun.security.sasl.ntlm.hostname", "this.is.com");
        SaslClient clnt = Sasl.createSaslClient(
                new String[]{MECH}, USER1, PROTOCOL, null, pc,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                NameCallback ncb = (NameCallback)cb;
                                ncb.setName(ncb.getDefaultName());
                            } else if (cb instanceof PasswordCallback) {
                                ((PasswordCallback)cb).setPassword(PASS1);
                            }
                        }
                    }
                });
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, REALM, null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        String domain = null, name = null;
                        PasswordCallback pcb = null;
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                name = ((NameCallback)cb).getDefaultName();
                            } else if (cb instanceof RealmCallback) {
                                domain = ((RealmCallback)cb).getDefaultText();
                            } else if (cb instanceof PasswordCallback) {
                                pcb = (PasswordCallback)cb;
                            }
                        }
                        if (pcb != null) {
                            pcb.setPassword(getPass(domain, name));
                        }
                    }
                });
        handshake(clnt, srv);
        if (!"this.is.com".equals(
                srv.getNegotiatedProperty("com.sun.security.sasl.ntlm.hostname"))) {
            throw new Exception("Hostname not trasmitted to server");
        }
    }
    private static void checkClientDomainOverride() throws Exception {
        SaslClient clnt = Sasl.createSaslClient(
                new String[]{MECH}, USER1, PROTOCOL, "ANOTHERREALM", null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                NameCallback ncb = (NameCallback)cb;
                                ncb.setName(ncb.getDefaultName());
                            } else if(cb instanceof RealmCallback) {
                                RealmCallback dcb = (RealmCallback)cb;
                                dcb.setText("THIRDDOMAIN");
                            } else if (cb instanceof PasswordCallback) {
                                ((PasswordCallback)cb).setPassword(PASS1);
                            }
                        }
                    }
                });
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, REALM, null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        String domain = null, name = null;
                        PasswordCallback pcb = null;
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                name = ((NameCallback)cb).getDefaultName();
                            } else if (cb instanceof RealmCallback) {
                                domain = ((RealmCallback)cb).getDefaultText();
                            } else if (cb instanceof PasswordCallback) {
                                pcb = (PasswordCallback)cb;
                            }
                        }
                        if (pcb != null) {
                            pcb.setPassword(getPass(domain, name));
                        }
                    }
                });
        handshake(clnt, srv);
    }
    private static void checkClientNameOverride() throws Exception {
        SaslClient clnt = Sasl.createSaslClient(
                new String[]{MECH}, null, PROTOCOL, null, null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                NameCallback ncb = (NameCallback)cb;
                                ncb.setName(USER1);
                            } else if (cb instanceof PasswordCallback) {
                                ((PasswordCallback)cb).setPassword(PASS1);
                            }
                        }
                    }
                });
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, REALM, null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        String domain = null, name = null;
                        PasswordCallback pcb = null;
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                name = ((NameCallback)cb).getDefaultName();
                            } else if (cb instanceof RealmCallback) {
                                domain = ((RealmCallback)cb).getDefaultText();
                            } else if (cb instanceof PasswordCallback) {
                                pcb = (PasswordCallback)cb;
                            }
                        }
                        if (pcb != null) {
                            pcb.setPassword(getPass(domain, name));
                        }
                    }
                });
        handshake(clnt, srv);
    }
    private static void checkServerDomainOverride() throws Exception {
        SaslClient clnt = Sasl.createSaslClient(
                new String[]{MECH}, USER1, PROTOCOL, null, null,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                NameCallback ncb = (NameCallback)cb;
                                ncb.setName(ncb.getDefaultName());
                            } else if (cb instanceof PasswordCallback) {
                                ((PasswordCallback)cb).setPassword(PASS1);
                            }
                        }
                    }
                });
        Map<String,Object> ps = new HashMap<>();
        ps.put("com.sun.security.sasl.ntlm.domain", REALM);
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, null, ps,
                new CallbackHandler() {
                    public void handle(Callback[] callbacks)
                            throws IOException, UnsupportedCallbackException {
                        String domain = null, name = null;
                        PasswordCallback pcb = null;
                        for (Callback cb: callbacks) {
                            if (cb instanceof NameCallback) {
                                name = ((NameCallback)cb).getDefaultName();
                            } else if (cb instanceof RealmCallback) {
                                domain = ((RealmCallback)cb).getDefaultText();
                            } else if (cb instanceof PasswordCallback) {
                                pcb = (PasswordCallback)cb;
                            }
                        }
                        if (pcb != null) {
                            pcb.setPassword(getPass(domain, name));
                        }
                    }
                });
        handshake(clnt, srv);
    }
    private static void checkAuthOnly() throws Exception {
        Map<String,Object> props = new HashMap<>();
        props.put(Sasl.QOP, "auth-conf");
        try {
            Sasl.createSaslClient(
                    new String[]{MECH}, USER2, PROTOCOL, REALM, props, null);
            throw new Exception("NTLM should not support auth-conf");
        } catch (SaslException se) {
        }
    }
    private static void handshake(SaslClient clnt, SaslServer srv)
            throws Exception {
        if (clnt == null) {
            throw new IllegalStateException(
                    "Unable to find client impl for " + MECH);
        }
        if (srv == null) {
            throw new IllegalStateException(
                    "Unable to find server impl for " + MECH);
        }
        byte[] response = (clnt.hasInitialResponse()
                ? clnt.evaluateChallenge(EMPTY) : EMPTY);
        System.out.println("Initial:");
        new sun.misc.HexDumpEncoder().encodeBuffer(response, System.out);
        byte[] challenge;
        while (!clnt.isComplete() || !srv.isComplete()) {
            challenge = srv.evaluateResponse(response);
            response = null;
            if (challenge != null) {
                System.out.println("Challenge:");
                new sun.misc.HexDumpEncoder().encodeBuffer(challenge, System.out);
                response = clnt.evaluateChallenge(challenge);
            }
            if (response != null) {
                System.out.println("Response:");
                new sun.misc.HexDumpEncoder().encodeBuffer(response, System.out);
            }
        }
        if (clnt.isComplete() && srv.isComplete()) {
            System.out.println("SUCCESS");
            if (!srv.getAuthorizationID().equals(USER1)) {
                throw new Exception("Not correct user");
            }
        } else {
            throw new IllegalStateException(
                    "FAILURE: mismatched state:"
                    + " client complete? " + clnt.isComplete()
                    + " server complete? " + srv.isComplete());
        }
        if (!clnt.getNegotiatedProperty(Sasl.QOP).equals("auth") ||
                !srv.getNegotiatedProperty(Sasl.QOP).equals("auth") ||
                !clnt.getNegotiatedProperty(
                    "com.sun.security.sasl.ntlm.domain").equals(REALM)) {
            throw new Exception("Negotiated property error");
        }
        clnt.dispose();
        srv.dispose();
    }
}
