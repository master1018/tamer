public final class DESedeKeyFactory extends SecretKeyFactorySpi {
    public DESedeKeyFactory() {
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
        throws InvalidKeySpecException {
        DESedeKey desEdeKey = null;
        try {
            if (keySpec instanceof DESedeKeySpec) {
                DESedeKeySpec desEdeKeySpec = (DESedeKeySpec)keySpec;
                desEdeKey = new DESedeKey(desEdeKeySpec.getKey());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
        }
        return desEdeKey;
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
        throws InvalidKeySpecException {
        try {
            if ((key instanceof SecretKey)
                && (key.getAlgorithm().equalsIgnoreCase("DESede"))
                && (key.getFormat().equalsIgnoreCase("RAW"))) {
                if (DESedeKeySpec.class.isAssignableFrom(keySpec)) {
                    return new DESedeKeySpec(key.getEncoded());
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
            if ((key != null)
                && (key.getAlgorithm().equalsIgnoreCase("DESede"))
                && (key.getFormat().equalsIgnoreCase("RAW"))) {
                if (key instanceof com.sun.crypto.provider.DESedeKey) {
                    return key;
                }
                DESedeKeySpec desEdeKeySpec
                    = (DESedeKeySpec)engineGetKeySpec(key,
                                                      DESedeKeySpec.class);
                return engineGenerateSecret(desEdeKeySpec);
            } else {
                throw new InvalidKeyException
                    ("Inappropriate key format/algorithm");
            }
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException("Cannot translate key");
        }
    }
}
