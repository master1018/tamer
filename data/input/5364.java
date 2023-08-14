public final class Aes128CtsHmacSha1EType extends EType {
    public int eType() {
        return EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96;
    }
    public int minimumPadSize() {
        return 0;
    }
    public int confounderSize() {
        return blockSize();
    }
    public int checksumType() {
        return Checksum.CKSUMTYPE_HMAC_SHA1_96_AES128;
    }
    public int checksumSize() {
        return Aes128.getChecksumLength();
    }
    public int blockSize() {
        return 16;
    }
    public int keyType() {
        return Krb5.KEYTYPE_AES;
    }
    public int keySize() {
        return 16; 
    }
    public byte[] encrypt(byte[] data, byte[] key, int usage)
        throws KrbCryptoException {
        byte[] ivec = new byte[blockSize()];
        return encrypt(data, key, ivec, usage);
    }
    public byte[] encrypt(byte[] data, byte[] key, byte[] ivec, int usage)
        throws KrbCryptoException {
        try {
            return Aes128.encrypt(key, usage, ivec, data, 0, data.length);
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        }
    }
    public byte[] decrypt(byte[] cipher, byte[] key, int usage)
        throws KrbApErrException, KrbCryptoException {
        byte[] ivec = new byte[blockSize()];
        return decrypt(cipher, key, ivec, usage);
    }
    public byte[] decrypt(byte[] cipher, byte[] key, byte[] ivec, int usage)
        throws KrbApErrException, KrbCryptoException {
        try {
            return Aes128.decrypt(key, usage, ivec, cipher, 0, cipher.length);
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        }
    }
    public byte[] decryptedData(byte[] data) {
        return data;
    }
}
