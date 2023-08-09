public class AesDkCrypto extends DkCrypto {
    private static final boolean debug = false;
    private static final int BLOCK_SIZE = 16;
    private static final int DEFAULT_ITERATION_COUNT = 4096;
    private static final byte[] ZERO_IV = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0,
                                                       0, 0, 0, 0, 0, 0, 0, 0 };
    private static final int hashSize = 96/8;
    private final int keyLength;
    public AesDkCrypto(int length) {
        keyLength = length;
    }
    protected int getKeySeedLength() {
        return keyLength;   
    }
    public byte[] stringToKey(char[] password, String salt, byte[] s2kparams)
        throws GeneralSecurityException {
        byte[] saltUtf8 = null;
        try {
            saltUtf8 = salt.getBytes("UTF-8");
            return stringToKey(password, saltUtf8, s2kparams);
        } catch (Exception e) {
            return null;
        } finally {
            if (saltUtf8 != null) {
                Arrays.fill(saltUtf8, (byte)0);
            }
        }
    }
    private byte[] stringToKey(char[] secret, byte[] salt, byte[] params)
        throws GeneralSecurityException {
        int iter_count = DEFAULT_ITERATION_COUNT;
        if (params != null) {
            if (params.length != 4) {
                throw new RuntimeException("Invalid parameter to stringToKey");
            }
            iter_count = readBigEndian(params, 0, 4);
        }
        byte[] tmpKey = randomToKey(PBKDF2(secret, salt, iter_count,
                                        getKeySeedLength()));
        byte[] result = dk(tmpKey, KERBEROS_CONSTANT);
        return result;
    }
    protected byte[] randomToKey(byte[] in) {
        return in;
    }
    protected Cipher getCipher(byte[] key, byte[] ivec, int mode)
        throws GeneralSecurityException {
        if (ivec == null) {
           ivec = ZERO_IV;
        }
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec encIv = new IvParameterSpec(ivec, 0, ivec.length);
        cipher.init(mode, secretKey, encIv);
        return cipher;
    }
    public int getChecksumLength() {
        return hashSize;  
    }
    protected byte[] getHmac(byte[] key, byte[] msg)
        throws GeneralSecurityException {
        SecretKey keyKi = new SecretKeySpec(key, "HMAC");
        Mac m = Mac.getInstance("HmacSHA1");
        m.init(keyKi);
        byte[] hash = m.doFinal(msg);
        byte[] output = new byte[hashSize];
        System.arraycopy(hash, 0, output, 0, hashSize);
        return output;
    }
    public byte[] calculateChecksum(byte[] baseKey, int usage, byte[] input,
        int start, int len) throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] constant = new byte[5];
        constant[0] = (byte) ((usage>>24)&0xff);
        constant[1] = (byte) ((usage>>16)&0xff);
        constant[2] = (byte) ((usage>>8)&0xff);
        constant[3] = (byte) (usage&0xff);
        constant[4] = (byte) 0x99;
        byte[] Kc = dk(baseKey, constant);  
        if (debug) {
            System.err.println("usage: " + usage);
            traceOutput("input", input, start, Math.min(len, 32));
            traceOutput("constant", constant, 0, constant.length);
            traceOutput("baseKey", baseKey, 0, baseKey.length);
            traceOutput("Kc", Kc, 0, Kc.length);
        }
        try {
            byte[] hmac = getHmac(Kc, input);
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
        } finally {
            Arrays.fill(Kc, 0, Kc.length, (byte)0);
        }
    }
    public byte[] encrypt(byte[] baseKey, int usage,
        byte[] ivec, byte[] new_ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                 + usage);
        }
        byte[] output = encryptCTS(baseKey, usage, ivec, new_ivec, plaintext,
                                        start, len, true);
        return output;
    }
    public byte[] encryptRaw(byte[] baseKey, int usage,
        byte[] ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] output = encryptCTS(baseKey, usage, ivec, null, plaintext,
                                        start, len, false);
        return output;
    }
    public byte[] decrypt(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len) throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] output = decryptCTS(baseKey, usage, ivec, ciphertext,
                                        start, len, true);
        return output;
    }
    public byte[] decryptRaw(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len)
        throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
        byte[] output = decryptCTS(baseKey, usage, ivec, ciphertext,
                                        start, len, false);
        return output;
    }
    private byte[] encryptCTS(byte[] baseKey, int usage, byte[] ivec,
        byte[] new_ivec, byte[] plaintext, int start, int len,
        boolean confounder_exists)
        throws GeneralSecurityException, KrbCryptoException {
        byte[] Ke = null;
        byte[] Ki = null;
        if (debug) {
            System.err.println("usage: " + usage);
            if (ivec != null) {
                traceOutput("old_state.ivec", ivec, 0, ivec.length);
            }
            traceOutput("plaintext", plaintext, start, Math.min(len, 32));
            traceOutput("baseKey", baseKey, 0, baseKey.length);
        }
        try {
            byte[] constant = new byte[5];
            constant[0] = (byte) ((usage>>24)&0xff);
            constant[1] = (byte) ((usage>>16)&0xff);
            constant[2] = (byte) ((usage>>8)&0xff);
            constant[3] = (byte) (usage&0xff);
            constant[4] = (byte) 0xaa;
            Ke = dk(baseKey, constant);  
            byte[] toBeEncrypted = null;
            if (confounder_exists) {
                byte[] confounder = Confounder.bytes(BLOCK_SIZE);
                toBeEncrypted = new byte[confounder.length + len];
                System.arraycopy(confounder, 0, toBeEncrypted,
                                        0, confounder.length);
                System.arraycopy(plaintext, start, toBeEncrypted,
                                        confounder.length, len);
            } else {
                toBeEncrypted = new byte[len];
                System.arraycopy(plaintext, start, toBeEncrypted, 0, len);
            }
            byte[] output = new byte[toBeEncrypted.length + hashSize];
            Cipher cipher = Cipher.getInstance("AES/CTS/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(Ke, "AES");
            IvParameterSpec encIv = new IvParameterSpec(ivec, 0, ivec.length);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, encIv);
            cipher.doFinal(toBeEncrypted, 0, toBeEncrypted.length, output);
            constant[4] = (byte) 0x55;
            Ki = dk(baseKey, constant);
            if (debug) {
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("Ki", Ki, 0, Ke.length);
            }
            byte[] hmac = getHmac(Ki, toBeEncrypted);
            System.arraycopy(hmac, 0, output, toBeEncrypted.length,
                                hmac.length);
            return output;
        } finally {
            if (Ke != null) {
                Arrays.fill(Ke, 0, Ke.length, (byte) 0);
            }
            if (Ki != null) {
                Arrays.fill(Ki, 0, Ki.length, (byte) 0);
            }
        }
    }
    private byte[] decryptCTS(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len, boolean confounder_exists)
        throws GeneralSecurityException {
        byte[] Ke = null;
        byte[] Ki = null;
        try {
            byte[] constant = new byte[5];
            constant[0] = (byte) ((usage>>24)&0xff);
            constant[1] = (byte) ((usage>>16)&0xff);
            constant[2] = (byte) ((usage>>8)&0xff);
            constant[3] = (byte) (usage&0xff);
            constant[4] = (byte) 0xaa;
            Ke = dk(baseKey, constant);  
            if (debug) {
                System.err.println("usage: " + usage);
                if (ivec != null) {
                    traceOutput("old_state.ivec", ivec, 0, ivec.length);
                }
                traceOutput("ciphertext", ciphertext, start, Math.min(len, 32));
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("baseKey", baseKey, 0, baseKey.length);
                traceOutput("Ke", Ke, 0, Ke.length);
            }
            Cipher cipher = Cipher.getInstance("AES/CTS/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(Ke, "AES");
            IvParameterSpec encIv = new IvParameterSpec(ivec, 0, ivec.length);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, encIv);
            byte[] plaintext = cipher.doFinal(ciphertext, start, len-hashSize);
            if (debug) {
                traceOutput("AES PlainText", plaintext, 0,
                                Math.min(plaintext.length, 32));
            }
            constant[4] = (byte) 0x55;
            Ki = dk(baseKey, constant);  
            if (debug) {
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("Ki", Ki, 0, Ke.length);
            }
            byte[] calculatedHmac = getHmac(Ki, plaintext);
            int hmacOffset = start + len - hashSize;
            if (debug) {
                traceOutput("calculated Hmac", calculatedHmac,
                                0, calculatedHmac.length);
                traceOutput("message Hmac", ciphertext, hmacOffset, hashSize);
            }
            boolean cksumFailed = false;
            if (calculatedHmac.length >= hashSize) {
                for (int i = 0; i < hashSize; i++) {
                    if (calculatedHmac[i] != ciphertext[hmacOffset+i]) {
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
            if (confounder_exists) {
                byte[] output = new byte[plaintext.length - BLOCK_SIZE];
                System.arraycopy(plaintext, BLOCK_SIZE, output,
                                        0, output.length);
                return output;
            } else {
                return plaintext;
            }
        } finally {
            if (Ke != null) {
                Arrays.fill(Ke, 0, Ke.length, (byte) 0);
            }
            if (Ki != null) {
                Arrays.fill(Ki, 0, Ki.length, (byte) 0);
            }
        }
    }
    private static byte[] PBKDF2(char[] secret, byte[] salt,
        int count, int keyLength) throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(secret, salt, count, keyLength);
        SecretKeyFactory skf =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey key = skf.generateSecret(keySpec);
        byte[] result = key.getEncoded();
        return result;
    }
    public static final int readBigEndian(byte[] data, int pos, int size) {
        int retVal = 0;
        int shifter = (size-1)*8;
        while (size > 0) {
            retVal += (data[pos] & 0xff) << shifter;
            shifter -= 8;
            pos++;
            size--;
        }
        return retVal;
    }
}
