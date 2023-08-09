public class NoSecurityLayer {
    private static final String MECH = "GSSAPI";
    private static final String SERVER_FQDN = "anti.imc.org";
    private static final String PROTOCOL = "sample";
    private static String namesfile, proxyfile;
    private static final byte[] EMPTY = new byte[0];
    private static boolean auto;
    private static boolean verbose = false;
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            namesfile = null;
            auto = true;
        } else {
            int i = 0;
            if (args[i].equals("-m")) {
                i++;
                auto = false;
            }
            if (args.length > i) {
                namesfile = args[i++];
                if (args.length > i) {
                    proxyfile = args[i];
                }
            } else {
                namesfile = null;
            }
        }
        CallbackHandler clntCbh = null;
        final CallbackHandler srvCbh = new PropertiesFileCallbackHandler(
            null, namesfile, proxyfile);
        Subject clntSubj = doLogin("client");
        Subject srvSubj = doLogin("server");
        final HashMap clntprops = new HashMap();
        final HashMap srvprops = new HashMap();
        clntprops.put(Sasl.QOP, "auth");
        srvprops.put(Sasl.QOP, "auth,auth-int,auth-conf");
        final SaslClient clnt = (SaslClient)
            Subject.doAs(clntSubj, new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    return Sasl.createSaslClient(
                        new String[]{MECH}, null, PROTOCOL, SERVER_FQDN,
                        clntprops, null);
                }
            });
        if (verbose) {
            System.out.println(clntSubj);
            System.out.println(srvSubj);
        }
        final SaslServer srv = (SaslServer)
            Subject.doAs(srvSubj, new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    return Sasl.createSaslServer(MECH, PROTOCOL, SERVER_FQDN,
                        srvprops, srvCbh);
                }
            });
        if (clnt == null) {
            throw new IllegalStateException(
                "Unable to find client impl for " + MECH);
        }
        if (srv == null) {
            throw new IllegalStateException(
                "Unable to find server impl for " + MECH);
        }
        byte[] response;
        byte[] challenge;
        response = (byte[]) Subject.doAs(clntSubj,
            new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                return (clnt.hasInitialResponse()? clnt.evaluateChallenge(EMPTY) : EMPTY);
            }});
        while (!clnt.isComplete() || !srv.isComplete()) {
            final byte[] responseCopy = response;
            challenge = (byte[]) Subject.doAs(srvSubj,
                new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    return srv.evaluateResponse(responseCopy);
                }});
            if (challenge != null) {
                final byte[] challengeCopy = challenge;
                response = (byte[]) Subject.doAs(clntSubj,
                    new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        return clnt.evaluateChallenge(challengeCopy);
                    }});
            }
        }
        if (clnt.isComplete() && srv.isComplete()) {
            if (verbose) {
                System.out.println("SUCCESS");
                System.out.println("authzid is " + srv.getAuthorizationID());
            }
        } else {
            throw new IllegalStateException("FAILURE: mismatched state:" +
                " client complete? " + clnt.isComplete() +
                " server complete? " + srv.isComplete());
        }
        if (verbose) {
            System.out.println(clnt.getNegotiatedProperty(Sasl.QOP));
        }
        byte[] clntBuf = new byte[]{0, 1, 2, 3};
        try {
            byte[] wrapped = clnt.wrap(clntBuf, 0, clntBuf.length);
            throw new Exception(
                "clnt wrap should not be allowed w/no security layer");
        } catch (IllegalStateException e) {
        }
        byte[] srvBuf = new byte[]{10, 11, 12, 13};
        try {
            byte[] wrapped = srv.wrap(srvBuf, 0, srvBuf.length);
            throw new Exception(
                "srv wrap should not be allowed w/no security layer");
        } catch (IllegalStateException e) {
        }
        try {
            byte[] unwrapped = clnt.unwrap(clntBuf, 0, clntBuf.length);
            throw new Exception(
                "clnt wrap should not be allowed w/no security layer");
        } catch (IllegalStateException e) {
        }
        try {
            byte[] unwrapped = srv.unwrap(srvBuf, 0, srvBuf.length);
            throw new Exception(
                "srv wrap should not be allowed w/no security layer");
        } catch (IllegalStateException e) {
        }
    }
    private static Subject doLogin(String msg) throws LoginException {
        LoginContext lc = null;
        if (verbose) {
            System.out.println(msg);
        }
        try {
            lc = new LoginContext(msg, new TextCallbackHandler());
            lc.login();
        } catch (LoginException le) {
            throw le;
        }
        return lc.getSubject();
    }
}
