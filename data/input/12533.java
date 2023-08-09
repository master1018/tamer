public class NullPerms {
    public static void main(String[]args) throws Exception {
        try {
            CodeSource cs =
                new CodeSource(null, (java.security.cert.Certificate[]) null);
            ProtectionDomain pd = new ProtectionDomain(cs, null);
            if (pd.implies(new SecurityPermission("foo"))) {
                throw new Exception("implies should return false");
            }
        } catch (NullPointerException npe) {
            throw new Exception("should not have caught an exception");
        }
    }
}
