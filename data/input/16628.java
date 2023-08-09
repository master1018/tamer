public class KeyStoreLoginModule implements LoginModule {
   static final java.util.ResourceBundle rb =
        java.util.ResourceBundle.getBundle("sun.security.util.AuthResources");
    private static final int UNINITIALIZED = 0;
    private static final int INITIALIZED = 1;
    private static final int AUTHENTICATED = 2;
    private static final int LOGGED_IN = 3;
    private static final int PROTECTED_PATH = 0;
    private static final int TOKEN = 1;
    private static final int NORMAL = 2;
    private static final String NONE = "NONE";
    private static final String P11KEYSTORE = "PKCS11";
    private static final TextOutputCallback bannerCallback =
                new TextOutputCallback
                        (TextOutputCallback.INFORMATION,
                        rb.getString("Please.enter.keystore.information"));
    private final ConfirmationCallback confirmationCallback =
                new ConfirmationCallback
                        (ConfirmationCallback.INFORMATION,
                        ConfirmationCallback.OK_CANCEL_OPTION,
                        ConfirmationCallback.OK);
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map<String, ?> options;
    private char[] keyStorePassword;
    private char[] privateKeyPassword;
    private KeyStore keyStore;
    private String keyStoreURL;
    private String keyStoreType;
    private String keyStoreProvider;
    private String keyStoreAlias;
    private String keyStorePasswordURL;
    private String privateKeyPasswordURL;
    private boolean debug;
    private javax.security.auth.x500.X500Principal principal;
    private Certificate[] fromKeyStore;
    private java.security.cert.CertPath certP = null;
    private X500PrivateCredential privateCredential;
    private int status = UNINITIALIZED;
    private boolean nullStream = false;
    private boolean token = false;
    private boolean protectedPath = false;
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String,?> sharedState,
                           Map<String,?> options)
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        processOptions();
        status = INITIALIZED;
    }
    private void processOptions() {
        keyStoreURL = (String) options.get("keyStoreURL");
        if (keyStoreURL == null) {
            keyStoreURL =
                "file:" +
                System.getProperty("user.home").replace(
                    File.separatorChar, '/') +
                '/' + ".keystore";
        } else if (NONE.equals(keyStoreURL)) {
            nullStream = true;
        }
        keyStoreType = (String) options.get("keyStoreType");
        if (keyStoreType == null) {
            keyStoreType = KeyStore.getDefaultType();
        }
        if (P11KEYSTORE.equalsIgnoreCase(keyStoreType)) {
            token = true;
        }
        keyStoreProvider = (String) options.get("keyStoreProvider");
        keyStoreAlias = (String) options.get("keyStoreAlias");
        keyStorePasswordURL = (String) options.get("keyStorePasswordURL");
        privateKeyPasswordURL = (String) options.get("privateKeyPasswordURL");
        protectedPath = "true".equalsIgnoreCase((String)options.get
                                        ("protected"));
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (debug) {
            debugPrint(null);
            debugPrint("keyStoreURL=" + keyStoreURL);
            debugPrint("keyStoreType=" + keyStoreType);
            debugPrint("keyStoreProvider=" + keyStoreProvider);
            debugPrint("keyStoreAlias=" + keyStoreAlias);
            debugPrint("keyStorePasswordURL=" + keyStorePasswordURL);
            debugPrint("privateKeyPasswordURL=" + privateKeyPasswordURL);
            debugPrint("protectedPath=" + protectedPath);
            debugPrint(null);
        }
    }
    public boolean login() throws LoginException {
        switch (status) {
        case UNINITIALIZED:
        default:
            throw new LoginException("The login module is not initialized");
        case INITIALIZED:
        case AUTHENTICATED:
            if (token && !nullStream) {
                throw new LoginException
                        ("if keyStoreType is " + P11KEYSTORE +
                        " then keyStoreURL must be " + NONE);
            }
            if (token && privateKeyPasswordURL != null) {
                throw new LoginException
                        ("if keyStoreType is " + P11KEYSTORE +
                        " then privateKeyPasswordURL must not be specified");
            }
            if (protectedPath &&
                (keyStorePasswordURL != null ||
                        privateKeyPasswordURL != null)) {
                throw new LoginException
                        ("if protected is true then keyStorePasswordURL and " +
                        "privateKeyPasswordURL must not be specified");
            }
            if (protectedPath) {
                getAliasAndPasswords(PROTECTED_PATH);
            } else if (token) {
                getAliasAndPasswords(TOKEN);
            } else {
                getAliasAndPasswords(NORMAL);
            }
            try {
                getKeyStoreInfo();
            } finally {
                if (privateKeyPassword != null &&
                    privateKeyPassword != keyStorePassword) {
                    Arrays.fill(privateKeyPassword, '\0');
                    privateKeyPassword = null;
                }
                if (keyStorePassword != null) {
                    Arrays.fill(keyStorePassword, '\0');
                    keyStorePassword = null;
                }
            }
            status = AUTHENTICATED;
            return true;
        case LOGGED_IN:
            return true;
        }
    }
    private void getAliasAndPasswords(int env) throws LoginException {
        if (callbackHandler == null) {
            switch (env) {
            case PROTECTED_PATH:
                checkAlias();
                break;
            case TOKEN:
                checkAlias();
                checkStorePass();
                break;
            case NORMAL:
                checkAlias();
                checkStorePass();
                checkKeyPass();
                break;
            }
        } else {
            NameCallback aliasCallback;
            if (keyStoreAlias == null || keyStoreAlias.length() == 0) {
                aliasCallback = new NameCallback(
                                        rb.getString("Keystore.alias."));
            } else {
                aliasCallback =
                    new NameCallback(rb.getString("Keystore.alias."),
                                     keyStoreAlias);
            }
            PasswordCallback storePassCallback = null;
            PasswordCallback keyPassCallback = null;
            switch (env) {
            case PROTECTED_PATH:
                break;
            case NORMAL:
                keyPassCallback = new PasswordCallback
                    (rb.getString("Private.key.password.optional."), false);
            case TOKEN:
                storePassCallback = new PasswordCallback
                    (rb.getString("Keystore.password."), false);
                break;
            }
            prompt(aliasCallback, storePassCallback, keyPassCallback);
        }
        if (debug) {
            debugPrint("alias=" + keyStoreAlias);
        }
    }
    private void checkAlias() throws LoginException {
        if (keyStoreAlias == null) {
            throw new LoginException
                ("Need to specify an alias option to use " +
                "KeyStoreLoginModule non-interactively.");
        }
    }
    private void checkStorePass() throws LoginException {
        if (keyStorePasswordURL == null) {
            throw new LoginException
                ("Need to specify keyStorePasswordURL option to use " +
                "KeyStoreLoginModule non-interactively.");
        }
        InputStream in = null;
        try {
            in = new URL(keyStorePasswordURL).openStream();
            keyStorePassword = Password.readPassword(in);
        } catch (IOException e) {
            LoginException le = new LoginException
                ("Problem accessing keystore password \"" +
                keyStorePasswordURL + "\"");
            le.initCause(e);
            throw le;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    LoginException le = new LoginException(
                        "Problem closing the keystore password stream");
                    le.initCause(ioe);
                    throw le;
                }
            }
        }
    }
    private void checkKeyPass() throws LoginException {
        if (privateKeyPasswordURL == null) {
            privateKeyPassword = keyStorePassword;
        } else {
            InputStream in = null;
            try {
                in = new URL(privateKeyPasswordURL).openStream();
                privateKeyPassword = Password.readPassword(in);
            } catch (IOException e) {
                LoginException le = new LoginException
                        ("Problem accessing private key password \"" +
                        privateKeyPasswordURL + "\"");
                le.initCause(e);
                throw le;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        LoginException le = new LoginException(
                            "Problem closing the private key password stream");
                        le.initCause(ioe);
                        throw le;
                    }
                }
            }
        }
    }
    private void prompt(NameCallback aliasCallback,
                        PasswordCallback storePassCallback,
                        PasswordCallback keyPassCallback)
                throws LoginException {
        if (storePassCallback == null) {
            try {
                callbackHandler.handle(
                    new Callback[] {
                        bannerCallback, aliasCallback, confirmationCallback
                    });
            } catch (IOException e) {
                LoginException le = new LoginException
                        ("Problem retrieving keystore alias");
                le.initCause(e);
                throw le;
            } catch (UnsupportedCallbackException e) {
                throw new LoginException(
                    "Error: " + e.getCallback().toString() +
                    " is not available to retrieve authentication " +
                    " information from the user");
            }
            int confirmationResult = confirmationCallback.getSelectedIndex();
            if (confirmationResult == ConfirmationCallback.CANCEL) {
                throw new LoginException("Login cancelled");
            }
            saveAlias(aliasCallback);
        } else if (keyPassCallback == null) {
            try {
                callbackHandler.handle(
                    new Callback[] {
                        bannerCallback, aliasCallback,
                        storePassCallback, confirmationCallback
                    });
            } catch (IOException e) {
                LoginException le = new LoginException
                        ("Problem retrieving keystore alias and password");
                le.initCause(e);
                throw le;
            } catch (UnsupportedCallbackException e) {
                throw new LoginException(
                    "Error: " + e.getCallback().toString() +
                    " is not available to retrieve authentication " +
                    " information from the user");
            }
            int confirmationResult = confirmationCallback.getSelectedIndex();
            if (confirmationResult == ConfirmationCallback.CANCEL) {
                throw new LoginException("Login cancelled");
            }
            saveAlias(aliasCallback);
            saveStorePass(storePassCallback);
        } else {
            try {
                callbackHandler.handle(
                    new Callback[] {
                        bannerCallback, aliasCallback,
                        storePassCallback, keyPassCallback,
                        confirmationCallback
                    });
            } catch (IOException e) {
                LoginException le = new LoginException
                        ("Problem retrieving keystore alias and passwords");
                le.initCause(e);
                throw le;
            } catch (UnsupportedCallbackException e) {
                throw new LoginException(
                    "Error: " + e.getCallback().toString() +
                    " is not available to retrieve authentication " +
                    " information from the user");
            }
            int confirmationResult = confirmationCallback.getSelectedIndex();
            if (confirmationResult == ConfirmationCallback.CANCEL) {
                throw new LoginException("Login cancelled");
            }
            saveAlias(aliasCallback);
            saveStorePass(storePassCallback);
            saveKeyPass(keyPassCallback);
        }
    }
    private void saveAlias(NameCallback cb) {
        keyStoreAlias = cb.getName();
    }
    private void saveStorePass(PasswordCallback c) {
        keyStorePassword = c.getPassword();
        if (keyStorePassword == null) {
            keyStorePassword = new char[0];
        }
        c.clearPassword();
    }
    private void saveKeyPass(PasswordCallback c) {
        privateKeyPassword = c.getPassword();
        if (privateKeyPassword == null || privateKeyPassword.length == 0) {
            privateKeyPassword = keyStorePassword;
        }
        c.clearPassword();
    }
    private void getKeyStoreInfo() throws LoginException {
        try {
            if (keyStoreProvider == null) {
                keyStore = KeyStore.getInstance(keyStoreType);
            } else {
                keyStore =
                    KeyStore.getInstance(keyStoreType, keyStoreProvider);
            }
        } catch (KeyStoreException e) {
            LoginException le = new LoginException
                ("The specified keystore type was not available");
            le.initCause(e);
            throw le;
        } catch (NoSuchProviderException e) {
            LoginException le = new LoginException
                ("The specified keystore provider was not available");
            le.initCause(e);
            throw le;
        }
        InputStream in = null;
        try {
            if (nullStream) {
                keyStore.load(null, keyStorePassword);
            } else {
                in = new URL(keyStoreURL).openStream();
                keyStore.load(in, keyStorePassword);
            }
        } catch (MalformedURLException e) {
            LoginException le = new LoginException
                                ("Incorrect keyStoreURL option");
            le.initCause(e);
            throw le;
        } catch (GeneralSecurityException e) {
            LoginException le = new LoginException
                                ("Error initializing keystore");
            le.initCause(e);
            throw le;
        } catch (IOException e) {
            LoginException le = new LoginException
                                ("Error initializing keystore");
            le.initCause(e);
            throw le;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    LoginException le = new LoginException
                                ("Error initializing keystore");
                    le.initCause(ioe);
                    throw le;
                }
            }
        }
        try {
            fromKeyStore =
                keyStore.getCertificateChain(keyStoreAlias);
            if (fromKeyStore == null
                || fromKeyStore.length == 0
                || !(fromKeyStore[0] instanceof X509Certificate))
            {
                throw new FailedLoginException(
                    "Unable to find X.509 certificate chain in keystore");
            } else {
                LinkedList<Certificate> certList = new LinkedList<>();
                for (int i=0; i < fromKeyStore.length; i++) {
                    certList.add(fromKeyStore[i]);
                }
                CertificateFactory certF=
                    CertificateFactory.getInstance("X.509");
                certP =
                    certF.generateCertPath(certList);
            }
        } catch (KeyStoreException e) {
            LoginException le = new LoginException("Error using keystore");
            le.initCause(e);
            throw le;
        } catch (CertificateException ce) {
            LoginException le = new LoginException
                ("Error: X.509 Certificate type unavailable");
            le.initCause(ce);
            throw le;
        }
        try {
            X509Certificate certificate = (X509Certificate)fromKeyStore[0];
            principal = new javax.security.auth.x500.X500Principal
                (certificate.getSubjectDN().getName());
            Key privateKey = keyStore.getKey(keyStoreAlias, privateKeyPassword);
            if (privateKey == null
                || !(privateKey instanceof PrivateKey))
            {
                throw new FailedLoginException(
                    "Unable to recover key from keystore");
            }
            privateCredential = new X500PrivateCredential(
                certificate, (PrivateKey) privateKey, keyStoreAlias);
        } catch (KeyStoreException e) {
            LoginException le = new LoginException("Error using keystore");
            le.initCause(e);
            throw le;
        } catch (NoSuchAlgorithmException e) {
            LoginException le = new LoginException("Error using keystore");
            le.initCause(e);
            throw le;
        } catch (UnrecoverableKeyException e) {
            FailedLoginException fle = new FailedLoginException
                                ("Unable to recover key from keystore");
            fle.initCause(e);
            throw fle;
        }
        if (debug) {
            debugPrint("principal=" + principal +
                       "\n certificate="
                       + privateCredential.getCertificate() +
                       "\n alias =" + privateCredential.getAlias());
        }
    }
    public boolean commit() throws LoginException {
        switch (status) {
        case UNINITIALIZED:
        default:
            throw new LoginException("The login module is not initialized");
        case INITIALIZED:
            logoutInternal();
            throw new LoginException("Authentication failed");
        case AUTHENTICATED:
            if (commitInternal()) {
                return true;
            } else {
                logoutInternal();
                throw new LoginException("Unable to retrieve certificates");
            }
        case LOGGED_IN:
            return true;
        }
    }
    private boolean commitInternal() throws LoginException {
        if (subject.isReadOnly()) {
            throw new LoginException ("Subject is set readonly");
        } else {
            subject.getPrincipals().add(principal);
            subject.getPublicCredentials().add(certP);
            subject.getPrivateCredentials().add(privateCredential);
            status = LOGGED_IN;
            return true;
        }
    }
    public boolean abort() throws LoginException {
        switch (status) {
        case UNINITIALIZED:
        default:
            return false;
        case INITIALIZED:
            return false;
        case AUTHENTICATED:
            logoutInternal();
            return true;
        case LOGGED_IN:
            logoutInternal();
            return true;
        }
    }
    public boolean logout() throws LoginException {
        if (debug)
            debugPrint("Entering logout " + status);
        switch (status) {
        case UNINITIALIZED:
            throw new LoginException
                ("The login module is not initialized");
        case INITIALIZED:
        case AUTHENTICATED:
        default:
            return false;
        case LOGGED_IN:
            logoutInternal();
            return true;
        }
    }
    private void logoutInternal() throws LoginException {
        if (debug) {
            debugPrint("Entering logoutInternal");
        }
        LoginException logoutException = null;
        Provider provider = keyStore.getProvider();
        if (provider instanceof AuthProvider) {
            AuthProvider ap = (AuthProvider)provider;
            try {
                ap.logout();
                if (debug) {
                    debugPrint("logged out of KeyStore AuthProvider");
                }
            } catch (LoginException le) {
                logoutException = le;
            }
        }
        if (subject.isReadOnly()) {
            principal = null;
            certP = null;
            status = INITIALIZED;
            Iterator<Object> it = subject.getPrivateCredentials().iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (privateCredential.equals(obj)) {
                    privateCredential = null;
                    try {
                        ((Destroyable)obj).destroy();
                        if (debug)
                            debugPrint("Destroyed private credential, " +
                                       obj.getClass().getName());
                        break;
                    } catch (DestroyFailedException dfe) {
                        LoginException le = new LoginException
                            ("Unable to destroy private credential, "
                             + obj.getClass().getName());
                        le.initCause(dfe);
                        throw le;
                    }
                }
            }
            throw new LoginException
                ("Unable to remove Principal ("
                 + "X500Principal "
                 + ") and public credential (certificatepath) "
                 + "from read-only Subject");
        }
        if (principal != null) {
            subject.getPrincipals().remove(principal);
            principal = null;
        }
        if (certP != null) {
            subject.getPublicCredentials().remove(certP);
            certP = null;
        }
        if (privateCredential != null) {
            subject.getPrivateCredentials().remove(privateCredential);
            privateCredential = null;
        }
        if (logoutException != null) {
            throw logoutException;
        }
        status = INITIALIZED;
    }
    private void debugPrint(String message) {
        if (message == null) {
            System.err.println();
        } else {
            System.err.println("Debug KeyStoreLoginModule: " + message);
        }
    }
}
