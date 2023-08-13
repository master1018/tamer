public final class DESKeyFactory extends SecretKeyFactorySpi {
    public DESKeyFactory() {
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
        throws InvalidKeySpecException {
        DESKey desKey = null;
        try {
            if (!(keySpec instanceof DESKeySpec)) {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
            else {
                DESKeySpec desKeySpec = (DESKeySpec)keySpec;
                desKey = new DESKey(desKeySpec.getKey());
            }
        } catch (InvalidKeyException e) {
        }
        return desKey;
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
        throws InvalidKeySpecException {
        try {
            if ((key instanceof SecretKey)
                && (key.getAlgorithm().equalsIgnoreCase("DES"))
                && (key.getFormat().equalsIgnoreCase("RAW"))) {
                if ((keySpec != null) &&
                    DESKeySpec.class.isAssignableFrom(keySpec)) {
                    return new DESKeySpec(key.getEncoded());
                } else {
                    throw new InvalidKeySpecException
                        ("Inappropriate key specification");
                }
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key format/algorithm");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException("Secret key has wrong size");
        }
    }
    protected SecretKey engineTranslateKey(SecretKey key)
        throws InvalidKeyException {
        try {
            if ((key != null) &&
                (key.getAlgorithm().equalsIgnoreCase("DES")) &&
                (key.getFormat().equalsIgnoreCase("RAW"))) {
                if (key instanceof com.sun.crypto.provider.DESKey) {
                    return key;
                }
                DESKeySpec desKeySpec
                    = (DESKeySpec)engineGetKeySpec(key, DESKeySpec.class);
                return engineGenerateSecret(desKeySpec);
            } else {
                throw new InvalidKeyException
                    ("Inappropriate key format/algorithm");
            }
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException("Cannot translate key");
        }
    }
}
