abstract class DSA extends SignatureSpi {
    private static final boolean debug = false;
    private DSAParams params;
    private BigInteger presetP, presetQ, presetG;
    private BigInteger presetY;
    private BigInteger presetX;
    private int[] Kseed;
    private byte[] KseedAsByteArray;
    private int[] previousKseed;
    private SecureRandom signingRandom;
    DSA() {
        super();
    }
    abstract byte[] getDigest() throws SignatureException;
    abstract void resetDigest();
    public static final class SHA1withDSA extends DSA {
        private final MessageDigest dataSHA;
        public SHA1withDSA() throws NoSuchAlgorithmException {
            dataSHA = MessageDigest.getInstance("SHA-1");
        }
        protected void engineUpdate(byte b) {
            dataSHA.update(b);
        }
        protected void engineUpdate(byte[] data, int off, int len) {
            dataSHA.update(data, off, len);
        }
        protected void engineUpdate(ByteBuffer b) {
            dataSHA.update(b);
        }
        byte[] getDigest() {
            return dataSHA.digest();
        }
        void resetDigest() {
            dataSHA.reset();
        }
    }
    public static final class RawDSA extends DSA {
        private final static int SHA1_LEN = 20;
        private final byte[] digestBuffer;
        private int ofs;
        public RawDSA() {
            digestBuffer = new byte[SHA1_LEN];
        }
        protected void engineUpdate(byte b) {
            if (ofs == SHA1_LEN) {
                ofs = SHA1_LEN + 1;
                return;
            }
            digestBuffer[ofs++] = b;
        }
        protected void engineUpdate(byte[] data, int off, int len) {
            if (ofs + len > SHA1_LEN) {
                ofs = SHA1_LEN + 1;
                return;
            }
            System.arraycopy(data, off, digestBuffer, ofs, len);
            ofs += len;
        }
        byte[] getDigest() throws SignatureException {
            if (ofs != SHA1_LEN) {
                throw new SignatureException
                        ("Data for RawDSA must be exactly 20 bytes long");
            }
            ofs = 0;
            return digestBuffer;
        }
        void resetDigest() {
            ofs = 0;
        }
    }
    protected void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException {
        if (!(privateKey instanceof java.security.interfaces.DSAPrivateKey)) {
            throw new InvalidKeyException("not a DSA private key: " +
                                          privateKey);
        }
        java.security.interfaces.DSAPrivateKey priv =
            (java.security.interfaces.DSAPrivateKey)privateKey;
        this.presetX = priv.getX();
        this.presetY = null;
        initialize(priv.getParams());
    }
    protected void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException {
        if (!(publicKey instanceof java.security.interfaces.DSAPublicKey)) {
            throw new InvalidKeyException("not a DSA public key: " +
                                          publicKey);
        }
        java.security.interfaces.DSAPublicKey pub =
            (java.security.interfaces.DSAPublicKey)publicKey;
        this.presetY = pub.getY();
        this.presetX = null;
        initialize(pub.getParams());
    }
    private void initialize(DSAParams params) throws InvalidKeyException {
        resetDigest();
        setParams(params);
    }
    protected byte[] engineSign() throws SignatureException {
        BigInteger k = generateK(presetQ);
        BigInteger r = generateR(presetP, presetQ, presetG, k);
        BigInteger s = generateS(presetX, presetQ, r, k);
        try {
            DerOutputStream outseq = new DerOutputStream(100);
            outseq.putInteger(r);
            outseq.putInteger(s);
            DerValue result = new DerValue(DerValue.tag_Sequence,
                                           outseq.toByteArray());
            return result.toByteArray();
        } catch (IOException e) {
            throw new SignatureException("error encoding signature");
        }
    }
    protected boolean engineVerify(byte[] signature)
            throws SignatureException {
        return engineVerify(signature, 0, signature.length);
    }
    protected boolean engineVerify(byte[] signature, int offset, int length)
            throws SignatureException {
        BigInteger r = null;
        BigInteger s = null;
        try {
            DerInputStream in = new DerInputStream(signature, offset, length);
            DerValue[] values = in.getSequence(2);
            r = values[0].getBigInteger();
            s = values[1].getBigInteger();
        } catch (IOException e) {
            throw new SignatureException("invalid encoding for signature");
        }
        if (r.signum() < 0) {
            r = new BigInteger(1, r.toByteArray());
        }
        if (s.signum() < 0) {
            s = new BigInteger(1, s.toByteArray());
        }
        if ((r.compareTo(presetQ) == -1) && (s.compareTo(presetQ) == -1)) {
            BigInteger w = generateW(presetP, presetQ, presetG, s);
            BigInteger v = generateV(presetY, presetP, presetQ, presetG, w, r);
            return v.equals(r);
        } else {
            throw new SignatureException("invalid signature: out of range values");
        }
    }
    private BigInteger generateR(BigInteger p, BigInteger q, BigInteger g,
                         BigInteger k) {
        BigInteger temp = g.modPow(k, p);
        return temp.remainder(q);
   }
    private BigInteger generateS(BigInteger x, BigInteger q,
            BigInteger r, BigInteger k) throws SignatureException {
        byte[] s2 = getDigest();
        BigInteger temp = new BigInteger(1, s2);
        BigInteger k1 = k.modInverse(q);
        BigInteger s = x.multiply(r);
        s = temp.add(s);
        s = k1.multiply(s);
        return s.remainder(q);
    }
    private BigInteger generateW(BigInteger p, BigInteger q,
                         BigInteger g, BigInteger s) {
        return s.modInverse(q);
    }
    private BigInteger generateV(BigInteger y, BigInteger p,
             BigInteger q, BigInteger g, BigInteger w, BigInteger r)
             throws SignatureException {
        byte[] s2 = getDigest();
        BigInteger temp = new BigInteger(1, s2);
        temp = temp.multiply(w);
        BigInteger u1 = temp.remainder(q);
        BigInteger u2 = (r.multiply(w)).remainder(q);
        BigInteger t1 = g.modPow(u1,p);
        BigInteger t2 = y.modPow(u2,p);
        BigInteger t3 = t1.multiply(t2);
        BigInteger t5 = t3.remainder(p);
        return t5.remainder(q);
    }
    private BigInteger generateK(BigInteger q) {
        BigInteger k = null;
        if (Kseed != null && !Arrays.equals(Kseed, previousKseed)) {
            k = generateK(Kseed, q);
            if (k.signum() > 0 && k.compareTo(q) < 0) {
                previousKseed = new int [Kseed.length];
                System.arraycopy(Kseed, 0, previousKseed, 0, Kseed.length);
                return k;
            }
        }
        SecureRandom random = getSigningRandom();
        while (true) {
            int[] seed = new int[5];
            for (int i = 0; i < 5; i++)
                seed[i] = random.nextInt();
            k = generateK(seed, q);
            if (k.signum() > 0 && k.compareTo(q) < 0) {
                previousKseed = new int [seed.length];
                System.arraycopy(seed, 0, previousKseed, 0, seed.length);
                return k;
            }
        }
    }
    private SecureRandom getSigningRandom() {
        if (signingRandom == null) {
            if (appRandom != null) {
                signingRandom = appRandom;
            } else {
                signingRandom = JCAUtil.getSecureRandom();
            }
        }
        return signingRandom;
    }
    private BigInteger generateK(int[] seed, BigInteger q) {
        int[] t = { 0xEFCDAB89, 0x98BADCFE, 0x10325476,
                    0xC3D2E1F0, 0x67452301 };
        int[] tmp = DSA.SHA_7(seed, t);
        byte[] tmpBytes = new byte[tmp.length * 4];
        for (int i = 0; i < tmp.length; i++) {
            int k = tmp[i];
            for (int j = 0; j < 4; j++) {
                tmpBytes[(i * 4) + j] = (byte) (k >>> (24 - (j * 8)));
            }
        }
        BigInteger k = new BigInteger(1, tmpBytes).mod(q);
        return k;
    }
    private static final int round1_kt = 0x5a827999;
    private static final int round2_kt = 0x6ed9eba1;
    private static final int round3_kt = 0x8f1bbcdc;
    private static final int round4_kt = 0xca62c1d6;
   static int[] SHA_7(int[] m1, int[] h) {
       int[] W = new int[80];
       System.arraycopy(m1,0,W,0,m1.length);
       int temp = 0;
        for (int t = 16; t <= 79; t++){
            temp = W[t-3] ^ W[t-8] ^ W[t-14] ^ W[t-16];
            W[t] = ((temp << 1) | (temp >>>(32 - 1)));
        }
       int a = h[0],b = h[1],c = h[2], d = h[3], e = h[4];
       for (int i = 0; i < 20; i++) {
            temp = ((a<<5) | (a>>>(32-5))) +
                ((b&c)|((~b)&d))+ e + W[i] + round1_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 20; i < 40; i++) {
            temp = ((a<<5) | (a>>>(32-5))) +
                (b ^ c ^ d) + e + W[i] + round2_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 40; i < 60; i++) {
            temp = ((a<<5) | (a>>>(32-5))) +
                ((b&c)|(b&d)|(c&d)) + e + W[i] + round3_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 60; i < 80; i++) {
            temp = ((a<<5) | (a>>>(32-5))) +
                (b ^ c ^ d) + e + W[i] + round4_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
       int[] md = new int[5];
       md[0] = h[0] + a;
       md[1] = h[1] + b;
       md[2] = h[2] + c;
       md[3] = h[3] + d;
       md[4] = h[4] + e;
       return md;
   }
    @Deprecated
    protected void engineSetParameter(String key, Object param) {
        if (key.equals("KSEED")) {
            if (param instanceof byte[]) {
                Kseed = byteArray2IntArray((byte[])param);
                KseedAsByteArray = (byte[])param;
            } else {
                debug("unrecognized param: " + key);
                throw new InvalidParameterException("Kseed not a byte array");
            }
        } else {
            throw new InvalidParameterException("invalid parameter");
        }
    }
    @Deprecated
    protected Object engineGetParameter(String key) {
        if (key.equals("KSEED")) {
            return KseedAsByteArray;
        } else {
            return null;
        }
    }
    private void setParams(DSAParams params) throws InvalidKeyException {
        if (params == null) {
            throw new InvalidKeyException("DSA public key lacks parameters");
        }
        this.params = params;
        this.presetP = params.getP();
        this.presetQ = params.getQ();
        this.presetG = params.getG();
    }
    public String toString() {
        String printable = "DSA Signature";
        if (presetP != null && presetQ != null && presetG != null) {
            printable += "\n\tp: " + Debug.toHexString(presetP);
            printable += "\n\tq: " + Debug.toHexString(presetQ);
            printable += "\n\tg: " + Debug.toHexString(presetG);
        } else {
            printable += "\n\t P, Q or G not initialized.";
        }
        if (presetY != null) {
            printable += "\n\ty: " + Debug.toHexString(presetY);
        }
        if (presetY == null && presetX == null) {
            printable += "\n\tUNINIIALIZED";
        }
        return printable;
    }
    private int[] byteArray2IntArray(byte[] byteArray) {
        int j = 0;
        byte[] newBA;
        int mod = byteArray.length % 4;
        switch (mod) {
            case 3:     newBA = new byte[byteArray.length + 1]; break;
            case 2:     newBA = new byte[byteArray.length + 2]; break;
            case 1:     newBA = new byte[byteArray.length + 3]; break;
            default:    newBA = new byte[byteArray.length + 0]; break;
        }
        System.arraycopy(byteArray, 0, newBA, 0, byteArray.length);
        int[] newSeed = new int[newBA.length / 4];
        for (int i = 0; i < newBA.length; i += 4) {
            newSeed[j] = newBA[i + 3] & 0xFF;
            newSeed[j] |= (newBA[i + 2] << 8) & 0xFF00;
            newSeed[j] |= (newBA[i + 1] << 16) & 0xFF0000;
            newSeed[j] |= (newBA[i + 0] << 24) & 0xFF000000;
            j++;
        }
        return newSeed;
    }
    private static void debug(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }
    private static void debug(String s) {
        if (debug) {
            System.err.println(s);
        }
    }
}
