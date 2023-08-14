public class FileLoginModule implements LoginModule {
    private static final String DEFAULT_PASSWORD_FILE_NAME =
        AccessController.doPrivileged(new GetPropertyAction("java.home")) +
        File.separatorChar + "lib" +
        File.separatorChar + "management" + File.separatorChar +
        ConnectorBootstrap.DefaultValues.PASSWORD_FILE_NAME;
    private static final String USERNAME_KEY =
        "javax.security.auth.login.name";
    private static final String PASSWORD_KEY =
        "javax.security.auth.login.password";
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc", "FileLoginModule");
    private boolean useFirstPass = false;
    private boolean tryFirstPass = false;
    private boolean storePass = false;
    private boolean clearPass = false;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;
    private String username;
    private char[] password;
    private JMXPrincipal user;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, Object> sharedState;
    private Map<String, ?> options;
    private String passwordFile;
    private String passwordFileDisplayName;
    private boolean userSuppliedPasswordFile;
    private boolean hasJavaHomePermission;
    private Properties userCredentials;
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options)
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = Util.cast(sharedState);
        this.options = options;
        tryFirstPass =
                "true".equalsIgnoreCase((String)options.get("tryFirstPass"));
        useFirstPass =
                "true".equalsIgnoreCase((String)options.get("useFirstPass"));
        storePass =
                "true".equalsIgnoreCase((String)options.get("storePass"));
        clearPass =
                "true".equalsIgnoreCase((String)options.get("clearPass"));
        passwordFile = (String)options.get("passwordFile");
        passwordFileDisplayName = passwordFile;
        userSuppliedPasswordFile = true;
        if (passwordFile == null) {
            passwordFile = DEFAULT_PASSWORD_FILE_NAME;
            userSuppliedPasswordFile = false;
            try {
                System.getProperty("java.home");
                hasJavaHomePermission = true;
                passwordFileDisplayName = passwordFile;
            } catch (SecurityException e) {
                hasJavaHomePermission = false;
                passwordFileDisplayName =
                        ConnectorBootstrap.DefaultValues.PASSWORD_FILE_NAME;
            }
        }
    }
    public boolean login() throws LoginException {
        try {
            loadPasswordFile();
        } catch (IOException ioe) {
            LoginException le = new LoginException(
                    "Error: unable to load the password file: " +
                    passwordFileDisplayName);
            throw EnvHelp.initCause(le, ioe);
        }
        if (userCredentials == null) {
            throw new LoginException
                ("Error: unable to locate the users' credentials.");
        }
        if (logger.debugOn()) {
            logger.debug("login",
                    "Using password file: " + passwordFileDisplayName);
        }
        if (tryFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (logger.debugOn()) {
                    logger.debug("login",
                        "Authentication using cached password has succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                logger.debug("login",
                    "Authentication using cached password has failed");
            }
        } else if (useFirstPass) {
            try {
                attemptAuthentication(true);
                succeeded = true;
                if (logger.debugOn()) {
                    logger.debug("login",
                        "Authentication using cached password has succeeded");
                }
                return true;
            } catch (LoginException le) {
                cleanState();
                logger.debug("login",
                    "Authentication using cached password has failed");
                throw le;
            }
        }
        if (logger.debugOn()) {
            logger.debug("login", "Acquiring password");
        }
        try {
            attemptAuthentication(false);
            succeeded = true;
            if (logger.debugOn()) {
                logger.debug("login", "Authentication has succeeded");
            }
            return true;
        } catch (LoginException le) {
            cleanState();
            logger.debug("login", "Authentication has failed");
            throw le;
        }
    }
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            return false;
        } else {
            if (subject.isReadOnly()) {
                cleanState();
                throw new LoginException("Subject is read-only");
            }
            if (!subject.getPrincipals().contains(user)) {
                subject.getPrincipals().add(user);
            }
            if (logger.debugOn()) {
                logger.debug("commit",
                    "Authentication has completed successfully");
            }
        }
        cleanState();
        commitSucceeded = true;
        return true;
    }
    public boolean abort() throws LoginException {
        if (logger.debugOn()) {
            logger.debug("abort",
                "Authentication has not completed successfully");
        }
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            succeeded = false;
            cleanState();
            user = null;
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
        subject.getPrincipals().remove(user);
        cleanState();
        succeeded = false;
        commitSucceeded = false;
        user = null;
        if (logger.debugOn()) {
            logger.debug("logout", "Subject is being logged out");
        }
        return true;
    }
    @SuppressWarnings("unchecked")  
    private void attemptAuthentication(boolean usePasswdFromSharedState)
        throws LoginException {
        getUsernamePassword(usePasswdFromSharedState);
        String localPassword;
        if (((localPassword = userCredentials.getProperty(username)) == null) ||
            (! localPassword.equals(new String(password)))) {
            if (logger.debugOn()) {
                logger.debug("login", "Invalid username or password");
            }
            throw new FailedLoginException("Invalid username or password");
        }
        if (storePass &&
            !sharedState.containsKey(USERNAME_KEY) &&
            !sharedState.containsKey(PASSWORD_KEY)) {
            sharedState.put(USERNAME_KEY, username);
            sharedState.put(PASSWORD_KEY, password);
        }
        user = new JMXPrincipal(username);
        if (logger.debugOn()) {
            logger.debug("login",
                "User '" + username + "' successfully validated");
        }
    }
    private void loadPasswordFile() throws IOException {
        FileInputStream fis;
        try {
            fis = new FileInputStream(passwordFile);
        } catch (SecurityException e) {
            if (userSuppliedPasswordFile || hasJavaHomePermission) {
                throw e;
            } else {
                final FilePermission fp =
                        new FilePermission(passwordFileDisplayName, "read");
                AccessControlException ace = new AccessControlException(
                        "access denied " + fp.toString());
                ace.setStackTrace(e.getStackTrace());
                throw ace;
            }
        }
        try {
            final BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                userCredentials = new Properties();
                userCredentials.load(bis);
            } finally {
                bis.close();
            }
        } finally {
            fis.close();
        }
    }
    private void getUsernamePassword(boolean usePasswdFromSharedState)
        throws LoginException {
        if (usePasswdFromSharedState) {
            username = (String)sharedState.get(USERNAME_KEY);
            password = (char[])sharedState.get(PASSWORD_KEY);
            return;
        }
        if (callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                "to garner authentication information from the user");
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("username");
        callbacks[1] = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback)callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            password = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0,
                                password, 0, tmpPassword.length);
            ((PasswordCallback)callbacks[1]).clearPassword();
        } catch (IOException ioe) {
            LoginException le = new LoginException(ioe.toString());
            throw EnvHelp.initCause(le, ioe);
        } catch (UnsupportedCallbackException uce) {
            LoginException le = new LoginException(
                                    "Error: " + uce.getCallback().toString() +
                                    " not available to garner authentication " +
                                    "information from the user");
            throw EnvHelp.initCause(le, uce);
        }
    }
    private void cleanState() {
        username = null;
        if (password != null) {
            Arrays.fill(password, ' ');
            password = null;
        }
        if (clearPass) {
            sharedState.remove(USERNAME_KEY);
            sharedState.remove(PASSWORD_KEY);
        }
    }
}
