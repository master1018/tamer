public final class JMXPluggableAuthenticator implements JMXAuthenticator {
    public JMXPluggableAuthenticator(Map<?, ?> env) {
        String loginConfigName = null;
        String passwordFile = null;
        if (env != null) {
            loginConfigName = (String) env.get(LOGIN_CONFIG_PROP);
            passwordFile = (String) env.get(PASSWORD_FILE_PROP);
        }
        try {
            if (loginConfigName != null) {
                loginContext =
                    new LoginContext(loginConfigName, new JMXCallbackHandler());
            } else {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(
                            new AuthPermission("createLoginContext." +
                                               LOGIN_CONFIG_NAME));
                }
                final String pf = passwordFile;
                try {
                    loginContext = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<LoginContext>() {
                            public LoginContext run() throws LoginException {
                                return new LoginContext(
                                                LOGIN_CONFIG_NAME,
                                                null,
                                                new JMXCallbackHandler(),
                                                new FileLoginConfig(pf));
                            }
                        });
                } catch (PrivilegedActionException pae) {
                    throw (LoginException) pae.getException();
                }
            }
        } catch (LoginException le) {
            authenticationFailure("authenticate", le);
        } catch (SecurityException se) {
            authenticationFailure("authenticate", se);
        }
    }
    public Subject authenticate(Object credentials) {
        if (!(credentials instanceof String[])) {
            if (credentials == null)
                authenticationFailure("authenticate", "Credentials required");
            final String message =
                "Credentials should be String[] instead of " +
                 credentials.getClass().getName();
            authenticationFailure("authenticate", message);
        }
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length != 2) {
            final String message =
                "Credentials should have 2 elements not " +
                aCredentials.length;
            authenticationFailure("authenticate", message);
        }
        username = aCredentials[0];
        password = aCredentials[1];
        if (username == null || password == null) {
            final String message = "Username or password is null";
            authenticationFailure("authenticate", message);
        }
        try {
            loginContext.login();
            final Subject subject = loginContext.getSubject();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        subject.setReadOnly();
                        return null;
                    }
                });
            return subject;
        } catch (LoginException le) {
            authenticationFailure("authenticate", le);
        }
        return null;
    }
    private static void authenticationFailure(String method, String message)
        throws SecurityException {
        final String msg = "Authentication failed! " + message;
        final SecurityException e = new SecurityException(msg);
        logException(method, msg, e);
        throw e;
    }
    private static void authenticationFailure(String method,
                                              Exception exception)
        throws SecurityException {
        String msg;
        SecurityException se;
        if (exception instanceof SecurityException) {
            msg = exception.getMessage();
            se = (SecurityException) exception;
        } else {
            msg = "Authentication failed! " + exception.getMessage();
            final SecurityException e = new SecurityException(msg);
            EnvHelp.initCause(e, exception);
            se = e;
        }
        logException(method, msg, se);
        throw se;
    }
    private static void logException(String method,
                                     String message,
                                     Exception e) {
        if (logger.traceOn()) {
            logger.trace(method, message);
        }
        if (logger.debugOn()) {
            logger.debug(method, e);
        }
    }
    private LoginContext loginContext;
    private String username;
    private String password;
    private static final String LOGIN_CONFIG_PROP =
        "jmx.remote.x.login.config";
    private static final String LOGIN_CONFIG_NAME = "JMXPluggableAuthenticator";
    private static final String PASSWORD_FILE_PROP =
        "jmx.remote.x.password.file";
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc", LOGIN_CONFIG_NAME);
private final class JMXCallbackHandler implements CallbackHandler {
    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback)callbacks[i]).setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback)callbacks[i])
                    .setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException
                    (callbacks[i], "Unrecognized Callback");
            }
        }
    }
}
private static class FileLoginConfig extends Configuration {
    private AppConfigurationEntry[] entries;
    private static final String FILE_LOGIN_MODULE =
        FileLoginModule.class.getName();
    private static final String PASSWORD_FILE_OPTION = "passwordFile";
    public FileLoginConfig(String passwordFile) {
        Map<String, String> options;
        if (passwordFile != null) {
            options = new HashMap<String, String>(1);
            options.put(PASSWORD_FILE_OPTION, passwordFile);
        } else {
            options = Collections.emptyMap();
        }
        entries = new AppConfigurationEntry[] {
            new AppConfigurationEntry(FILE_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options)
        };
    }
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return name.equals(LOGIN_CONFIG_NAME) ? entries : null;
    }
    public void refresh() {
    }
}
}
