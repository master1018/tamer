public class DHKeyAgreement2 {
    private DHKeyAgreement2() {}
    public static void main(String argv[]) throws Exception {
            String mode = "USE_SKIP_DH_PARAMS";
            SunJCE jce = new SunJCE();
            Security.addProvider(jce);
            DHKeyAgreement2 keyAgree = new DHKeyAgreement2();
            if (argv.length > 1) {
                keyAgree.usage();
                throw new Exception("Wrong number of command options");
            } else if (argv.length == 1) {
                if (!(argv[0].equals("-gen"))) {
                    keyAgree.usage();
                    throw new Exception("Unrecognized flag: " + argv[0]);
                }
                mode = "GENERATE_DH_PARAMS";
            }
            keyAgree.run(mode);
            System.out.println("Test Passed");
    }
    private void run(String mode) throws Exception {
        DHParameterSpec dhSkipParamSpec;
        if (mode.equals("GENERATE_DH_PARAMS")) {
            System.err.println("Creating Diffie-Hellman parameters ...");
            AlgorithmParameterGenerator paramGen
                = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(512);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = (DHParameterSpec)params.getParameterSpec
                (DHParameterSpec.class);
        } else {
            System.err.println("Using SKIP Diffie-Hellman parameters");
            dhSkipParamSpec = new DHParameterSpec(skip1024Modulus,
                                                  skip1024Base);
        }
        System.err.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(dhSkipParamSpec);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();
        System.out.println("Alice DH public key:\n" +
                           aliceKpair.getPublic().toString());
        System.out.println("Alice DH private key:\n" +
                           aliceKpair.getPrivate().toString());
        DHParameterSpec dhParamSpec =
            ((DHPublicKey)aliceKpair.getPublic()).getParams();
        AlgorithmParameters algParams = AlgorithmParameters.getInstance("DH");
        algParams.init(dhParamSpec);
        System.out.println("Alice DH parameters:\n"
                           + algParams.toString());
        System.err.println("ALICE: Execute PHASE1 ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
            (alicePubKeyEnc);
        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
        dhParamSpec = ((DHPublicKey)alicePubKey).getParams();
        System.err.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamSpec);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();
        System.out.println("Bob DH public key:\n" +
                           bobKpair.getPublic().toString());
        System.out.println("Bob DH private key:\n" +
                           bobKpair.getPrivate().toString());
        System.err.println("BOB: Execute PHASE1 ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        System.err.println("ALICE: Execute PHASE2 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);
        System.err.println("BOB: Execute PHASE2 ...");
        bobKeyAgree.doPhase(alicePubKey, true);
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;
        byte[] bobSharedSecret = new byte[aliceLen];
        int bobLen;
        try {
            bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 1);
            System.out.println("NIGHTLY:  Should *NOT* be here!!!\n" +
                "aliceLen = " + aliceLen + "\n" +
                "Alice's shared secret");
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                hd.encodeBuffer(
                    new ByteArrayInputStream(aliceSharedSecret), System.out);
            } catch (IOException e) { }
            System.out.println("bobLen = " + bobLen);
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                hd.encodeBuffer(
                    new ByteArrayInputStream(bobSharedSecret), System.out);
            } catch (IOException e) { }
            throw new Exception("Shouldn't be succeeding.");
        } catch (ShortBufferException e) {
            System.out.println("EXPECTED:  " + e.getMessage());
        }
        bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);
        System.out.println("Alice secret: " + toHexString(aliceSharedSecret));
        System.out.println("Bob secret: " + toHexString(bobSharedSecret));
        if (aliceLen != bobLen) {
            throw new Exception("Shared secrets have different lengths");
        }
        for (int i=0; i<aliceLen; i++) {
            if (aliceSharedSecret[i] != bobSharedSecret[i]) {
                throw new Exception("Shared secrets differ");
            }
        }
        System.err.println("Shared secrets are the same");
        System.out.println("Return shared secret as SecretKey object ...");
        bobKeyAgree.doPhase(alicePubKey, true);
        SecretKey desKey = bobKeyAgree.generateSecret("DES");
        Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher.init(Cipher.ENCRYPT_MODE, desKey);
        byte[] cleartext = "This is just an example".getBytes();
        byte[] ciphertext = desCipher.doFinal(cleartext);
        desCipher.init(Cipher.DECRYPT_MODE, desKey);
        byte[] cleartext1 = desCipher.doFinal(ciphertext);
        int clearLen = cleartext.length;
        int clear1Len = cleartext1.length;
        if (clearLen != clear1Len) {
            throw new Exception("DIFFERENT");
        }
        for (int i=0; i < clear1Len; i++) {
            if (cleartext[i] != cleartext1[i]) {
                throw new Exception("DIFFERENT");
            }
        }
        System.err.println("SAME");
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
