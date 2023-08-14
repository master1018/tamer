public final class DesCbcMd5EType extends DesCbcEType {
    public DesCbcMd5EType() {
    }
    public int eType() {
        return EncryptedData.ETYPE_DES_CBC_MD5;
    }
    public int minimumPadSize() {
        return 0;
    }
    public int confounderSize() {
        return 8;
    }
    public int checksumType() {
        return Checksum.CKSUMTYPE_RSA_MD5;
    }
    public int checksumSize() {
        return 16;
    }
    protected byte[] calculateChecksum(byte[] data, int size)
         throws KrbCryptoException {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new KrbCryptoException("JCE provider may not be installed. " + e.getMessage());
        }
        try {
            md5.update(data);
            return(md5.digest());
        } catch (Exception e) {
            throw new KrbCryptoException(e.getMessage());
        }
    }
}
