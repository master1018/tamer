public class ResetConfigModule {
    public static void main(String[] args) throws Exception {
        Configuration.setConfiguration(new MyConfig());
        LoginContext lc = new LoginContext("test");
        try {
            lc.login();
            throw new SecurityException("test 1 failed");
        } catch (LoginException le) {
            if (le.getCause() != null &&
                le.getCause() instanceof SecurityException) {
                System.out.println("good so far");
            } else {
                throw le;
            }
        }
        LoginContext lc2 = new LoginContext("test2");
        try {
            lc2.login();
            throw new SecurityException("test 2 failed");
        } catch (LoginException le) {
            if (le.getCause() != null &&
                le.getCause()  instanceof SecurityException) {
                System.out.println("test succeeded");
            } else {
                throw le;
            }
        }
    }
}
class MyConfig extends Configuration {
    private AppConfigurationEntry[] entries = {
        new AppConfigurationEntry("ResetModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                new HashMap()) };
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return entries;
    }
    public void refresh() { }
}
