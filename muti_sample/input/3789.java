public class HmacSha1Aes128CksumType extends CksumType {
    public HmacSha1Aes128CksumType() {
    }
    public int confounderSize() {
        return 16;
    }
    public int cksumType() {
        return Checksum.CKSUMTYPE_HMAC_SHA1_96_AES128;
    }
    public boolean isSafe() {
        return true;
    }
    public int cksumSize() {
        return 12;  
    }
    public int keyType() {
        return Krb5.KEYTYPE_AES;
    }
    public int keySize() {
        return 16;   
    }
    public byte[] calculateChecksum(byte[] data, int size) {
        return null;
    }
    public byte[] calculateKeyedChecksum(byte[] data, int size, byte[] key,
        int usage) throws KrbCryptoException {
         try {
            return Aes128.calculateChecksum(key, usage, data, 0, size);
         } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
         }
    }
    public boolean verifyKeyedChecksum(byte[] data, int size,
        byte[] key, byte[] checksum, int usage) throws KrbCryptoException {
         try {
            byte[] newCksum = Aes128.calculateChecksum(key, usage,
                                                        data, 0, size);
            return isChecksumEqual(checksum, newCksum);
         } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
         }
    }
}
