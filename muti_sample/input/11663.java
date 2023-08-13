public class TestKATForECB_IV
{
    private static final String ALGO = "AES";
    private static final String MODE = "ECB";
    private static final String PADDING = "NoPadding";
    private static String[] PTS = {
        "000102030405060708090A0B0C0D0E0F",
        "000102030405060708090A0B0C0D0E0F",
        "000102030405060708090A0B0C0D0E0F"
    };
    private static String[] CTS = {
        "0A940BB5416EF045F1C39458C653EA5A",
        "0060BFFE46834BB8DA5CF9A61FF220AE",
        "5A6E045708FB7196F02E553D02C3A692"
    };
    private static String[] KEYS = {
        "000102030405060708090A0B0C0D0E0F",
        "000102030405060708090A0B0C0D0E0F1011121314151617",
        "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F"
    };
    private static SecretKey constructAESKey(String s) throws Exception {
        int len = s.length()/2;
        if ((len != 16) && (len != 24) && (len != 32)) {
            throw new IllegalArgumentException("Wrong Key Length: " + len);
        }
        byte[] rawKeyValue = constructByteArray(s);
        SecretKeySpec key = new SecretKeySpec(rawKeyValue, "AES");
        return key;
    }
    private static byte[] constructByteArray(String s) {
        int len = s.length()/2;
        byte[] tempValue = new byte[len];
        for (int i = 0; i < len; i++) {
            tempValue[i] = Integer.valueOf(s.substring(2*i, 2*i+2),
                                           16).byteValue();
        }
        return tempValue;
    }
    public boolean execute() throws Exception {
        String transformation = ALGO+"/"+MODE+"/"+PADDING;
        Cipher c = Cipher.getInstance(transformation, "SunJCE");
        for (int i=0; i<KEYS.length; i++) {
            SecretKey aesKey = constructAESKey(KEYS[i]);
            if (aesKey.getEncoded().length*8 >
                Cipher.getMaxAllowedKeyLength(transformation)) {
                continue;
            }
            try {
            c.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] plainText = constructByteArray(PTS[i]);
            byte[] cipherText = c.doFinal(plainText);
            byte[] answer = constructByteArray(CTS[i]);
            if (!Arrays.equals(cipherText, answer)) {
                throw new Exception((i+1) + "th known answer test failed for encryption");
            }
            c.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] recoveredText = c.doFinal(answer);
            if (!Arrays.equals(recoveredText, plainText)) {
                throw new Exception("known answer test failed for decryption");
            }
            System.out.println("Finished KAT for " + aesKey.getEncoded().length + "-byte key");
            } catch (SecurityException se) {
                TestUtil.handleSE(se);
            }
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        TestKATForECB_IV test = new TestKATForECB_IV();
        String testName = test.getClass().getName() + "[" + ALGO +
            "/" + MODE + "/" + PADDING + "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
