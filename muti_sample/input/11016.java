public class SecretKeysBasic extends PKCS11Test {
    private static final char SEP = File.separatorChar;
    private static char[] tokenPwd;
    private static final char[] nssPwd =
            new char[]{'t', 'e', 's', 't', '1', '2'};
    private static final char[] solarisPwd =
            new char[]{'p', 'i', 'n'};
    private static SecretKey sk1;
    private static SecretKey sk2;
    private static SecretKey softkey;
    private static KeyStore ks;
    private static final String KS_TYPE = "PKCS11";
    private static Provider provider;
    public static void main(String[] args) throws Exception {
        main(new SecretKeysBasic());
    }
    public void main(Provider p) throws Exception {
        this.provider = p;
        byte[] keyVal = new byte[16];
        (new SecureRandom()).nextBytes(keyVal);
        if (keyVal[0] == 0) {
            keyVal[0] = 1;
        }
        softkey = new SecretKeySpec(keyVal, "AES");
        dumpKey("softkey", softkey);
        KeyGenerator kg = KeyGenerator.getInstance("DESede", provider);
        sk1 = kg.generateKey();
        dumpKey("skey1", sk1);
        sk2 = kg.generateKey();
        dumpKey("skey2", sk2);
        String token = System.getProperty("TOKEN");
        if (token == null || token.length() == 0) {
            System.out.println("Error: missing TOKEN system property");
            throw new Exception("token arg required");
        }
        if ("nss".equals(token)) {
            tokenPwd = nssPwd;
        } else if ("solaris".equals(token)) {
            tokenPwd = solarisPwd;
        }
        int testnum = 1;
        doTest();
    }
    private static boolean checkSecretKeyEntry(String alias,
            SecretKey expected,
            boolean saveBeforeCheck)
            throws Exception {
        if (saveBeforeCheck) {
            ks.setKeyEntry(alias, expected, null, null);
        }
        SecretKey result = (SecretKey) (ks.getKey(alias, null));
        String keyEncFormat = result.getFormat();
        if (keyEncFormat == null) {
            byte[] data = new byte[64];
            Cipher c =
                    Cipher.getInstance(result.getAlgorithm() + "/CBC/NoPadding",
                    provider);
            c.init(Cipher.ENCRYPT_MODE, expected);
            byte[] encOut = c.doFinal(data);
            c.init(Cipher.DECRYPT_MODE, result, c.getParameters());
            byte[] decOut = c.doFinal(encOut);
            if (!Arrays.equals(data, decOut)) {
                return false;
            }
        } else if (keyEncFormat.toUpperCase().equals("RAW")) {
            if (!Arrays.equals(result.getEncoded(), expected.getEncoded())) {
                dumpKey("\texpected:", expected);
                dumpKey("\treturns:", result);
                return false;
            }
        }
        return true;
    }
    private static void dumpKey(String info, SecretKey key) {
        System.out.println(info + "> " + key);
        System.out.println("\tALGO=" + key.getAlgorithm());
        if (key.getFormat() != null) {
            System.out.println("\t[" + key.getFormat() + "] VALUE=" +
                    new BigInteger(key.getEncoded()));
        } else {
            System.out.println("\tVALUE=n/a");
        }
    }
    private static void doTest() throws Exception {
        if (ks == null) {
            ks = KeyStore.getInstance(KS_TYPE, provider);
            ks.load(null, tokenPwd);
        }
        System.out.println("Number of entries: " + ks.size());
        if (ks.size() != 0) {
            System.out.println("Deleting entries under aliases: ");
            for (Enumeration<String> aliases = ks.aliases();
                    aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                System.out.println("\t" + alias);
                ks.deleteEntry(alias);
            }
        }
        String alias = "testSKey";
        boolean testResult = checkSecretKeyEntry(alias, softkey, true);
        if (!testResult) {
            System.out.println("FAILURE: setKey() w/ softSecretKey failed");
        }
        if (!checkSecretKeyEntry(alias, sk1, true)) {
            testResult = false;
            System.out.println("FAILURE: setKey() w/ skey1 failed");
        }
        if (!checkSecretKeyEntry(alias, sk2, true)) {
            testResult = false;
            System.out.println("FAILURE: setKey() w/ skey2 failed");
        }
        ks.store(null);
        System.out.println("Reloading keystore...");
        ks.load(null, "whatever".toCharArray());
        if (ks.size() != 1) {
            System.out.println("FAILURE: reload#1 ks.size() != 1");
        }
        if (!checkSecretKeyEntry(alias, sk2, false)) {
            testResult = false;
            System.out.println("FAILURE: reload#1 ks entry check failed");
        }
        ks.deleteEntry(alias);
        ks.store(null);
        System.out.println("Reloading keystore...");
        ks.load(null, "whatever".toCharArray());
        if (ks.size() != 0) {
            testResult = false;
            System.out.println("FAILURE: reload#2 ks.size() != 0");
        }
        if (!testResult) {
            throw new Exception("One or more test failed!");
        }
    }
}
