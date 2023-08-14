public class DesAPITest {
    Cipher cipher;
    IvParameterSpec params = null;
    SecretKey cipherKey = null;
    public static byte[] key = {
        (byte)0x01,(byte)0x23,(byte)0x45,(byte)0x67,
        (byte)0x89,(byte)0xab,(byte)0xcd,(byte)0xef
    };
    public static byte[] key3 = {
        (byte)0x01,(byte)0x23,(byte)0x45,(byte)0x67,
        (byte)0x89,(byte)0xab,(byte)0xcd,(byte)0xef,
        (byte)0xf0,(byte)0xe1,(byte)0xd2,(byte)0xc3,
        (byte)0xb4,(byte)0xa5,(byte)0x96,(byte)0x87,
        (byte)0xfe,(byte)0xdc,(byte)0xba,(byte)0x98,
        (byte)0x76,(byte)0x54,(byte)0x32,(byte)0x10};
    public static byte[] iv  = {
        (byte)0xfe,(byte)0xdc,(byte)0xba,(byte)0x98,
        (byte)0x76,(byte)0x54,(byte)0x32,(byte)0x10};
    static String[] crypts = {"DES", "DESede"};
    static String[] modes = {"CFB24"};
    static String[] paddings = {"PKCS5Padding"};
    public static void main(String[] args) throws Exception {
        DesAPITest test = new DesAPITest();
        test.run();
    }
    public void run() throws Exception {
        for (int i=0; i<crypts.length; i++) {
            for (int j=0; j<modes.length; j++) {
                for (int k=0; k<paddings.length; k++) {
                    System.out.println
                        ("===============================");
                    System.out.println
                        (crypts[i]+" "+modes[j]+" " + paddings[k]);
                    init(crypts[i], modes[j], paddings[k]);
                    runTest();
                }
            }
        }
    }
    public void init(String crypt, String mode, String padding)
        throws Exception {
        SunJCE jce = new SunJCE();
        Security.addProvider(jce);
        KeySpec desKeySpec = null;
        SecretKeyFactory factory = null;
        StringBuffer cipherName = new StringBuffer(crypt);
        if (mode.length() != 0)
            cipherName.append("/" + mode);
        if (padding.length() != 0)
            cipherName.append("/" + padding);
        cipher = Cipher.getInstance(cipherName.toString());
        if (crypt.endsWith("ede")) {
            desKeySpec = new DESedeKeySpec(key3);
            factory = SecretKeyFactory.getInstance("DESede", "SunJCE");
        }
        else {
            desKeySpec = new DESKeySpec(key);
            factory = SecretKeyFactory.getInstance("DES", "SunJCE");
        }
        cipherKey = factory.generateSecret(desKeySpec);
        if ( !mode.equals("ECB"))
            params = new IvParameterSpec(iv);
        else
            params = null;
    }
    public void runTest() throws Exception {
        int bufferLen = 512;
        byte[] input = new byte[bufferLen];
        int len;
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, params);
        System.out.println("getIV, " + cipher.getIV());
        byte[] output = null;
        boolean thrown = false;
        try {
            input = null;
            output = cipher.update(input, 0, -1);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        if (!thrown) {
            throw new Exception("Expected IAE not thrown!");
        }
        byte[] inbuf = "itaoti7890123456".getBytes();
        System.out.println("inputLength: " + inbuf.length);
        output = cipher.update(inbuf);
        len = cipher.getOutputSize(16);
        byte[] out = new byte[len];
        output = cipher.doFinal();
        System.out.println(len + " " + TestUtility.hexDump(output));
    }
}
