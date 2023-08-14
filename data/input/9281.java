public class DefaultPKCS11 {
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name", "(null)");
        String osVersion = System.getProperty("os.version", "(null)");
        System.out.println("Running on " + osName + " " + osVersion);
        Provider[] ps = Security.getProviders();
        System.out.println("Providers: " + Arrays.asList(ps));
        System.out.println();
        if (osName.equals("SunOS") == false) {
            System.out.println("Test only applies to Solaris, skipping");
            return;
        }
        String[] v = osVersion.split("\\.");
        if (v.length < 2) {
            throw new Exception("Failed to parse Solaris version: " + Arrays.asList(v));
        }
        if (Integer.parseInt(v[0]) != 5) {
            throw new Exception("Unknown Solaris major version: " + v[0]);
        }
        if (Integer.parseInt(v[1]) < 10) {
            System.out.println("Test only applies to Solaris 10 and later, skipping");
            return;
        }
        if (ps[0].getName().equals("SunPKCS11-Solaris") == false) {
            throw new Exception("SunPKCS11-Solaris provider not installed");
        }
        System.out.println("OK");
    }
}
