public class HmacMd5ArcFourCksumType extends CksumType {
    public HmacMd5ArcFourCksumType() {
    }
    public int confounderSize() {
        return 8;
    }
    public int cksumType() {
        return Checksum.CKSUMTYPE_HMAC_MD5_ARCFOUR;
    }
    public boolean isSafe() {
        return true;
    }
    public int cksumSize() {
        return 16;  
    }
    public int keyType() {
        return Krb5.KEYTYPE_ARCFOUR_HMAC;
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
             return ArcFourHmac.calculateChecksum(key, usage, data, 0, size);
         } catch (GeneralSecurityException e) {
             KrbCryptoException ke = new KrbCryptoException(e.getMessage());
             ke.initCause(e);
             throw ke;
         }
    }
    public boolean verifyKeyedChecksum(byte[] data, int size,
        byte[] key, byte[] checksum, int usage) throws KrbCryptoException {
         try {
             byte[] newCksum = ArcFourHmac.calculateChecksum(key, usage,
                 data, 0, size);
             return isChecksumEqual(checksum, newCksum);
         } catch (GeneralSecurityException e) {
             KrbCryptoException ke = new KrbCryptoException(e.getMessage());
             ke.initCause(e);
             throw ke;
         }
     }
}
