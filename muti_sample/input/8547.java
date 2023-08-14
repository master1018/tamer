public class CombinedPerms {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        URL u = new URL("file:/home/duke");
        CodeSource cs =
            new CodeSource(u, (java.security.cert.Certificate[]) null);
        Permissions p = new Permissions();
        p.add(new SocketPermission(host, "connect"));
        ProtectionDomain pd = new ProtectionDomain(cs, p, null, null);
        if (pd.implies(new SocketPermission(host, "connect,accept"))) {
            System.out.println("Test Passed");
        } else {
            throw new SecurityException("Test Failed");
        }
    }
}
