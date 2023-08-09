public class ConfigWithQuotations {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +"/edu.mit.Kerberos");
        Config config = Config.getInstance();
        System.out.println(config);
        if (!config.getDefaultRealm().equals("MAC.LOCAL")) {
            throw new Exception("Realm error");
        }
        if (!config.getKDCList("MAC.LOCAL").equals("kdc.mac.local:88")) {
            throw new Exception("KDC error");
        }
    }
}
