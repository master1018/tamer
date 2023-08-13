public final class PropertiesFileCallbackHandler implements CallbackHandler {
    private Properties pwDb, namesDb, proxyDb;
    public PropertiesFileCallbackHandler(String pwFile, String namesFile,
        String proxyFile) throws IOException {
        String dir = System.getProperty("test.src");
        if (dir == null) {
            dir = ".";
        }
        dir = dir + "/";
        if (pwFile != null) {
            pwDb = new Properties();
            pwDb.load(new FileInputStream(dir+pwFile));
        }
        if (namesFile != null) {
            namesDb = new Properties();
            namesDb.load(new FileInputStream(dir+namesFile));
        }
        if (proxyFile != null) {
            proxyDb = new Properties();
            proxyDb.load(new FileInputStream(dir+proxyFile));
        }
    }
    public void handle(Callback[] callbacks)
        throws UnsupportedCallbackException {
        NameCallback ncb = null;
        PasswordCallback pcb = null;
        AuthorizeCallback acb = null;
        RealmCallback rcb = null;
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ncb = (NameCallback) callbacks[i];
            } else if (callbacks[i] instanceof PasswordCallback) {
                pcb = (PasswordCallback) callbacks[i];
            } else if (callbacks[i] instanceof AuthorizeCallback) {
                acb = (AuthorizeCallback) callbacks[i];
            } else if (callbacks[i] instanceof RealmCallback) {
                rcb = (RealmCallback) callbacks[i];
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
        if (pcb != null && ncb != null) {
            String username = ncb.getDefaultName();
            String pw = pwDb.getProperty(username);
            if (pw != null) {
                char[] pwchars = pw.toCharArray();
                pcb.setPassword(pwchars);
                for (int i = 0; i <pwchars.length; i++) {
                    pwchars[i] = 0;
                }
                String canonAuthid =
                    (namesDb != null? namesDb.getProperty(username) : null);
                if (canonAuthid != null) {
                    ncb.setName(canonAuthid);
                }
            }
        }
        if (acb != null) {
            String authid = acb.getAuthenticationID();
            String authzid = acb.getAuthorizationID();
            if (authid.equals(authzid)) {
                acb.setAuthorized(true);
            } else {
                String authzes = (proxyDb != null ? proxyDb.getProperty(authid)
                    : null);
                if (authzes != null && authzes.indexOf(authzid) >= 0) {
                    acb.setAuthorized(true);
                }
            }
            if (acb.isAuthorized()) {
                String canonAuthzid = (namesDb != null ?
                    namesDb.getProperty(authzid) : null);
                if (canonAuthzid != null) {
                    acb.setAuthorizedID(canonAuthzid);
                }
            }
        }
    }
}
