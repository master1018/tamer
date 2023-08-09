public class ConfigConstructorNoPerm {
    private static Subject s = new Subject();
    private static CallbackHandler ch =
                new com.sun.security.auth.callback.TextCallbackHandler();
    private static Configuration c = new MyConfig();
    public static void main(String[] args) throws Exception {
        try {
            LoginContext lc1 = new LoginContext
                        ("module1",
                        s,
                        ch);
            throw new RuntimeException("Test 1 Failed");
        } catch (SecurityException se) {
        }
        System.out.println("Test 1 Succeeded");
        try {
            LoginContext lc2 = new LoginContext
                        ("module1",
                        s,
                        ch,
                        null);
            throw new RuntimeException("Test 2 Failed");
        } catch (SecurityException se) {
        }
        System.out.println("Test 2 Succeeded");
        LoginContext lc3 = new LoginContext
                        ("module1",
                        s,
                        ch,
                        c);
        System.out.println("Test 3 Succeeded");
        try {
            LoginContext lc4 = new LoginContext
                        ("goToOther",
                        s,
                        ch);
            throw new RuntimeException("Test 4 Failed");
        } catch (SecurityException se) {
        }
        System.out.println("Test 4 Succeeded");
        try {
            LoginContext lc5 = new LoginContext
                        ("goToOther",
                        s,
                        ch,
                        null);
            throw new RuntimeException("Test 5 Failed");
        } catch (SecurityException se) {
        }
        System.out.println("Test 5 Succeeded");
    }
    private static class MyConfig extends Configuration {
        public MyConfig() { }
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            java.util.HashMap map = new java.util.HashMap();
            AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
            if (name.equals("module1")) {
                AppConfigurationEntry entry = new AppConfigurationEntry
                        ("ConfigConstructor$MyModule1",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        map);
                entries[0] = entry;
            } else {
                entries = null;
            }
            return entries;
        }
        public void refresh() { }
    }
}
