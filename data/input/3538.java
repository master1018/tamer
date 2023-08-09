public class DesMacKCksumType extends CksumType {
    public DesMacKCksumType() {
    }
    public int confounderSize() {
        return 0;
    }
    public int cksumType() {
        return Checksum.CKSUMTYPE_DES_MAC_K;
    }
    public boolean isSafe() {
        return true;
    }
    public int cksumSize() {
        return 16;
    }
    public int keyType() {
        return Krb5.KEYTYPE_DES;
    }
    public int keySize() {
        return 8;
    }
    public byte[] calculateChecksum(byte[] data, int size) {
        return null;
    }
    public byte[] calculateKeyedChecksum(byte[] data, int size, byte[] key,
        int usage) throws KrbCryptoException {
        try {
            if (DESKeySpec.isWeak(key, 0)) {
                key[7] = (byte)(key[7] ^ 0xF0);
            }
        } catch (InvalidKeyException ex) {
        }
        byte[] ivec = new byte[key.length];
        System.arraycopy(key, 0, ivec, 0, key.length);
        byte[] cksum = Des.des_cksum(ivec, data, key);
        return cksum;
    }
    public boolean verifyKeyedChecksum(byte[] data, int size,
        byte[] key, byte[] checksum, int usage) throws KrbCryptoException {
        byte[] new_cksum = calculateKeyedChecksum(data, data.length, key, usage);
        return isChecksumEqual(checksum, new_cksum);
    }
}
