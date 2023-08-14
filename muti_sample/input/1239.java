public class TestPremaster {
    public static void main(String[] args) throws Exception {
        Provider provider = Security.getProvider("SunJCE");
        KeyGenerator kg;
        kg = KeyGenerator.getInstance("SunTlsRsaPremasterSecret", provider);
        try {
            kg.generateKey();
            throw new Exception("no exception");
        } catch (IllegalStateException e) {
            System.out.println("OK: " + e);
        }
        test(kg, 3, 0);
        test(kg, 3, 1);
        test(kg, 3, 2);
        test(kg, 4, 0);
        System.out.println("Done.");
    }
    private static void test(KeyGenerator kg, int major, int minor)
            throws Exception {
        kg.init(new TlsRsaPremasterSecretParameterSpec(major, minor));
        SecretKey key = kg.generateKey();
        byte[] encoded = key.getEncoded();
        if (encoded.length != 48) {
            throw new Exception("length: " + encoded.length);
        }
        if ((encoded[0] != major) || (encoded[1] != minor)) {
            throw new Exception("version mismatch: "  + encoded[0] +
                "." + encoded[1]);
        }
        System.out.println("OK: " + major + "." + minor);
    }
}
