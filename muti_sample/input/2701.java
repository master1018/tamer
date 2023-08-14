public final class RSACore {
    private RSACore() {
    }
    public static int getByteLength(BigInteger b) {
        int n = b.bitLength();
        return (n + 7) >> 3;
    }
    public static int getByteLength(RSAKey key) {
        return getByteLength(key.getModulus());
    }
    public static byte[] convert(byte[] b, int ofs, int len) {
        if ((ofs == 0) && (len == b.length)) {
            return b;
        } else {
            byte[] t = new byte[len];
            System.arraycopy(b, ofs, t, 0, len);
            return t;
        }
    }
    public static byte[] rsa(byte[] msg, RSAPublicKey key)
            throws BadPaddingException {
        return crypt(msg, key.getModulus(), key.getPublicExponent());
    }
    public static byte[] rsa(byte[] msg, RSAPrivateKey key)
            throws BadPaddingException {
        if (key instanceof RSAPrivateCrtKey) {
            return crtCrypt(msg, (RSAPrivateCrtKey)key);
        } else {
            return crypt(msg, key.getModulus(), key.getPrivateExponent());
        }
    }
    private static byte[] crypt(byte[] msg, BigInteger n, BigInteger exp)
            throws BadPaddingException {
        BigInteger m = parseMsg(msg, n);
        BigInteger c = m.modPow(exp, n);
        return toByteArray(c, getByteLength(n));
    }
    private static byte[] crtCrypt(byte[] msg, RSAPrivateCrtKey key)
            throws BadPaddingException {
        BigInteger n = key.getModulus();
        BigInteger c = parseMsg(msg, n);
        BigInteger p = key.getPrimeP();
        BigInteger q = key.getPrimeQ();
        BigInteger dP = key.getPrimeExponentP();
        BigInteger dQ = key.getPrimeExponentQ();
        BigInteger qInv = key.getCrtCoefficient();
        BlindingParameters params;
        if (ENABLE_BLINDING) {
            params = getBlindingParameters(key);
            c = c.multiply(params.re).mod(n);
        } else {
            params = null;
        }
        BigInteger m1 = c.modPow(dP, p);
        BigInteger m2 = c.modPow(dQ, q);
        BigInteger mtmp = m1.subtract(m2);
        if (mtmp.signum() < 0) {
            mtmp = mtmp.add(p);
        }
        BigInteger h = mtmp.multiply(qInv).mod(p);
        BigInteger m = h.multiply(q).add(m2);
        if (params != null) {
            m = m.multiply(params.rInv).mod(n);
        }
        return toByteArray(m, getByteLength(n));
    }
    private static BigInteger parseMsg(byte[] msg, BigInteger n)
            throws BadPaddingException {
        BigInteger m = new BigInteger(1, msg);
        if (m.compareTo(n) >= 0) {
            throw new BadPaddingException("Message is larger than modulus");
        }
        return m;
    }
    private static byte[] toByteArray(BigInteger bi, int len) {
        byte[] b = bi.toByteArray();
        int n = b.length;
        if (n == len) {
            return b;
        }
        if ((n == len + 1) && (b[0] == 0)) {
            byte[] t = new byte[len];
            System.arraycopy(b, 1, t, 0, len);
            return t;
        }
        assert (n < len);
        byte[] t = new byte[len];
        System.arraycopy(b, 0, t, (len - n), n);
        return t;
    }
    private final static boolean ENABLE_BLINDING = true;
    private final static int BLINDING_MAX_REUSE = 50;
    private final static Map<BigInteger, BlindingParameters> blindingCache =
                new WeakHashMap<>();
    private static final class BlindingParameters {
        final BigInteger e;
        final BigInteger re;
        final BigInteger rInv;
        private volatile int remainingUses;
        BlindingParameters(BigInteger e, BigInteger re, BigInteger rInv) {
            this.e = e;
            this.re = re;
            this.rInv = rInv;
            remainingUses = BLINDING_MAX_REUSE - 1;
        }
        boolean valid(BigInteger e) {
            int k = remainingUses--;
            return (k > 0) && this.e.equals(e);
        }
    }
    private static BlindingParameters getBlindingParameters
            (RSAPrivateCrtKey key) {
        BigInteger modulus = key.getModulus();
        BigInteger e = key.getPublicExponent();
        BlindingParameters params;
        synchronized (blindingCache) {
            params = blindingCache.get(modulus);
        }
        if ((params != null) && params.valid(e)) {
            return params;
        }
        int len = modulus.bitLength();
        SecureRandom random = JCAUtil.getSecureRandom();
        BigInteger r = new BigInteger(len, random).mod(modulus);
        BigInteger re = r.modPow(e, modulus);
        BigInteger rInv = r.modInverse(modulus);
        params = new BlindingParameters(e, re, rInv);
        synchronized (blindingCache) {
            blindingCache.put(modulus, params);
        }
        return params;
    }
}
