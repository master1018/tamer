public class CleanState {
    public static void main(String[] args) throws Exception {
        CleanState x = new CleanState();
        new OneKDC(null);
        x.go();
    }
    void go() throws Exception {
        Krb5LoginModule krb5 = new Krb5LoginModule();
        final String name = OneKDC.USER;
        final char[] password = OneKDC.PASS;
        char[] badpassword = "hellokitty".toCharArray();
        Map<String,String> map = new HashMap<>();
        map.put("useTicketCache", "false");
        map.put("doNotPrompt", "false");
        map.put("tryFirstPass", "true");
        Map<String,Object> shared = new HashMap<>();
        shared.put("javax.security.auth.login.name", name);
        shared.put("javax.security.auth.login.password", badpassword);
        krb5.initialize(new Subject(), new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) {
                for(Callback callback: callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback)callback).setName(name);
                    }
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback)callback).setPassword(password);
                    }
                }
            }
        }, shared, map);
        krb5.login();
    }
}
