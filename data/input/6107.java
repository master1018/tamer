public final class DESedeWrapCipher extends CipherSpi {
    private static final byte[] IV2 = {
        (byte) 0x4a, (byte) 0xdd, (byte) 0xa2, (byte) 0x2c,
        (byte) 0x79, (byte) 0xe8, (byte) 0x21, (byte) 0x05
    };
    private FeedbackCipher cipher;
    private byte[] iv = null;
    private Key cipherKey = null;
    private boolean decrypting = false;
    public DESedeWrapCipher() {
        cipher = new CipherBlockChaining(new DESedeCrypt());
    }
    protected void engineSetMode(String mode)
        throws NoSuchAlgorithmException {
        if (!mode.equalsIgnoreCase("CBC")) {
            throw new NoSuchAlgorithmException(mode + " cannot be used");
        }
    }
    protected void engineSetPadding(String padding)
        throws NoSuchPaddingException {
        if (!padding.equalsIgnoreCase("NoPadding")) {
            throw new NoSuchPaddingException(padding + " cannot be used");
        }
    }
    protected int engineGetBlockSize() {
        return DESConstants.DES_BLOCK_SIZE;
    }
    protected int engineGetOutputSize(int inputLen) {
        int result = 0;
        if (decrypting) {
            result = inputLen - 16;
        } else {
            result = inputLen + 16;
        }
        return (result < 0? 0:result);
    }
    protected byte[] engineGetIV() {
        return (iv == null? null:(byte[]) iv.clone());
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
        throws InvalidKeyException {
        try {
            engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
        } catch (InvalidAlgorithmParameterException iape) {
            InvalidKeyException ike =
                new InvalidKeyException("Parameters required");
            ike.initCause(iape);
            throw ike;
        }
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] currIv = null;
        if (opmode == Cipher.WRAP_MODE) {
            decrypting = false;
            if (params == null) {
                iv = new byte[8];
                if (random == null) {
                    random = SunJCE.RANDOM;
                }
                random.nextBytes(iv);
            }
            else if (params instanceof IvParameterSpec) {
                iv = ((IvParameterSpec) params).getIV();
            } else {
                throw new InvalidAlgorithmParameterException
                    ("Wrong parameter type: IV expected");
            }
            currIv = iv;
        } else if (opmode == Cipher.UNWRAP_MODE) {
            if (params != null) {
                throw new InvalidAlgorithmParameterException
                    ("No parameter accepted for unwrapping keys");
            }
            iv = null;
            decrypting = true;
            currIv = IV2;
        } else {
            throw new UnsupportedOperationException("This cipher can " +
                "only be used for key wrapping and unwrapping");
        }
        cipher.init(decrypting, key.getAlgorithm(), key.getEncoded(),
                    currIv);
        cipherKey = key;
    }
    protected void engineInit(int opmode, Key key,
                              AlgorithmParameters params,
                              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = null;
        if (params != null) {
            try {
                DESedeParameters paramsEng = new DESedeParameters();
                paramsEng.engineInit(params.getEncoded());
                ivSpec = (IvParameterSpec)
                    paramsEng.engineGetParameterSpec(IvParameterSpec.class);
            } catch (Exception ex) {
                InvalidAlgorithmParameterException iape =
                    new InvalidAlgorithmParameterException
                        ("Wrong parameter type: IV expected");
                iape.initCause(ex);
                throw iape;
            }
        }
        engineInit(opmode, key, ivSpec, random);
    }
    protected byte[] engineUpdate(byte[] in, int inOffset, int inLen) {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected int engineUpdate(byte[] in, int inOffset, int inLen,
                               byte[] out, int outOffset)
        throws ShortBufferException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected byte[] engineDoFinal(byte[] in, int inOffset, int inLen)
        throws IllegalBlockSizeException, BadPaddingException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen,
                                byte[] output, int outputOffset)
        throws IllegalBlockSizeException, ShortBufferException,
               BadPaddingException {
        throw new IllegalStateException("Cipher has not been initialized");
    }
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters params = null;
        if (iv != null) {
            String algo = cipherKey.getAlgorithm();
            try {
                params = AlgorithmParameters.getInstance(algo, "SunJCE");
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Cannot find " + algo +
                    " AlgorithmParameters implementation in SunJCE provider");
            } catch (NoSuchProviderException nspe) {
                throw new RuntimeException("Cannot find SunJCE provider");
            }
            try {
                params.init(new IvParameterSpec(iv));
            } catch (InvalidParameterSpecException ipse) {
                throw new RuntimeException("IvParameterSpec not supported");
            }
        }
        return params;
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        if (encoded.length != 24) {
            throw new InvalidKeyException("Invalid key length: " +
                encoded.length + " bytes");
        }
        return 112;
    }
    protected byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException {
        byte[] keyVal = key.getEncoded();
        if ((keyVal == null) || (keyVal.length == 0)) {
            throw new InvalidKeyException("Cannot get an encoding of " +
                                          "the key to be wrapped");
        }
        byte[] cks = getChecksum(keyVal);
        byte[] out = new byte[iv.length + keyVal.length + cks.length];
        System.arraycopy(keyVal, 0, out, iv.length, keyVal.length);
        System.arraycopy(cks, 0, out, iv.length+keyVal.length, cks.length);
        cipher.encrypt(out, iv.length, keyVal.length+cks.length,
                       out, iv.length);
        System.arraycopy(iv, 0, out, 0, iv.length);
        for (int i = 0; i < out.length/2; i++) {
            byte temp = out[i];
            out[i] = out[out.length-1-i];
            out[out.length-1-i] = temp;
        }
        try {
            cipher.init(false, cipherKey.getAlgorithm(),
                        cipherKey.getEncoded(), IV2);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException("Internal cipher key is corrupted");
        }
        cipher.encrypt(out, 0, out.length, out, 0);
        try {
            cipher.init(decrypting, cipherKey.getAlgorithm(),
                        cipherKey.getEncoded(), iv);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException("Internal cipher key is corrupted");
        }
        return out;
    }
    protected Key engineUnwrap(byte[] wrappedKey,
                               String wrappedKeyAlgorithm,
                               int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException {
        if (wrappedKey.length == 0) {
            throw new InvalidKeyException("The wrapped key is empty");
        }
        byte[] buffer = new byte[wrappedKey.length];
        cipher.decrypt(wrappedKey, 0, wrappedKey.length, buffer, 0);
        for (int i = 0; i < buffer.length/2; i++) {
            byte temp = buffer[i];
            buffer[i] = buffer[buffer.length-1-i];
            buffer[buffer.length-1-i] = temp;
        }
        iv = new byte[IV2.length];
        System.arraycopy(buffer, 0, iv, 0, iv.length);
        cipher.init(true, cipherKey.getAlgorithm(), cipherKey.getEncoded(),
                    iv);
        cipher.decrypt(buffer, iv.length, buffer.length-iv.length,
                       buffer, iv.length);
        int origLen = buffer.length - iv.length - 8;
        byte[] cks = getChecksum(buffer, iv.length, origLen);
        int offset = iv.length + origLen;
        for (int i = 0; i < cks.length; i++) {
            if (buffer[offset + i] != cks[i]) {
                throw new InvalidKeyException("Checksum comparison failed");
            }
        }
        cipher.init(decrypting, cipherKey.getAlgorithm(),
                    cipherKey.getEncoded(), IV2);
        byte[] out = new byte[origLen];
        System.arraycopy(buffer, iv.length, out, 0, out.length);
        return ConstructKeys.constructKey(out, wrappedKeyAlgorithm,
                                          wrappedKeyType);
    }
    private static final byte[] getChecksum(byte[] in) {
        return getChecksum(in, 0, in.length);
    }
    private static final byte[] getChecksum(byte[] in, int offset, int len) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("SHA1 message digest not available");
        }
        md.update(in, offset, len);
        byte[] cks = new byte[8];
        System.arraycopy(md.digest(), 0, cks, 0, cks.length);
        return cks;
    }
}
