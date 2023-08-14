public class TestSymmCiphers extends PKCS11Test {
    private static class CI { 
        String transformation;
        String keyAlgo;
        int dataSize;
        CI(String transformation, String keyAlgo, int dataSize) {
            this.transformation = transformation;
            this.keyAlgo = keyAlgo;
            this.dataSize = dataSize;
        }
    }
    private static final CI[] TEST_LIST = {
        new CI("ARCFOUR", "ARCFOUR", 400),
        new CI("RC4", "RC4", 401),
        new CI("DES/CBC/NoPadding", "DES", 400),
        new CI("DESede/CBC/NoPadding", "DESede", 160),
        new CI("AES/CBC/NoPadding", "AES", 4800),
        new CI("Blowfish/CBC/NoPadding", "Blowfish", 24),
        new CI("DES/cbc/PKCS5Padding", "DES", 6401),
        new CI("DESede/CBC/PKCS5Padding", "DESede", 402),
        new CI("AES/CBC/PKCS5Padding", "AES", 30),
        new CI("Blowfish/CBC/PKCS5Padding", "Blowfish", 19),
        new CI("DES/ECB/NoPadding", "DES", 400),
        new CI("DESede/ECB/NoPadding", "DESede", 160),
        new CI("AES/ECB/NoPadding", "AES", 4800),
        new CI("DES/ECB/PKCS5Padding", "DES", 32),
        new CI("DES/ECB/PKCS5Padding", "DES", 6400),
        new CI("DESede/ECB/PKCS5Padding", "DESede", 400),
        new CI("AES/ECB/PKCS5Padding", "AES", 64),
        new CI("DES", "DES", 6400),
        new CI("DESede", "DESede", 408),
        new CI("AES", "AES", 128),
        new CI("AES/CTR/NoPadding", "AES", 3200)
    };
    private static StringBuffer debugBuf = new StringBuffer();
    public void main(Provider p) throws Exception {
        int firstBlkSize = 16;
        boolean status = true;
        Random random = new Random();
        try {
            for (int i = 0; i < TEST_LIST.length; i++) {
                CI currTest = TEST_LIST[i];
                System.out.println("===" + currTest.transformation + "===");
                try {
                    KeyGenerator kg =
                            KeyGenerator.getInstance(currTest.keyAlgo, p);
                    SecretKey key = kg.generateKey();
                    Cipher c1 = Cipher.getInstance(currTest.transformation, p);
                    Cipher c2 = Cipher.getInstance(currTest.transformation,
                            "SunJCE");
                    byte[] plainTxt = new byte[currTest.dataSize];
                    random.nextBytes(plainTxt);
                    System.out.println("Testing inLen = " + plainTxt.length);
                    c2.init(Cipher.ENCRYPT_MODE, key);
                    AlgorithmParameters params = c2.getParameters();
                    byte[] answer = c2.doFinal(plainTxt);
                    System.out.println("Encryption tests: START");
                    test(c1, Cipher.ENCRYPT_MODE, key, params, firstBlkSize,
                            plainTxt, answer);
                    System.out.println("Encryption tests: DONE");
                    c2.init(Cipher.DECRYPT_MODE, key, params);
                    byte[] answer2 = c2.doFinal(answer);
                    System.out.println("Decryption tests: START");
                    test(c1, Cipher.DECRYPT_MODE, key, params, firstBlkSize,
                            answer, answer2);
                    System.out.println("Decryption tests: DONE");
                } catch (NoSuchAlgorithmException nsae) {
                    System.out.println("Skipping unsupported algorithm: " +
                            nsae);
                }
            }
        } catch (Exception ex) {
            if (debugBuf != null) {
                System.out.println(debugBuf.toString());
                debugBuf = new StringBuffer();
            }
            throw ex;
        }
    }
    private static void test(Cipher cipher, int mode, SecretKey key,
            AlgorithmParameters params, int firstBlkSize,
            byte[] in, byte[] answer) throws Exception {
        long startTime, endTime;
        cipher.init(mode, key, params);
        int outLen = cipher.getOutputSize(in.length);
        ByteBuffer inBuf = ByteBuffer.allocate(in.length);
        inBuf.put(in);
        inBuf.position(0);
        ByteBuffer inDirectBuf = ByteBuffer.allocateDirect(in.length);
        inDirectBuf.put(in);
        inDirectBuf.position(0);
        ByteBuffer outBuf = ByteBuffer.allocate(outLen);
        ByteBuffer outDirectBuf = ByteBuffer.allocateDirect(outLen);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        startTime = System.nanoTime();
        byte[] temp = cipher.update(in, 0, firstBlkSize);
        if (temp != null && temp.length > 0) {
            baos.write(temp, 0, temp.length);
        }
        temp = cipher.doFinal(in, firstBlkSize, in.length - firstBlkSize);
        if (temp != null && temp.length > 0) {
            baos.write(temp, 0, temp.length);
        }
        byte[] testOut1 = baos.toByteArray();
        endTime = System.nanoTime();
        perfOut("stream InBuf + stream OutBuf: " +
                (endTime - startTime));
        match(testOut1, answer);
        startTime = System.nanoTime();
        cipher.update(inBuf, outBuf);
        cipher.doFinal(inBuf, outBuf);
        endTime = System.nanoTime();
        perfOut("non-direct InBuf + non-direct OutBuf: " +
                (endTime - startTime));
        match(outBuf, answer);
        startTime = System.nanoTime();
        cipher.update(inDirectBuf, outDirectBuf);
        cipher.doFinal(inDirectBuf, outDirectBuf);
        endTime = System.nanoTime();
        perfOut("direct InBuf + direct OutBuf: " +
                (endTime - startTime));
        match(outDirectBuf, answer);
        inDirectBuf.position(0);
        outBuf.position(0);
        startTime = System.nanoTime();
        cipher.update(inDirectBuf, outBuf);
        cipher.doFinal(inDirectBuf, outBuf);
        endTime = System.nanoTime();
        perfOut("direct InBuf + non-direct OutBuf: " +
                (endTime - startTime));
        match(outBuf, answer);
        inBuf.position(0);
        outDirectBuf.position(0);
        startTime = System.nanoTime();
        cipher.update(inBuf, outDirectBuf);
        cipher.doFinal(inBuf, outDirectBuf);
        endTime = System.nanoTime();
        perfOut("non-direct InBuf + direct OutBuf: " +
                (endTime - startTime));
        match(outDirectBuf, answer);
        debugBuf = null;
    }
    private static void perfOut(String msg) {
        if (debugBuf != null) {
            debugBuf.append("PERF>" + msg);
        }
    }
    private static void debugOut(String msg) {
        if (debugBuf != null) {
            debugBuf.append(msg);
        }
    }
    private static void match(byte[] b1, byte[] b2) throws Exception {
        if (b1.length != b2.length) {
            debugOut("got len   : " + b1.length + "\n");
            debugOut("expect len: " + b2.length + "\n");
            throw new Exception("mismatch - different length! got: " + b1.length + ", expect: " + b2.length + "\n");
        } else {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    debugOut("got   : " + toString(b1) + "\n");
                    debugOut("expect: " + toString(b2) + "\n");
                    throw new Exception("mismatch");
                }
            }
        }
    }
    private static void match(ByteBuffer bb, byte[] answer) throws Exception {
        byte[] bbTemp = new byte[bb.position()];
        bb.position(0);
        bb.get(bbTemp, 0, bbTemp.length);
        match(bbTemp, answer);
    }
    public static void main(String[] args) throws Exception {
        main(new TestSymmCiphers());
    }
}
