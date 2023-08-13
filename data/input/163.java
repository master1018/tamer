final class SslMacCore {
    private final MessageDigest md;
    private final byte[] pad1, pad2;
    private boolean first;       
    private byte[] secret;
    SslMacCore(String digestAlgorithm, byte[] pad1, byte[] pad2)
            throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance(digestAlgorithm);
        this.pad1 = pad1;
        this.pad2 = pad2;
        first = true;
    }
    int getDigestLength() {
        return md.getDigestLength();
    }
    void init(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("SslMac does not use parameters");
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Secret key expected");
        }
        secret = key.getEncoded();
        if (secret == null || secret.length == 0) {
            throw new InvalidKeyException("Missing key data");
        }
        reset();
    }
    void update(byte input) {
        if (first == true) {
            md.update(secret);
            md.update(pad1);
            first = false;
        }
        md.update(input);
    }
    void update(byte input[], int offset, int len) {
        if (first == true) {
            md.update(secret);
            md.update(pad1);
            first = false;
        }
        md.update(input, offset, len);
    }
    void update(ByteBuffer input) {
        if (first == true) {
            md.update(secret);
            md.update(pad1);
            first = false;
        }
        md.update(input);
    }
    byte[] doFinal() {
        if (first == true) {
            md.update(secret);
            md.update(pad1);
        } else {
            first = true;
        }
        try {
            byte[] tmp = md.digest();
            md.update(secret);
            md.update(pad2);
            md.update(tmp);
            md.digest(tmp, 0, tmp.length);
            return tmp;
        } catch (DigestException e) {
            throw new ProviderException(e);
        }
    }
    void reset() {
        if (first == false) {
            md.reset();
            first = true;
        }
    }
    public static final class SslMacMD5 extends MacSpi {
        private final SslMacCore core;
        public SslMacMD5() throws NoSuchAlgorithmException {
            core = new SslMacCore("MD5", md5Pad1, md5Pad2);
        }
        protected int engineGetMacLength() {
            return core.getDigestLength();
        }
        protected void engineInit(Key key, AlgorithmParameterSpec params)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            core.init(key, params);
        }
        protected void engineUpdate(byte input) {
            core.update(input);
        }
        protected void engineUpdate(byte input[], int offset, int len) {
            core.update(input, offset, len);
        }
        protected void engineUpdate(ByteBuffer input) {
            core.update(input);
        }
        protected byte[] engineDoFinal() {
            return core.doFinal();
        }
        protected void engineReset() {
            core.reset();
        }
        static final byte[] md5Pad1 = genPad((byte)0x36, 48);
        static final byte[] md5Pad2 = genPad((byte)0x5c, 48);
    }
    public static final class SslMacSHA1 extends MacSpi {
        private final SslMacCore core;
        public SslMacSHA1() throws NoSuchAlgorithmException {
            core = new SslMacCore("SHA", shaPad1, shaPad2);
        }
        protected int engineGetMacLength() {
            return core.getDigestLength();
        }
        protected void engineInit(Key key, AlgorithmParameterSpec params)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            core.init(key, params);
        }
        protected void engineUpdate(byte input) {
            core.update(input);
        }
        protected void engineUpdate(byte input[], int offset, int len) {
            core.update(input, offset, len);
        }
        protected void engineUpdate(ByteBuffer input) {
            core.update(input);
        }
        protected byte[] engineDoFinal() {
            return core.doFinal();
        }
        protected void engineReset() {
            core.reset();
        }
        static final byte[] shaPad1 = genPad((byte)0x36, 40);
        static final byte[] shaPad2 = genPad((byte)0x5c, 40);
    }
}
