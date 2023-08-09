abstract class DesCbcEType extends EType {
    protected abstract byte[] calculateChecksum(byte[] data, int size)
        throws KrbCryptoException;
    public int blockSize() {
        return 8;
    }
    public int keyType() {
        return Krb5.KEYTYPE_DES;
    }
    public int keySize() {
        return 8;
    }
    public byte[] encrypt(byte[] data, byte[] key, int usage)
         throws KrbCryptoException {
        byte[] ivec = new byte[keySize()];
        return encrypt(data, key, ivec, usage);
    }
    public byte[] encrypt(byte[] data, byte[] key, byte[] ivec,
        int usage) throws KrbCryptoException {
        if (key.length > 8)
        throw new KrbCryptoException("Invalid DES Key!");
        int new_size = data.length + confounderSize() + checksumSize();
        byte[] new_data;
        byte pad;
        if (new_size % blockSize() == 0) {
            new_data = new byte[new_size + blockSize()];
            pad = (byte)8;
        }
        else {
            new_data = new byte[new_size + blockSize() - new_size % blockSize()];
            pad = (byte)(blockSize() - new_size % blockSize());
        }
        for (int i = new_size; i < new_data.length; i++) {
            new_data[i] = pad;
        }
        byte[] conf = Confounder.bytes(confounderSize());
        System.arraycopy(conf, 0, new_data, 0, confounderSize());
        System.arraycopy(data, 0, new_data, startOfData(), data.length);
        byte[] cksum = calculateChecksum(new_data, new_data.length);
        System.arraycopy(cksum, 0, new_data, startOfChecksum(),
                         checksumSize());
        byte[] cipher = new byte[new_data.length];
        Des.cbc_encrypt(new_data, cipher, key, ivec, true);
        return cipher;
    }
    public byte[] decrypt(byte[] cipher, byte[] key, int usage)
        throws KrbApErrException, KrbCryptoException{
        byte[] ivec = new byte[keySize()];
        return decrypt(cipher, key, ivec, usage);
    }
    public byte[] decrypt(byte[] cipher, byte[] key, byte[] ivec, int usage)
        throws KrbApErrException, KrbCryptoException {
        if (key.length > 8)
            throw new KrbCryptoException("Invalid DES Key!");
        byte[] data = new byte[cipher.length];
        Des.cbc_encrypt(cipher, data, key, ivec, false);
        if (!isChecksumValid(data))
            throw new KrbApErrException(Krb5.KRB_AP_ERR_BAD_INTEGRITY);
        return data;
    }
    private void copyChecksumField(byte[] data, byte[] cksum) {
        for (int i = 0; i < checksumSize();  i++)
            data[startOfChecksum() + i] = cksum[i];
    }
    private byte[] checksumField(byte[] data) {
        byte[] result = new byte[checksumSize()];
        for (int i = 0; i < checksumSize(); i++)
        result[i] = data[startOfChecksum() + i];
        return result;
    }
    private void resetChecksumField(byte[] data) {
        for (int i = startOfChecksum(); i < startOfChecksum() +
                 checksumSize();  i++)
            data[i] = 0;
    }
    private byte[] generateChecksum(byte[] data) throws KrbCryptoException{
        byte[] cksum1 = checksumField(data);
        resetChecksumField(data);
        byte[] cksum2 = calculateChecksum(data, data.length);
        copyChecksumField(data, cksum1);
        return cksum2;
    }
    private boolean isChecksumEqual(byte[] cksum1, byte[] cksum2) {
        if (cksum1 == cksum2)
            return true;
        if ((cksum1 == null && cksum2 != null) ||
            (cksum1 != null && cksum2 == null))
            return false;
        if (cksum1.length != cksum2.length)
            return false;
        for (int i = 0; i < cksum1.length; i++)
            if (cksum1[i] != cksum2[i])
                return false;
        return true;
    }
    protected boolean isChecksumValid(byte[] data) throws KrbCryptoException {
        byte[] cksum1 = checksumField(data);
        byte[] cksum2 = generateChecksum(data);
        return isChecksumEqual(cksum1, cksum2);
    }
}
