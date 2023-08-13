public class InnerClassConfig {
    public static void main(String[] args) {
        Configuration config = null;
        try {
            config = Configuration.getConfiguration();
        } catch (SecurityException se) {
            System.out.println("test 1 failed");
            throw se;
        }
        AppConfigurationEntry[] entries =
                config.getAppConfigurationEntry("InnerClassConfig");
        System.out.println("module = " +
                        entries[0].getLoginModuleName());
        if (entries[0].getLoginModuleName().equals("package.Foo$Bar")) {
            System.out.println("test succeeded");
        } else {
            System.out.println("test 2 failed");
            throw new SecurityException("package name incorrect");
        }
    }
}
