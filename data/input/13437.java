public final class HmacPKCS12PBESHA1 extends MacSpi implements Cloneable {
    private HmacCore hmac = null;
    private static final int SHA1_BLOCK_LENGTH = 64;
    public HmacPKCS12PBESHA1() throws NoSuchAlgorithmException {
        this.hmac = new HmacCore(MessageDigest.getInstance("SHA1"),
                                 SHA1_BLOCK_LENGTH);
    }
    protected int engineGetMacLength() {
        return hmac.getDigestLength();
    }
    protected void engineInit(Key key, AlgorithmParameterSpec params)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        char[] passwdChars;
        byte[] salt = null;
        int iCount = 0;
        if (key instanceof javax.crypto.interfaces.PBEKey) {
            javax.crypto.interfaces.PBEKey pbeKey =
                (javax.crypto.interfaces.PBEKey) key;
            passwdChars = pbeKey.getPassword();
            salt = pbeKey.getSalt(); 
            iCount = pbeKey.getIterationCount(); 
        } else if (key instanceof SecretKey) {
            byte[] passwdBytes = key.getEncoded();
            if ((passwdBytes == null) ||
                !(key.getAlgorithm().regionMatches(true, 0, "PBE", 0, 3))) {
                throw new InvalidKeyException("Missing password");
            }
            passwdChars = new char[passwdBytes.length];
            for (int i=0; i<passwdChars.length; i++) {
                passwdChars[i] = (char) (passwdBytes[i] & 0x7f);
            }
        } else {
            throw new InvalidKeyException("SecretKey of PBE type required");
        }
        if (params == null) {
            if (salt == null) {
                salt = new byte[20];
                SunJCE.RANDOM.nextBytes(salt);
            }
            if (iCount == 0) iCount = 100;
        } else if (!(params instanceof PBEParameterSpec)) {
            throw new InvalidAlgorithmParameterException
                ("PBEParameterSpec type required");
        } else {
            PBEParameterSpec pbeParams = (PBEParameterSpec) params;
            if (salt != null) {
                if (!Arrays.equals(salt, pbeParams.getSalt())) {
                    throw new InvalidAlgorithmParameterException
                        ("Inconsistent value of salt between key and params");
                }
            } else {
                salt = pbeParams.getSalt();
            }
            if (iCount != 0) {
                if (iCount != pbeParams.getIterationCount()) {
                    throw new InvalidAlgorithmParameterException
                        ("Different iteration count between key and params");
                }
            } else {
                iCount = pbeParams.getIterationCount();
            }
        }
        if (salt.length < 8) {
            throw new InvalidAlgorithmParameterException
                ("Salt must be at least 8 bytes long");
        }
        if (iCount <= 0) {
            throw new InvalidAlgorithmParameterException
                ("IterationCount must be a positive number");
        }
        byte[] derivedKey = PKCS12PBECipherCore.derive(passwdChars, salt,
            iCount, hmac.getDigestLength(), PKCS12PBECipherCore.MAC_KEY);
        SecretKey cipherKey = new SecretKeySpec(derivedKey, "HmacSHA1");
        hmac.init(cipherKey, null);
    }
    protected void engineUpdate(byte input) {
        hmac.update(input);
    }
    protected void engineUpdate(byte input[], int offset, int len) {
        hmac.update(input, offset, len);
    }
    protected void engineUpdate(ByteBuffer input) {
        hmac.update(input);
    }
    protected byte[] engineDoFinal() {
        return hmac.doFinal();
    }
    protected void engineReset() {
        hmac.reset();
    }
    public Object clone() {
        HmacPKCS12PBESHA1 that = null;
        try {
            that = (HmacPKCS12PBESHA1)super.clone();
            that.hmac = (HmacCore)this.hmac.clone();
        } catch (CloneNotSupportedException e) {
        }
        return that;
    }
}
