public class NoQuoteParams {
    private static Logger logger = Logger.getLogger("global");
    private static final String DIGEST_MD5 = "DIGEST-MD5";
    private static final byte[] EMPTY = new byte[0];
    private static CallbackHandler authCallbackHandler =
        new SampleCallbackHandler();
    public static void main(String[] args) throws Exception {
        Map<String, String> props = new TreeMap<String, String>();
        props.put(Sasl.QOP, "auth");
        SaslClient client = Sasl.createSaslClient(new String[]{ DIGEST_MD5 },
            "user1", "xmpp", "127.0.0.1", props, authCallbackHandler);
        if (client == null) {
            throw new Exception("Unable to find client implementation for: " +
                DIGEST_MD5);
        }
        byte[] response = client.hasInitialResponse()
            ? client.evaluateChallenge(EMPTY) : EMPTY;
        logger.info("initial: " + new String(response));
        byte[] challenge = null;
        SaslServer server = Sasl.createSaslServer(DIGEST_MD5, "xmpp",
          "127.0.0.1", props, authCallbackHandler);
        if (server == null) {
            throw new Exception("Unable to find server implementation for: " +
                DIGEST_MD5);
        }
        if (!client.isComplete() || !server.isComplete()) {
            challenge = server.evaluateResponse(response);
            logger.info("challenge: " + new String(challenge));
            if (challenge != null) {
                response = client.evaluateChallenge(challenge);
            }
        }
        String challengeString = new String(challenge, "UTF-8").toLowerCase();
        if (challengeString.indexOf("\"md5-sess\"") > 0 ||
            challengeString.indexOf("\"utf-8\"") > 0) {
            throw new Exception("The challenge string's charset and " +
                "algorithm values must not be enclosed within quotes");
        }
        client.dispose();
        server.dispose();
    }
}
class SampleCallbackHandler implements CallbackHandler {
    public void handle(Callback[] callbacks)
        throws java.io.IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback cb = (NameCallback)callbacks[i];
                    cb.setName(getInput(cb.getPrompt()));
                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback cb = (PasswordCallback)callbacks[i];
                    String pw = getInput(cb.getPrompt());
                    char[] passwd = new char[pw.length()];
                    pw.getChars(0, passwd.length, passwd, 0);
                    cb.setPassword(passwd);
                } else if (callbacks[i] instanceof RealmCallback) {
                    RealmCallback cb = (RealmCallback)callbacks[i];
                    cb.setText(getInput(cb.getPrompt()));
                } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
    }
    private String getInput(String prompt) throws IOException {
        return "dummy-value";
    }
}
