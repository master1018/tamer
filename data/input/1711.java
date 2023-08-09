class CipherHelper {
    private static final int KG_USAGE_SEAL = 22;
    private static final int KG_USAGE_SIGN = 23;
    private static final int KG_USAGE_SEQ = 24;
    private static final int DES_CHECKSUM_SIZE = 8;
    private static final int DES_IV_SIZE = 8;
    private static final int AES_IV_SIZE = 16;
    private static final int HMAC_CHECKSUM_SIZE = 8;
    private static final int KG_USAGE_SIGN_MS = 15;
    private static final boolean DEBUG = Krb5Util.DEBUG;
    private static final byte[] ZERO_IV = new byte[DES_IV_SIZE];
    private static final byte[] ZERO_IV_AES = new byte[AES_IV_SIZE];
    private int etype;
    private int sgnAlg, sealAlg;
    private byte[] keybytes;
    private int proto = 0;
    CipherHelper(EncryptionKey key) throws GSSException {
        etype = key.getEType();
        keybytes = key.getBytes();
        switch (etype) {
        case EncryptedData.ETYPE_DES_CBC_CRC:
        case EncryptedData.ETYPE_DES_CBC_MD5:
            sgnAlg = MessageToken.SGN_ALG_DES_MAC_MD5;
            sealAlg = MessageToken.SEAL_ALG_DES;
            break;
        case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
            sgnAlg = MessageToken.SGN_ALG_HMAC_SHA1_DES3_KD;
            sealAlg = MessageToken.SEAL_ALG_DES3_KD;
            break;
        case EncryptedData.ETYPE_ARCFOUR_HMAC:
            sgnAlg = MessageToken.SGN_ALG_HMAC_MD5_ARCFOUR;
            sealAlg = MessageToken.SEAL_ALG_ARCFOUR_HMAC;
            break;
        case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
        case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
            sgnAlg = -1;
            sealAlg = -1;
            proto = 1;
            break;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported encryption type: " + etype);
        }
    }
    int getSgnAlg() {
        return sgnAlg;
    }
    int getSealAlg() {
        return sealAlg;
    }
    int getProto() {
        return proto;
    }
    int getEType() {
        return etype;
    }
    boolean isArcFour() {
        boolean flag = false;
        if (etype == EncryptedData.ETYPE_ARCFOUR_HMAC) {
            flag = true;
        }
        return flag;
    }
    byte[] calculateChecksum(int alg, byte[] header, byte[] trailer,
        byte[] data, int start, int len, int tokenId) throws GSSException {
        switch (alg) {
        case MessageToken.SGN_ALG_DES_MAC_MD5:
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(header);
                md5.update(data, start, len);
                if (trailer != null) {
                    md5.update(trailer);
                }
                data = md5.digest();
                start = 0;
                len = data.length;
                header = null;
                trailer = null;
            } catch (NoSuchAlgorithmException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not get MD5 Message Digest - " + e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_DES_MAC:
            return getDesCbcChecksum(keybytes, header, data, start, len);
        case MessageToken.SGN_ALG_HMAC_SHA1_DES3_KD:
            byte[] buf;
            int offset, total;
            if (header == null && trailer == null) {
                buf = data;
                total = len;
                offset = start;
            } else {
                total = ((header != null ? header.length : 0) + len +
                    (trailer != null ? trailer.length : 0));
                buf = new byte[total];
                int pos = 0;
                if (header != null) {
                    System.arraycopy(header, 0, buf, 0, header.length);
                    pos = header.length;
                }
                System.arraycopy(data, start, buf, pos, len);
                pos += len;
                if (trailer != null) {
                    System.arraycopy(trailer, 0, buf, pos, trailer.length);
                }
                offset = 0;
            }
            try {
                byte[] answer = Des3.calculateChecksum(keybytes,
                    KG_USAGE_SIGN, buf, offset, total);
                return answer;
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not use HMAC-SHA1-DES3-KD signing algorithm - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_HMAC_MD5_ARCFOUR:
            byte[] buffer;
            int off, tot;
            if (header == null && trailer == null) {
                buffer = data;
                tot = len;
                off = start;
            } else {
                tot = ((header != null ? header.length : 0) + len +
                      (trailer != null ? trailer.length : 0));
                buffer = new byte[tot];
                int pos = 0;
                if (header != null) {
                    System.arraycopy(header, 0, buffer, 0, header.length);
                    pos = header.length;
                }
                System.arraycopy(data, start, buffer, pos, len);
                pos += len;
                if (trailer != null) {
                    System.arraycopy(trailer, 0, buffer, pos, trailer.length);
                }
                off = 0;
            }
            try {
                int key_usage = KG_USAGE_SIGN;
                if (tokenId == Krb5Token.MIC_ID) {
                        key_usage = KG_USAGE_SIGN_MS;
                }
                byte[] answer = ArcFourHmac.calculateChecksum(keybytes,
                    key_usage, buffer, off, tot);
                byte[] output = new byte[getChecksumLength()];
                System.arraycopy(answer, 0, output, 0, output.length);
                return output;
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not use HMAC_MD5_ARCFOUR signing algorithm - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported signing algorithm: " + sgnAlg);
        }
    }
    byte[] calculateChecksum(byte[] header, byte[] data, int start, int len,
        int key_usage) throws GSSException {
        int total = ((header != null ? header.length : 0) + len);
        byte[] buf = new byte[total];
        System.arraycopy(data, start, buf, 0, len);
        if (header != null) {
            System.arraycopy(header, 0, buf, len, header.length);
        }
        switch (etype) {
        case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
            try {
                byte[] answer = Aes128.calculateChecksum(keybytes, key_usage,
                                        buf, 0, total);
                return answer;
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not use AES128 signing algorithm - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
            try {
                byte[] answer = Aes256.calculateChecksum(keybytes, key_usage,
                                        buf, 0, total);
                return answer;
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not use AES256 signing algorithm - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported encryption type: " + etype);
        }
    }
    byte[] encryptSeq(byte[] ivec, byte[] plaintext, int start, int len)
    throws GSSException {
        switch (sgnAlg) {
        case MessageToken.SGN_ALG_DES_MAC_MD5:
        case MessageToken.SGN_ALG_DES_MAC:
            try {
                Cipher des = getInitializedDes(true, keybytes, ivec);
                return des.doFinal(plaintext, start, len);
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not encrypt sequence number using DES - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_HMAC_SHA1_DES3_KD:
            byte[] iv;
            if (ivec.length == DES_IV_SIZE) {
                iv = ivec;
            } else {
                iv = new byte[DES_IV_SIZE];
                System.arraycopy(ivec, 0, iv, 0, DES_IV_SIZE);
            }
            try {
                return Des3.encryptRaw(keybytes, KG_USAGE_SEQ, iv,
                    plaintext, start, len);
            } catch (Exception e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not encrypt sequence number using DES3-KD - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_HMAC_MD5_ARCFOUR:
            byte[] checksum;
            if (ivec.length == HMAC_CHECKSUM_SIZE) {
                checksum = ivec;
            } else {
                checksum = new byte[HMAC_CHECKSUM_SIZE];
                System.arraycopy(ivec, 0, checksum, 0, HMAC_CHECKSUM_SIZE);
            }
            try {
                return ArcFourHmac.encryptSeq(keybytes, KG_USAGE_SEQ, checksum,
                    plaintext, start, len);
            } catch (Exception e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not encrypt sequence number using RC4-HMAC - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported signing algorithm: " + sgnAlg);
        }
    }
    byte[] decryptSeq(byte[] ivec, byte[] ciphertext, int start, int len)
        throws GSSException {
        switch (sgnAlg) {
        case MessageToken.SGN_ALG_DES_MAC_MD5:
        case MessageToken.SGN_ALG_DES_MAC:
            try {
                Cipher des = getInitializedDes(false, keybytes, ivec);
                return des.doFinal(ciphertext, start, len);
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not decrypt sequence number using DES - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_HMAC_SHA1_DES3_KD:
            byte[] iv;
            if (ivec.length == DES_IV_SIZE) {
                iv = ivec;
            } else {
                iv = new byte[8];
                System.arraycopy(ivec, 0, iv, 0, DES_IV_SIZE);
            }
            try {
                return Des3.decryptRaw(keybytes, KG_USAGE_SEQ, iv,
                    ciphertext, start, len);
            } catch (Exception e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not decrypt sequence number using DES3-KD - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        case MessageToken.SGN_ALG_HMAC_MD5_ARCFOUR:
            byte[] checksum;
            if (ivec.length == HMAC_CHECKSUM_SIZE) {
                checksum = ivec;
            } else {
                checksum = new byte[HMAC_CHECKSUM_SIZE];
                System.arraycopy(ivec, 0, checksum, 0, HMAC_CHECKSUM_SIZE);
            }
            try {
                return ArcFourHmac.decryptSeq(keybytes, KG_USAGE_SEQ, checksum,
                    ciphertext, start, len);
            } catch (Exception e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not decrypt sequence number using RC4-HMAC - " +
                    e.getMessage());
                ge.initCause(e);
                throw ge;
            }
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported signing algorithm: " + sgnAlg);
        }
    }
    int getChecksumLength() throws GSSException {
        switch (etype) {
        case EncryptedData.ETYPE_DES_CBC_CRC:
        case EncryptedData.ETYPE_DES_CBC_MD5:
            return DES_CHECKSUM_SIZE;
        case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
            return Des3.getChecksumLength();
        case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
            return Aes128.getChecksumLength();
        case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
            return Aes256.getChecksumLength();
        case EncryptedData.ETYPE_ARCFOUR_HMAC:
            return HMAC_CHECKSUM_SIZE;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported encryption type: " + etype);
        }
    }
    void decryptData(WrapToken token, byte[] ciphertext, int cStart, int cLen,
        byte[] plaintext, int pStart) throws GSSException {
        switch (sealAlg) {
        case MessageToken.SEAL_ALG_DES:
            desCbcDecrypt(token, getDesEncryptionKey(keybytes),
                ciphertext, cStart, cLen, plaintext, pStart);
            break;
        case MessageToken.SEAL_ALG_DES3_KD:
            des3KdDecrypt(token, ciphertext, cStart, cLen, plaintext, pStart);
            break;
        case MessageToken.SEAL_ALG_ARCFOUR_HMAC:
            arcFourDecrypt(token, ciphertext, cStart, cLen, plaintext, pStart);
            break;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported seal algorithm: " + sealAlg);
        }
    }
    void decryptData(WrapToken_v2 token, byte[] ciphertext, int cStart,
                int cLen, byte[] plaintext, int pStart, int key_usage)
        throws GSSException {
        switch (etype) {
            case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                    aes128Decrypt(token, ciphertext, cStart, cLen,
                                plaintext, pStart, key_usage);
                    break;
            case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                    aes256Decrypt(token, ciphertext, cStart, cLen,
                                plaintext, pStart, key_usage);
                    break;
            default:
                    throw new GSSException(GSSException.FAILURE, -1,
                        "Unsupported etype: " + etype);
            }
    }
    void decryptData(WrapToken token, InputStream cipherStream, int cLen,
        byte[] plaintext, int pStart)
        throws GSSException, IOException {
        switch (sealAlg) {
        case MessageToken.SEAL_ALG_DES:
            desCbcDecrypt(token, getDesEncryptionKey(keybytes),
                cipherStream, cLen, plaintext, pStart);
            break;
        case MessageToken.SEAL_ALG_DES3_KD:
            byte[] ciphertext = new byte[cLen];
            try {
                Krb5Token.readFully(cipherStream, ciphertext, 0, cLen);
            } catch (IOException e) {
                GSSException ge = new GSSException(
                    GSSException.DEFECTIVE_TOKEN, -1,
                    "Cannot read complete token");
                ge.initCause(e);
                throw ge;
            }
            des3KdDecrypt(token, ciphertext, 0, cLen, plaintext, pStart);
            break;
        case MessageToken.SEAL_ALG_ARCFOUR_HMAC:
            byte[] ctext = new byte[cLen];
            try {
                Krb5Token.readFully(cipherStream, ctext, 0, cLen);
            } catch (IOException e) {
                GSSException ge = new GSSException(
                    GSSException.DEFECTIVE_TOKEN, -1,
                    "Cannot read complete token");
                ge.initCause(e);
                throw ge;
            }
            arcFourDecrypt(token, ctext, 0, cLen, plaintext, pStart);
            break;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported seal algorithm: " + sealAlg);
        }
    }
    void decryptData(WrapToken_v2 token, InputStream cipherStream, int cLen,
        byte[] plaintext, int pStart, int key_usage)
        throws GSSException, IOException {
        byte[] ciphertext = new byte[cLen];
        try {
                Krb5Token.readFully(cipherStream, ciphertext, 0, cLen);
        } catch (IOException e) {
                GSSException ge = new GSSException(
                    GSSException.DEFECTIVE_TOKEN, -1,
                    "Cannot read complete token");
                ge.initCause(e);
                throw ge;
        }
        switch (etype) {
            case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                    aes128Decrypt(token, ciphertext, 0, cLen,
                                plaintext, pStart, key_usage);
                    break;
            case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                    aes256Decrypt(token, ciphertext, 0, cLen,
                                plaintext, pStart, key_usage);
                    break;
            default:
                    throw new GSSException(GSSException.FAILURE, -1,
                        "Unsupported etype: " + etype);
        }
    }
    void encryptData(WrapToken token, byte[] confounder, byte[] plaintext,
        int start, int len, byte[] padding, OutputStream os)
        throws GSSException, IOException {
        switch (sealAlg) {
        case MessageToken.SEAL_ALG_DES:
            Cipher des = getInitializedDes(true, getDesEncryptionKey(keybytes),
                ZERO_IV);
            CipherOutputStream cos = new CipherOutputStream(os, des);
            cos.write(confounder);
            cos.write(plaintext, start, len);
            cos.write(padding);
            break;
        case MessageToken.SEAL_ALG_DES3_KD:
            byte[] ctext = des3KdEncrypt(confounder, plaintext, start, len,
                padding);
            os.write(ctext);
            break;
        case MessageToken.SEAL_ALG_ARCFOUR_HMAC:
            byte[] ciphertext = arcFourEncrypt(token, confounder, plaintext,
                start, len, padding);
            os.write(ciphertext);
            break;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported seal algorithm: " + sealAlg);
        }
    }
    byte[] encryptData(WrapToken_v2 token, byte[] confounder, byte[] tokenHeader,
            byte[] plaintext, int start, int len, int key_usage)
            throws GSSException {
        switch (etype) {
            case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                return aes128Encrypt(confounder, tokenHeader,
                            plaintext, start, len, key_usage);
            case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                return aes256Encrypt(confounder, tokenHeader,
                            plaintext, start, len, key_usage);
            default:
                throw new GSSException(GSSException.FAILURE, -1,
                    "Unsupported etype: " + etype);
        }
    }
    void encryptData(WrapToken token, byte[] confounder, byte[] plaintext,
        int pStart, int pLen, byte[] padding, byte[] ciphertext, int cStart)
        throws GSSException {
        switch (sealAlg) {
        case MessageToken.SEAL_ALG_DES:
            int pos = cStart;
            Cipher des = getInitializedDes(true, getDesEncryptionKey(keybytes),
                ZERO_IV);
            try {
                pos += des.update(confounder, 0, confounder.length,
                                  ciphertext, pos);
                pos += des.update(plaintext, pStart, pLen,
                                  ciphertext, pos);
                des.update(padding, 0, padding.length,
                           ciphertext, pos);
                des.doFinal();
            } catch (GeneralSecurityException e) {
                GSSException ge = new GSSException(GSSException.FAILURE, -1,
                    "Could not use DES Cipher - " + e.getMessage());
                ge.initCause(e);
                throw ge;
            }
            break;
        case MessageToken.SEAL_ALG_DES3_KD:
            byte[] ctext = des3KdEncrypt(confounder, plaintext, pStart, pLen,
                padding);
            System.arraycopy(ctext, 0, ciphertext, cStart, ctext.length);
            break;
        case MessageToken.SEAL_ALG_ARCFOUR_HMAC:
            byte[] ctext2 = arcFourEncrypt(token, confounder, plaintext, pStart,
                pLen, padding);
            System.arraycopy(ctext2, 0, ciphertext, cStart, ctext2.length);
            break;
        default:
            throw new GSSException(GSSException.FAILURE, -1,
                "Unsupported seal algorithm: " + sealAlg);
        }
    }
    int encryptData(WrapToken_v2 token, byte[] confounder, byte[] tokenHeader,
        byte[] plaintext, int pStart, int pLen, byte[] ciphertext, int cStart,
        int key_usage) throws GSSException {
        byte[] ctext = null;
        switch (etype) {
            case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                    ctext = aes128Encrypt(confounder, tokenHeader,
                                plaintext, pStart, pLen, key_usage);
                    break;
            case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                    ctext = aes256Encrypt(confounder, tokenHeader,
                                plaintext, pStart, pLen, key_usage);
                    break;
            default:
                    throw new GSSException(GSSException.FAILURE, -1,
                        "Unsupported etype: " + etype);
        }
        System.arraycopy(ctext, 0, ciphertext, cStart, ctext.length);
        return ctext.length;
    }
    private byte[] getDesCbcChecksum(byte key[],
                                     byte[] header,
                                     byte[] data, int offset, int len)
        throws GSSException {
        Cipher des = getInitializedDes(true, key, ZERO_IV);
        int blockSize = des.getBlockSize();
        byte[] finalBlock = new byte[blockSize];
        int numBlocks = len / blockSize;
        int lastBytes = len % blockSize;
        if (lastBytes == 0) {
            numBlocks -= 1;
            System.arraycopy(data, offset + numBlocks*blockSize,
                             finalBlock, 0, blockSize);
        } else {
            System.arraycopy(data, offset + numBlocks*blockSize,
                             finalBlock, 0, lastBytes);
        }
        try {
            byte[] temp = new byte[Math.max(blockSize,
                (header == null? blockSize : header.length))];
            if (header != null) {
                des.update(header, 0, header.length, temp, 0);
            }
            for (int i = 0; i < numBlocks; i++) {
                des.update(data, offset, blockSize,
                           temp, 0);
                offset += blockSize;
            }
            byte[] retVal = new byte[blockSize];
            des.update(finalBlock, 0, blockSize, retVal, 0);
            des.doFinal();
            return retVal;
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use DES Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private final Cipher getInitializedDes(boolean encryptMode, byte[] key,
                                          byte[] ivBytes)
        throws  GSSException  {
        try {
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKey jceKey = (SecretKey) (new SecretKeySpec(key, "DES"));
            Cipher desCipher = Cipher.getInstance("DES/CBC/NoPadding");
            desCipher.init(
                (encryptMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE),
                jceKey, iv);
            return desCipher;
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private void desCbcDecrypt(WrapToken token, byte[] key, byte[] cipherText,
        int offset, int len, byte[] dataOutBuf, int dataOffset)
         throws GSSException {
        try {
            int temp = 0;
            Cipher des = getInitializedDes(false, key, ZERO_IV);
            temp = des.update(cipherText, offset, WrapToken.CONFOUNDER_SIZE,
                              token.confounder);
            offset += WrapToken.CONFOUNDER_SIZE;
            len -= WrapToken.CONFOUNDER_SIZE;
            int blockSize = des.getBlockSize();
            int numBlocks = len / blockSize - 1;
            for (int i = 0; i < numBlocks; i++) {
                temp = des.update(cipherText, offset, blockSize,
                                  dataOutBuf, dataOffset);
                offset += blockSize;
                dataOffset += blockSize;
            }
            byte[] finalBlock = new byte[blockSize];
            des.update(cipherText, offset, blockSize, finalBlock);
            des.doFinal();
            int padSize = finalBlock[blockSize - 1];
            if (padSize < 1  || padSize > 8)
                throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                        "Invalid padding on Wrap Token");
            token.padding = WrapToken.pads[padSize];
            blockSize -= padSize;
            System.arraycopy(finalBlock, 0, dataOutBuf, dataOffset,
                             blockSize);
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use DES cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private void desCbcDecrypt(WrapToken token, byte[] key,
        InputStream is, int len, byte[] dataOutBuf, int dataOffset)
        throws GSSException, IOException {
        int temp = 0;
        Cipher des = getInitializedDes(false, key, ZERO_IV);
        WrapTokenInputStream truncatedInputStream =
            new WrapTokenInputStream(is, len);
        CipherInputStream cis = new CipherInputStream(truncatedInputStream,
                                                      des);
        temp = cis.read(token.confounder);
        len -= temp;
        int blockSize = des.getBlockSize();
        int numBlocks = len / blockSize - 1;
        for (int i = 0; i < numBlocks; i++) {
            temp = cis.read(dataOutBuf, dataOffset, blockSize);
            dataOffset += blockSize;
        }
        byte[] finalBlock = new byte[blockSize];
        temp = cis.read(finalBlock);
        try {
            des.doFinal();
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use DES cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
        int padSize = finalBlock[blockSize - 1];
        if (padSize < 1  || padSize > 8)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                   "Invalid padding on Wrap Token");
        token.padding = WrapToken.pads[padSize];
        blockSize -= padSize;
        System.arraycopy(finalBlock, 0, dataOutBuf, dataOffset,
                         blockSize);
    }
    private static byte[] getDesEncryptionKey(byte[] key)
        throws GSSException {
        if (key.length > 8)
            throw new GSSException(GSSException.FAILURE, -100,
                                   "Invalid DES Key!");
        byte[] retVal = new byte[key.length];
        for (int i = 0; i < key.length; i++)
            retVal[i] = (byte)(key[i] ^ 0xf0);  
        return retVal;
    }
    private void des3KdDecrypt(WrapToken token, byte[] ciphertext,
        int cStart, int cLen, byte[] plaintext, int pStart)
        throws GSSException {
        byte[] ptext;
        try {
            ptext = Des3.decryptRaw(keybytes, KG_USAGE_SEAL, ZERO_IV,
                ciphertext, cStart, cLen);
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use DES3-KD Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
        int padSize = ptext[ptext.length - 1];
        if (padSize < 1  || padSize > 8)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                "Invalid padding on Wrap Token");
        token.padding = WrapToken.pads[padSize];
        int len = ptext.length - WrapToken.CONFOUNDER_SIZE - padSize;
        System.arraycopy(ptext, WrapToken.CONFOUNDER_SIZE,
            plaintext, pStart, len);
        System.arraycopy(ptext, 0, token.confounder,
            0, WrapToken.CONFOUNDER_SIZE);
    }
    private byte[] des3KdEncrypt(byte[] confounder, byte[] plaintext,
        int start, int len, byte[] padding) throws GSSException {
        byte[] all = new byte[confounder.length + len + padding.length];
        System.arraycopy(confounder, 0, all, 0, confounder.length);
        System.arraycopy(plaintext, start, all, confounder.length, len);
        System.arraycopy(padding, 0, all, confounder.length + len,
            padding.length);
        try {
            byte[] answer = Des3.encryptRaw(keybytes, KG_USAGE_SEAL, ZERO_IV,
                all, 0, all.length);
            return answer;
        } catch (Exception e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use DES3-KD Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private void arcFourDecrypt(WrapToken token, byte[] ciphertext,
        int cStart, int cLen, byte[] plaintext, int pStart)
        throws GSSException {
        byte[] seqNum = decryptSeq(token.getChecksum(),
                token.getEncSeqNumber(), 0, 8);
        byte[] ptext;
        try {
            ptext = ArcFourHmac.decryptRaw(keybytes, KG_USAGE_SEAL, ZERO_IV,
                ciphertext, cStart, cLen, seqNum);
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use ArcFour Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
        int padSize = ptext[ptext.length - 1];
        if (padSize < 1)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                "Invalid padding on Wrap Token");
        token.padding = WrapToken.pads[padSize];
        int len = ptext.length - WrapToken.CONFOUNDER_SIZE - padSize;
        System.arraycopy(ptext, WrapToken.CONFOUNDER_SIZE,
            plaintext, pStart, len);
        System.arraycopy(ptext, 0, token.confounder,
            0, WrapToken.CONFOUNDER_SIZE);
    }
    private byte[] arcFourEncrypt(WrapToken token, byte[] confounder,
        byte[] plaintext, int start, int len, byte[] padding)
        throws GSSException {
        byte[] all = new byte[confounder.length + len + padding.length];
        System.arraycopy(confounder, 0, all, 0, confounder.length);
        System.arraycopy(plaintext, start, all, confounder.length, len);
        System.arraycopy(padding, 0, all, confounder.length + len,
            padding.length);
        byte[] seqNum = new byte[4];
        token.writeBigEndian(token.getSequenceNumber(), seqNum);
        try {
            byte[] answer = ArcFourHmac.encryptRaw(keybytes, KG_USAGE_SEAL,
                                        seqNum, all, 0, all.length);
            return answer;
        } catch (Exception e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use ArcFour Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private byte[] aes128Encrypt(byte[] confounder, byte[] tokenHeader,
        byte[] plaintext, int start, int len, int key_usage)
        throws GSSException {
        byte[] all = new byte[confounder.length + len + tokenHeader.length];
        System.arraycopy(confounder, 0, all, 0, confounder.length);
        System.arraycopy(plaintext, start, all, confounder.length, len);
        System.arraycopy(tokenHeader, 0, all, confounder.length+len,
                                tokenHeader.length);
        try {
            byte[] answer = Aes128.encryptRaw(keybytes, key_usage,
                                ZERO_IV_AES,
                                all, 0, all.length);
            return answer;
        } catch (Exception e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use AES128 Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private void aes128Decrypt(WrapToken_v2 token, byte[] ciphertext,
        int cStart, int cLen, byte[] plaintext, int pStart, int key_usage)
        throws GSSException {
        byte[] ptext = null;
        try {
            ptext = Aes128.decryptRaw(keybytes, key_usage,
                        ZERO_IV_AES, ciphertext, cStart, cLen);
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use AES128 Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
        int len = ptext.length - WrapToken_v2.CONFOUNDER_SIZE -
                        WrapToken_v2.TOKEN_HEADER_SIZE;
        System.arraycopy(ptext, WrapToken_v2.CONFOUNDER_SIZE,
                                plaintext, pStart, len);
    }
    private byte[] aes256Encrypt(byte[] confounder, byte[] tokenHeader,
        byte[] plaintext, int start, int len, int key_usage)
        throws GSSException {
        byte[] all = new byte[confounder.length + len + tokenHeader.length];
        System.arraycopy(confounder, 0, all, 0, confounder.length);
        System.arraycopy(plaintext, start, all, confounder.length, len);
        System.arraycopy(tokenHeader, 0, all, confounder.length+len,
                                tokenHeader.length);
        try {
            byte[] answer = Aes256.encryptRaw(keybytes, key_usage,
                                ZERO_IV_AES, all, 0, all.length);
            return answer;
        } catch (Exception e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use AES256 Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }
    private void aes256Decrypt(WrapToken_v2 token, byte[] ciphertext,
        int cStart, int cLen, byte[] plaintext, int pStart, int key_usage)
        throws GSSException {
        byte[] ptext;
        try {
            ptext = Aes256.decryptRaw(keybytes, key_usage,
                        ZERO_IV_AES, ciphertext, cStart, cLen);
        } catch (GeneralSecurityException e) {
            GSSException ge = new GSSException(GSSException.FAILURE, -1,
                "Could not use AES128 Cipher - " + e.getMessage());
            ge.initCause(e);
            throw ge;
        }
        int len = ptext.length - WrapToken_v2.CONFOUNDER_SIZE -
                        WrapToken_v2.TOKEN_HEADER_SIZE;
        System.arraycopy(ptext, WrapToken_v2.CONFOUNDER_SIZE,
                                plaintext, pStart, len);
    }
    class WrapTokenInputStream extends InputStream {
        private InputStream is;
        private int length;
        private int remaining;
        private int temp;
        public WrapTokenInputStream(InputStream is, int length) {
            this.is = is;
            this.length = length;
            remaining = length;
        }
        public final int read() throws IOException {
            if (remaining == 0)
                return -1;
            else {
                temp = is.read();
                if (temp != -1)
                    remaining -= temp;
                return temp;
            }
        }
        public final int read(byte[] b) throws IOException {
            if (remaining == 0)
                return -1;
            else {
                temp = Math.min(remaining, b.length);
                temp = is.read(b, 0, temp);
                if (temp != -1)
                    remaining -= temp;
                return temp;
            }
        }
        public final int read(byte[] b,
                              int off,
                              int len) throws IOException {
            if (remaining == 0)
                return -1;
            else {
                temp = Math.min(remaining, len);
                temp = is.read(b, off, temp);
                if (temp != -1)
                    remaining -= temp;
                return temp;
            }
        }
        public final long skip(long n)  throws IOException {
            if (remaining == 0)
                return 0;
            else {
                temp = (int) Math.min(remaining, n);
                temp = (int) is.skip(temp);
                remaining -= temp;
                return temp;
            }
        }
        public final int available() throws IOException {
            return Math.min(remaining, is.available());
        }
        public final void close() throws IOException {
            remaining = 0;
        }
    }
}
