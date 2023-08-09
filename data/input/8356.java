public class ModuleSubjectModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;
    private String username;
    private char[] password;
    private int attemptNumber = 1;
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                        Map<String,?> sharedState, Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }
    public boolean login() throws LoginException {
        if (attemptNumber == 1) {
            attemptNumber = 2;
            throw new LoginException("attempt 1 fails");
        }
        return true;
    }
    public boolean commit() throws LoginException {
        com.sun.security.auth.NTUserPrincipal p = new
                com.sun.security.auth.NTUserPrincipal("testPrincipal");
        subject.getPrincipals().add(p);
        return true;
    }
    public boolean abort() throws LoginException {
        return true;
    }
    public boolean logout() throws LoginException {
        return true;
    }
}
