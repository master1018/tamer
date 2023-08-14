public class PropertyExpansion {
    public static void main(String[] args) {
        try {
            ConfigFile config = new ConfigFile();
            throw new IllegalStateException("test 1 failed");
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        Configuration config = null;
        try {
            config = Configuration.getConfiguration();
        } catch (SecurityException se) {
            System.out.println("test 2 failed");
            throw se;
        }
        AppConfigurationEntry[] entries =
                config.getAppConfigurationEntry("PropertyExpansion");
        if (entries.length != 2)
                throw new IllegalStateException("test 2 failed");
        for (int i = 0; i < 2; i++) {
                System.out.println("module " + i + " = " +
                        entries[i].getLoginModuleName());
                System.out.println("control flag " + i + " = " +
                        entries[i].getControlFlag());
                java.util.Map map = entries[i].getOptions();
                System.out.println("option " + i + " = useFile, " +
                        "value = " + map.get("useFile"));
                System.out.println("option " + i + " = debug, " +
                        "value = " + map.get("debug"));
                if (i == 0 && map.get("useFile") == null ||
                    i == 0 && map.get("debug") != null) {
                    throw new IllegalStateException("test 3 failed");
                }
                if (i == 1 && map.get("useFile") != null ||
                    i == 1 && map.get("debug") == null) {
                    throw new IllegalStateException("test 4 failed");
                }
        }
        System.out.println("test succeeded");
    }
}
