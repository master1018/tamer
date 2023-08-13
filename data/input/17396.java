abstract class PBEKeyFactory extends SecretKeyFactorySpi {
    private String type;
    private static HashSet<String> validTypes;
    private PBEKeyFactory(String keytype) {
        type = keytype;
    }
    static {
        validTypes = new HashSet<String>(4);
        validTypes.add("PBEWithMD5AndDES".toUpperCase());
        validTypes.add("PBEWithSHA1AndDESede".toUpperCase());
        validTypes.add("PBEWithSHA1AndRC2_40".toUpperCase());
        validTypes.add("PBEWithMD5AndTripleDES".toUpperCase());
    }
    public static final class PBEWithMD5AndDES
            extends PBEKeyFactory {
        public PBEWithMD5AndDES()  {
            super("PBEWithMD5AndDES");
        }
    }
    public static final class PBEWithSHA1AndDESede
            extends PBEKeyFactory {
        public PBEWithSHA1AndDESede()  {
            super("PBEWithSHA1AndDESede");
        }
    }
    public static final class PBEWithSHA1AndRC2_40
            extends PBEKeyFactory {
        public PBEWithSHA1AndRC2_40()  {
            super("PBEWithSHA1AndRC2_40");
        }
    }
    public static final class PBEWithMD5AndTripleDES
            extends PBEKeyFactory {
        public PBEWithMD5AndTripleDES()  {
            super("PBEWithMD5AndTripleDES");
        }
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (!(keySpec instanceof PBEKeySpec)) {
            throw new InvalidKeySpecException("Invalid key spec");
        }
        return new PBEKey((PBEKeySpec)keySpec, type);
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpecCl)
        throws InvalidKeySpecException {
        if ((key instanceof SecretKey)
            && (validTypes.contains(key.getAlgorithm().toUpperCase()))
            && (key.getFormat().equalsIgnoreCase("RAW"))) {
            if ((keySpecCl != null)
                && PBEKeySpec.class.isAssignableFrom(keySpecCl)) {
                byte[] passwdBytes = key.getEncoded();
                char[] passwdChars = new char[passwdBytes.length];
                for (int i=0; i<passwdChars.length; i++)
                    passwdChars[i] = (char) (passwdBytes[i] & 0x7f);
                PBEKeySpec ret = new PBEKeySpec(passwdChars);
                java.util.Arrays.fill(passwdChars, ' ');
                java.util.Arrays.fill(passwdBytes, (byte)0x00);
                return ret;
            } else {
                throw new InvalidKeySpecException("Invalid key spec");
            }
        } else {
            throw new InvalidKeySpecException("Invalid key "
                                              + "format/algorithm");
        }
    }
    protected SecretKey engineTranslateKey(SecretKey key)
        throws InvalidKeyException
    {
        try {
            if ((key != null) &&
                (validTypes.contains(key.getAlgorithm().toUpperCase())) &&
                (key.getFormat().equalsIgnoreCase("RAW"))) {
                if (key instanceof com.sun.crypto.provider.PBEKey) {
                    return key;
                }
                PBEKeySpec pbeKeySpec = (PBEKeySpec)engineGetKeySpec
                    (key, PBEKeySpec.class);
                return engineGenerateSecret(pbeKeySpec);
            } else {
                throw new InvalidKeyException("Invalid key format/algorithm");
            }
        } catch (InvalidKeySpecException ikse) {
            throw new InvalidKeyException("Cannot translate key: "
                                          + ikse.getMessage());
        }
    }
}
