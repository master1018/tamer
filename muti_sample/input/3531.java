public final class ARCFOURCipher extends CipherSpi {
    private final int[] S;
    private int is, js;
    private byte[] lastKey;
    public ARCFOURCipher() {
        S = new int[256];
    }
    private void init(byte[] key) {
        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }
        for (int i = 0, j = 0, ki = 0; i < 256; i++) {
            int Si = S[i];
            j = (j + Si + key[ki]) & 0xff;
            S[i] = S[j];
            S[j] = Si;
            ki++;
            if (ki == key.length) {
                ki = 0;
            }
        }
        is = 0;
        js = 0;
    }
    private void crypt(byte[] in, int inOfs, int inLen, byte[] out,
            int outOfs) {
        if (is < 0) {
            init(lastKey);
        }
        while (inLen-- > 0) {
            is = (is + 1) & 0xff;
            int Si = S[is];
            js = (js + Si) & 0xff;
            int Sj = S[js];
            S[is] = Sj;
            S[js] = Si;
            out[outOfs++] = (byte)(in[inOfs++] ^ S[(Si + Sj) & 0xff]);
        }
    }
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (mode.equalsIgnoreCase("ECB") == false) {
            throw new NoSuchAlgorithmException("Unsupported mode " + mode);
        }
    }
    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
        if (padding.equalsIgnoreCase("NoPadding") == false) {
            throw new NoSuchPaddingException("Padding must be NoPadding");
        }
    }
    protected int engineGetBlockSize() {
        return 0;
    }
    protected int engineGetOutputSize(int inputLen) {
        return inputLen;
    }
    protected byte[] engineGetIV() {
        return null;
    }
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        init(opmode, key);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
        }
        init(opmode, key);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
        }
        init(opmode, key);
    }
    private void init(int opmode, Key key) throws InvalidKeyException {
        if ((opmode < Cipher.ENCRYPT_MODE) || (opmode > Cipher.UNWRAP_MODE)) {
            throw new InvalidKeyException("Unknown opmode: " + opmode);
        }
        lastKey = getEncodedKey(key);
        init(lastKey);
    }
    private static byte[] getEncodedKey(Key key) throws InvalidKeyException {
        String keyAlg = key.getAlgorithm();
        if (!keyAlg.equals("RC4") && !keyAlg.equals("ARCFOUR")) {
            throw new InvalidKeyException("Not an ARCFOUR key: " + keyAlg);
        }
        if ("RAW".equals(key.getFormat()) == false) {
            throw new InvalidKeyException("Key encoding format must be RAW");
        }
        byte[] encodedKey = key.getEncoded();
        if ((encodedKey.length < 5) || (encodedKey.length > 128)) {
            throw new InvalidKeyException
                ("Key length must be between 40 and 1024 bit");
        }
        return encodedKey;
    }
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        byte[] out = new byte[inLen];
        crypt(in, inOfs, inLen, out, 0);
        return out;
    }
    protected int engineUpdate(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException {
        if (out.length - outOfs < inLen) {
            throw new ShortBufferException("Output buffer too small");
        }
        crypt(in, inOfs, inLen, out, outOfs);
        return inLen;
    }
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen) {
        byte[] out = engineUpdate(in, inOfs, inLen);
        is = -1;
        return out;
    }
    protected int engineDoFinal(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException {
        int outLen = engineUpdate(in, inOfs, inLen, out, outOfs);
        is = -1;
        return outLen;
    }
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException,
            InvalidKeyException {
        byte[] encoded = key.getEncoded();
        if ((encoded == null) || (encoded.length == 0)) {
            throw new InvalidKeyException("Could not obtain encoded key");
        }
        return engineDoFinal(encoded, 0, encoded.length);
    }
    protected Key engineUnwrap(byte[] wrappedKey, String algorithm,
            int type) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
        return ConstructKeys.constructKey(encoded, algorithm, type);
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encodedKey = getEncodedKey(key);
        return encodedKey.length << 3;
    }
}
