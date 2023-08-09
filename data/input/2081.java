public class CheckParity {
    static byte [] testKey = {
        (byte)0x00,             
        (byte)0x01,             
        (byte)0x02,             
        (byte)0x03,             
        (byte)0xfc,             
        (byte)0xfd,             
        (byte)0xfe,             
        (byte)0xff,             
    };
    static byte [] expectedKey = {
        (byte)0x01,             
        (byte)0x01,             
        (byte)0x02,             
        (byte)0x02,             
        (byte)0xfd,             
        (byte)0xfd,             
        (byte)0xfe,             
        (byte)0xfe,             
    };
    static private void check(String alg, byte [] key,
            byte [] expected, KeySpec ks) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(alg, "SunJCE");
        SecretKey sk = skf.generateSecret(ks);
        if (DESKeySpec.isParityAdjusted(key, 0)) {
            throw new Exception("Initial Key is somehow parity adjusted!");
        }
        byte [] encoded = sk.getEncoded();
        if (!Arrays.equals(expected, encoded)) {
            throw new Exception("encoded key is not the expected key");
        }
        if (!DESKeySpec.isParityAdjusted(encoded, 0)) {
            throw new Exception("Generated Key is not parity adjusted");
        }
    }
    static private void checkDESKey() throws Exception {
        check("DES", testKey, expectedKey, new DESKeySpec(testKey));
    }
    static private void checkDESedeKey() throws Exception {
        byte [] key3 = new byte [testKey.length * 3];
        byte [] expectedKey3 = new byte [expectedKey.length * 3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(testKey, 0, key3,
                i * testKey.length, testKey.length);
            System.arraycopy(expectedKey, 0, expectedKey3,
                i * testKey.length, testKey.length);
        }
        check("DESede", key3, expectedKey3, new DESedeKeySpec(key3));
    }
    public static void main(String[] args) throws Exception {
        checkDESKey();
        checkDESedeKey();
        System.out.println("Test Passed!");
    }
}
