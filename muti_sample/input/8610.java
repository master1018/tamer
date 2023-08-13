public class DefaultHandlerModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;
    private String username;
    private char[] password;
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                        Map<String,?> sharedState, Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                        "to garner authentication information from the user");
        } else {
            System.out.println("DefaultHandlerModule got CallbackHandler: " +
                        callbackHandler.toString());
        }
        return true;
    }
    public boolean commit() throws LoginException {
        return true;
    }
    public boolean abort() throws LoginException {
        return true;
    }
    public boolean logout() throws LoginException {
        return true;
    }
}
