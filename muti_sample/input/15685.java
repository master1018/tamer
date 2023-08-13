public class LdapLoginModule implements LoginModule {
    private static final ResourceBundle rb = AccessController.doPrivileged(
            new PrivilegedAction<ResourceBundle>() {
                public ResourceBundle run() {
                    return ResourceBundle.getBundle(
                        "sun.security.util.AuthResources");
                }
            }
        );
    private static final String USERNAME_KEY = "javax.security.auth.login.name";
    private static final String PASSWORD_KEY =
        "javax.security.auth.login.password";
    private static final String USER_PROVIDER = "userProvider";
    private static final String USER_FILTER = "userFilter";
    private static final String AUTHC_IDENTITY = "authIdentity";
    private static final String AUTHZ_IDENTITY = "authzIdentity";
    private static final String USERNAME_TOKEN = "{USERNAME}";
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("\\{USERNAME\\}");
    private String userProvider;
    private String userFilter;
    private String authcIdentity;
    private String authzIdentity;
    private String authzIdentityAttr = null;
    private boolean useSSL = true;
    private boolean authFirst = false;
    private boolean authOnly = false;
    private boolean useFirstPass = false;
    private boolean tryFirstPass = false;
    private boolean storePass = false;
    private boolean clearPass = false;
    private boolean debug = false;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private String username;
    private char[] password;
    private LdapPrincipal ldapPrincipal;
    private UserPrincipal userPrincipal;
    private UserPrincipal authzPrincipal;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map<String, ?> options;
    private LdapContext ctx;
    private Matcher identityMatcher = null;
    private Matcher filterMatcher = null;
    private Hashtable ldapEnvironment;
    private SearchControls constraints = null;
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                        Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        ldapEnvironment = new Hashtable(9);
        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        for (String key : options.keySet()) {
            if (key.indexOf(".") > -1) {
                ldapEnvironment.put(key, options.get(key));
            }
        }
        userProvider = (String)options.get(USER_PROVIDER);
        if (userProvider != null) {
            ldapEnvironment.put(Context.PROVIDER_URL, userProvider);
        }
        authcIdentity = (String)options.get(AUTHC_IDENTITY);
        if (authcIdentity != null &&
            (authcIdentity.indexOf(USERNAME_TOKEN) != -1)) {
            identityMatcher = USERNAME_PATTERN.matcher(authcIdentity);
        }
        userFilter = (String)options.get(USER_FILTER);
        if (userFilter != null) {
            if (userFilter.indexOf(USERNAME_TOKEN) != -1) {
                filterMatcher = USERNAME_PATTERN.matcher(userFilter);
            }
            constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(new String[0]); 
            constraints.setReturningObjFlag(true); 
        }
        authzIdentity = (String)options.get(AUTHZ_IDENTITY);
        if (authzIdentity != null &&
            authzIdentity.startsWith("{") && authzIdentity.endsWith("}")) {
            if (constraints != null) {
                authzIdentityAttr =
                    authzIdentity.substring(1, authzIdentity.length() - 1);
                constraints.setReturningAttributes(
                    new String[]{authzIdentityAttr});
            }
            authzIdentity = null; 
        }
        if (authcIdentity != null) {
            if (userFilter != null) {
                authFirst = true; 
            } else {
                authOnly = true; 
            }
        }
        if ("false".equalsIgnoreCase((String)options.get("useSSL"))) {
            useSSL = false;
            ldapEnvironment.remove(Context.SECURITY_PROTOCOL);
        } else {
            ldapEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        tryFirstPass =
                "true".equalsIgnoreCase((String)options.get("tryFirstPass"));
        useFirstPass =
                "true".equalsIgnoreCase((String)options.get("useFirstPass"));
        storePass = "true".equalsIgnoreCase((String)options.get("storePass"));
        clearPass = "true".equalsIgnoreCase((String)options.get("clearPass"));
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
        if (debug) {
            if (authFirst) {
                System.out.println("\t\t[LdapLoginModule] " +
                    "authentication-first mode; " +
                    (useSSL ? "SSL enabled" : "SSL disabled"));
            } else if (authOnly) {
                System.out.println("\t\t[LdapLoginModule] " +
                    "authentication-only mode; " +
                    (useSSL ? "SSL enabled" : "SSL disabled"));
            } else {
                System.out.println("\t\t[LdapLoginModule] " +
                    "search-first mode; " +
                    (useSSL ? "SSL enabled" : "SSL disabled"));
            }
        }
    }
    public boolean login() throws LoginException {
        if (userProvider == null) {
            throw new LoginException
                ("Unable to locate the LDAP directory service");
        }
        if (debug) {
            System.out.println("\t\t[LdapLoginModule] user provider: " +
                userProvider);
        }
        if (tryFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                                "tryFirstPass succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                                "tryFirstPass failed: " + le.toString());
                }
            }
        } else if (useFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                                "useFirstPass succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                                "useFirstPass failed");
                }
                throw le;
            }
        }
        try {
            attemptAuthentication(false);
           succeeded = true;
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                                "authentication succeeded");
            }
            return true;
        } catch (LoginException le) {
            cleanState();
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                                "authentication failed");
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
                throw new LoginException ("Subject is read-only");
            }
            Set<Principal> principals = subject.getPrincipals();
            if (! principals.contains(ldapPrincipal)) {
                principals.add(ldapPrincipal);
            }
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                                   "added LdapPrincipal \"" +
                                   ldapPrincipal +
                                   "\" to Subject");
            }
            if (! principals.contains(userPrincipal)) {
                principals.add(userPrincipal);
            }
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                                   "added UserPrincipal \"" +
                                   userPrincipal +
                                   "\" to Subject");
            }
            if (authzPrincipal != null &&
                (! principals.contains(authzPrincipal))) {
                principals.add(authzPrincipal);
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                                   "added UserPrincipal \"" +
                                   authzPrincipal +
                                   "\" to Subject");
                }
            }
        }
        cleanState();
        commitSucceeded = true;
        return true;
    }
    public boolean abort() throws LoginException {
        if (debug)
            System.out.println("\t\t[LdapLoginModule] " +
                "aborted authentication");
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            succeeded = false;
            cleanState();
            ldapPrincipal = null;
            userPrincipal = null;
            authzPrincipal = null;
        } else {
            logout();
        }
        return true;
    }
    public boolean logout() throws LoginException {
        if (subject.isReadOnly()) {
            cleanState();
            throw new LoginException ("Subject is read-only");
        }
        Set<Principal> principals = subject.getPrincipals();
        principals.remove(ldapPrincipal);
        principals.remove(userPrincipal);
        if (authzIdentity != null) {
            principals.remove(authzPrincipal);
        }
        cleanState();
        succeeded = false;
        commitSucceeded = false;
        ldapPrincipal = null;
        userPrincipal = null;
        authzPrincipal = null;
        if (debug) {
            System.out.println("\t\t[LdapLoginModule] logged out Subject");
        }
        return true;
    }
    private void attemptAuthentication(boolean getPasswdFromSharedState)
        throws LoginException {
        getUsernamePassword(getPasswdFromSharedState);
        if (password == null || password.length == 0) {
            throw (LoginException)
                new FailedLoginException("No password was supplied");
        }
        String dn = "";
        if (authFirst || authOnly) {
            String id = replaceUsernameToken(identityMatcher, authcIdentity);
            ldapEnvironment.put(Context.SECURITY_CREDENTIALS, password);
            ldapEnvironment.put(Context.SECURITY_PRINCIPAL, id);
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                    "attempting to authenticate user: " + username);
            }
            try {
                ctx = new InitialLdapContext(ldapEnvironment, null);
            } catch (NamingException e) {
                throw (LoginException)
                    new FailedLoginException("Cannot bind to LDAP server")
                        .initCause(e);
            }
            if (userFilter != null) {
                dn = findUserDN(ctx);
            } else {
                dn = id;
            }
        } else {
            try {
                ctx = new InitialLdapContext(ldapEnvironment, null);
            } catch (NamingException e) {
                throw (LoginException)
                    new FailedLoginException("Cannot connect to LDAP server")
                        .initCause(e);
            }
            dn = findUserDN(ctx);
            try {
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] " +
                        "attempting to authenticate user: " + username);
                }
                ctx.reconnect(null);
            } catch (NamingException e) {
                throw (LoginException)
                    new FailedLoginException("Cannot bind to LDAP server")
                        .initCause(e);
            }
        }
        if (storePass &&
            !sharedState.containsKey(USERNAME_KEY) &&
            !sharedState.containsKey(PASSWORD_KEY)) {
            sharedState.put(USERNAME_KEY, username);
            sharedState.put(PASSWORD_KEY, password);
        }
        userPrincipal = new UserPrincipal(username);
        if (authzIdentity != null) {
            authzPrincipal = new UserPrincipal(authzIdentity);
        }
        try {
            ldapPrincipal = new LdapPrincipal(dn);
        } catch (InvalidNameException e) {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                                   "cannot create LdapPrincipal: bad DN");
            }
            throw (LoginException)
                new FailedLoginException("Cannot create LdapPrincipal")
                    .initCause(e);
        }
    }
    private String findUserDN(LdapContext ctx) throws LoginException {
        String userDN = "";
        if (userFilter != null) {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                    "searching for entry belonging to user: " + username);
            }
        } else {
            if (debug) {
                System.out.println("\t\t[LdapLoginModule] " +
                    "cannot search for entry belonging to user: " + username);
            }
            throw (LoginException)
                new FailedLoginException("Cannot find user's LDAP entry");
        }
        try {
            NamingEnumeration<SearchResult> results = ctx.search("",
                replaceUsernameToken(filterMatcher, userFilter), constraints);
            if (results.hasMore()) {
                SearchResult entry = results.next();
                userDN = ((Context)entry.getObject()).getNameInNamespace();
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] found entry: " +
                        userDN);
                }
                if (authzIdentityAttr != null) {
                    Attribute attr =
                        entry.getAttributes().get(authzIdentityAttr);
                    if (attr != null) {
                        Object val = attr.get();
                        if (val instanceof String) {
                            authzIdentity = (String) val;
                        }
                    }
                }
                results.close();
            } else {
                if (debug) {
                    System.out.println("\t\t[LdapLoginModule] user's entry " +
                        "not found");
                }
            }
        } catch (NamingException e) {
        }
        if (userDN.equals("")) {
            throw (LoginException)
                new FailedLoginException("Cannot find user's LDAP entry");
        } else {
            return userDN;
        }
    }
    private String replaceUsernameToken(Matcher matcher, String string) {
        return matcher != null ? matcher.replaceAll(username) : string;
    }
    private void getUsernamePassword(boolean getPasswdFromSharedState)
        throws LoginException {
        if (getPasswdFromSharedState) {
            username = (String)sharedState.get(USERNAME_KEY);
            password = (char[])sharedState.get(PASSWORD_KEY);
            return;
        }
        if (callbackHandler == null)
            throw new LoginException("No CallbackHandler available " +
                "to acquire authentication information from the user");
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback(rb.getString("username."));
        callbacks[1] = new PasswordCallback(rb.getString("password."), false);
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
                        " not available to acquire authentication information" +
                        " from the user");
        }
    }
    private void cleanState() {
        username = null;
        if (password != null) {
            Arrays.fill(password, ' ');
            password = null;
        }
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException e) {
        }
        ctx = null;
        if (clearPass) {
            sharedState.remove(USERNAME_KEY);
            sharedState.remove(PASSWORD_KEY);
        }
    }
}
