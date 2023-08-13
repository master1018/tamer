public class NegotiateCallbackHandler implements CallbackHandler {
    private String username;
    private char[] password;
    private boolean answered;
    private final HttpCallerInfo hci;
    public NegotiateCallbackHandler(HttpCallerInfo hci) {
        this.hci = hci;
    }
    private void getAnswer() {
        if (!answered) {
            answered = true;
            PasswordAuthentication passAuth =
                    Authenticator.requestPasswordAuthentication(
                    hci.host, hci.addr, hci.port, hci.protocol,
                    hci.prompt, hci.scheme, hci.url, hci.authType);
            if (passAuth != null) {
                username = passAuth.getUserName();
                password = passAuth.getPassword();
            }
        }
    }
    public void handle(Callback[] callbacks) throws
            UnsupportedCallbackException, IOException {
        for (int i=0; i<callbacks.length; i++) {
            Callback callBack = callbacks[i];
            if (callBack instanceof NameCallback) {
                getAnswer();
                ((NameCallback)callBack).setName(username);
            } else if (callBack instanceof PasswordCallback) {
                getAnswer();
                ((PasswordCallback)callBack).setPassword(password);
                if (password != null) Arrays.fill(password, ' ');
            } else {
                throw new UnsupportedCallbackException(callBack,
                        "Call back not supported");
            }
        }
    }
}
