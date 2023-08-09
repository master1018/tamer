public final class RSAPadding {
    public final static int PAD_BLOCKTYPE_1    = 1;
    public final static int PAD_BLOCKTYPE_2    = 2;
    public final static int PAD_NONE           = 3;
    public final static int PAD_OAEP_MGF1 = 4;
    private final int type;
    private final int paddedSize;
    private SecureRandom random;
    private final int maxDataSize;
    private MessageDigest md;
    private MessageDigest mgfMd;
    private byte[] lHash;
    public static RSAPadding getInstance(int type, int paddedSize)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, null, null);
    }
    public static RSAPadding getInstance(int type, int paddedSize,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, random, null);
    }
    public static RSAPadding getInstance(int type, int paddedSize,
            SecureRandom random, OAEPParameterSpec spec)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        return new RSAPadding(type, paddedSize, random, spec);
    }
    private RSAPadding(int type, int paddedSize, SecureRandom random,
            OAEPParameterSpec spec) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        this.type = type;
        this.paddedSize = paddedSize;
        this.random = random;
        if (paddedSize < 64) {
            throw new InvalidKeyException("Padded size must be at least 64");
        }
        switch (type) {
        case PAD_BLOCKTYPE_1:
        case PAD_BLOCKTYPE_2:
            maxDataSize = paddedSize - 11;
            break;
        case PAD_NONE:
            maxDataSize = paddedSize;
            break;
        case PAD_OAEP_MGF1:
            String mdName = "SHA-1";
            String mgfMdName = "SHA-1";
            byte[] digestInput = null;
            try {
                if (spec != null) {
                    mdName = spec.getDigestAlgorithm();
                    String mgfName = spec.getMGFAlgorithm();
                    if (!mgfName.equalsIgnoreCase("MGF1")) {
                        throw new InvalidAlgorithmParameterException
                            ("Unsupported MGF algo: " + mgfName);
                    }
                    mgfMdName = ((MGF1ParameterSpec)spec.getMGFParameters()).getDigestAlgorithm();
                    PSource pSrc = spec.getPSource();
                    String pSrcAlgo = pSrc.getAlgorithm();
                    if (!pSrcAlgo.equalsIgnoreCase("PSpecified")) {
                        throw new InvalidAlgorithmParameterException
                            ("Unsupported pSource algo: " + pSrcAlgo);
                    }
                    digestInput = ((PSource.PSpecified) pSrc).getValue();
                }
                md = MessageDigest.getInstance(mdName);
                mgfMd = MessageDigest.getInstance(mgfMdName);
            } catch (NoSuchAlgorithmException e) {
                throw new InvalidKeyException
                        ("Digest " + mdName + " not available", e);
            }
            lHash = getInitialHash(md, digestInput);
            int digestLen = lHash.length;
            maxDataSize = paddedSize - 2 - 2 * digestLen;
            if (maxDataSize <= 0) {
                throw new InvalidKeyException
                        ("Key is too short for encryption using OAEPPadding" +
                         " with " + mdName + " and MGF1" + mgfMdName);
            }
            break;
        default:
            throw new InvalidKeyException("Invalid padding: " + type);
        }
    }
    private static final Map<String,byte[]> emptyHashes =
        Collections.synchronizedMap(new HashMap<String,byte[]>());
    private static byte[] getInitialHash(MessageDigest md,
        byte[] digestInput) {
        byte[] result = null;
        if ((digestInput == null) || (digestInput.length == 0)) {
            String digestName = md.getAlgorithm();
            result = emptyHashes.get(digestName);
            if (result == null) {
                result = md.digest();
                emptyHashes.put(digestName, result);
            }
        } else {
            result = md.digest(digestInput);
        }
        return result;
    }
    public int getMaxDataSize() {
        return maxDataSize;
    }
    public byte[] pad(byte[] data, int ofs, int len)
            throws BadPaddingException {
        return pad(RSACore.convert(data, ofs, len));
    }
    public byte[] pad(byte[] data) throws BadPaddingException {
        if (data.length > maxDataSize) {
            throw new BadPaddingException("Data must be shorter than "
                + (maxDataSize + 1) + " bytes");
        }
        switch (type) {
        case PAD_NONE:
            return data;
        case PAD_BLOCKTYPE_1:
        case PAD_BLOCKTYPE_2:
            return padV15(data);
        case PAD_OAEP_MGF1:
            return padOAEP(data);
        default:
            throw new AssertionError();
        }
    }
    public byte[] unpad(byte[] padded, int ofs, int len)
            throws BadPaddingException {
        return unpad(RSACore.convert(padded, ofs, len));
    }
    public byte[] unpad(byte[] padded) throws BadPaddingException {
        if (padded.length != paddedSize) {
            throw new BadPaddingException("Padded length must be " + paddedSize);
        }
        switch (type) {
        case PAD_NONE:
            return padded;
        case PAD_BLOCKTYPE_1:
        case PAD_BLOCKTYPE_2:
            return unpadV15(padded);
        case PAD_OAEP_MGF1:
            return unpadOAEP(padded);
        default:
            throw new AssertionError();
        }
    }
    private byte[] padV15(byte[] data) throws BadPaddingException {
        byte[] padded = new byte[paddedSize];
        System.arraycopy(data, 0, padded, paddedSize - data.length, data.length);
        int psSize = paddedSize - 3 - data.length;
        int k = 0;
        padded[k++] = 0;
        padded[k++] = (byte)type;
        if (type == PAD_BLOCKTYPE_1) {
            while (psSize-- > 0) {
                padded[k++] = (byte)0xff;
            }
        } else {
            if (random == null) {
                random = JCAUtil.getSecureRandom();
            }
            byte[] r = new byte[64];
            int i = -1;
            while (psSize-- > 0) {
                int b;
                do {
                    if (i < 0) {
                        random.nextBytes(r);
                        i = r.length - 1;
                    }
                    b = r[i--] & 0xff;
                } while (b == 0);
                padded[k++] = (byte)b;
            }
        }
        return padded;
    }
    private byte[] unpadV15(byte[] padded) throws BadPaddingException {
        int k = 0;
        if (padded[k++] != 0) {
            throw new BadPaddingException("Data must start with zero");
        }
        if (padded[k++] != type) {
            throw new BadPaddingException("Blocktype mismatch: " + padded[1]);
        }
        while (true) {
            int b = padded[k++] & 0xff;
            if (b == 0) {
                break;
            }
            if (k == padded.length) {
                throw new BadPaddingException("Padding string not terminated");
            }
            if ((type == PAD_BLOCKTYPE_1) && (b != 0xff)) {
                throw new BadPaddingException("Padding byte not 0xff: " + b);
            }
        }
        int n = padded.length - k;
        if (n > maxDataSize) {
            throw new BadPaddingException("Padding string too short");
        }
        byte[] data = new byte[n];
        System.arraycopy(padded, padded.length - n, data, 0, n);
        return data;
    }
    private byte[] padOAEP(byte[] M) throws BadPaddingException {
        if (random == null) {
            random = JCAUtil.getSecureRandom();
        }
        int hLen = lHash.length;
        byte[] seed = new byte[hLen];
        random.nextBytes(seed);
        byte[] EM = new byte[paddedSize];
        int seedStart = 1;
        int seedLen = hLen;
        System.arraycopy(seed, 0, EM, seedStart, seedLen);
        int dbStart = hLen + 1;
        int dbLen = EM.length - dbStart;
        int mStart = paddedSize - M.length;
        System.arraycopy(lHash, 0, EM, dbStart, hLen);
        EM[mStart - 1] = 1;
        System.arraycopy(M, 0, EM, mStart, M.length);
        mgf1(EM, seedStart, seedLen, EM, dbStart, dbLen);
        mgf1(EM, dbStart, dbLen, EM, seedStart, seedLen);
        return EM;
    }
    private byte[] unpadOAEP(byte[] padded) throws BadPaddingException {
        byte[] EM = padded;
        int hLen = lHash.length;
        if (EM[0] != 0) {
            throw new BadPaddingException("Data must start with zero");
        }
        int seedStart = 1;
        int seedLen = hLen;
        int dbStart = hLen + 1;
        int dbLen = EM.length - dbStart;
        mgf1(EM, dbStart, dbLen, EM, seedStart, seedLen);
        mgf1(EM, seedStart, seedLen, EM, dbStart, dbLen);
        for (int i = 0; i < hLen; i++) {
            if (lHash[i] != EM[dbStart + i]) {
                throw new BadPaddingException("lHash mismatch");
            }
        }
        int i = dbStart + hLen;
        while (EM[i] == 0) {
            i++;
            if (i >= EM.length) {
                throw new BadPaddingException("Padding string not terminated");
            }
        }
        if (EM[i++] != 1) {
            throw new BadPaddingException
                ("Padding string not terminated by 0x01 byte");
        }
        int mLen = EM.length - i;
        byte[] m = new byte[mLen];
        System.arraycopy(EM, i, m, 0, mLen);
        return m;
    }
    private void mgf1(byte[] seed, int seedOfs, int seedLen,
            byte[] out, int outOfs, int maskLen)  throws BadPaddingException {
        byte[] C = new byte[4]; 
        byte[] digest = new byte[20]; 
        while (maskLen > 0) {
            mgfMd.update(seed, seedOfs, seedLen);
            mgfMd.update(C);
            try {
                mgfMd.digest(digest, 0, digest.length);
            } catch (DigestException e) {
                throw new BadPaddingException(e.toString());
            }
            for (int i = 0; (i < digest.length) && (maskLen > 0); maskLen--) {
                out[outOfs++] ^= digest[i++];
            }
            if (maskLen > 0) {
                for (int i = C.length - 1; (++C[i] == 0) && (i > 0); i--) {
                }
            }
        }
    }
}
