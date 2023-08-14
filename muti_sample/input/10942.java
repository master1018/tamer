public class DesCbcCrcEType extends DesCbcEType {
    public DesCbcCrcEType() {
    }
    public int eType() {
        return EncryptedData.ETYPE_DES_CBC_CRC;
    }
    public int minimumPadSize() {
        return 4;
    }
    public int confounderSize() {
        return 8;
    }
    public int checksumType() {
        return Checksum.CKSUMTYPE_CRC32;
    }
    public int checksumSize() {
        return 4;
    }
    public byte[] encrypt(byte[] data, byte[] key, int usage)
         throws KrbCryptoException {
        return encrypt(data, key, key, usage);
    }
    public byte[] decrypt(byte[] cipher, byte[] key, int usage)
         throws KrbApErrException, KrbCryptoException{
        return decrypt(cipher, key, key, usage);
    }
    protected byte[] calculateChecksum(byte[] data, int size) {
        return crc32.byte2crc32sum_bytes(data, size);
    }
}
