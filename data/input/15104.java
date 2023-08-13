final class ECDHCrypt {
    private PrivateKey privateKey;
    private ECPublicKey publicKey;
    ECDHCrypt(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = (ECPublicKey)publicKey;
    }
    ECDHCrypt(String curveName, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("EC");
            ECGenParameterSpec params = new ECGenParameterSpec(curveName);
            kpg.initialize(params, random);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey = (ECPublicKey)kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }
    ECDHCrypt(ECParameterSpec params, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("EC");
            kpg.initialize(params, random);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey = (ECPublicKey)kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }
    PublicKey getPublicKey() {
        return publicKey;
    }
    SecretKey getAgreedSecret(PublicKey peerPublicKey) {
        try {
            KeyAgreement ka = JsseJce.getKeyAgreement("ECDH");
            ka.init(privateKey);
            ka.doPhase(peerPublicKey, true);
            return ka.generateSecret("TlsPremasterSecret");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        }
    }
    SecretKey getAgreedSecret(byte[] encodedPoint) {
        try {
            ECParameterSpec params = publicKey.getParams();
            ECPoint point = JsseJce.decodePoint(encodedPoint, params.getCurve());
            KeyFactory kf = JsseJce.getKeyFactory("EC");
            ECPublicKeySpec spec = new ECPublicKeySpec(point, params);
            PublicKey peerPublicKey = kf.generatePublic(spec);
            return getAgreedSecret(peerPublicKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not generate secret", e);
        }
    }
}
