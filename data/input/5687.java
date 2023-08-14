public class DefaultHandler {
    public static void main(String[] args) {
        LoginContext lc = null;
        try {
            lc = new LoginContext("SampleLogin");
        } catch (LoginException le) {
            System.out.println
                ("DefaultHandler test failed - login construction failed");
            throw new SecurityException(le.getMessage());
        }
        try {
            lc.login();
            throw new SecurityException
                ("DefaultHandler test failed: got a handler!");
        } catch (LoginException le) {
            System.out.println
                ("Good: CallbackHandler implementation not found");
            le.printStackTrace();
        }
        java.security.Security.setProperty("auth.login.defaultCallbackHandler",
                "DefaultHandlerImpl");
        LoginContext lc2 = null;
        try {
            lc2 = new LoginContext("SampleLogin");
        } catch (LoginException le) {
            System.out.println
                ("DefaultHandler test failed - constructing LoginContext");
            throw new SecurityException(le.getMessage());
        }
        try {
            lc2.login();
        } catch (LoginException le) {
            System.out.println
                ("DefaultHandler test failed - login method");
            throw new SecurityException(le.getMessage());
        }
        System.out.println("DefaultHandler test succeeded");
    }
}
