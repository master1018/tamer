public class X509Key implements PublicKey {
    private static final long serialVersionUID = -5359250853002055002L;
    protected AlgorithmId algid;
    @Deprecated
    protected byte[] key = null;
    private int unusedBits = 0;
    private BitArray bitStringKey = null;
    protected byte[] encodedKey;
    public X509Key() { }
    private X509Key(AlgorithmId algid, BitArray key)
    throws InvalidKeyException {
        this.algid = algid;
        setKey(key);
        encode();
    }
    protected void setKey(BitArray key) {
        this.bitStringKey = (BitArray)key.clone();
        this.key = key.toByteArray();
        int remaining = key.length() % 8;
        this.unusedBits =
            ((remaining == 0) ? 0 : 8 - remaining);
    }
    protected BitArray getKey() {
        this.bitStringKey = new BitArray(
                          this.key.length * 8 - this.unusedBits,
                          this.key);
        return (BitArray)bitStringKey.clone();
    }
    public static PublicKey parse(DerValue in) throws IOException
    {
        AlgorithmId     algorithm;
        PublicKey       subjectKey;
        if (in.tag != DerValue.tag_Sequence)
            throw new IOException("corrupt subject key");
        algorithm = AlgorithmId.parse(in.data.getDerValue());
        try {
            subjectKey = buildX509Key(algorithm,
                                      in.data.getUnalignedBitString());
        } catch (InvalidKeyException e) {
            throw new IOException("subject key, " + e.getMessage(), e);
        }
        if (in.data.available() != 0)
            throw new IOException("excess subject key");
        return subjectKey;
    }
    protected void parseKeyBits() throws IOException, InvalidKeyException {
        encode();
    }
    static PublicKey buildX509Key(AlgorithmId algid, BitArray key)
      throws IOException, InvalidKeyException
    {
        DerOutputStream x509EncodedKeyStream = new DerOutputStream();
        encode(x509EncodedKeyStream, algid, key);
        X509EncodedKeySpec x509KeySpec
            = new X509EncodedKeySpec(x509EncodedKeyStream.toByteArray());
        try {
            KeyFactory keyFac = KeyFactory.getInstance(algid.getName());
            return keyFac.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException(e.getMessage(), e);
        }
        String classname = "";
        try {
            Properties props;
            String keytype;
            Provider sunProvider;
            sunProvider = Security.getProvider("SUN");
            if (sunProvider == null)
                throw new InstantiationException();
            classname = sunProvider.getProperty("PublicKey.X.509." +
              algid.getName());
            if (classname == null) {
                throw new InstantiationException();
            }
            Class keyClass = null;
            try {
                keyClass = Class.forName(classname);
            } catch (ClassNotFoundException e) {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    keyClass = cl.loadClass(classname);
                }
            }
            Object      inst = null;
            X509Key     result;
            if (keyClass != null)
                inst = keyClass.newInstance();
            if (inst instanceof X509Key) {
                result = (X509Key) inst;
                result.algid = algid;
                result.setKey(key);
                result.parseKeyBits();
                return result;
            }
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
            throw new IOException (classname + " [internal error]");
        }
        X509Key result = new X509Key(algid, key);
        return result;
    }
    public String getAlgorithm() {
        return algid.getName();
    }
    public AlgorithmId  getAlgorithmId() { return algid; }
    public final void encode(DerOutputStream out) throws IOException
    {
        encode(out, this.algid, getKey());
    }
    public byte[] getEncoded() {
        try {
            return getEncodedInternal().clone();
        } catch (InvalidKeyException e) {
        }
        return null;
    }
    public byte[] getEncodedInternal() throws InvalidKeyException {
        byte[] encoded = encodedKey;
        if (encoded == null) {
            try {
                DerOutputStream out = new DerOutputStream();
                encode(out);
                encoded = out.toByteArray();
            } catch (IOException e) {
                throw new InvalidKeyException("IOException : " +
                                               e.getMessage());
            }
            encodedKey = encoded;
        }
        return encoded;
    }
    public String getFormat() {
        return "X.509";
    }
    public byte[] encode() throws InvalidKeyException {
        return getEncodedInternal().clone();
    }
    public String toString()
    {
        HexDumpEncoder  encoder = new HexDumpEncoder();
        return "algorithm = " + algid.toString()
            + ", unparsed keybits = \n" + encoder.encodeBuffer(key);
    }
    public void decode(InputStream in)
    throws InvalidKeyException
    {
        DerValue        val;
        try {
            val = new DerValue(in);
            if (val.tag != DerValue.tag_Sequence)
                throw new InvalidKeyException("invalid key format");
            algid = AlgorithmId.parse(val.data.getDerValue());
            setKey(val.data.getUnalignedBitString());
            parseKeyBits();
            if (val.data.available() != 0)
                throw new InvalidKeyException ("excess key data");
        } catch (IOException e) {
            throw new InvalidKeyException("IOException: " +
                                          e.getMessage());
        }
    }
    public void decode(byte[] encodedKey) throws InvalidKeyException {
        decode(new ByteArrayInputStream(encodedKey));
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.write(getEncoded());
    }
    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            decode(stream);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IOException("deserialized key is invalid: " +
                                  e.getMessage());
        }
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Key == false) {
            return false;
        }
        try {
            byte[] thisEncoded = this.getEncodedInternal();
            byte[] otherEncoded;
            if (obj instanceof X509Key) {
                otherEncoded = ((X509Key)obj).getEncodedInternal();
            } else {
                otherEncoded = ((Key)obj).getEncoded();
            }
            return Arrays.equals(thisEncoded, otherEncoded);
        } catch (InvalidKeyException e) {
            return false;
        }
    }
    public int hashCode() {
        try {
            byte[] b1 = getEncodedInternal();
            int r = b1.length;
            for (int i = 0; i < b1.length; i++) {
                r += (b1[i] & 0xff) * 37;
            }
            return r;
        } catch (InvalidKeyException e) {
            return 0;
        }
    }
    static void encode(DerOutputStream out, AlgorithmId algid, BitArray key)
        throws IOException {
            DerOutputStream tmp = new DerOutputStream();
            algid.encode(tmp);
            tmp.putUnalignedBitString(key);
            out.write(DerValue.tag_Sequence, tmp);
    }
}
