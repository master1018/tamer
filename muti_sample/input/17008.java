final class PBKDF2KeyImpl implements javax.crypto.interfaces.PBEKey {
    static final long serialVersionUID = -2234868909660948157L;
    private char[] passwd;
    private byte[] salt;
    private int iterCount;
    private byte[] key;
    private Mac prf;
    private static byte[] getPasswordBytes(char[] passwd) {
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.wrap(passwd);
        ByteBuffer bb = utf8.encode(cb);
        int len = bb.limit();
        byte[] passwdBytes = new byte[len];
        bb.get(passwdBytes, 0, len);
        return passwdBytes;
    }
    PBKDF2KeyImpl(PBEKeySpec keySpec, String prfAlgo)
        throws InvalidKeySpecException {
        char[] passwd = keySpec.getPassword();
        if (passwd == null) {
            this.passwd = new char[0];
        } else {
            this.passwd = passwd.clone();
        }
        byte[] passwdBytes = getPasswordBytes(this.passwd);
        this.salt = keySpec.getSalt();
        if (salt == null) {
            throw new InvalidKeySpecException("Salt not found");
        }
        this.iterCount = keySpec.getIterationCount();
        if (iterCount == 0) {
            throw new InvalidKeySpecException("Iteration count not found");
        } else if (iterCount < 0) {
            throw new InvalidKeySpecException("Iteration count is negative");
        }
        int keyLength = keySpec.getKeyLength();
        if (keyLength == 0) {
            throw new InvalidKeySpecException("Key length not found");
        } else if (keyLength == 0) {
            throw new InvalidKeySpecException("Key length is negative");
        }
        try {
            this.prf = Mac.getInstance(prfAlgo, "SunJCE");
        } catch (NoSuchAlgorithmException nsae) {
            InvalidKeySpecException ike = new InvalidKeySpecException();
            ike.initCause(nsae);
            throw ike;
        } catch (NoSuchProviderException nspe) {
            InvalidKeySpecException ike = new InvalidKeySpecException();
            ike.initCause(nspe);
            throw ike;
        }
        this.key = deriveKey(prf, passwdBytes, salt, iterCount, keyLength);
    }
    private static byte[] deriveKey(final Mac prf, final byte[] password,
            byte[] salt, int iterCount, int keyLengthInBit) {
        int keyLength = keyLengthInBit/8;
        byte[] key = new byte[keyLength];
        try {
            int hlen = prf.getMacLength();
            int intL = (keyLength + hlen - 1)/hlen; 
            int intR = keyLength - (intL - 1)*hlen; 
            byte[] ui = new byte[hlen];
            byte[] ti = new byte[hlen];
            SecretKey macKey = new SecretKey() {
                @Override
                public String getAlgorithm() {
                    return prf.getAlgorithm();
                }
                @Override
                public String getFormat() {
                    return "RAW";
                }
                @Override
                public byte[] getEncoded() {
                    return password;
                }
                @Override
                public int hashCode() {
                    return Arrays.hashCode(password) * 41 +
                            prf.getAlgorithm().toLowerCase().hashCode();
                }
                @Override
                public boolean equals(Object obj) {
                    if (this == obj) return true;
                    if (this.getClass() != obj.getClass()) return false;
                    SecretKey sk = (SecretKey)obj;
                    return prf.getAlgorithm().equalsIgnoreCase(
                        sk.getAlgorithm()) &&
                        Arrays.equals(password, sk.getEncoded());
                }
            };
            prf.init(macKey);
            byte[] ibytes = new byte[4];
            for (int i = 1; i <= intL; i++) {
                prf.update(salt);
                ibytes[3] = (byte) i;
                ibytes[2] = (byte) ((i >> 8) & 0xff);
                ibytes[1] = (byte) ((i >> 16) & 0xff);
                ibytes[0] = (byte) ((i >> 24) & 0xff);
                prf.update(ibytes);
                prf.doFinal(ui, 0);
                System.arraycopy(ui, 0, ti, 0, ui.length);
                for (int j = 2; j <= iterCount; j++) {
                    prf.update(ui);
                    prf.doFinal(ui, 0);
                    for (int k = 0; k < ui.length; k++) {
                        ti[k] ^= ui[k];
                    }
                }
                if (i == intL) {
                    System.arraycopy(ti, 0, key, (i-1)*hlen, intR);
                } else {
                    System.arraycopy(ti, 0, key, (i-1)*hlen, hlen);
                }
            }
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException("Error deriving PBKDF2 keys");
        }
        return key;
    }
    public byte[] getEncoded() {
        return (byte[]) key.clone();
    }
    public String getAlgorithm() {
        return "PBKDF2With" + prf.getAlgorithm();
    }
    public int getIterationCount() {
        return iterCount;
    }
    public char[] getPassword() {
        return (char[]) passwd.clone();
    }
    public byte[] getSalt() {
        return salt.clone();
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
        SecretKey that = (SecretKey) obj;
        if (!(that.getAlgorithm().equalsIgnoreCase(getAlgorithm())))
            return false;
        if (!(that.getFormat().equalsIgnoreCase("RAW")))
            return false;
        byte[] thatEncoded = that.getEncoded();
        boolean ret = Arrays.equals(key, that.getEncoded());
        java.util.Arrays.fill(thatEncoded, (byte)0x00);
        return ret;
    }
    private Object writeReplace() throws ObjectStreamException {
            return new KeyRep(KeyRep.Type.SECRET, getAlgorithm(),
                              getFormat(), getEncoded());
    }
    protected void finalize() throws Throwable {
        try {
            if (this.passwd != null) {
                java.util.Arrays.fill(this.passwd, (char) '0');
                this.passwd = null;
            }
            if (this.key != null) {
                java.util.Arrays.fill(this.key, (byte)0x00);
                this.key = null;
            }
        } finally {
            super.finalize();
        }
    }
}
