public class Login extends PKCS11Test {
    private static final String KS_TYPE = "PKCS11";
    private static char[] password;
    public static void main(String[] args) throws Exception {
        main(new Login());
    }
    public void main(Provider p) throws Exception {
        int testnum = 1;
        KeyStore ks = KeyStore.getInstance(KS_TYPE, p);
        if (ks.getProvider() instanceof java.security.AuthProvider) {
            System.out.println("keystore provider instance of AuthProvider");
            System.out.println("test " + testnum++ + " passed");
        } else {
            throw new SecurityException("did not get AuthProvider KeyStore");
        }
        AuthProvider ap = (AuthProvider)ks.getProvider();
        try {
            System.out.println("*** enter [foo] as the password ***");
            password = new char[] { 'f', 'o', 'o' };
            ap.login(new Subject(), new PasswordCallbackHandler());
            ap.logout();
            throw new SecurityException("test failed, expected LoginException");
        } catch (FailedLoginException fle) {
            System.out.println("test " + testnum++ + " passed");
        }
        try {
            System.out.println("*** enter [foo] as the password ***");
            password = new char[] { 'f', 'o', 'o' };
            Security.setProperty("auth.login.defaultCallbackHandler",
                "Login$PasswordCallbackHandler");
            ap.login(new Subject(), null);
            ap.logout();
            throw new SecurityException("test failed, expected LoginException");
        } catch (FailedLoginException fle) {
            System.out.println("test " + testnum++ + " passed");
        }
        System.out.println("*** enter test12 (correct) password ***");
        password = new char[] { 't', 'e', 's', 't', '1', '2' };
        Security.setProperty("auth.login.defaultCallbackHandler", "");
        ap.setCallbackHandler
                (new com.sun.security.auth.callback.DialogCallbackHandler());
        ap.setCallbackHandler(new PasswordCallbackHandler());
        ap.login(new Subject(), null);
        System.out.println("test " + testnum++ + " passed");
        ap.setCallbackHandler(null);
        ap.login(new Subject(), null);
        System.out.println("test " + testnum++ + " passed");
        ap.logout();
        ap.setCallbackHandler(new PasswordCallbackHandler());
        ks.load(null, (char[])null);
        System.out.println("test " + testnum++ + " passed");
    }
    public static class PasswordCallbackHandler implements CallbackHandler {
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            if (!(callbacks[0] instanceof PasswordCallback)) {
                throw new UnsupportedCallbackException(callbacks[0]);
            }
            PasswordCallback pc = (PasswordCallback)callbacks[0];
            pc.setPassword(Login.password);
        }
    }
}
