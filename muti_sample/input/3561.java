public class ConfPlusProp {
    Config config;
    public static void main(String[] args) throws Exception {
        new ConfPlusProp().run();
    }
    void refresh() throws Exception {
        Config.refresh();
        config = Config.getInstance();
    }
    void checkDefaultRealm(String r) throws Exception {
        try {
            if (!config.getDefaultRealm().equals(r)) {
                throw new AssertionError("Default realm error");
            }
        } catch (Exception e) {
            if (r != null) throw e;
        }
    }
    void check(String r, String k) throws Exception {
        try {
            if (!config.getKDCList(r).equals(k)) {
                throw new AssertionError(r + " kdc not " + k);
            }
        } catch (Exception e) {
            if (k != null) throw e;
        }
    }
    void run() throws Exception {
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/confplusprop.conf");
        refresh();
        checkDefaultRealm("R1");
        check("R1", "k1");
        check("R2", "old");
        check("R3", null);
        if (!config.getDefault("forwardable", "libdefaults").equals("well")) {
            throw new Exception("Extra config error");
        }
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/confplusprop2.conf");
        refresh();
        checkDefaultRealm(null);
        check("R1", "k12");
        check("R2", "old");
        check("R3", null);
        int version = System.getProperty("java.version").charAt(2) - '0';
        System.out.println("JDK version is " + version);
        if (version >= 7) {
            System.setProperty("java.security.krb5.conf", "i-am-not-a file");
            refresh();
            check("R1", null);
            check("R2", null);
            check("R3", null);
            if (config.getDefault("forwardable", "libdefaults") != null) {
                throw new Exception("Extra config error");
            }
        }
        System.setProperty("java.security.krb5.realm", "R2");
        System.setProperty("java.security.krb5.kdc", "k2");
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/confplusprop.conf");
        refresh();
        checkDefaultRealm("R2");
        check("R1", "k1");
        check("R2", "k2");
        check("R3", "k2");
        if (!config.getDefault("forwardable", "libdefaults").equals("well")) {
            throw new Exception("Extra config error");
        }
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/confplusprop2.conf");
        refresh();
        checkDefaultRealm("R2");
        check("R1", "k12");
        check("R2", "k2");
        check("R3", "k2");
        System.setProperty("java.security.krb5.conf", "i-am-not-a file");
        refresh();
        checkDefaultRealm("R2");
        check("R1", "k2");
        check("R2", "k2");
        check("R3", "k2");
        if (config.getDefault("forwardable", "libdefaults") != null) {
            throw new Exception("Extra config error");
        }
    }
}
