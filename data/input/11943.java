public class UnixLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    private boolean debug = true;
    private UnixSystem ss;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private UnixPrincipal userPrincipal;
    private UnixNumericUserPrincipal UIDPrincipal;
    private UnixNumericGroupPrincipal GIDPrincipal;
    private LinkedList<UnixNumericGroupPrincipal> supplementaryGroups =
                new LinkedList<>();
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
    }
    public boolean login() throws LoginException {
        long[] unixGroups = null;
        ss = new UnixSystem();
        if (ss == null) {
            succeeded = false;
            throw new FailedLoginException
                                ("Failed in attempt to import " +
                                "the underlying system identity information");
        } else {
            userPrincipal = new UnixPrincipal(ss.getUsername());
            UIDPrincipal = new UnixNumericUserPrincipal(ss.getUid());
            GIDPrincipal = new UnixNumericGroupPrincipal(ss.getGid(), true);
            if (ss.getGroups() != null && ss.getGroups().length > 0) {
                unixGroups = ss.getGroups();
                for (int i = 0; i < unixGroups.length; i++) {
                    UnixNumericGroupPrincipal ngp =
                        new UnixNumericGroupPrincipal
                        (unixGroups[i], false);
                    if (!ngp.getName().equals(GIDPrincipal.getName()))
                        supplementaryGroups.add(ngp);
                }
            }
            if (debug) {
                System.out.println("\t\t[UnixLoginModule]: " +
                        "succeeded importing info: ");
                System.out.println("\t\t\tuid = " + ss.getUid());
                System.out.println("\t\t\tgid = " + ss.getGid());
                unixGroups = ss.getGroups();
                for (int i = 0; i < unixGroups.length; i++) {
                    System.out.println("\t\t\tsupp gid = " + unixGroups[i]);
                }
            }
            succeeded = true;
            return true;
        }
    }
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            if (debug) {
                System.out.println("\t\t[UnixLoginModule]: " +
                    "did not add any Principals to Subject " +
                    "because own authentication failed.");
            }
            return false;
        } else {
            if (subject.isReadOnly()) {
                throw new LoginException
                    ("commit Failed: Subject is Readonly");
            }
            if (!subject.getPrincipals().contains(userPrincipal))
                subject.getPrincipals().add(userPrincipal);
            if (!subject.getPrincipals().contains(UIDPrincipal))
                subject.getPrincipals().add(UIDPrincipal);
            if (!subject.getPrincipals().contains(GIDPrincipal))
                subject.getPrincipals().add(GIDPrincipal);
            for (int i = 0; i < supplementaryGroups.size(); i++) {
                if (!subject.getPrincipals().contains
                    (supplementaryGroups.get(i)))
                    subject.getPrincipals().add(supplementaryGroups.get(i));
            }
            if (debug) {
                System.out.println("\t\t[UnixLoginModule]: " +
                    "added UnixPrincipal,");
                System.out.println("\t\t\t\tUnixNumericUserPrincipal,");
                System.out.println("\t\t\t\tUnixNumericGroupPrincipal(s),");
                System.out.println("\t\t\t to Subject");
            }
            commitSucceeded = true;
            return true;
        }
    }
    public boolean abort() throws LoginException {
        if (debug) {
            System.out.println("\t\t[UnixLoginModule]: " +
                "aborted authentication attempt");
        }
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            succeeded = false;
            ss = null;
            userPrincipal = null;
            UIDPrincipal = null;
            GIDPrincipal = null;
            supplementaryGroups = new LinkedList<UnixNumericGroupPrincipal>();
        } else {
            logout();
        }
        return true;
    }
    public boolean logout() throws LoginException {
        if (subject.isReadOnly()) {
                throw new LoginException
                    ("logout Failed: Subject is Readonly");
            }
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(UIDPrincipal);
        subject.getPrincipals().remove(GIDPrincipal);
        for (int i = 0; i < supplementaryGroups.size(); i++) {
            subject.getPrincipals().remove(supplementaryGroups.get(i));
        }
        ss = null;
        succeeded = false;
        commitSucceeded = false;
        userPrincipal = null;
        UIDPrincipal = null;
        GIDPrincipal = null;
        supplementaryGroups = new LinkedList<UnixNumericGroupPrincipal>();
        if (debug) {
            System.out.println("\t\t[UnixLoginModule]: " +
                "logged out Subject");
        }
        return true;
    }
}
