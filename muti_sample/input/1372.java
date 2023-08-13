class KeyImpl implements SecretKey, Destroyable, Serializable {
    private static final long serialVersionUID = -7889313790214321193L;
    private transient byte[] keyBytes;
    private transient int keyType;
    private transient volatile boolean destroyed = false;
    public KeyImpl(byte[] keyBytes,
                       int keyType) {
        this.keyBytes = keyBytes.clone();
        this.keyType = keyType;
    }
    public KeyImpl(KerberosPrincipal principal,
                   char[] password,
                   String algorithm) {
        try {
            PrincipalName princ = new PrincipalName(principal.getName());
            EncryptionKey key =
                new EncryptionKey(password, princ.getSalt(), algorithm);
            this.keyBytes = key.getBytes();
            this.keyType = key.getEType();
        } catch (KrbException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    public final int getKeyType() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return keyType;
    }
    public final String getAlgorithm() {
        return getAlgorithmName(keyType);
    }
    private String getAlgorithmName(int eType) {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        switch (eType) {
        case EncryptedData.ETYPE_DES_CBC_CRC:
        case EncryptedData.ETYPE_DES_CBC_MD5:
            return "DES";
        case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
            return "DESede";
        case EncryptedData.ETYPE_ARCFOUR_HMAC:
            return "ArcFourHmac";
        case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
            return "AES128";
        case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
            return "AES256";
        case EncryptedData.ETYPE_NULL:
            return "NULL";
        default:
            throw new IllegalArgumentException(
                "Unsupported encryption type: " + eType);
        }
    }
    public final String getFormat() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return "RAW";
    }
    public final byte[] getEncoded() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return keyBytes.clone();
    }
    public void destroy() throws DestroyFailedException {
        if (!destroyed) {
            destroyed = true;
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
    public boolean isDestroyed() {
        return destroyed;
    }
    private void writeObject(ObjectOutputStream ois)
                throws IOException {
        if (destroyed) {
           throw new IOException("This key is no longer valid");
        }
        try {
           ois.writeObject((new EncryptionKey(keyType, keyBytes)).asn1Encode());
        } catch (Asn1Exception ae) {
           throw new IOException(ae.getMessage());
        }
    }
    private void readObject(ObjectInputStream ois)
                throws IOException, ClassNotFoundException {
        try {
            EncryptionKey encKey = new EncryptionKey(new
                                     DerValue((byte[])ois.readObject()));
            keyType = encKey.getEType();
            keyBytes = encKey.getBytes();
        } catch (Asn1Exception ae) {
            throw new IOException(ae.getMessage());
        }
    }
    public String toString() {
        HexDumpEncoder hd = new HexDumpEncoder();
        return "EncryptionKey: keyType=" + keyType
                          + " keyBytes (hex dump)="
                          + (keyBytes == null || keyBytes.length == 0 ?
                             " Empty Key" :
                             '\n' + hd.encodeBuffer(keyBytes)
                          + '\n');
    }
    public int hashCode() {
        int result = 17;
        if(isDestroyed()) {
            return result;
        }
        result = 37 * result + Arrays.hashCode(keyBytes);
        return 37 * result + keyType;
    }
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (! (other instanceof KeyImpl)) {
            return false;
        }
        KeyImpl otherKey = ((KeyImpl) other);
        if (isDestroyed() || otherKey.isDestroyed()) {
            return false;
        }
        if(keyType != otherKey.getKeyType() ||
                !Arrays.equals(keyBytes, otherKey.getEncoded())) {
            return false;
        }
        return true;
    }
}
