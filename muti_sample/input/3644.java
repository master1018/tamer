public final class RSACipher extends CipherSpi {
    private final static byte[] B0 = new byte[0];
    private final static int MODE_ENCRYPT = 1;
    private final static int MODE_DECRYPT = 2;
    private final static int MODE_SIGN    = 3;
    private final static int MODE_VERIFY  = 4;
    private final static String PAD_NONE  = "NoPadding";
    private final static String PAD_PKCS1 = "PKCS1Padding";
    private final static String PAD_OAEP_MGF1  = "OAEP";
    private int mode;
    private String paddingType;
    private RSAPadding padding;
    private OAEPParameterSpec spec = null;
    private byte[] buffer;
    private int bufOfs;
    private int outputSize;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private String oaepHashAlgorithm = "SHA-1";
    public RSACipher() {
        paddingType = PAD_PKCS1;
    }
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (mode.equalsIgnoreCase("ECB") == false) {
            throw new NoSuchAlgorithmException("Unsupported mode " + mode);
        }
    }
    protected void engineSetPadding(String paddingName)
            throws NoSuchPaddingException {
        if (paddingName.equalsIgnoreCase(PAD_NONE)) {
            paddingType = PAD_NONE;
        } else if (paddingName.equalsIgnoreCase(PAD_PKCS1)) {
            paddingType = PAD_PKCS1;
        } else {
            String lowerPadding = paddingName.toLowerCase(Locale.ENGLISH);
            if (lowerPadding.equals("oaeppadding")) {
                paddingType = PAD_OAEP_MGF1;
            } else if (lowerPadding.startsWith("oaepwith") &&
                       lowerPadding.endsWith("andmgf1padding")) {
                paddingType = PAD_OAEP_MGF1;
                oaepHashAlgorithm =
                        paddingName.substring(8, paddingName.length() - 14);
                if (Providers.getProviderList().getService
                        ("MessageDigest", oaepHashAlgorithm) == null) {
                    throw new NoSuchPaddingException
                        ("MessageDigest not available for " + paddingName);
                }
            } else {
                throw new NoSuchPaddingException
                    ("Padding " + paddingName + " not supported");
            }
        }
    }
    protected int engineGetBlockSize() {
        return 0;
    }
    protected int engineGetOutputSize(int inputLen) {
        return outputSize;
    }
    protected byte[] engineGetIV() {
        return null;
    }
    protected AlgorithmParameters engineGetParameters() {
        if (spec != null) {
            try {
                AlgorithmParameters params =
                    AlgorithmParameters.getInstance("OAEP", "SunJCE");
                params.init(spec);
                return params;
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Cannot find OAEP " +
                    " AlgorithmParameters implementation in SunJCE provider");
            } catch (NoSuchProviderException nspe) {
                throw new RuntimeException("Cannot find SunJCE provider");
            } catch (InvalidParameterSpecException ipse) {
                throw new RuntimeException("OAEPParameterSpec not supported");
            }
        } else {
            return null;
        }
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        try {
            init(opmode, key, random, null);
        } catch (InvalidAlgorithmParameterException iape) {
            InvalidKeyException ike =
                new InvalidKeyException("Wrong parameters");
            ike.initCause(iape);
            throw ike;
        }
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        init(opmode, key, random, params);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params == null) {
            init(opmode, key, random, null);
        } else {
            try {
                OAEPParameterSpec spec = (OAEPParameterSpec)
                    params.getParameterSpec(OAEPParameterSpec.class);
                init(opmode, key, random, spec);
            } catch (InvalidParameterSpecException ipse) {
                InvalidAlgorithmParameterException iape =
                    new InvalidAlgorithmParameterException("Wrong parameter");
                iape.initCause(ipse);
                throw iape;
            }
        }
    }
    private void init(int opmode, Key key, SecureRandom random,
            AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        boolean encrypt;
        switch (opmode) {
        case Cipher.ENCRYPT_MODE:
        case Cipher.WRAP_MODE:
            encrypt = true;
            break;
        case Cipher.DECRYPT_MODE:
        case Cipher.UNWRAP_MODE:
            encrypt = false;
            break;
        default:
            throw new InvalidKeyException("Unknown mode: " + opmode);
        }
        RSAKey rsaKey = RSAKeyFactory.toRSAKey(key);
        if (key instanceof RSAPublicKey) {
            mode = encrypt ? MODE_ENCRYPT : MODE_VERIFY;
            publicKey = (RSAPublicKey)key;
            privateKey = null;
        } else { 
            mode = encrypt ? MODE_SIGN : MODE_DECRYPT;
            privateKey = (RSAPrivateKey)key;
            publicKey = null;
        }
        int n = RSACore.getByteLength(rsaKey.getModulus());
        outputSize = n;
        bufOfs = 0;
        if (paddingType == PAD_NONE) {
            if (params != null) {
                throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
            }
            padding = RSAPadding.getInstance(RSAPadding.PAD_NONE, n, random);
            buffer = new byte[n];
        } else if (paddingType == PAD_PKCS1) {
            if (params != null) {
                throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
            }
            int blockType = (mode <= MODE_DECRYPT) ? RSAPadding.PAD_BLOCKTYPE_2
                                                   : RSAPadding.PAD_BLOCKTYPE_1;
            padding = RSAPadding.getInstance(blockType, n, random);
            if (encrypt) {
                int k = padding.getMaxDataSize();
                buffer = new byte[k];
            } else {
                buffer = new byte[n];
            }
        } else { 
            if ((mode == MODE_SIGN) || (mode == MODE_VERIFY)) {
                throw new InvalidKeyException
                        ("OAEP cannot be used to sign or verify signatures");
            }
            OAEPParameterSpec myParams;
            if (params != null) {
                if (!(params instanceof OAEPParameterSpec)) {
                    throw new InvalidAlgorithmParameterException
                        ("Wrong Parameters for OAEP Padding");
                }
                myParams = (OAEPParameterSpec) params;
            } else {
                myParams = new OAEPParameterSpec(oaepHashAlgorithm, "MGF1",
                    MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            }
            padding = RSAPadding.getInstance(RSAPadding.PAD_OAEP_MGF1, n,
                random, myParams);
            if (encrypt) {
                int k = padding.getMaxDataSize();
                buffer = new byte[k];
            } else {
                buffer = new byte[n];
            }
        }
    }
    private void update(byte[] in, int inOfs, int inLen) {
        if ((inLen == 0) || (in == null)) {
            return;
        }
        if (bufOfs + inLen > buffer.length) {
            bufOfs = buffer.length + 1;
            return;
        }
        System.arraycopy(in, inOfs, buffer, bufOfs, inLen);
        bufOfs += inLen;
    }
    private byte[] doFinal() throws BadPaddingException,
            IllegalBlockSizeException {
        if (bufOfs > buffer.length) {
            throw new IllegalBlockSizeException("Data must not be longer "
                + "than " + buffer.length + " bytes");
        }
        try {
            byte[] data;
            switch (mode) {
            case MODE_SIGN:
                data = padding.pad(buffer, 0, bufOfs);
                return RSACore.rsa(data, privateKey);
            case MODE_VERIFY:
                byte[] verifyBuffer = RSACore.convert(buffer, 0, bufOfs);
                data = RSACore.rsa(verifyBuffer, publicKey);
                return padding.unpad(data);
            case MODE_ENCRYPT:
                data = padding.pad(buffer, 0, bufOfs);
                return RSACore.rsa(data, publicKey);
            case MODE_DECRYPT:
                byte[] decryptBuffer = RSACore.convert(buffer, 0, bufOfs);
                data = RSACore.rsa(decryptBuffer, privateKey);
                return padding.unpad(data);
            default:
                throw new AssertionError("Internal error");
            }
        } finally {
            bufOfs = 0;
        }
    }
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        update(in, inOfs, inLen);
        return B0;
    }
    protected int engineUpdate(byte[] in, int inOfs, int inLen, byte[] out,
            int outOfs) {
        update(in, inOfs, inLen);
        return 0;
    }
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen)
            throws BadPaddingException, IllegalBlockSizeException {
        update(in, inOfs, inLen);
        return doFinal();
    }
    protected int engineDoFinal(byte[] in, int inOfs, int inLen, byte[] out,
            int outOfs) throws ShortBufferException, BadPaddingException,
            IllegalBlockSizeException {
        if (outputSize > out.length - outOfs) {
            throw new ShortBufferException
                ("Need " + outputSize + " bytes for output");
        }
        update(in, inOfs, inLen);
        byte[] result = doFinal();
        int n = result.length;
        System.arraycopy(result, 0, out, outOfs, n);
        return n;
    }
    protected byte[] engineWrap(Key key) throws InvalidKeyException,
            IllegalBlockSizeException {
        byte[] encoded = key.getEncoded();
        if ((encoded == null) || (encoded.length == 0)) {
            throw new InvalidKeyException("Could not obtain encoded key");
        }
        if (encoded.length > buffer.length) {
            throw new InvalidKeyException("Key is too long for wrapping");
        }
        update(encoded, 0, encoded.length);
        try {
            return doFinal();
        } catch (BadPaddingException e) {
            throw new InvalidKeyException("Wrapping failed", e);
        }
    }
    protected Key engineUnwrap(byte[] wrappedKey, String algorithm,
            int type) throws InvalidKeyException, NoSuchAlgorithmException {
        if (wrappedKey.length > buffer.length) {
            throw new InvalidKeyException("Key is too long for unwrapping");
        }
        update(wrappedKey, 0, wrappedKey.length);
        try {
            byte[] encoded = doFinal();
            return ConstructKeys.constructKey(encoded, algorithm, type);
        } catch (BadPaddingException e) {
            throw new InvalidKeyException("Unwrapping failed", e);
        } catch (IllegalBlockSizeException e) {
            throw new InvalidKeyException("Unwrapping failed", e);
        }
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        RSAKey rsaKey = RSAKeyFactory.toRSAKey(key);
        return rsaKey.getModulus().bitLength();
    }
}
