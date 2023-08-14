final class ConstructKeys {
    private static final PublicKey constructPublicKey(byte[] encodedKey,
                                              String encodedKeyAlgorithm)
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        PublicKey key = null;
        try {
            KeyFactory keyFactory =
                KeyFactory.getInstance(encodedKeyAlgorithm, "SunJCE");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            key = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException nsae) {
            try {
                KeyFactory keyFactory =
                    KeyFactory.getInstance(encodedKeyAlgorithm);
                X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(encodedKey);
                key = keyFactory.generatePublic(keySpec);
            } catch (NoSuchAlgorithmException nsae2) {
                throw new NoSuchAlgorithmException("No installed providers " +
                                                   "can create keys for the " +
                                                   encodedKeyAlgorithm +
                                                   "algorithm");
            } catch (InvalidKeySpecException ikse2) {
                InvalidKeyException ike =
                    new InvalidKeyException("Cannot construct public key");
                ike.initCause(ikse2);
                throw ike;
            }
        } catch (InvalidKeySpecException ikse) {
            InvalidKeyException ike =
                new InvalidKeyException("Cannot construct public key");
            ike.initCause(ikse);
            throw ike;
        } catch (NoSuchProviderException nspe) {
        }
        return key;
    }
    private static final PrivateKey constructPrivateKey(byte[] encodedKey,
                                                String encodedKeyAlgorithm)
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        PrivateKey key = null;
        try {
            KeyFactory keyFactory =
                KeyFactory.getInstance(encodedKeyAlgorithm, "SunJCE");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException nsae) {
            try {
                KeyFactory keyFactory =
                    KeyFactory.getInstance(encodedKeyAlgorithm);
                PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(encodedKey);
                key = keyFactory.generatePrivate(keySpec);
            } catch (NoSuchAlgorithmException nsae2) {
                throw new NoSuchAlgorithmException("No installed providers " +
                                                   "can create keys for the " +
                                                   encodedKeyAlgorithm +
                                                   "algorithm");
            } catch (InvalidKeySpecException ikse2) {
                InvalidKeyException ike =
                    new InvalidKeyException("Cannot construct private key");
                ike.initCause(ikse2);
                throw ike;
            }
        } catch (InvalidKeySpecException ikse) {
            InvalidKeyException ike =
                new InvalidKeyException("Cannot construct private key");
            ike.initCause(ikse);
            throw ike;
        } catch (NoSuchProviderException nspe) {
        }
        return key;
    }
    private static final SecretKey constructSecretKey(byte[] encodedKey,
                                              String encodedKeyAlgorithm)
    {
        return (new SecretKeySpec(encodedKey, encodedKeyAlgorithm));
    }
    static final Key constructKey(byte[] encoding, String keyAlgorithm,
                                  int keyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        Key result = null;
        switch (keyType) {
        case Cipher.SECRET_KEY:
            result = ConstructKeys.constructSecretKey(encoding,
                                                      keyAlgorithm);
            break;
        case Cipher.PRIVATE_KEY:
            result = ConstructKeys.constructPrivateKey(encoding,
                                                       keyAlgorithm);
            break;
        case Cipher.PUBLIC_KEY:
            result = ConstructKeys.constructPublicKey(encoding,
                                                      keyAlgorithm);
            break;
        }
        return result;
    }
}
