public class JndiLoginModule implements LoginModule {
    static final java.util.ResourceBundle rb =
        java.util.ResourceBundle.getBundle("sun.security.util.AuthResources");
    public final String USER_PROVIDER = "user.provider.url";
    public final String GROUP_PROVIDER = "group.provider.url";
    private boolean debug = false;
    private boolean strongDebug = false;
    private String userProvider;
    private String groupProvider;
    private boolean useFirstPass = false;
    private boolean tryFirstPass = false;
    private boolean storePass = false;
    private boolean clearPass = false;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private String username;
    private char[] password;
    DirContext ctx;
    private UnixPrincipal userPrincipal;
    private UnixNumericUserPrincipal UIDPrincipal;
    private UnixNumericGroupPrincipal GIDPrincipal;
    private LinkedList<UnixNumericGroupPrincipal> supplementaryGroups =
                                new LinkedList<>();
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map<String, ?> options;
    private static final String CRYPT = "{crypt}";
    private static final String USER_PWD = "userPassword";
    private static final String USER_UID = "uidNumber";
    private static final String USER_GID = "gidNumber";
    private static final String GROUP_ID = "gidNumber";
    private static final String NAME = "javax.security.auth.login.name";
    private static final String PWD = "javax.security.auth.login.password";
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
        strongDebug =
                "true".equalsIgnoreCase((String)options.get("strongDebug"));
        userProvider = (String)options.get(USER_PROVIDER);
        groupProvider = (String)options.get(GROUP_PROVIDER);
        tryFirstPass =
                "true".equalsIgnoreCase((String)options.get("tryFirstPass"));
        useFirstPass =
                "true".equalsIgnoreCase((String)options.get("useFirstPass"));
        storePass =
                "true".equalsIgnoreCase((String)options.get("storePass"));
        clearPass =
                "true".equalsIgnoreCase((String)options.get("clearPass"));
    }
    public boolean login() throws LoginException {
        if (userProvider == null) {
            throw new LoginException
                ("Error: Unable to locate JNDI user provider");
        }
        if (groupProvider == null) {
            throw new LoginException
                ("Error: Unable to locate JNDI group provider");
        }
        if (debug) {
            System.out.println("\t\t[JndiLoginModule] user provider: " +
                                userProvider);
            System.out.println("\t\t[JndiLoginModule] group provider: " +
                                groupProvider);
        }
        if (tryFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "tryFirstPass succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                if (debug) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "tryFirstPass failed with:" +
                                le.toString());
                }
            }
        } else if (useFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "useFirstPass succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                if (debug) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "useFirstPass failed");
                }
                throw le;
            }
        }
        try {
            attemptAuthentication(false);
           succeeded = true;
            if (debug) {
                System.out.println("\t\t[JndiLoginModule] " +
                                "regular authentication succeeded");
            }
            return true;
        } catch (LoginException le) {
            cleanState();
            if (debug) {
                System.out.println("\t\t[JndiLoginModule] " +
                                "regular authentication failed");
            }
            throw le;
        }
    }
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            return false;
        } else {
            if (subject.isReadOnly()) {
                cleanState();
                throw new LoginException ("Subject is Readonly");
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
                System.out.println("\t\t[JndiLoginModule]: " +
                                   "added UnixPrincipal,");
                System.out.println("\t\t\t\tUnixNumericUserPrincipal,");
                System.out.println("\t\t\t\tUnixNumericGroupPrincipal(s),");
                System.out.println("\t\t\t to Subject");
            }
        }
        cleanState();
        commitSucceeded = true;
        return true;
    }
    public boolean abort() throws LoginException {
        if (debug)
            System.out.println("\t\t[JndiLoginModule]: " +
                "aborted authentication failed");
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            succeeded = false;
            cleanState();
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
            cleanState();
            throw new LoginException ("Subject is Readonly");
        }
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(UIDPrincipal);
        subject.getPrincipals().remove(GIDPrincipal);
        for (int i = 0; i < supplementaryGroups.size(); i++) {
            subject.getPrincipals().remove(supplementaryGroups.get(i));
        }
        cleanState();
        succeeded = false;
        commitSucceeded = false;
        userPrincipal = null;
        UIDPrincipal = null;
        GIDPrincipal = null;
        supplementaryGroups = new LinkedList<UnixNumericGroupPrincipal>();
        if (debug) {
            System.out.println("\t\t[JndiLoginModule]: " +
                "logged out Subject");
        }
        return true;
    }
    private void attemptAuthentication(boolean getPasswdFromSharedState)
    throws LoginException {
        String encryptedPassword = null;
        getUsernamePassword(getPasswdFromSharedState);
        try {
            InitialContext iCtx = new InitialContext();
            ctx = (DirContext)iCtx.lookup(userProvider);
            SearchControls controls = new SearchControls();
            NamingEnumeration<SearchResult> ne = ctx.search("",
                                        "(uid=" + username + ")",
                                        controls);
            if (ne.hasMore()) {
                SearchResult result = ne.next();
                Attributes attributes = result.getAttributes();
                Attribute pwd = attributes.get(USER_PWD);
                String encryptedPwd = new String((byte[])pwd.get(), "UTF8");
                encryptedPassword = encryptedPwd.substring(CRYPT.length());
                if (verifyPassword
                    (encryptedPassword, new String(password)) == true) {
                    if (debug)
                        System.out.println("\t\t[JndiLoginModule] " +
                                "attemptAuthentication() succeeded");
                } else {
                    if (debug)
                        System.out.println("\t\t[JndiLoginModule] " +
                                "attemptAuthentication() failed");
                    throw new FailedLoginException("Login incorrect");
                }
                if (storePass &&
                    !sharedState.containsKey(NAME) &&
                    !sharedState.containsKey(PWD)) {
                    sharedState.put(NAME, username);
                    sharedState.put(PWD, password);
                }
                userPrincipal = new UnixPrincipal(username);
                Attribute uid = attributes.get(USER_UID);
                String uidNumber = (String)uid.get();
                UIDPrincipal = new UnixNumericUserPrincipal(uidNumber);
                if (debug && uidNumber != null) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "user: '" + username + "' has UID: " +
                                uidNumber);
                }
                Attribute gid = attributes.get(USER_GID);
                String gidNumber = (String)gid.get();
                GIDPrincipal = new UnixNumericGroupPrincipal
                                (gidNumber, true);
                if (debug && gidNumber != null) {
                    System.out.println("\t\t[JndiLoginModule] " +
                                "user: '" + username + "' has GID: " +
                                gidNumber);
                }
                ctx = (DirContext)iCtx.lookup(groupProvider);
                ne = ctx.search("", new BasicAttributes("memberUid", username));
                while (ne.hasMore()) {
                    result = ne.next();
                    attributes = result.getAttributes();
                    gid = attributes.get(GROUP_ID);
                    String suppGid = (String)gid.get();
                    if (!gidNumber.equals(suppGid)) {
                        UnixNumericGroupPrincipal suppPrincipal =
                            new UnixNumericGroupPrincipal(suppGid, false);
                        supplementaryGroups.add(suppPrincipal);
                        if (debug && suppGid != null) {
                            System.out.println("\t\t[JndiLoginModule] " +
                                "user: '" + username +
                                "' has Supplementary Group: " +
                                suppGid);
                        }
                    }
                }
            } else {
                if (debug) {
                    System.out.println("\t\t[JndiLoginModule]: User not found");
                }
                throw new FailedLoginException("User not found");
            }
        } catch (NamingException ne) {
            if (debug) {
                System.out.println("\t\t[JndiLoginModule]:  User not found");
                ne.printStackTrace();
            }
            throw new FailedLoginException("User not found");
        } catch (java.io.UnsupportedEncodingException uee) {
            if (debug) {
                System.out.println("\t\t[JndiLoginModule]:  " +
                                "password incorrectly encoded");
                uee.printStackTrace();
            }
            throw new LoginException("Login failure due to incorrect " +
                                "password encoding in the password database");
        }
    }
    private void getUsernamePassword(boolean getPasswdFromSharedState)
    throws LoginException {
        if (getPasswdFromSharedState) {
            username = (String)sharedState.get(NAME);
            password = (char[])sharedState.get(PWD);
            return;
        }
        if (callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                "to garner authentication information from the user");
        String protocol = userProvider.substring(0, userProvider.indexOf(":"));
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback(protocol + " "
                                            + rb.getString("username."));
        callbacks[1] = new PasswordCallback(protocol + " " +
                                                rb.getString("password."),
                                            false);
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback)callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            password = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0,
                                password, 0, tmpPassword.length);
            ((PasswordCallback)callbacks[1]).clearPassword();
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                        " not available to garner authentication information " +
                        "from the user");
        }
        if (strongDebug) {
            System.out.println("\t\t[JndiLoginModule] " +
                                "user entered username: " +
                                username);
            System.out.print("\t\t[JndiLoginModule] " +
                                "user entered password: ");
            for (int i = 0; i < password.length; i++)
                System.out.print(password[i]);
            System.out.println();
        }
    }
    private boolean verifyPassword(String encryptedPassword, String password) {
        if (encryptedPassword == null)
            return false;
        Crypt c = new Crypt();
        try {
            byte oldCrypt[] = encryptedPassword.getBytes("UTF8");
            byte newCrypt[] = c.crypt(password.getBytes("UTF8"),
                                      oldCrypt);
            if (newCrypt.length != oldCrypt.length)
                return false;
            for (int i = 0; i < newCrypt.length; i++) {
                if (oldCrypt[i] != newCrypt[i])
                    return false;
            }
        } catch (java.io.UnsupportedEncodingException uee) {
            return false;
        }
        return true;
    }
    private void cleanState() {
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;
        }
        ctx = null;
        if (clearPass) {
            sharedState.remove(NAME);
            sharedState.remove(PWD);
        }
    }
}
