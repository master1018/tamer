public class LoginContext {
    private static final String INIT_METHOD             = "initialize";
    private static final String LOGIN_METHOD            = "login";
    private static final String COMMIT_METHOD           = "commit";
    private static final String ABORT_METHOD            = "abort";
    private static final String LOGOUT_METHOD           = "logout";
    private static final String OTHER                   = "other";
    private static final String DEFAULT_HANDLER         =
                                "auth.login.defaultCallbackHandler";
    private Subject subject = null;
    private boolean subjectProvided = false;
    private boolean loginSucceeded = false;
    private CallbackHandler callbackHandler;
    private Map state = new HashMap();
    private Configuration config;
    private boolean configProvided = false;
    private AccessControlContext creatorAcc = null;
    private ModuleInfo[] moduleStack;
    private ClassLoader contextClassLoader = null;
    private static final Class[] PARAMS = { };
    private int moduleIndex = 0;
    private LoginException firstError = null;
    private LoginException firstRequiredError = null;
    private boolean success = false;
    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("logincontext", "\t[LoginContext]");
    private void init(String name) throws LoginException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && !configProvided) {
            sm.checkPermission(new AuthPermission
                                ("createLoginContext." + name));
        }
        if (name == null)
            throw new LoginException
                (ResourcesMgr.getString("Invalid.null.input.name"));
        if (config == null) {
            config = java.security.AccessController.doPrivileged
                (new java.security.PrivilegedAction<Configuration>() {
                public Configuration run() {
                    return Configuration.getConfiguration();
                }
            });
        }
        AppConfigurationEntry[] entries = config.getAppConfigurationEntry(name);
        if (entries == null) {
            if (sm != null && !configProvided) {
                sm.checkPermission(new AuthPermission
                                ("createLoginContext." + OTHER));
            }
            entries = config.getAppConfigurationEntry(OTHER);
            if (entries == null) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("No.LoginModules.configured.for.name"));
                Object[] source = {name};
                throw new LoginException(form.format(source));
            }
        }
        moduleStack = new ModuleInfo[entries.length];
        for (int i = 0; i < entries.length; i++) {
            moduleStack[i] = new ModuleInfo
                                (new AppConfigurationEntry
                                        (entries[i].getLoginModuleName(),
                                        entries[i].getControlFlag(),
                                        entries[i].getOptions()),
                                null);
        }
        contextClassLoader = java.security.AccessController.doPrivileged
                (new java.security.PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
        });
    }
    private void loadDefaultCallbackHandler() throws LoginException {
        try {
            final ClassLoader finalLoader = contextClassLoader;
            this.callbackHandler = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction<CallbackHandler>() {
                public CallbackHandler run() throws Exception {
                    String defaultHandler = java.security.Security.getProperty
                        (DEFAULT_HANDLER);
                    if (defaultHandler == null || defaultHandler.length() == 0)
                        return null;
                    Class c = Class.forName(defaultHandler,
                                        true,
                                        finalLoader);
                    return (CallbackHandler)c.newInstance();
                }
            });
        } catch (java.security.PrivilegedActionException pae) {
            throw new LoginException(pae.getException().toString());
        }
        if (this.callbackHandler != null && !configProvided) {
            this.callbackHandler = new SecureCallbackHandler
                                (java.security.AccessController.getContext(),
                                this.callbackHandler);
        }
    }
    public LoginContext(String name) throws LoginException {
        init(name);
        loadDefaultCallbackHandler();
    }
    public LoginContext(String name, Subject subject)
    throws LoginException {
        init(name);
        if (subject == null)
            throw new LoginException
                (ResourcesMgr.getString("invalid.null.Subject.provided"));
        this.subject = subject;
        subjectProvided = true;
        loadDefaultCallbackHandler();
    }
    public LoginContext(String name, CallbackHandler callbackHandler)
    throws LoginException {
        init(name);
        if (callbackHandler == null)
            throw new LoginException(ResourcesMgr.getString
                                ("invalid.null.CallbackHandler.provided"));
        this.callbackHandler = new SecureCallbackHandler
                                (java.security.AccessController.getContext(),
                                callbackHandler);
    }
    public LoginContext(String name, Subject subject,
                        CallbackHandler callbackHandler) throws LoginException {
        this(name, subject);
        if (callbackHandler == null)
            throw new LoginException(ResourcesMgr.getString
                                ("invalid.null.CallbackHandler.provided"));
        this.callbackHandler = new SecureCallbackHandler
                                (java.security.AccessController.getContext(),
                                callbackHandler);
    }
    public LoginContext(String name, Subject subject,
                        CallbackHandler callbackHandler,
                        Configuration config) throws LoginException {
        this.config = config;
        configProvided = (config != null) ? true : false;
        if (configProvided) {
            creatorAcc = java.security.AccessController.getContext();
        }
        init(name);
        if (subject != null) {
            this.subject = subject;
            subjectProvided = true;
        }
        if (callbackHandler == null) {
            loadDefaultCallbackHandler();
        } else if (!configProvided) {
            this.callbackHandler = new SecureCallbackHandler
                                (java.security.AccessController.getContext(),
                                callbackHandler);
        } else {
            this.callbackHandler = callbackHandler;
        }
    }
    public void login() throws LoginException {
        loginSucceeded = false;
        if (subject == null) {
            subject = new Subject();
        }
        try {
            if (configProvided) {
                invokeCreatorPriv(LOGIN_METHOD);
                invokeCreatorPriv(COMMIT_METHOD);
            } else {
                invokePriv(LOGIN_METHOD);
                invokePriv(COMMIT_METHOD);
            }
            loginSucceeded = true;
        } catch (LoginException le) {
            try {
                if (configProvided) {
                    invokeCreatorPriv(ABORT_METHOD);
                } else {
                    invokePriv(ABORT_METHOD);
                }
            } catch (LoginException le2) {
                throw le;
            }
            throw le;
        }
    }
    public void logout() throws LoginException {
        if (subject == null) {
            throw new LoginException(ResourcesMgr.getString
                ("null.subject.logout.called.before.login"));
        }
        if (configProvided) {
            invokeCreatorPriv(LOGOUT_METHOD);
        } else {
            invokePriv(LOGOUT_METHOD);
        }
    }
    public Subject getSubject() {
        if (!loginSucceeded && !subjectProvided)
            return null;
        return subject;
    }
    private void clearState() {
        moduleIndex = 0;
        firstError = null;
        firstRequiredError = null;
        success = false;
    }
    private void throwException(LoginException originalError, LoginException le)
    throws LoginException {
        clearState();
        LoginException error = (originalError != null) ? originalError : le;
        throw error;
    }
    private void invokePriv(final String methodName) throws LoginException {
        try {
            java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction<Void>() {
                public Void run() throws LoginException {
                    invoke(methodName);
                    return null;
                }
            });
        } catch (java.security.PrivilegedActionException pae) {
            throw (LoginException)pae.getException();
        }
    }
    private void invokeCreatorPriv(final String methodName)
                throws LoginException {
        try {
            java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction<Void>() {
                public Void run() throws LoginException {
                    invoke(methodName);
                    return null;
                }
            }, creatorAcc);
        } catch (java.security.PrivilegedActionException pae) {
            throw (LoginException)pae.getException();
        }
    }
    private void invoke(String methodName) throws LoginException {
        for (int i = moduleIndex; i < moduleStack.length; i++, moduleIndex++) {
            try {
                int mIndex = 0;
                Method[] methods = null;
                if (moduleStack[i].module != null) {
                    methods = moduleStack[i].module.getClass().getMethods();
                } else {
                    Class c = Class.forName
                                (moduleStack[i].entry.getLoginModuleName(),
                                true,
                                contextClassLoader);
                    Constructor constructor = c.getConstructor(PARAMS);
                    Object[] args = { };
                    moduleStack[i].module = constructor.newInstance(args);
                    methods = moduleStack[i].module.getClass().getMethods();
                    for (mIndex = 0; mIndex < methods.length; mIndex++) {
                        if (methods[mIndex].getName().equals(INIT_METHOD))
                            break;
                    }
                    Object[] initArgs = {subject,
                                        callbackHandler,
                                        state,
                                        moduleStack[i].entry.getOptions() };
                    methods[mIndex].invoke(moduleStack[i].module, initArgs);
                }
                for (mIndex = 0; mIndex < methods.length; mIndex++) {
                    if (methods[mIndex].getName().equals(methodName))
                        break;
                }
                Object[] args = { };
                boolean status = ((Boolean)methods[mIndex].invoke
                                (moduleStack[i].module, args)).booleanValue();
                if (status == true) {
                    if (!methodName.equals(ABORT_METHOD) &&
                        !methodName.equals(LOGOUT_METHOD) &&
                        moduleStack[i].entry.getControlFlag() ==
                    AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT &&
                        firstRequiredError == null) {
                        clearState();
                        if (debug != null)
                            debug.println(methodName + " SUFFICIENT success");
                        return;
                    }
                    if (debug != null)
                        debug.println(methodName + " success");
                    success = true;
                } else {
                    if (debug != null)
                        debug.println(methodName + " ignored");
                }
            } catch (NoSuchMethodException nsme) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("unable.to.instantiate.LoginModule.module.because.it.does.not.provide.a.no.argument.constructor"));
                Object[] source = {moduleStack[i].entry.getLoginModuleName()};
                throwException(null, new LoginException(form.format(source)));
            } catch (InstantiationException ie) {
                throwException(null, new LoginException(ResourcesMgr.getString
                        ("unable.to.instantiate.LoginModule.") +
                        ie.getMessage()));
            } catch (ClassNotFoundException cnfe) {
                throwException(null, new LoginException(ResourcesMgr.getString
                        ("unable.to.find.LoginModule.class.") +
                        cnfe.getMessage()));
            } catch (IllegalAccessException iae) {
                throwException(null, new LoginException(ResourcesMgr.getString
                        ("unable.to.access.LoginModule.") +
                        iae.getMessage()));
            } catch (InvocationTargetException ite) {
                LoginException le;
                if (ite.getCause() instanceof PendingException &&
                    methodName.equals(LOGIN_METHOD)) {
                    throw (PendingException)ite.getCause();
                } else if (ite.getCause() instanceof LoginException) {
                    le = (LoginException)ite.getCause();
                } else if (ite.getCause() instanceof SecurityException) {
                    le = new LoginException("Security Exception");
                    le.initCause(new SecurityException());
                    if (debug != null) {
                        debug.println
                            ("original security exception with detail msg " +
                            "replaced by new exception with empty detail msg");
                        debug.println("original security exception: " +
                                ite.getCause().toString());
                    }
                } else {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    ite.getCause().printStackTrace
                                                (new java.io.PrintWriter(sw));
                    sw.flush();
                    le = new LoginException(sw.toString());
                }
                if (moduleStack[i].entry.getControlFlag() ==
                    AppConfigurationEntry.LoginModuleControlFlag.REQUISITE) {
                    if (debug != null)
                        debug.println(methodName + " REQUISITE failure");
                    if (methodName.equals(ABORT_METHOD) ||
                        methodName.equals(LOGOUT_METHOD)) {
                        if (firstRequiredError == null)
                            firstRequiredError = le;
                    } else {
                        throwException(firstRequiredError, le);
                    }
                } else if (moduleStack[i].entry.getControlFlag() ==
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED) {
                    if (debug != null)
                        debug.println(methodName + " REQUIRED failure");
                    if (firstRequiredError == null)
                        firstRequiredError = le;
                } else {
                    if (debug != null)
                        debug.println(methodName + " OPTIONAL failure");
                    if (firstError == null)
                        firstError = le;
                }
            }
        }
        if (firstRequiredError != null) {
            throwException(firstRequiredError, null);
        } else if (success == false && firstError != null) {
            throwException(firstError, null);
        } else if (success == false) {
            throwException(new LoginException
                (ResourcesMgr.getString("Login.Failure.all.modules.ignored")),
                null);
        } else {
            clearState();
            return;
        }
    }
    private static class SecureCallbackHandler implements CallbackHandler {
        private final java.security.AccessControlContext acc;
        private final CallbackHandler ch;
        SecureCallbackHandler(java.security.AccessControlContext acc,
                        CallbackHandler ch) {
            this.acc = acc;
            this.ch = ch;
        }
        public void handle(final Callback[] callbacks)
                throws java.io.IOException, UnsupportedCallbackException {
            try {
                java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedExceptionAction<Void>() {
                    public Void run() throws java.io.IOException,
                                        UnsupportedCallbackException {
                        ch.handle(callbacks);
                        return null;
                    }
                }, acc);
            } catch (java.security.PrivilegedActionException pae) {
                if (pae.getException() instanceof java.io.IOException) {
                    throw (java.io.IOException)pae.getException();
                } else {
                    throw (UnsupportedCallbackException)pae.getException();
                }
            }
        }
    }
    private static class ModuleInfo {
        AppConfigurationEntry entry;
        Object module;
        ModuleInfo(AppConfigurationEntry newEntry, Object newModule) {
            this.entry = newEntry;
            this.module = newModule;
        }
    }
}
