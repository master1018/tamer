final class DHCrypt {
    private BigInteger modulus;                 
    private BigInteger base;                    
    private PrivateKey privateKey;
    private BigInteger publicValue;             
    DHCrypt(int keyLength, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("DiffieHellman");
            kpg.initialize(keyLength, random);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            DHPublicKeySpec spec = getDHPublicKeySpec(kp.getPublic());
            publicValue = spec.getY();
            modulus = spec.getP();
            base = spec.getG();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }
    DHCrypt(BigInteger modulus, BigInteger base, SecureRandom random) {
        this.modulus = modulus;
        this.base = base;
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("DiffieHellman");
            DHParameterSpec params = new DHParameterSpec(modulus, base);
            kpg.initialize(params, random);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            DHPublicKeySpec spec = getDHPublicKeySpec(kp.getPublic());
            publicValue = spec.getY();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }
    static DHPublicKeySpec getDHPublicKeySpec(PublicKey key) {
        if (key instanceof DHPublicKey) {
            DHPublicKey dhKey = (DHPublicKey)key;
            DHParameterSpec params = dhKey.getParams();
            return new DHPublicKeySpec(dhKey.getY(), params.getP(), params.getG());
        }
        try {
            KeyFactory factory = JsseJce.getKeyFactory("DH");
            return factory.getKeySpec(key, DHPublicKeySpec.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    BigInteger getModulus() {
        return modulus;
    }
    BigInteger getBase() {
        return base;
    }
    BigInteger getPublicKey() {
        return publicValue;
    }
    SecretKey getAgreedSecret(BigInteger peerPublicValue) {
        try {
            KeyFactory kf = JsseJce.getKeyFactory("DiffieHellman");
            DHPublicKeySpec spec =
                        new DHPublicKeySpec(peerPublicValue, modulus, base);
            PublicKey publicKey = kf.generatePublic(spec);
            KeyAgreement ka = JsseJce.getKeyAgreement("DiffieHellman");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            return ka.generateSecret("TlsPremasterSecret");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        }
    }
}
