final class CipherCore {
    private byte[] buffer = null;
    private int blockSize = 0;
    private int unitBytes = 0;
    private int buffered = 0;
    private int minBytes = 0;
    private int diffBlocksize = 0;
    private Padding padding = null;
    private FeedbackCipher cipher = null;
    private int cipherMode = ECB_MODE;
    private boolean decrypting = false;
    private static final int ECB_MODE = 0;
    private static final int CBC_MODE = 1;
    private static final int CFB_MODE = 2;
    private static final int OFB_MODE = 3;
    private static final int PCBC_MODE = 4;
    private static final int CTR_MODE = 5;
    private static final int CTS_MODE = 6;
    CipherCore(SymmetricCipher impl, int blkSize) {
        blockSize = blkSize;
        unitBytes = blkSize;
        diffBlocksize = blkSize;
        buffer = new byte[blockSize*2];
        cipher = new ElectronicCodeBook(impl);
        padding = new PKCS5Padding(blockSize);
    }
    void setMode(String mode) throws NoSuchAlgorithmException {
        if (mode == null)
            throw new NoSuchAlgorithmException("null mode");
        String modeUpperCase = mode.toUpperCase(Locale.ENGLISH);
        if (modeUpperCase.equals("ECB")) {
            return;
        }
        SymmetricCipher rawImpl = cipher.getEmbeddedCipher();
        if (modeUpperCase.equals("CBC")) {
            cipherMode = CBC_MODE;
            cipher = new CipherBlockChaining(rawImpl);
        }
        else if (modeUpperCase.equals("CTS")) {
            cipherMode = CTS_MODE;
            cipher = new CipherTextStealing(rawImpl);
            minBytes = blockSize+1;
            padding = null;
        }
        else if (modeUpperCase.equals("CTR")) {
            cipherMode = CTR_MODE;
            cipher = new CounterMode(rawImpl);
            unitBytes = 1;
            padding = null;
        }
        else if (modeUpperCase.startsWith("CFB")) {
            cipherMode = CFB_MODE;
            unitBytes = getNumOfUnit(mode, "CFB".length(), blockSize);
            cipher = new CipherFeedback(rawImpl, unitBytes);
        }
        else if (modeUpperCase.startsWith("OFB")) {
            cipherMode = OFB_MODE;
            unitBytes = getNumOfUnit(mode, "OFB".length(), blockSize);
            cipher = new OutputFeedback(rawImpl, unitBytes);
        }
        else if (modeUpperCase.equals("PCBC")) {
            cipherMode = PCBC_MODE;
            cipher = new PCBC(rawImpl);
        }
        else {
            throw new NoSuchAlgorithmException("Cipher mode: " + mode
                                               + " not found");
        }
    }
    private static int getNumOfUnit(String mode, int offset, int blockSize)
        throws NoSuchAlgorithmException {
        int result = blockSize; 
        if (mode.length() > offset) {
            int numInt;
            try {
                Integer num = Integer.valueOf(mode.substring(offset));
                numInt = num.intValue();
                result = numInt >> 3;
            } catch (NumberFormatException e) {
                throw new NoSuchAlgorithmException
                    ("Algorithm mode: " + mode + " not implemented");
            }
            if ((numInt % 8 != 0) || (result > blockSize)) {
                throw new NoSuchAlgorithmException
                    ("Invalid algorithm mode: " + mode);
            }
        }
        return result;
    }
    void setPadding(String paddingScheme)
        throws NoSuchPaddingException
    {
        if (paddingScheme == null) {
            throw new NoSuchPaddingException("null padding");
        }
        if (paddingScheme.equalsIgnoreCase("NoPadding")) {
            padding = null;
        } else if (paddingScheme.equalsIgnoreCase("ISO10126Padding")) {
            padding = new ISO10126Padding(blockSize);
        } else if (!paddingScheme.equalsIgnoreCase("PKCS5Padding")) {
            throw new NoSuchPaddingException("Padding: " + paddingScheme
                                             + " not implemented");
        }
        if ((padding != null) &&
            ((cipherMode == CTR_MODE) || (cipherMode == CTS_MODE))) {
            padding = null;
            throw new NoSuchPaddingException
                ((cipherMode == CTR_MODE? "CTR":"CTS") +
                 " mode must be used with NoPadding");
        }
    }
    int getOutputSize(int inputLen) {
        int totalLen = buffered + inputLen;
        if (padding == null)
            return totalLen;
        if (decrypting)
            return totalLen;
        if (unitBytes != blockSize) {
            if (totalLen < diffBlocksize)
                return diffBlocksize;
            else
                return (totalLen + blockSize -
                        ((totalLen - diffBlocksize) % blockSize));
        } else {
            return totalLen + padding.padLength(totalLen);
        }
    }
    byte[] getIV() {
        byte[] iv = cipher.getIV();
        return (iv == null) ? null : (byte[])iv.clone();
    }
    AlgorithmParameters getParameters(String algName) {
        AlgorithmParameters params = null;
        if (cipherMode == ECB_MODE) return null;
        byte[] iv = getIV();
        if (iv != null) {
            AlgorithmParameterSpec ivSpec;
            if (algName.equals("RC2")) {
                RC2Crypt rawImpl = (RC2Crypt) cipher.getEmbeddedCipher();
                ivSpec = new RC2ParameterSpec(rawImpl.getEffectiveKeyBits(),
                                              iv);
            } else {
                ivSpec = new IvParameterSpec(iv);
            }
            try {
                params = AlgorithmParameters.getInstance(algName, "SunJCE");
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("Cannot find " + algName +
                    " AlgorithmParameters implementation in SunJCE provider");
            } catch (NoSuchProviderException nspe) {
                throw new RuntimeException("Cannot find SunJCE provider");
            }
            try {
                params.init(ivSpec);
            } catch (InvalidParameterSpecException ipse) {
                throw new RuntimeException("IvParameterSpec not supported");
            }
        }
        return params;
    }
    void init(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        try {
            init(opmode, key, (AlgorithmParameterSpec)null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }
    void init(int opmode, Key key, AlgorithmParameterSpec params,
            SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        decrypting = (opmode == Cipher.DECRYPT_MODE)
                  || (opmode == Cipher.UNWRAP_MODE);
        byte[] keyBytes = getKeyBytes(key);
        byte[] ivBytes;
        if (params == null) {
            ivBytes = null;
        } else if (params instanceof IvParameterSpec) {
            ivBytes = ((IvParameterSpec)params).getIV();
            if ((ivBytes == null) || (ivBytes.length != blockSize)) {
                throw new InvalidAlgorithmParameterException
                    ("Wrong IV length: must be " + blockSize +
                    " bytes long");
            }
        } else if (params instanceof RC2ParameterSpec) {
            ivBytes = ((RC2ParameterSpec)params).getIV();
            if ((ivBytes != null) && (ivBytes.length != blockSize)) {
                throw new InvalidAlgorithmParameterException
                    ("Wrong IV length: must be " + blockSize +
                    " bytes long");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Wrong parameter "
                                                         + "type: IV "
                                                         + "expected");
        }
        if (cipherMode == ECB_MODE) {
            if (ivBytes != null) {
                throw new InvalidAlgorithmParameterException
                                                ("ECB mode cannot use IV");
            }
        } else if (ivBytes == null) {
            if (decrypting) {
                throw new InvalidAlgorithmParameterException("Parameters "
                                                             + "missing");
            }
            if (random == null) {
                random = SunJCE.RANDOM;
            }
            ivBytes = new byte[blockSize];
            random.nextBytes(ivBytes);
        }
        buffered = 0;
        diffBlocksize = blockSize;
        String algorithm = key.getAlgorithm();
        cipher.init(decrypting, algorithm, keyBytes, ivBytes);
    }
    void init(int opmode, Key key, AlgorithmParameters params,
              SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = null;
        if (params != null) {
            try {
                ivSpec = (IvParameterSpec)params.getParameterSpec
                    (IvParameterSpec.class);
            } catch (InvalidParameterSpecException ipse) {
                throw new InvalidAlgorithmParameterException("Wrong parameter "
                                                             + "type: IV "
                                                             + "expected");
            }
        }
        init(opmode, key, ivSpec, random);
    }
    static byte[] getKeyBytes(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("No key given");
        }
        if (!"RAW".equalsIgnoreCase(key.getFormat())) {
            throw new InvalidKeyException("Wrong format: RAW bytes needed");
        }
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null) {
            throw new InvalidKeyException("RAW key bytes missing");
        }
        return keyBytes;
    }
    byte[] update(byte[] input, int inputOffset, int inputLen) {
        byte[] output = null;
        byte[] out = null;
        try {
            output = new byte[getOutputSize(inputLen)];
            int len = update(input, inputOffset, inputLen, output,
                             0);
            if (len == output.length) {
                out = output;
            } else {
                out = new byte[len];
                System.arraycopy(output, 0, out, 0, len);
            }
        } catch (ShortBufferException e) {
        }
        return out;
    }
    int update(byte[] input, int inputOffset, int inputLen, byte[] output,
               int outputOffset) throws ShortBufferException {
        int len = buffered + inputLen - minBytes;
        if (padding != null && decrypting) {
            len -= blockSize;
        }
        len = (len > 0 ? (len - (len%unitBytes)) : 0);
        if ((output == null) || ((output.length - outputOffset) < len)) {
            throw new ShortBufferException("Output buffer must be "
                                           + "(at least) " + len
                                           + " bytes long");
        }
        if (len != 0) {
            byte[] in = new byte[len];
            int inputConsumed = len - buffered;
            int bufferedConsumed = buffered;
            if (inputConsumed < 0) {
                inputConsumed = 0;
                bufferedConsumed = len;
            }
            if (buffered != 0) {
                System.arraycopy(buffer, 0, in, 0, bufferedConsumed);
            }
            if (inputConsumed > 0) {
                System.arraycopy(input, inputOffset, in,
                                 bufferedConsumed, inputConsumed);
            }
            if (decrypting) {
                cipher.decrypt(in, 0, len, output, outputOffset);
            } else {
                cipher.encrypt(in, 0, len, output, outputOffset);
            }
            if (unitBytes != blockSize) {
                if (len < diffBlocksize)
                    diffBlocksize -= len;
                else
                    diffBlocksize = blockSize -
                        ((len - diffBlocksize) % blockSize);
            }
            inputLen -= inputConsumed;
            inputOffset += inputConsumed;
            outputOffset += len;
            buffered -= bufferedConsumed;
            if (buffered > 0) {
                System.arraycopy(buffer, bufferedConsumed, buffer, 0,
                                 buffered);
            }
        }
        if (inputLen > 0) {
            System.arraycopy(input, inputOffset, buffer, buffered,
                             inputLen);
        }
        buffered += inputLen;
        return len;
    }
    byte[] doFinal(byte[] input, int inputOffset, int inputLen)
        throws IllegalBlockSizeException, BadPaddingException {
        byte[] output = null;
        byte[] out = null;
        try {
            output = new byte[getOutputSize(inputLen)];
            int len = doFinal(input, inputOffset, inputLen, output, 0);
            if (len < output.length) {
                out = new byte[len];
                if (len != 0)
                    System.arraycopy(output, 0, out, 0, len);
            } else {
                out = output;
            }
        } catch (ShortBufferException e) {
        }
        return out;
    }
    int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output,
                int outputOffset)
        throws IllegalBlockSizeException, ShortBufferException,
               BadPaddingException {
        int totalLen = buffered + inputLen;
        int paddedLen = totalLen;
        int paddingLen = 0;
        if (unitBytes != blockSize) {
            if (totalLen < diffBlocksize) {
                paddingLen = diffBlocksize - totalLen;
            } else {
                paddingLen = blockSize -
                    ((totalLen - diffBlocksize) % blockSize);
            }
        } else if (padding != null) {
            paddingLen = padding.padLength(totalLen);
        }
        if ((paddingLen > 0) && (paddingLen != blockSize) &&
            (padding != null) && decrypting) {
            throw new IllegalBlockSizeException
                ("Input length must be multiple of " + blockSize +
                 " when decrypting with padded cipher");
        }
        if (!decrypting && padding != null)
            paddedLen += paddingLen;
        if (output == null) {
            throw new ShortBufferException("Output buffer is null");
        }
        int outputCapacity = output.length - outputOffset;
        if (((!decrypting) || (padding == null)) &&
            (outputCapacity < paddedLen) ||
            (decrypting && (outputCapacity < (paddedLen - blockSize)))) {
            throw new ShortBufferException("Output buffer too short: "
                                           + outputCapacity + " bytes given, "
                                           + paddedLen + " bytes needed");
        }
        byte[] finalBuf = input;
        int finalOffset = inputOffset;
        if ((buffered != 0) || (!decrypting && padding != null)) {
            finalOffset = 0;
            finalBuf = new byte[paddedLen];
            if (buffered != 0) {
                System.arraycopy(buffer, 0, finalBuf, 0, buffered);
            }
            if (inputLen != 0) {
                System.arraycopy(input, inputOffset, finalBuf,
                                 buffered, inputLen);
            }
            if (!decrypting && padding != null) {
                padding.padWithLen(finalBuf, totalLen, paddingLen);
            }
        }
        if (decrypting) {
            if (outputCapacity < paddedLen) {
                cipher.save();
            }
            byte[] outWithPadding = new byte[totalLen];
            totalLen = finalNoPadding(finalBuf, finalOffset, outWithPadding,
                                      0, totalLen);
            if (padding != null) {
                int padStart = padding.unpad(outWithPadding, 0, totalLen);
                if (padStart < 0) {
                    throw new BadPaddingException("Given final block not "
                                                  + "properly padded");
                }
                totalLen = padStart;
            }
            if ((output.length - outputOffset) < totalLen) {
                cipher.restore();
                throw new ShortBufferException("Output buffer too short: "
                                               + (output.length-outputOffset)
                                               + " bytes given, " + totalLen
                                               + " bytes needed");
            }
            for (int i = 0; i < totalLen; i++) {
                output[outputOffset + i] = outWithPadding[i];
            }
        } else { 
            totalLen = finalNoPadding(finalBuf, finalOffset, output,
                                      outputOffset, paddedLen);
        }
        buffered = 0;
        diffBlocksize = blockSize;
        if (cipherMode != ECB_MODE) {
            ((FeedbackCipher)cipher).reset();
        }
        return totalLen;
    }
    private int finalNoPadding(byte[] in, int inOff, byte[] out, int outOff,
                               int len)
        throws IllegalBlockSizeException
    {
        if (in == null || len == 0)
            return 0;
        if ((cipherMode != CFB_MODE) && (cipherMode != OFB_MODE)
            && ((len % unitBytes) != 0) && (cipherMode != CTS_MODE)) {
            if (padding != null) {
                throw new IllegalBlockSizeException
                    ("Input length (with padding) not multiple of " +
                     unitBytes + " bytes");
            } else {
                throw new IllegalBlockSizeException
                    ("Input length not multiple of " + unitBytes
                     + " bytes");
            }
        }
        if (decrypting) {
            cipher.decryptFinal(in, inOff, len, out, outOff);
        } else {
            cipher.encryptFinal(in, inOff, len, out, outOff);
        }
        return len;
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
    Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm,
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
