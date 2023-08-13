public class CheckConfigs {
    public static void main(String[] args) throws Exception {
        SecurityManager securityManager = System.getSecurityManager();
        System.out.println(securityManager == null
            ? "[security manager is not running]"
            : "[security manager is running: " +
                securityManager.getClass().getName() + "]");
        init();
        checkConfigModes();
    }
    private static void init() throws Exception {
    }
    private static void checkConfigModes() throws Exception {
        LoginContext ldapLogin;
        System.out.println("Testing search-first mode...");
        try {
            ldapLogin = new LoginContext(LdapConfiguration.LOGIN_CONFIG_NAME,
                null, new TestCallbackHandler(), new SearchFirstMode());
            ldapLogin.login();
            throw new SecurityException("expected a LoginException");
        } catch (LoginException le) {
            if (!(le.getCause() instanceof CommunicationException)) {
                throw le;
            }
        }
        System.out.println("\nTesting authentication-first mode...");
        try {
            ldapLogin = new LoginContext(LdapConfiguration.LOGIN_CONFIG_NAME,
                null, new TestCallbackHandler(), new AuthFirstMode());
            ldapLogin.login();
            throw new SecurityException("expected a LoginException");
        } catch (LoginException le) {
            if (!(le.getCause() instanceof CommunicationException)) {
                throw le;
            }
        }
        System.out.println("\nTesting authentication-only mode...");
        try {
            ldapLogin = new LoginContext(LdapConfiguration.LOGIN_CONFIG_NAME,
                null, new TestCallbackHandler(), new AuthOnlyMode());
            ldapLogin.login();
            throw new SecurityException("expected a LoginException");
        } catch (LoginException le) {
            if (!(le.getCause() instanceof CommunicationException)) {
                throw le;
            }
        }
    }
    private static class TestCallbackHandler implements CallbackHandler {
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback)callbacks[i]).setName("myname");
                } else if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback)callbacks[i])
                        .setPassword("mypassword".toCharArray());
                } else {
                    throw new UnsupportedCallbackException
                        (callbacks[i], "Unrecognized callback");
                }
            }
        }
    }
}
class LdapConfiguration extends Configuration {
    public static final String LOGIN_CONFIG_NAME = "TestAuth";
    protected static AppConfigurationEntry[] entries;
    protected static final String LDAP_LOGIN_MODULE =
        LdapLoginModule.class.getName();
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return name.equals(LOGIN_CONFIG_NAME) ? entries : null;
    }
    public void refresh() {
    }
}
class SearchFirstMode extends LdapConfiguration {
    public SearchFirstMode() {
        super();
        Map<String, String> options = new HashMap<>(4);
        options.put("userProvider", "ldap:
        options.put("userFilter",
            "(&(uid={USERNAME})(objectClass=inetOrgPerson))");
        options.put("authzIdentity", "{EMPLOYEENUMBER}");
        options.put("debug", "true");
        entries = new AppConfigurationEntry[] {
            new AppConfigurationEntry(LDAP_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options)
        };
    }
}
class AuthFirstMode extends LdapConfiguration {
    public AuthFirstMode() {
        super();
        Map<String, String> options = new HashMap<>(5);
        options.put("userProvider", "ldap:
        options.put("authIdentity", "{USERNAME}");
        options.put("userFilter",
            "(&(|(samAccountName={USERNAME})(userPrincipalName={USERNAME})" +
            "(cn={USERNAME}))(objectClass=user))");
        options.put("useSSL", "false");
        options.put("debug", "true");
        entries = new AppConfigurationEntry[] {
            new AppConfigurationEntry(LDAP_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options)
        };
    }
}
class AuthOnlyMode extends LdapConfiguration {
    public AuthOnlyMode() {
        super();
        Map<String, String> options = new HashMap<>(4);
        options.put("userProvider",
            "ldap:
        options.put("authIdentity",
            "cn={USERNAME},ou=people,dc=example,dc=com");
        options.put("authzIdentity", "staff");
        options.put("debug", "true");
        entries = new AppConfigurationEntry[] {
            new AppConfigurationEntry(LDAP_LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options)
        };
    }
}
