public class ArcFourCrypto extends DkCrypto {
    private static final boolean debug = false;
    private static final int confounderSize = 8;
    private static final byte[] ZERO_IV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};
    private static final int hashSize = 16;
    private final int keyLength;
    public ArcFourCrypto(int length) {
        keyLength = length;
    }
    protected int getKeySeedLength() {
        return keyLength;   
    }
    protected byte[] randomToKey(byte[] in) {
        return in;
    }
    public byte[] stringToKey(char[] passwd)
        throws GeneralSecurityException {
        return stringToKey(passwd, null);
    }
    private byte[] stringToKey(char[] secret, byte[] opaque)
        throws GeneralSecurityException {
        if (opaque != null && opaque.length > 0) {
            throw new RuntimeException("Invalid parameter to stringToKey");
        }
        byte[] passwd = null;
        byte[] digest = null;
        try {
            passwd = charToUtf16(secret);
            MessageDigest md = sun.security.provider.MD4.getInstance();
            md.update(passwd);
            digest = md.digest();
        } catch (Exception e) {
            return null;
        } finally {
            if (passwd != null) {
                Arrays.fill(passwd, (byte)0);
            }
        }
        return digest;
    }
    protected Cipher getCipher(byte[] key, byte[] ivec, int mode)
        throws GeneralSecurityException {
        if (ivec == null) {
           ivec = ZERO_IV;
        }
        SecretKeySpec secretKey = new SecretKeySpec(key, "ARCFOUR");
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        IvParameterSpec encIv = new IvParameterSpec(ivec, 0, ivec.length);
        cipher.init(mode, secretKey, encIv);
        return cipher;
    }
    public int getChecksumLength() {
        return hashSize;  
    }
    protected byte[] getHmac(byte[] key, byte[] msg)
        throws GeneralSecurityException {
        SecretKey keyKi = new SecretKeySpec(key, "HmacMD5");
        Mac m = Mac.getInstance("HmacMD5");
        m.init(keyKi);
        byte[] hash = m.doFinal(msg);
        return hash;
    }
    public byte[] calculateChecksum(byte[] baseKey, int usage, byte[] input,
        int start, int len) throws GeneralSecurityException {
        if (debug) {
            System.out.println("ARCFOUR: calculateChecksum with usage = " +
                                                usage);
        }
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] Ksign = null;
        try {
           byte[] ss = "signaturekey".getBytes();
           byte[] new_ss = new byte[ss.length+1];
           System.arraycopy(ss, 0, new_ss, 0, ss.length);
           Ksign = getHmac(baseKey, new_ss);
        } catch (Exception e) {
            GeneralSecurityException gse =
                new GeneralSecurityException("Calculate Checkum Failed!");
            gse.initCause(e);
            throw gse;
        }
        byte[] salt = getSalt(usage);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            GeneralSecurityException gse =
                new GeneralSecurityException("Calculate Checkum Failed!");
            gse.initCause(e);
            throw gse;
        }
        messageDigest.update(salt);
        messageDigest.update(input, start, len);
        byte[] md5tmp = messageDigest.digest();
        byte[] hmac = getHmac(Ksign, md5tmp);
        if (debug) {
            traceOutput("hmac", hmac, 0, hmac.length);
        }
        if (hmac.length == getChecksumLength()) {
            return hmac;
        } else if (hmac.length > getChecksumLength()) {
            byte[] buf = new byte[getChecksumLength()];
            System.arraycopy(hmac, 0, buf, 0, buf.length);
            return buf;
        } else {
            throw new GeneralSecurityException("checksum size too short: " +
                        hmac.length + "; expecting : " + getChecksumLength());
        }
    }
    public byte[] encryptSeq(byte[] baseKey, int usage,
        byte[] checksum, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] salt = new byte[4];
        byte[] kSeq = getHmac(baseKey, salt);
        kSeq = getHmac(kSeq, checksum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(kSeq, "ARCFOUR");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] output = cipher.doFinal(plaintext, start, len);
        return output;
    }
    public byte[] decryptSeq(byte[] baseKey, int usage,
        byte[] checksum, byte[] ciphertext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] salt = new byte[4];
        byte[] kSeq = getHmac(baseKey, salt);
        kSeq = getHmac(kSeq, checksum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(kSeq, "ARCFOUR");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] output = cipher.doFinal(ciphertext, start, len);
        return output;
    }
    public byte[] encrypt(byte[] baseKey, int usage,
        byte[] ivec, byte[] new_ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                 + usage);
        }
        if (debug) {
            System.out.println("ArcFour: ENCRYPT with key usage = " + usage);
        }
        byte[] confounder = Confounder.bytes(confounderSize);
        int plainSize = roundup(confounder.length + len, 1);
        byte[] toBeEncrypted = new byte[plainSize];
        System.arraycopy(confounder, 0, toBeEncrypted, 0, confounder.length);
        System.arraycopy(plaintext, start, toBeEncrypted,
                                confounder.length, len);
        byte[] k1 = new byte[baseKey.length];
        System.arraycopy(baseKey, 0, k1, 0, baseKey.length);
        byte[] salt = getSalt(usage);
        byte[] k2 = getHmac(k1, salt);
        byte[] checksum = getHmac(k2, toBeEncrypted);
        byte[] k3 = getHmac(k2, checksum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(k3, "ARCFOUR");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] output = cipher.doFinal(toBeEncrypted, 0, toBeEncrypted.length);
        byte[] result = new byte[hashSize + output.length];
        System.arraycopy(checksum, 0, result, 0, hashSize);
        System.arraycopy(output, 0, result, hashSize, output.length);
        return result;
    }
    public byte[] encryptRaw(byte[] baseKey, int usage,
        byte[] seqNum, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        if (debug) {
            System.out.println("\nARCFOUR: encryptRaw with usage = " + usage);
        }
        byte[] klocal = new byte[baseKey.length];
        for (int i = 0; i <= 15; i++) {
            klocal[i] = (byte) (baseKey[i] ^ 0xF0);
        }
        byte[] salt = new byte[4];
        byte[] kcrypt = getHmac(klocal, salt);
        kcrypt = getHmac(kcrypt, seqNum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(kcrypt, "ARCFOUR");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] output = cipher.doFinal(plaintext, start, len);
        return output;
    }
    public byte[] decrypt(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len)
        throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        if (debug) {
            System.out.println("\nARCFOUR: DECRYPT using key usage = " + usage);
        }
        byte[] k1 = new byte[baseKey.length];
        System.arraycopy(baseKey, 0, k1, 0, baseKey.length);
        byte[] salt = getSalt(usage);
        byte[] k2 = getHmac(k1, salt);
        byte[] checksum = new byte[hashSize];
        System.arraycopy(ciphertext, start, checksum, 0, hashSize);
        byte[] k3 = getHmac(k2, checksum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(k3, "ARCFOUR");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] plaintext = cipher.doFinal(ciphertext, start+hashSize,
                                                len-hashSize);
        byte[] calculatedHmac = getHmac(k2, plaintext);
        if (debug) {
            traceOutput("calculated Hmac", calculatedHmac, 0,
                                calculatedHmac.length);
            traceOutput("message Hmac", ciphertext, 0,
                                hashSize);
        }
        boolean cksumFailed = false;
        if (calculatedHmac.length >= hashSize) {
            for (int i = 0; i < hashSize; i++) {
                if (calculatedHmac[i] != ciphertext[i]) {
                    cksumFailed = true;
                    if (debug) {
                        System.err.println("Checksum failed !");
                    }
                    break;
                }
            }
        }
        if (cksumFailed) {
            throw new GeneralSecurityException("Checksum failed");
        }
        byte[] output = new byte[plaintext.length - confounderSize];
        System.arraycopy(plaintext, confounderSize, output, 0, output.length);
        return output;
    }
    public byte[] decryptRaw(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len, byte[] seqNum)
        throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        if (debug) {
            System.out.println("\nARCFOUR: decryptRaw with usage = " + usage);
        }
        byte[] klocal = new byte[baseKey.length];
        for (int i = 0; i <= 15; i++) {
            klocal[i] = (byte) (baseKey[i] ^ 0xF0);
        }
        byte[] salt = new byte[4];
        byte[] kcrypt = getHmac(klocal, salt);
        byte[] sequenceNum = new byte[4];
        System.arraycopy(seqNum, 0, sequenceNum, 0, sequenceNum.length);
        kcrypt = getHmac(kcrypt, sequenceNum);
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        SecretKeySpec secretKey = new SecretKeySpec(kcrypt, "ARCFOUR");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] output = cipher.doFinal(ciphertext, start, len);
        return output;
    }
    private byte[] getSalt(int usage) {
        int ms_usage = arcfour_translate_usage(usage);
        byte[] salt = new byte[4];
        salt[0] = (byte)(ms_usage & 0xff);
        salt[1] = (byte)((ms_usage >> 8) & 0xff);
        salt[2] = (byte)((ms_usage >> 16) & 0xff);
        salt[3] = (byte)((ms_usage >> 24) & 0xff);
        return salt;
    }
    private int arcfour_translate_usage(int usage) {
        switch (usage) {
            case 3: return 8;
            case 9: return 8;
            case 23: return 13;
            default: return usage;
        }
    }
}
