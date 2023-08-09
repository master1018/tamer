public class LoginModuleOptions {
    private static final String NAME = "javax.security.auth.login.name";
    private static final String PWD = "javax.security.auth.login.password";
    public static void main(String[] args) throws Exception {
        OneKDC kdc = new OneKDC(null);
        kdc.addPrincipal("foo", "bar".toCharArray());
        kdc.writeKtab(OneKDC.KTAB); 
        login(null, "useKeyTab", "true", "principal", "dummy");
        login(null, "tryFirstPass", "true", NAME, OneKDC.USER,
                PWD, OneKDC.PASS);
        System.setProperty("test.kdc.save.ccache", "krbcc");
        login(new MyCallback(OneKDC.USER, OneKDC.PASS));    
        System.clearProperty("test.kdc.save.ccache");
        login(null, "useTicketCache", "true", "ticketCache", "krbcc");
        login(null, "useTicketCache", "true", "ticketCache", "krbcc_non_exists",
                "useKeyTab", "true", "principal", "dummy");
        login(null, "useKeyTab", "true", "principal", "dummy",
                "keyTab", "ktab_non_exist",
                "tryFirstPass", "true", NAME, OneKDC.USER, PWD, OneKDC.PASS);
        boolean failed = false;
        try {
            login(new MyCallback(OneKDC.USER, OneKDC.PASS),
                    "useFirstPass", "true",
                    NAME, OneKDC.USER, PWD, "haha".toCharArray());
        } catch (Exception e) {
            failed = true;
        }
        if (!failed) {
            throw new Exception("useFirstPass should not fallback to callback");
        }
        login(new MyCallback(OneKDC.USER, OneKDC.PASS),
                "tryFirstPass", "true",
                NAME, OneKDC.USER, PWD, "haha".toCharArray());
        login(new MyCallback("foo", null),
                "useTicketCache", "true", "ticketCache", "krbcc",
                "useKeyTab", "true");
        login(null, "useKeyTab", "true", "principal", "dummy",
                "tryFirstPass", "true", NAME, "foo", PWD, "bar".toCharArray());
        login(new MyCallback("foo", "bar".toCharArray()),
                "tryFirstPass", "true", NAME, OneKDC.USER, PWD, OneKDC.PASS);
        login(null, "principal", OneKDC.USER,
                "tryFirstPass", "true", NAME, "someone_else", PWD, OneKDC.PASS);
        login(null, "principal", OneKDC.USER,
                "tryFirstPass", "true", PWD, OneKDC.PASS);
        login(new MyCallback("someone_else", OneKDC.PASS),
                "principal", OneKDC.USER);
        login(new MyCallback("someone_else", null),
                "tryFirstPass", "true", NAME, OneKDC.USER, PWD, OneKDC.PASS);
        failed = false;
        try {
            login(new MyCallback(OneKDC.USER, null),
                    "tryFirstPass", "true", PWD, OneKDC.PASS);
        } catch (Exception e) {
            failed = true;
        }
        if (!failed) {
            throw new Exception("useFirstPass must provide a NAME");
        }
        login(new MyCallback(OneKDC.USER, null),
                "tryFirstPass", "true", NAME, "", PWD, OneKDC.PASS);
        login(null, "doNotPrompt", "true", "storeKey", "true",
                "tryFirstPass", "true", NAME, OneKDC.USER, PWD, OneKDC.PASS);
    }
    static void login(CallbackHandler callback, Object... options)
            throws Exception {
        Krb5LoginModule krb5 = new Krb5LoginModule();
        Subject subject = new Subject();
        Map<String, String> map = new HashMap<>();
        Map<String, Object> shared = new HashMap<>();
        int count = options.length / 2;
        for (int i = 0; i < count; i++) {
            String key = (String) options[2 * i];
            Object value = options[2 * i + 1];
            if (key.startsWith("javax")) {
                shared.put(key, value);
            } else {
                map.put(key, (String) value);
            }
        }
        krb5.initialize(subject, callback, shared, map);
        krb5.login();
        krb5.commit();
        if (!subject.getPrincipals().iterator().next()
                .getName().startsWith(OneKDC.USER)) {
            throw new Exception("The authenticated is not " + OneKDC.USER);
        }
    }
    static class MyCallback implements CallbackHandler {
        private String name;
        private char[] password;
        public MyCallback(String name, char[] password) {
            this.name = name;
            this.password = password;
        }
        public void handle(Callback[] callbacks) {
            for (Callback callback : callbacks) {
                System.err.println(callback);
                if (callback instanceof NameCallback) {
                    System.err.println("name is " + name);
                    ((NameCallback) callback).setName(name);
                }
                if (callback instanceof PasswordCallback) {
                    System.err.println("pass is " + new String(password));
                    ((PasswordCallback) callback).setPassword(password);
                }
            }
        }
    }
}
