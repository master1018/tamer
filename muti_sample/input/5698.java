public class PKCS12SameKeyId {
    private static final String JKSFILE = "PKCS12SameKeyId.jks";
    private static final String P12FILE = "PKCS12SameKeyId.p12";
    private static final char[] PASSWORD = "changeit".toCharArray();
    private static final int SIZE = 10;
    public static void main(String[] args) throws Exception {
        new File(JKSFILE).delete();
        for (int i=0; i<SIZE; i++) {
            System.err.print(".");
            String cmd = "-keystore " + JKSFILE
                    + " -storepass changeit -keypass changeit "
                    + "-genkeypair -alias p" + i + " -dname CN=" + i;
            KeyTool.main(cmd.split(" "));
        }
        AlgorithmParameters algParams =
                AlgorithmParameters.getInstance("PBEWithSHA1AndDESede");
        algParams.init(new PBEParameterSpec("12345678".getBytes(), 1024));
        AlgorithmId algid = new AlgorithmId(
                new ObjectIdentifier("1.2.840.113549.1.12.1.3"), algParams);
        PBEKeySpec keySpec = new PBEKeySpec(PASSWORD);
        SecretKeyFactory skFac = SecretKeyFactory.getInstance("PBE");
        SecretKey skey = skFac.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
        cipher.init(Cipher.ENCRYPT_MODE, skey, algParams);
        byte[][] keys = new byte[SIZE][];
        Certificate[][] certChains = new Certificate[SIZE][];
        String[] aliases = new String[SIZE];
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(new FileInputStream(JKSFILE), PASSWORD);
        for (int i=0; i<SIZE; i++) {
            aliases[i] = "p" + i;
            byte[] enckey = cipher.doFinal(
                    ks.getKey(aliases[i], PASSWORD).getEncoded());
            keys[i] = new EncryptedPrivateKeyInfo(algid, enckey).getEncoded();
            certChains[i] = ks.getCertificateChain(aliases[i]);
        }
        KeyStore p12 = KeyStore.getInstance("pkcs12");
        p12.load(null, PASSWORD);
        for (int i=0; i<SIZE; i++) {
            p12.setKeyEntry(aliases[i], keys[i], certChains[i]);
        }
        p12.store(new FileOutputStream(P12FILE), PASSWORD);
        p12 = KeyStore.getInstance("pkcs12");
        p12.load(new FileInputStream(P12FILE), PASSWORD);
        for (int i=0; i<SIZE; i++) {
            String a = "p" + i;
            X509Certificate x = (X509Certificate)p12.getCertificate(a);
            X500Name name = (X500Name)x.getSubjectDN();
            if (!name.getCommonName().equals(""+i)) {
                throw new Exception(a + "'s cert is " + name);
            }
        }
    }
}
