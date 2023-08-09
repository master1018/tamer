final class DESKey implements SecretKey {
    static final long serialVersionUID = 7724971015953279128L;
    private byte[] key;
    DESKey(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }
    DESKey(byte[] key, int offset) throws InvalidKeyException {
        if (key == null || key.length - offset < DESKeySpec.DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[DESKeySpec.DES_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, DESKeySpec.DES_KEY_LEN);
        DESKeyGenerator.setParityBit(this.key, 0);
    }
    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }
    public String getAlgorithm() {
        return "DES";
    }
    public String getFormat() {
        return "RAW";
    }
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        return(retval ^= "des".hashCode());
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SecretKey))
            return false;
        String thatAlg = ((SecretKey)obj).getAlgorithm();
        if (!(thatAlg.equalsIgnoreCase("DES")))
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
