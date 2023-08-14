public abstract class CipherWithWrappingSpi extends CipherSpi {
    protected final byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException
    {
        byte[] result = null;
        try {
            byte[] encodedKey = key.getEncoded();
            if ((encodedKey == null) || (encodedKey.length == 0)) {
                throw new InvalidKeyException("Cannot get an encoding of " +
                                              "the key to be wrapped");
            }
            result = engineDoFinal(encodedKey, 0, encodedKey.length);
        } catch (BadPaddingException e) {
        }
        return result;
    }
    protected final Key engineUnwrap(byte[] wrappedKey,
                                     String wrappedKeyAlgorithm,
                                     int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] encodedKey;
        Key result = null;
        try {
            encodedKey = engineDoFinal(wrappedKey, 0,
                                       wrappedKey.length);
        } catch (BadPaddingException ePadding) {
            throw new InvalidKeyException();
        } catch (IllegalBlockSizeException eBlockSize) {
            throw new InvalidKeyException();
        }
        switch (wrappedKeyType) {
        case Cipher.SECRET_KEY:
            result = constructSecretKey(encodedKey,
                                        wrappedKeyAlgorithm);
            break;
        case Cipher.PRIVATE_KEY:
            result = constructPrivateKey(encodedKey,
                                         wrappedKeyAlgorithm);
            break;
        case Cipher.PUBLIC_KEY:
            result = constructPublicKey(encodedKey,
                                        wrappedKeyAlgorithm);
            break;
        }
        return result;
    }
    private final PublicKey constructPublicKey(byte[] encodedKey,
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
            }
        } catch (InvalidKeySpecException ikse) {
        } catch (NoSuchProviderException nspe) {
        }
        return key;
    }
    private final PrivateKey constructPrivateKey(byte[] encodedKey,
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
            }
        } catch (InvalidKeySpecException ikse) {
        } catch (NoSuchProviderException nspe) {
        }
        return key;
    }
    private final SecretKey constructSecretKey(byte[] encodedKey,
                                               String encodedKeyAlgorithm)
    {
        return (new SecretKeySpec(encodedKey, encodedKeyAlgorithm));
    }
}
