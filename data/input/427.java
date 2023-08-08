public final class DHKeyAgreement extends KeyAgreementSpi {
    private boolean generateSecret = false;
    private BigInteger init_p = null;
    private BigInteger init_g = null;
    private BigInteger x = BigInteger.ZERO;
    private BigInteger y = BigInteger.ZERO;
    public DHKeyAgreement() {
    }
    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        try {
            engineInit(key, null, random);
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        generateSecret = false;
        init_p = null;
        init_g = null;
        if ((params != null) && !(params instanceof DHParameterSpec)) {
            throw new InvalidAlgorithmParameterException("Diffie-Hellman parameters expected");
        }
        if (!(key instanceof javax.crypto.interfaces.DHPrivateKey)) {
            throw new InvalidKeyException("Diffie-Hellman private key " + "expected");
        }
        javax.crypto.interfaces.DHPrivateKey dhPrivKey;
        dhPrivKey = (javax.crypto.interfaces.DHPrivateKey) key;
        if (params != null) {
            init_p = ((DHParameterSpec) params).getP();
            init_g = ((DHParameterSpec) params).getG();
        }
        BigInteger priv_p = dhPrivKey.getParams().getP();
        BigInteger priv_g = dhPrivKey.getParams().getG();
        if (init_p != null && priv_p != null && !(init_p.equals(priv_p))) {
            throw new InvalidKeyException("Incompatible parameters");
        }
        if (init_g != null && priv_g != null && !(init_g.equals(priv_g))) {
            throw new InvalidKeyException("Incompatible parameters");
        }
        if ((init_p == null && priv_p == null) || (init_g == null && priv_g == null)) {
            throw new InvalidKeyException("Missing parameters");
        }
        init_p = priv_p;
        init_g = priv_g;
        this.x = dhPrivKey.getX();
    }
    protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
        if (!(key instanceof javax.crypto.interfaces.DHPublicKey)) {
            throw new InvalidKeyException("Diffie-Hellman public key " + "expected");
        }
        javax.crypto.interfaces.DHPublicKey dhPubKey;
        dhPubKey = (javax.crypto.interfaces.DHPublicKey) key;
        if (init_p == null || init_g == null) {
            throw new IllegalStateException("Not initialized");
        }
        BigInteger pub_p = dhPubKey.getParams().getP();
        BigInteger pub_g = dhPubKey.getParams().getG();
        if (pub_p != null && !(init_p.equals(pub_p))) {
            throw new InvalidKeyException("Incompatible parameters");
        }
        if (pub_g != null && !(init_g.equals(pub_g))) {
            throw new InvalidKeyException("Incompatible parameters");
        }
        this.y = dhPubKey.getY();
        generateSecret = true;
        if (lastPhase == false) {
            byte[] intermediate = engineGenerateSecret();
            return new DHPublicKey(new BigInteger(1, intermediate), init_p, init_g);
        } else {
            return null;
        }
    }
    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (generateSecret == false) {
            throw new IllegalStateException("Key agreement has not been completed yet");
        }
        generateSecret = false;
        BigInteger modulus = init_p;
        BigInteger tmpResult = y.modPow(x, modulus);
        byte[] secret = tmpResult.toByteArray();
        if ((tmpResult.bitLength() % 8) == 0) {
            byte retval[] = new byte[secret.length - 1];
            System.arraycopy(secret, 1, retval, 0, retval.length);
            return retval;
        } else {
            return secret;
        }
    }
    protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        if (generateSecret == false) {
            throw new IllegalStateException("Key agreement has not been completed yet");
        }
        if (sharedSecret == null) {
            throw new ShortBufferException("No buffer provided for shared secret");
        }
        BigInteger modulus = init_p;
        byte[] secret = this.y.modPow(this.x, modulus).toByteArray();
        if ((secret.length << 3) != modulus.bitLength()) {
            if ((sharedSecret.length - offset) < (secret.length - 1)) {
                throw new ShortBufferException("Buffer too short for shared secret");
            }
            System.arraycopy(secret, 1, sharedSecret, offset, secret.length - 1);
            generateSecret = false;
            return secret.length - 1;
        } else {
            if ((sharedSecret.length - offset) < secret.length) {
                throw new ShortBufferException("Buffer too short to hold shared secret");
            }
            System.arraycopy(secret, 0, sharedSecret, offset, secret.length);
            generateSecret = false;
            return secret.length;
        }
    }
    protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException, InvalidKeyException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("null algorithm");
        }
        byte[] secret = engineGenerateSecret();
        if (algorithm.equalsIgnoreCase("DES")) {
            return new DESKey(secret);
        } else if (algorithm.equalsIgnoreCase("DESede") || algorithm.equalsIgnoreCase("TripleDES")) {
            return new DESedeKey(secret);
        } else if (algorithm.equalsIgnoreCase("Blowfish")) {
            int keysize = secret.length;
            if (keysize >= BlowfishConstants.BLOWFISH_MAX_KEYSIZE) keysize = BlowfishConstants.BLOWFISH_MAX_KEYSIZE;
            SecretKeySpec skey = new SecretKeySpec(secret, 0, keysize, "Blowfish");
            return skey;
        } else if (algorithm.equalsIgnoreCase("AES")) {
            int keysize = secret.length;
            SecretKeySpec skey = null;
            int idx = AESConstants.AES_KEYSIZES.length - 1;
            while (skey == null && idx >= 0) {
                if (keysize >= AESConstants.AES_KEYSIZES[idx]) {
                    keysize = AESConstants.AES_KEYSIZES[idx];
                    skey = new SecretKeySpec(secret, 0, keysize, "AES");
                }
                idx--;
            }
            if (skey == null) {
                throw new InvalidKeyException("Key material is too short");
            }
            return skey;
        } else if (algorithm.equals("TlsPremasterSecret")) {
            return new SecretKeySpec(secret, "TlsPremasterSecret");
        } else {
            throw new NoSuchAlgorithmException("Unsupported secret key " + "algorithm: " + algorithm);
        }
    }
}
