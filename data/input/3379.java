public class HmacSha1Des3KdCksumType extends CksumType {
    public HmacSha1Des3KdCksumType() {
    }
    public int confounderSize() {
        return 8;
    }
    public int cksumType() {
        return Checksum.CKSUMTYPE_HMAC_SHA1_DES3_KD;
    }
    public boolean isSafe() {
        return true;
    }
    public int cksumSize() {
        return 20;  
    }
    public int keyType() {
        return Krb5.KEYTYPE_DES3;
    }
    public int keySize() {
        return 24;   
    }
    public byte[] calculateChecksum(byte[] data, int size) {
        return null;
    }
    public byte[] calculateKeyedChecksum(byte[] data, int size, byte[] key,
        int usage) throws KrbCryptoException {
         try {
             return Des3.calculateChecksum(key, usage, data, 0, size);
         } catch (GeneralSecurityException e) {
             KrbCryptoException ke = new KrbCryptoException(e.getMessage());
             ke.initCause(e);
             throw ke;
         }
    }
    public boolean verifyKeyedChecksum(byte[] data, int size,
        byte[] key, byte[] checksum, int usage) throws KrbCryptoException {
         try {
             byte[] newCksum = Des3.calculateChecksum(key, usage,
                 data, 0, size);
             return isChecksumEqual(checksum, newCksum);
         } catch (GeneralSecurityException e) {
             KrbCryptoException ke = new KrbCryptoException(e.getMessage());
             ke.initCause(e);
             throw ke;
         }
     }
}
