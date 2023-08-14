final class PBEKey implements SecretKey {
    static final long serialVersionUID = -2234768909660948176L;
    private byte[] key;
    private String type;
    PBEKey(PBEKeySpec keySpec, String keytype) throws InvalidKeySpecException {
        char[] passwd = keySpec.getPassword();
        if (passwd == null) {
            passwd = new char[0];
        }
        for (int i=0; i<passwd.length; i++) {
            if ((passwd[i] < '\u0020') || (passwd[i] > '\u007E')) {
                throw new InvalidKeySpecException("Password is not ASCII");
            }
        }
        this.key = new byte[passwd.length];
        for (int i=0; i<passwd.length; i++)
            this.key[i] = (byte) (passwd[i] & 0x7f);
        java.util.Arrays.fill(passwd, ' ');
        type = keytype;
    }
    public byte[] getEncoded() {
        return (byte[])this.key.clone();
    }
    public String getAlgorithm() {
        return type;
    }
    public String getFormat() {
        return "RAW";
    }
    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        return(retval ^= getAlgorithm().toLowerCase().hashCode());
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof SecretKey))
            return false;
        SecretKey that = (SecretKey)obj;
        if (!(that.getAlgorithm().equalsIgnoreCase(type)))
            return false;
        byte[] thatEncoded = that.getEncoded();
        boolean ret = java.util.Arrays.equals(this.key, thatEncoded);
        java.util.Arrays.fill(thatEncoded, (byte)0x00);
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
