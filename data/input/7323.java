public class AddPrivateKey extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        String configName = BASE + SEP + "nss.cfg";
        Provider p = getSunPKCS11(configName);
        boolean supportsEC = (p.getService("KeyFactory", "EC") != null);
        System.out.println(p);
        System.out.println();
        Security.addProvider(p);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, password);
        for (String alias : aliases(ks)) {
            System.out.println("Deleting: " + alias);
            ks.deleteEntry(alias);
        }
        KeyStore jks = KeyStore.getInstance("JKS");
        InputStream in = new FileInputStream(new File(BASE, "keystore.jks"));
        char[] jkspass = "passphrase".toCharArray();
        jks.load(in, jkspass);
        List<PrivateKeyEntry> entries = new ArrayList<PrivateKeyEntry>();
        for (String alias : Collections.list(jks.aliases())) {
            if (jks.entryInstanceOf(alias, PrivateKeyEntry.class)) {
                PrivateKeyEntry entry = (PrivateKeyEntry)jks.getEntry(alias, new PasswordProtection(jkspass));
                String algorithm = entry.getPrivateKey().getAlgorithm();
                System.out.println("-Entry " + alias + " (" + algorithm + ")");
                if ((supportsEC == false) && algorithm.equals("EC")) {
                    System.out.println("EC not supported by provider, skipping");
                    continue;
                }
                if ((supportsEC == false) && algorithm.equals("DSA")) {
                    System.out.println("Provider does not appear to have CKA_NETSCAPE_DB fix, skipping");
                    continue;
                }
                test(p, entry);
            } 
        }
        System.out.println("OK");
    }
    private static List<String> aliases(KeyStore ks) throws KeyStoreException {
        return Collections.list(ks.aliases());
    }
    private final static String ALIAS1 = "entry1";
    private final static String ALIAS2 = "entry2";
    private final static String ALIAS3 = "entry3";
    private static void test(Provider p, PrivateKeyEntry entry) throws Exception {
        PrivateKey key = entry.getPrivateKey();
        X509Certificate[] chain = (X509Certificate[])entry.getCertificateChain();
        PublicKey publicKey = chain[0].getPublicKey();
        System.out.println(toString(key));
        sign(p, key, publicKey);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, null);
        if (ks.size() != 0) {
            throw new Exception("KeyStore not empty");
        }
        List<String> aliases;
        ks.setKeyEntry(ALIAS1, key, null, chain);
        aliases = aliases(ks);
        if (aliases.size() != 1) {
            throw new Exception("size not 1: " + aliases);
        }
        if (aliases.get(0).equals(ALIAS1) == false) {
            throw new Exception("alias mismatch: " + aliases);
        }
        PrivateKey key2 = (PrivateKey)ks.getKey(ALIAS1, null);
        System.out.println(toString(key2));
        X509Certificate[] chain2 = (X509Certificate[])ks.getCertificateChain(ALIAS1);
        if (Arrays.equals(chain, chain2) == false) {
            throw new Exception("chain mismatch");
        }
        sign(p, key2, publicKey);
        ks.deleteEntry(ALIAS1);
        if (ks.size() != 0) {
            throw new Exception("KeyStore not empty");
        }
        KeyFactory kf = KeyFactory.getInstance(key.getAlgorithm(), p);
        PrivateKey key3 = (PrivateKey)kf.translateKey(key);
        System.out.println(toString(key3));
        sign(p, key3, publicKey);
        ks.setKeyEntry(ALIAS2, key3, null, chain);
        aliases = aliases(ks);
        if (aliases.size() != 1) {
            throw new Exception("size not 1");
        }
        if (aliases.get(0).equals(ALIAS2) == false) {
            throw new Exception("alias mismatch: " + aliases);
        }
        PrivateKey key4 = (PrivateKey)ks.getKey(ALIAS2, null);
        System.out.println(toString(key4));
        X509Certificate[] chain4 = (X509Certificate[])ks.getCertificateChain(ALIAS2);
        if (Arrays.equals(chain, chain4) == false) {
            throw new Exception("chain mismatch");
        }
        sign(p, key4, publicKey);
        ks.setKeyEntry(ALIAS3, key3, null, chain);
        aliases = aliases(ks);
        if (aliases.size() != 1) {
            throw new Exception("size not 1");
        }
        if (aliases.get(0).equals(ALIAS3) == false) {
            throw new Exception("alias mismatch: " + aliases);
        }
        PrivateKey key5 = (PrivateKey)ks.getKey(ALIAS3, null);
        System.out.println(toString(key5));
        X509Certificate[] chain5 = (X509Certificate[])ks.getCertificateChain(ALIAS3);
        if (Arrays.equals(chain, chain5) == false) {
            throw new Exception("chain mismatch");
        }
        sign(p, key5, publicKey);
        ks.deleteEntry(ALIAS3);
        if (ks.size() != 0) {
            throw new Exception("KeyStore not empty");
        }
        System.out.println("OK");
    }
    private final static byte[] DATA = new byte[4096];
    static {
        Random random = new Random();
        random.nextBytes(DATA);
    }
    private static void sign(Provider p, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        String keyAlg = privateKey.getAlgorithm();
        String alg;
        if (keyAlg.equals("RSA")) {
            alg = "SHA1withRSA";
        } else if (keyAlg.equals("DSA")) {
            alg = "SHA1withDSA";
        } else if (keyAlg.equals("EC")) {
            alg = "SHA1withECDSA";
        } else {
            throw new Exception("Unknown algorithm " + keyAlg);
        }
        Signature s = Signature.getInstance(alg, p);
        s.initSign(privateKey);
        s.update(DATA);
        byte[] sig = s.sign();
        s.initVerify(publicKey);
        s.update(DATA);
        if (s.verify(sig) == false) {
            throw new Exception("Signature did not verify");
        }
    }
    private final static int MAX_LINE = 85;
    private static String toString(Object o) {
        String s = String.valueOf(o).split("\n")[0];
        return (s.length() <= MAX_LINE) ? s : s.substring(0, MAX_LINE);
    }
}
