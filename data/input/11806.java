public final class PBKDF2HmacSHA1Factory extends SecretKeyFactorySpi {
    public PBKDF2HmacSHA1Factory() {
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (!(keySpec instanceof PBEKeySpec)) {
            throw new InvalidKeySpecException("Invalid key spec");
        }
        PBEKeySpec ks = (PBEKeySpec) keySpec;
        return new PBKDF2KeyImpl(ks, "HmacSHA1");
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpecCl)
        throws InvalidKeySpecException {
        if (key instanceof javax.crypto.interfaces.PBEKey) {
            if ((keySpecCl != null)
                && PBEKeySpec.class.isAssignableFrom(keySpecCl)) {
                javax.crypto.interfaces.PBEKey pKey =
                    (javax.crypto.interfaces.PBEKey) key;
                return new PBEKeySpec
                    (pKey.getPassword(), pKey.getSalt(),
                     pKey.getIterationCount(), pKey.getEncoded().length*8);
            } else {
                throw new InvalidKeySpecException("Invalid key spec");
            }
        } else {
            throw new InvalidKeySpecException("Invalid key " +
                                               "format/algorithm");
        }
    }
    protected SecretKey engineTranslateKey(SecretKey key)
        throws InvalidKeyException {
        if ((key != null) &&
            (key.getAlgorithm().equalsIgnoreCase("PBKDF2WithHmacSHA1")) &&
            (key.getFormat().equalsIgnoreCase("RAW"))) {
            if (key instanceof com.sun.crypto.provider.PBKDF2KeyImpl) {
                return key;
            }
            if (key instanceof javax.crypto.interfaces.PBEKey) {
                javax.crypto.interfaces.PBEKey pKey =
                    (javax.crypto.interfaces.PBEKey) key;
                try {
                    PBEKeySpec spec =
                        new PBEKeySpec(pKey.getPassword(),
                                       pKey.getSalt(),
                                       pKey.getIterationCount(),
                                       pKey.getEncoded().length*8);
                    return new PBKDF2KeyImpl(spec, "HmacSHA1");
                } catch (InvalidKeySpecException re) {
                    InvalidKeyException ike = new InvalidKeyException
                        ("Invalid key component(s)");
                    ike.initCause(re);
                    throw ike;
                }
            }
        }
        throw new InvalidKeyException("Invalid key format/algorithm");
    }
}
