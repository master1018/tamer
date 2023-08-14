public class GetAlgName {
    private static String PASSWD = "password";
    private static final String[] ALGOS = {
        "PBEWithMD5AndDES", "PBEWithSHA1AndDESede", "PBEWithSHA1AndRC2_40"
    };
    private static final byte[] BYTES = new byte[20];
    public static void main(String[] argv) throws Exception {
        boolean status = true;
        PBEKeySpec ks = new PBEKeySpec(PASSWD.toCharArray());
        EncryptedPrivateKeyInfo epki;
        for (int i = 0; i < ALGOS.length; i++) {
            String algo = ALGOS[i];
            SecretKeyFactory skf =
                SecretKeyFactory.getInstance(algo, "SunJCE");
            SecretKey key = skf.generateSecret(ks);
            Cipher c = Cipher.getInstance(algo, "SunJCE");
            c.init(Cipher.ENCRYPT_MODE, key);
            c.doFinal(BYTES); 
            AlgorithmParameters ap = c.getParameters();
            epki = new EncryptedPrivateKeyInfo(ap, BYTES);
            if (!epki.getAlgName().equalsIgnoreCase(algo)) {
                System.out.println("...expect: " + algo);
                System.out.println("...got: " + epki.getAlgName());
                status = false;
            }
        }
        if (!status) {
            throw new Exception("One or more tests failed");
        } else {
            System.out.println("Test Passed");
        }
    }
}
