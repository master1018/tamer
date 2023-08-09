public class DHKeyAgreement3 {
    private DHKeyAgreement3() {}
    public static void main(String argv[]) throws Exception {
            SunJCE jce = new SunJCE();
            Security.addProvider(jce);
            DHKeyAgreement3 keyAgree = new DHKeyAgreement3();
            keyAgree.run();
            System.out.println("Test Passed");
    }
    private void run() throws Exception {
        DHParameterSpec dhSkipParamSpec;
        System.err.println("Using SKIP Diffie-Hellman parameters");
        dhSkipParamSpec = new DHParameterSpec(skip1024Modulus, skip1024Base);
        System.err.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(dhSkipParamSpec);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();
        System.err.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhSkipParamSpec);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();
        System.err.println("CAROL: Generate DH keypair ...");
        KeyPairGenerator carolKpairGen = KeyPairGenerator.getInstance("DH");
        carolKpairGen.initialize(dhSkipParamSpec);
        KeyPair carolKpair = carolKpairGen.generateKeyPair();
        System.err.println("ALICE: Initialize ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());
        System.err.println("BOB: Initialize ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());
        System.err.println("CAROL: Initialize ...");
        KeyAgreement carolKeyAgree = KeyAgreement.getInstance("DH");
        carolKeyAgree.init(carolKpair.getPrivate());
        Key ac = aliceKeyAgree.doPhase(carolKpair.getPublic(), false);
        Key ba = bobKeyAgree.doPhase(aliceKpair.getPublic(), false);
        Key cb = carolKeyAgree.doPhase(bobKpair.getPublic(), false);
        aliceKeyAgree.doPhase(cb, true);
        bobKeyAgree.doPhase(ac, true);
        carolKeyAgree.doPhase(ba, true);
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;
        System.out.println("Alice secret: " + toHexString(aliceSharedSecret));
        byte[] bobSharedSecret = bobKeyAgree.generateSecret();
        int bobLen = bobSharedSecret.length;
        System.out.println("Bob secret: " + toHexString(bobSharedSecret));
        byte[] carolSharedSecret = carolKeyAgree.generateSecret();
        int carolLen = carolSharedSecret.length;
        System.out.println("Carol secret: " + toHexString(carolSharedSecret));
        if (aliceLen != bobLen) {
            throw new Exception("Alice and Bob have different lengths");
        }
        for (int i=0; i<aliceLen; i++) {
            if (aliceSharedSecret[i] != bobSharedSecret[i]) {
                throw new Exception("Alice and Bob differ");
            }
        }
        System.err.println("Alice and Bob are the same");
        if (bobLen != carolLen) {
            throw new Exception("Bob and Carol have different lengths");
        }
        for (int i=0; i<bobLen; i++) {
            if (bobSharedSecret[i] != carolSharedSecret[i]) {
                throw new Exception("Bob and Carol differ");
            }
        }
        System.err.println("Bob and Carol are the same");
    }
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }
    private void usage() {
        System.err.print("DHKeyAgreement usage: ");
        System.err.println("[-gen]");
    }
    private static final byte skip1024ModulusBytes[] = {
        (byte)0xF4, (byte)0x88, (byte)0xFD, (byte)0x58,
        (byte)0x4E, (byte)0x49, (byte)0xDB, (byte)0xCD,
        (byte)0x20, (byte)0xB4, (byte)0x9D, (byte)0xE4,
        (byte)0x91, (byte)0x07, (byte)0x36, (byte)0x6B,
        (byte)0x33, (byte)0x6C, (byte)0x38, (byte)0x0D,
        (byte)0x45, (byte)0x1D, (byte)0x0F, (byte)0x7C,
        (byte)0x88, (byte)0xB3, (byte)0x1C, (byte)0x7C,
        (byte)0x5B, (byte)0x2D, (byte)0x8E, (byte)0xF6,
        (byte)0xF3, (byte)0xC9, (byte)0x23, (byte)0xC0,
        (byte)0x43, (byte)0xF0, (byte)0xA5, (byte)0x5B,
        (byte)0x18, (byte)0x8D, (byte)0x8E, (byte)0xBB,
        (byte)0x55, (byte)0x8C, (byte)0xB8, (byte)0x5D,
        (byte)0x38, (byte)0xD3, (byte)0x34, (byte)0xFD,
        (byte)0x7C, (byte)0x17, (byte)0x57, (byte)0x43,
        (byte)0xA3, (byte)0x1D, (byte)0x18, (byte)0x6C,
        (byte)0xDE, (byte)0x33, (byte)0x21, (byte)0x2C,
        (byte)0xB5, (byte)0x2A, (byte)0xFF, (byte)0x3C,
        (byte)0xE1, (byte)0xB1, (byte)0x29, (byte)0x40,
        (byte)0x18, (byte)0x11, (byte)0x8D, (byte)0x7C,
        (byte)0x84, (byte)0xA7, (byte)0x0A, (byte)0x72,
        (byte)0xD6, (byte)0x86, (byte)0xC4, (byte)0x03,
        (byte)0x19, (byte)0xC8, (byte)0x07, (byte)0x29,
        (byte)0x7A, (byte)0xCA, (byte)0x95, (byte)0x0C,
        (byte)0xD9, (byte)0x96, (byte)0x9F, (byte)0xAB,
        (byte)0xD0, (byte)0x0A, (byte)0x50, (byte)0x9B,
        (byte)0x02, (byte)0x46, (byte)0xD3, (byte)0x08,
        (byte)0x3D, (byte)0x66, (byte)0xA4, (byte)0x5D,
        (byte)0x41, (byte)0x9F, (byte)0x9C, (byte)0x7C,
        (byte)0xBD, (byte)0x89, (byte)0x4B, (byte)0x22,
        (byte)0x19, (byte)0x26, (byte)0xBA, (byte)0xAB,
        (byte)0xA2, (byte)0x5E, (byte)0xC3, (byte)0x55,
        (byte)0xE9, (byte)0x2F, (byte)0x78, (byte)0xC7
    };
    private static final BigInteger skip1024Modulus
    = new BigInteger(1, skip1024ModulusBytes);
    private static final BigInteger skip1024Base = BigInteger.valueOf(2);
}
