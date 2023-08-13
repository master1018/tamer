public abstract class DkCrypto {
    protected static final boolean debug = false;
    static final byte[] KERBEROS_CONSTANT =
        {0x6b, 0x65, 0x72, 0x62, 0x65, 0x72, 0x6f, 0x73};
    protected abstract int getKeySeedLength();  
    protected abstract byte[] randomToKey(byte[] in);
    protected abstract Cipher getCipher(byte[] key, byte[] ivec, int mode)
        throws GeneralSecurityException;
    public abstract int getChecksumLength();  
    protected abstract byte[] getHmac(byte[] key, byte[] plaintext)
        throws GeneralSecurityException;
    public byte[] encrypt(byte[] baseKey, int usage,
        byte[] ivec, byte[] new_ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
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
                traceOutput("plaintext", plaintext, start, Math.min(len, 32));
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("baseKey", baseKey, 0, baseKey.length);
                traceOutput("Ke", Ke, 0, Ke.length);
            }
            Cipher encCipher = getCipher(Ke, ivec, Cipher.ENCRYPT_MODE);
            int blockSize = encCipher.getBlockSize();
            byte[] confounder = Confounder.bytes(blockSize);
            int plainSize = roundup(confounder.length + len, blockSize);
            if (debug) {
                System.err.println("confounder = " + confounder.length +
                    "; plaintext = " + len + "; padding = " +
                    (plainSize - confounder.length - len) + "; total = " +
                        plainSize);
                traceOutput("confounder", confounder, 0, confounder.length);
            }
            byte[] toBeEncrypted = new byte[plainSize];
            System.arraycopy(confounder, 0, toBeEncrypted,
                                0, confounder.length);
            System.arraycopy(plaintext, start, toBeEncrypted,
                                confounder.length, len);
            Arrays.fill(toBeEncrypted, confounder.length + len, plainSize,
                        (byte)0);
            int cipherSize = encCipher.getOutputSize(plainSize);
            int ccSize =  cipherSize + getChecksumLength();  
            byte[] ciphertext = new byte[ccSize];
            encCipher.doFinal(toBeEncrypted, 0, plainSize, ciphertext, 0);
            if (new_ivec != null && new_ivec.length == blockSize) {
                System.arraycopy(ciphertext,  cipherSize - blockSize,
                    new_ivec, 0, blockSize);
                if (debug) {
                    traceOutput("new_ivec", new_ivec, 0, new_ivec.length);
                }
            }
            constant[4] = (byte) 0x55;
            Ki = dk(baseKey, constant);
            if (debug) {
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("Ki", Ki, 0, Ke.length);
            }
            byte[] hmac = getHmac(Ki, toBeEncrypted);
            if (debug) {
                traceOutput("hmac", hmac, 0, hmac.length);
                traceOutput("ciphertext", ciphertext, 0,
                                Math.min(ciphertext.length, 32));
            }
            System.arraycopy(hmac, 0, ciphertext, cipherSize,
                                getChecksumLength());
            return ciphertext;
        } finally {
            if (Ke != null) {
                Arrays.fill(Ke, 0, Ke.length, (byte) 0);
            }
            if (Ki != null) {
                Arrays.fill(Ki, 0, Ki.length, (byte) 0);
            }
        }
    }
    public byte[] encryptRaw(byte[] baseKey, int usage,
        byte[] ivec, byte[] plaintext, int start, int len)
        throws GeneralSecurityException, KrbCryptoException {
        if (debug) {
            System.err.println("usage: " + usage);
            if (ivec != null) {
                traceOutput("old_state.ivec", ivec, 0, ivec.length);
            }
            traceOutput("plaintext", plaintext, start, Math.min(len, 32));
            traceOutput("baseKey", baseKey, 0, baseKey.length);
        }
        Cipher encCipher = getCipher(baseKey, ivec, Cipher.ENCRYPT_MODE);
        int blockSize = encCipher.getBlockSize();
        if ((len % blockSize) != 0) {
            throw new GeneralSecurityException(
                "length of data to be encrypted (" + len +
                ") is not a multiple of the blocksize (" + blockSize + ")");
        }
        int cipherSize = encCipher.getOutputSize(len);
        byte[] ciphertext = new byte[cipherSize];
        encCipher.doFinal(plaintext, 0, len, ciphertext, 0);
        return ciphertext;
    }
    public byte[] decryptRaw(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len)
        throws GeneralSecurityException {
        if (debug) {
            System.err.println("usage: " + usage);
            if (ivec != null) {
                traceOutput("old_state.ivec", ivec, 0, ivec.length);
            }
            traceOutput("ciphertext", ciphertext, start, Math.min(len, 32));
            traceOutput("baseKey", baseKey, 0, baseKey.length);
        }
        Cipher decCipher = getCipher(baseKey, ivec, Cipher.DECRYPT_MODE);
        int blockSize = decCipher.getBlockSize();
        if ((len % blockSize) != 0) {
            throw new GeneralSecurityException(
                "length of data to be decrypted (" + len +
                ") is not a multiple of the blocksize (" + blockSize + ")");
        }
        byte[] decrypted = decCipher.doFinal(ciphertext, start, len);
        if (debug) {
            traceOutput("decrypted", decrypted, 0,
                Math.min(decrypted.length, 32));
        }
        return decrypted;
    }
    public byte[] decrypt(byte[] baseKey, int usage, byte[] ivec,
        byte[] ciphertext, int start, int len) throws GeneralSecurityException {
        if (!KeyUsage.isValid(usage)) {
            throw new GeneralSecurityException("Invalid key usage number: "
                                                + usage);
        }
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
            Cipher decCipher = getCipher(Ke, ivec, Cipher.DECRYPT_MODE);
            int blockSize = decCipher.getBlockSize();
            int cksumSize = getChecksumLength();
            int cipherSize = len - cksumSize;
            byte[] decrypted = decCipher.doFinal(ciphertext, start, cipherSize);
            if (debug) {
                traceOutput("decrypted", decrypted, 0,
                                Math.min(decrypted.length, 32));
            }
            constant[4] = (byte) 0x55;
            Ki = dk(baseKey, constant);  
            if (debug) {
                traceOutput("constant", constant, 0, constant.length);
                traceOutput("Ki", Ki, 0, Ke.length);
            }
            byte[] calculatedHmac = getHmac(Ki, decrypted);
            if (debug) {
                traceOutput("calculated Hmac", calculatedHmac, 0,
                    calculatedHmac.length);
                traceOutput("message Hmac", ciphertext, cipherSize,
                    cksumSize);
            }
            boolean cksumFailed = false;
            if (calculatedHmac.length >= cksumSize) {
                for (int i = 0; i < cksumSize; i++) {
                    if (calculatedHmac[i] != ciphertext[cipherSize+i]) {
                        cksumFailed = true;
                        break;
                    }
                }
            }
            if (cksumFailed) {
                throw new GeneralSecurityException("Checksum failed");
            }
            if (ivec != null && ivec.length == blockSize) {
                System.arraycopy(ciphertext,  start + cipherSize - blockSize,
                    ivec, 0, blockSize);
                if (debug) {
                    traceOutput("new_state.ivec", ivec, 0, ivec.length);
                }
            }
            byte[] plaintext = new byte[decrypted.length - blockSize];
            System.arraycopy(decrypted, blockSize, plaintext,
                                0, plaintext.length);
            return plaintext; 
        } finally {
            if (Ke != null) {
                Arrays.fill(Ke, 0, Ke.length, (byte) 0);
            }
            if (Ki != null) {
                Arrays.fill(Ki, 0, Ki.length, (byte) 0);
            }
        }
    }
    int roundup(int n, int blocksize) {
        return (((n + blocksize - 1) / blocksize) * blocksize);
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
    byte[] dk(byte[] key, byte[] constant)
        throws GeneralSecurityException {
        return randomToKey(dr(key, constant));
    }
    private byte[] dr(byte[] key, byte[] constant)
        throws GeneralSecurityException {
        Cipher encCipher = getCipher(key, null, Cipher.ENCRYPT_MODE);
        int blocksize = encCipher.getBlockSize();
        if (constant.length != blocksize) {
            constant = nfold(constant, blocksize * 8);
        }
        byte[] toBeEncrypted = constant;
        int keybytes = (getKeySeedLength()>>3);  
        byte[] rawkey = new byte[keybytes];
        int posn = 0;
        int n = 0, len;
        while (n < keybytes) {
            if (debug) {
                System.err.println("Encrypting: " +
                    bytesToString(toBeEncrypted));
            }
            byte[] cipherBlock = encCipher.doFinal(toBeEncrypted);
            if (debug) {
                System.err.println("K: " + ++posn + " = " +
                    bytesToString(cipherBlock));
            }
            len = (keybytes - n <= cipherBlock.length ? (keybytes - n) :
                cipherBlock.length);
            if (debug) {
                System.err.println("copying " + len + " key bytes");
            }
            System.arraycopy(cipherBlock, 0, rawkey, n, len);
            n += len;
            toBeEncrypted = cipherBlock;
        }
        return rawkey;
    }
    static byte[] nfold(byte[] in, int outbits) {
        int inbits = in.length;
        outbits >>= 3;    
        int a, b, c, lcm;
        a = outbits;  
        b = inbits;   
        while (b != 0) {
            c = b;
            b = a % b;
            a = c;
        }
        lcm = outbits*inbits/a;
        if (debug) {
            System.err.println("k: " + inbits);
            System.err.println("n: " + outbits);
            System.err.println("lcm: " + lcm);
        }
        byte[] out = new byte[outbits];
        Arrays.fill(out, (byte)0);
        int thisbyte = 0;
        int msbit, i, bval, oval;
        for (i = lcm-1; i >= 0; i--) {
            msbit = (
                ((inbits<<3)-1)
                + (((inbits<<3)+13)*(i/inbits))
                + ((inbits-(i%inbits)) << 3)) % (inbits << 3);
            bval =  ((((in[((inbits-1)-(msbit>>>3))%inbits]&0xff)<<8)|
                (in[((inbits)-(msbit>>>3))%inbits]&0xff))
                >>>((msbit&7)+1))&0xff;
            thisbyte += bval;
            oval = (out[i%outbits]&0xff);
            thisbyte += oval;
            out[i%outbits] = (byte) (thisbyte&0xff);
            if (debug) {
                System.err.println("msbit[" + i + "] = " +  msbit + "\tbval=" +
                    Integer.toHexString(bval) + "\toval=" +
                    Integer.toHexString(oval)
                    + "\tsum = " + Integer.toHexString(thisbyte));
            }
            thisbyte >>>= 8;
            if (debug) {
                System.err.println("carry=" + thisbyte);
            }
        }
        if (thisbyte != 0) {
            for (i = outbits-1; i >= 0; i--) {
                thisbyte += (out[i]&0xff);
                out[i] = (byte) (thisbyte&0xff);
                thisbyte >>>= 8;
            }
        }
        return out;
    }
    static String bytesToString(byte[] digest) {
        StringBuffer digestString = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            if ((digest[i] & 0x000000ff) < 0x10) {
                digestString.append("0" +
                    Integer.toHexString(digest[i] & 0x000000ff));
            } else {
                digestString.append(
                    Integer.toHexString(digest[i] & 0x000000ff));
            }
        }
        return digestString.toString();
    }
    private static byte[] binaryStringToBytes(String str) {
        char[] usageStr = str.toCharArray();
        byte[] usage = new byte[usageStr.length/2];
        for (int i = 0; i < usage.length; i++) {
            byte a = Byte.parseByte(new String(usageStr, i*2, 1), 16);
            byte b = Byte.parseByte(new String(usageStr, i*2 + 1, 1), 16);
            usage[i] = (byte) ((a<<4)|b);
        }
        return usage;
    }
    static void traceOutput(String traceTag, byte[] output, int offset,
        int len) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(len);
            new HexDumpEncoder().encodeBuffer(
                new ByteArrayInputStream(output, offset, len), out);
            System.err.println(traceTag + ":" + out.toString());
        } catch (Exception e) {
        }
    }
    static byte[] charToUtf8(char[] chars) {
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.wrap(chars);
        ByteBuffer bb = utf8.encode(cb);
        int len = bb.limit();
        byte[] answer = new byte[len];
        bb.get(answer, 0, len);
        return answer;
    }
    static byte[] charToUtf16(char[] chars) {
        Charset utf8 = Charset.forName("UTF-16LE");
        CharBuffer cb = CharBuffer.wrap(chars);
        ByteBuffer bb = utf8.encode(cb);
        int len = bb.limit();
        byte[] answer = new byte[len];
        bb.get(answer, 0, len);
        return answer;
    }
}
