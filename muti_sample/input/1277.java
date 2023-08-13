public class CaseInsensitiveAlgNames {
    public static void main(String[] args)
        throws NoSuchAlgorithmException, NoSuchProviderException {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md = MessageDigest.getInstance("sha");
            md = MessageDigest.getInstance("Sha-1");
            md = MessageDigest.getInstance("shA1");
            md = MessageDigest.getInstance("SHA", "SUN");
            md = MessageDigest.getInstance("sha", "SUN");
            md = MessageDigest.getInstance("Sha-1", "SUN");
            md = MessageDigest.getInstance("shA1", "SUN");
            KeyPairGenerator kGen = KeyPairGenerator.getInstance("DSA");
            kGen = KeyPairGenerator.getInstance("dsa");
            kGen = KeyPairGenerator.getInstance("dSA");
            kGen = KeyPairGenerator.getInstance("OId.1.2.840.10040.4.1");
            kGen = KeyPairGenerator.getInstance("1.2.840.10040.4.1");
            kGen = KeyPairGenerator.getInstance("DSA", "SUN");
            kGen = KeyPairGenerator.getInstance("dsa", "SUN");
            kGen = KeyPairGenerator.getInstance("dSA", "SUN");
            kGen = KeyPairGenerator.getInstance("OId.1.2.840.10040.4.1",
                                                "SUN");
            kGen = KeyPairGenerator.getInstance("1.2.840.10040.4.1", "SUN");
    }
}
