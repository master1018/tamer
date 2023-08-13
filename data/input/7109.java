public class ExtensibleAlgorithmId {
    public static void main(String[] args) throws Exception {
        TestProvider p = new TestProvider();
        Security.addProvider(p);
        AlgorithmId algid = AlgorithmId.getAlgorithmId("XYZ");
        String alias = "Alg.Alias.Signature.OID." + algid.toString();
        String stdAlgName = p.getProperty(alias);
        if (stdAlgName == null || !stdAlgName.equalsIgnoreCase("XYZ")) {
            throw new Exception("Wrong OID");
        }
    }
}
class TestProvider extends Provider {
    public TestProvider() {
        super("Dummy", 1.0, "XYZ algorithm");
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                put("Signature.XYZ", "test.xyz");
                put("Alg.Alias.Signature.OID.1.2.3.4.5.6.7.8.9.0",
                    "XYZ");
                put("Alg.Alias.Signature.9.8.7.6.5.4.3.2.1.0",
                    "XYZ");
                return null;
            }
        });
    }
}
