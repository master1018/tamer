final class DESedeKey implements SecretKey {
    static final long serialVersionUID = 2463986565756745178L;
    private byte[] key;
    DESedeKey(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }
    DESedeKey(byte[] key, int offset) throws InvalidKeyException {
        if (key==null || ((key.length-offset)<DESedeKeySpec.DES_EDE_KEY_LEN)) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0,
                         DESedeKeySpec.DES_EDE_KEY_LEN);
        DESKeyGenerator.setParityBit(this.key, 0);
        DESKeyGenerator.setParityBit(this.key, 8);
        DESKeyGenerator.setParityBit(this.key, 16);
    }
    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }
    public String getAlgorithm() {
        return "DESede";
    }
    public String getFormat() {
        return "RAW";
    }
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        return(retval ^= "desede".hashCode());
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SecretKey))
            return false;
        String thatAlg = ((SecretKey)obj).getAlgorithm();
        if (!(thatAlg.equalsIgnoreCase("DESede"))
            && !(thatAlg.equalsIgnoreCase("TripleDES")))
            return false;
        byte[] thatKey = ((SecretKey)obj).getEncoded();
        boolean ret = java.util.Arrays.equals(this.key, thatKey);
        java.util.Arrays.fill(thatKey, (byte)0x00);
        return ret;
    }
    private void readObject(java.io.ObjectInputStream s)
         throws java.io.IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        key = (byte[])key.clone();
    }
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.SECRET,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
    protected void finalize() throws Throwable {
        try {
            if (this.key != null) {
                java.util.Arrays.fill(this.key, (byte)0x00);
                this.key = null;
            }
        } finally {
            super.finalize();
        }
    }
}
