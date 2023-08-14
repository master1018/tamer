final class HmacCore implements Cloneable {
    private final MessageDigest md;
    private final byte[] k_ipad; 
    private final byte[] k_opad; 
    private boolean first;       
    private final int blockLen;
    HmacCore(MessageDigest md, int bl) {
        this.md = md;
        this.blockLen = bl;
        this.k_ipad = new byte[blockLen];
        this.k_opad = new byte[blockLen];
        first = true;
    }
    HmacCore(String digestAlgorithm, int bl) throws NoSuchAlgorithmException {
        this(MessageDigest.getInstance(digestAlgorithm), bl);
    }
    private HmacCore(HmacCore other) throws CloneNotSupportedException {
        this.md = (MessageDigest)other.md.clone();
        this.blockLen = other.blockLen;
        this.k_ipad = (byte[])other.k_ipad.clone();
        this.k_opad = (byte[])other.k_opad.clone();
        this.first = other.first;
    }
    int getDigestLength() {
        return this.md.getDigestLength();
    }
    void init(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("HMAC does not use parameters");
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Secret key expected");
        }
        byte[] secret = key.getEncoded();
        if (secret == null) {
            throw new InvalidKeyException("Missing key data");
        }
        if (secret.length > blockLen) {
            byte[] tmp = md.digest(secret);
            Arrays.fill(secret, (byte)0);
            secret = tmp;
        }
        for (int i = 0; i < blockLen; i++) {
            int si = (i < secret.length) ? secret[i] : 0;
            k_ipad[i] = (byte)(si ^ 0x36);
            k_opad[i] = (byte)(si ^ 0x5c);
        }
        Arrays.fill(secret, (byte)0);
        secret = null;
        reset();
    }
    void update(byte input) {
        if (first == true) {
            md.update(k_ipad);
            first = false;
        }
        md.update(input);
    }
    void update(byte input[], int offset, int len) {
        if (first == true) {
            md.update(k_ipad);
            first = false;
        }
        md.update(input, offset, len);
    }
    void update(ByteBuffer input) {
        if (first == true) {
            md.update(k_ipad);
            first = false;
        }
        md.update(input);
    }
    byte[] doFinal() {
        if (first == true) {
            md.update(k_ipad);
        } else {
            first = true;
        }
        try {
            byte[] tmp = md.digest();
            md.update(k_opad);
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
    public Object clone() throws CloneNotSupportedException {
        return new HmacCore(this);
    }
    public static final class HmacSHA256 extends MacSpi implements Cloneable {
        private final HmacCore core;
        public HmacSHA256() throws NoSuchAlgorithmException {
            core = new HmacCore("SHA-256", 64);
        }
        private HmacSHA256(HmacSHA256 base) throws CloneNotSupportedException {
            core = (HmacCore)base.core.clone();
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
        public Object clone() throws CloneNotSupportedException {
            return new HmacSHA256(this);
        }
    }
    public static final class HmacSHA384 extends MacSpi implements Cloneable {
        private final HmacCore core;
        public HmacSHA384() throws NoSuchAlgorithmException {
            core = new HmacCore("SHA-384", 128);
        }
        private HmacSHA384(HmacSHA384 base) throws CloneNotSupportedException {
            core = (HmacCore)base.core.clone();
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
        public Object clone() throws CloneNotSupportedException {
            return new HmacSHA384(this);
        }
    }
    public static final class HmacSHA512 extends MacSpi implements Cloneable {
        private final HmacCore core;
        public HmacSHA512() throws NoSuchAlgorithmException {
            core = new HmacCore("SHA-512", 128);
        }
        private HmacSHA512(HmacSHA512 base) throws CloneNotSupportedException {
            core = (HmacCore)base.core.clone();
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
        public Object clone() throws CloneNotSupportedException {
            return new HmacSHA512(this);
        }
    }
}
