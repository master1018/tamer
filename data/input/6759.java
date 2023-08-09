final class PBECipherCore {
    private CipherCore cipher;
    private MessageDigest md;
    private int blkSize;
    private String algo = null;
    private byte[] salt = null;
    private int iCount = 10;
    PBECipherCore(String cipherAlg) throws NoSuchAlgorithmException,
        NoSuchPaddingException {
        algo = cipherAlg;
        if (algo.equals("DES")) {
            cipher = new CipherCore(new DESCrypt(),
                                    DESConstants.DES_BLOCK_SIZE);
        } else if (algo.equals("DESede")) {
            cipher = new CipherCore(new DESedeCrypt(),
                                    DESConstants.DES_BLOCK_SIZE);
        } else {
            throw new NoSuchAlgorithmException("No Cipher implementation " +
                                               "for PBEWithMD5And" + algo);
        }
        cipher.setMode("CBC");
        cipher.setPadding("PKCS5Padding");
        md = MessageDigest.getInstance("MD5");
    }
    void setMode(String mode) throws NoSuchAlgorithmException {
        cipher.setMode(mode);
    }
    void setPadding(String paddingScheme) throws NoSuchPaddingException {
        cipher.setPadding(paddingScheme);
    }
    int getBlockSize() {
        return DESConstants.DES_BLOCK_SIZE;
    }
    int getOutputSize(int inputLen) {
        return cipher.getOutputSize(inputLen);
    }
    byte[] getIV() {
        return cipher.getIV();
    }
    AlgorithmParameters getParameters() {
        AlgorithmParameters params = null;
        if (salt == null) {
            salt = new byte[8];
            SunJCE.RANDOM.nextBytes(salt);
        }
        PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, iCount);
        try {
            params = AlgorithmParameters.getInstance("PBEWithMD5And" +
                (algo.equalsIgnoreCase("DES")? "DES":"TripleDES"), "SunJCE");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("SunJCE called, but not configured");
        } catch (NoSuchProviderException nspe) {
            throw new RuntimeException("SunJCE called, but not configured");
        }
        try {
            params.init(pbeSpec);
        } catch (InvalidParameterSpecException ipse) {
            throw new RuntimeException("PBEParameterSpec not supported");
        }
        return params;
    }
    void init(int opmode, Key key, AlgorithmParameterSpec params,
              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (((opmode == Cipher.DECRYPT_MODE) ||
             (opmode == Cipher.UNWRAP_MODE)) && (params == null)) {
            throw new InvalidAlgorithmParameterException("Parameters "
                                                         + "missing");
        }
        if ((key == null) ||
            (key.getEncoded() == null) ||
            !(key.getAlgorithm().regionMatches(true, 0, "PBE", 0, 3))) {
            throw new InvalidKeyException("Missing password");
        }
        if (params == null) {
            salt = new byte[8];
            random.nextBytes(salt);
        } else {
            if (!(params instanceof PBEParameterSpec)) {
                throw new InvalidAlgorithmParameterException
                    ("Wrong parameter type: PBE expected");
            }
            salt = ((PBEParameterSpec) params).getSalt();
            if (salt.length != 8) {
                throw new InvalidAlgorithmParameterException
                    ("Salt must be 8 bytes long");
            }
            iCount = ((PBEParameterSpec) params).getIterationCount();
            if (iCount <= 0) {
                throw new InvalidAlgorithmParameterException
                    ("IterationCount must be a positive number");
            }
        }
        byte[] derivedKey = deriveCipherKey(key);
        SecretKeySpec cipherKey = new SecretKeySpec(derivedKey, 0,
                                                    derivedKey.length-8, algo);
        IvParameterSpec ivSpec = new IvParameterSpec(derivedKey,
                                                     derivedKey.length-8,
                                                     8);
        cipher.init(opmode, cipherKey, ivSpec, random);
    }
    private byte[] deriveCipherKey(Key key) {
        byte[] result = null;
        byte[] passwdBytes = key.getEncoded();
        if (algo.equals("DES")) {
            byte[] concat = new byte[passwdBytes.length + salt.length];
            System.arraycopy(passwdBytes, 0, concat, 0, passwdBytes.length);
            java.util.Arrays.fill(passwdBytes, (byte)0x00);
            System.arraycopy(salt, 0, concat, passwdBytes.length, salt.length);
            byte[] toBeHashed = concat;
            for (int i = 0; i < iCount; i++) {
                md.update(toBeHashed);
                toBeHashed = md.digest(); 
            }
            java.util.Arrays.fill(concat, (byte)0x00);
            result = toBeHashed;
        } else if (algo.equals("DESede")) {
            int i;
            for (i=0; i<4; i++) {
                if (salt[i] != salt[i+4])
                    break;
            }
            if (i==4) { 
                for (i=0; i<2; i++) {
                    byte tmp = salt[i];
                    salt[i] = salt[3-i];
                    salt[3-1] = tmp;
                }
            }
            byte[] kBytes = null;
            IvParameterSpec iv = null;
            byte[] toBeHashed = null;
            result = new byte[DESedeKeySpec.DES_EDE_KEY_LEN +
                              DESConstants.DES_BLOCK_SIZE];
            for (i = 0; i < 2; i++) {
                toBeHashed = new byte[salt.length/2];
                System.arraycopy(salt, i*(salt.length/2), toBeHashed, 0,
                                 toBeHashed.length);
                for (int j=0; j < iCount; j++) {
                    md.update(toBeHashed);
                    md.update(passwdBytes);
                    toBeHashed = md.digest(); 
                }
                System.arraycopy(toBeHashed, 0, result, i*16,
                                 toBeHashed.length);
            }
        }
        return result;
    }
    void init(int opmode, Key key, AlgorithmParameters params,
              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        PBEParameterSpec pbeSpec = null;
        if (params != null) {
            try {
                pbeSpec = (PBEParameterSpec) params.getParameterSpec
                    (PBEParameterSpec.class);
            } catch (InvalidParameterSpecException ipse) {
                throw new InvalidAlgorithmParameterException("Wrong parameter "
                                                             + "type: PBE "
                                                             + "expected");
            }
        }
        init(opmode, key, pbeSpec, random);
    }
    byte[] update(byte[] input, int inputOffset, int inputLen) {
        return cipher.update(input, inputOffset, inputLen);
    }
    int update(byte[] input, int inputOffset, int inputLen,
               byte[] output, int outputOffset)
        throws ShortBufferException {
        return cipher.update(input, inputOffset, inputLen,
                             output, outputOffset);
    }
    byte[] doFinal(byte[] input, int inputOffset, int inputLen)
        throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(input, inputOffset, inputLen);
    }
    int doFinal(byte[] input, int inputOffset, int inputLen,
                byte[] output, int outputOffset)
        throws ShortBufferException, IllegalBlockSizeException,
               BadPaddingException {
        return cipher.doFinal(input, inputOffset, inputLen,
                                    output, outputOffset);
    }
    byte[] wrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException {
        byte[] result = null;
        try {
            byte[] encodedKey = key.getEncoded();
            if ((encodedKey == null) || (encodedKey.length == 0)) {
                throw new InvalidKeyException("Cannot get an encoding of " +
                                              "the key to be wrapped");
            }
            result = doFinal(encodedKey, 0, encodedKey.length);
        } catch (BadPaddingException e) {
        }
        return result;
    }
    Key unwrap(byte[] wrappedKey,
               String wrappedKeyAlgorithm,
               int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encodedKey;
        try {
            encodedKey = doFinal(wrappedKey, 0, wrappedKey.length);
        } catch (BadPaddingException ePadding) {
            throw new InvalidKeyException("The wrapped key is not padded " +
                                          "correctly");
        } catch (IllegalBlockSizeException eBlockSize) {
            throw new InvalidKeyException("The wrapped key does not have " +
                                          "the correct length");
        }
        return ConstructKeys.constructKey(encodedKey, wrappedKeyAlgorithm,
                                          wrappedKeyType);
    }
}
