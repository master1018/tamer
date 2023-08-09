final class DHPublicKey implements PublicKey,
javax.crypto.interfaces.DHPublicKey, Serializable {
    static final long serialVersionUID = 7647557958927458271L;
    private BigInteger y;
    private byte[] key;
    private byte[] encodedKey;
    private BigInteger p;
    private BigInteger g;
    private int l;
    private int DH_data[] = { 1, 2, 840, 113549, 1, 3, 1 };
    DHPublicKey(BigInteger y, BigInteger p, BigInteger g)
        throws InvalidKeyException {
        this(y, p, g, 0);
    }
    DHPublicKey(BigInteger y, BigInteger p, BigInteger g, int l) {
        this.y = y;
        this.p = p;
        this.g = g;
        this.l = l;
        try {
            this.key = new DerValue(DerValue.tag_Integer,
                                    this.y.toByteArray()).toByteArray();
            this.encodedKey = getEncoded();
        } catch (IOException e) {
            throw new ProviderException("Cannot produce ASN.1 encoding", e);
        }
    }
    DHPublicKey(byte[] encodedKey) throws InvalidKeyException {
        InputStream inStream = new ByteArrayInputStream(encodedKey);
        try {
            DerValue derKeyVal = new DerValue(inStream);
            if (derKeyVal.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException ("Invalid key format");
            }
            DerValue algid = derKeyVal.data.getDerValue();
            if (algid.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException("AlgId is not a SEQUENCE");
            }
            DerInputStream derInStream = algid.toDerInputStream();
            ObjectIdentifier oid = derInStream.getOID();
            if (oid == null) {
                throw new InvalidKeyException("Null OID");
            }
            if (derInStream.available() == 0) {
                throw new InvalidKeyException("Parameters missing");
            }
            DerValue params = derInStream.getDerValue();
            if (params.tag == DerValue.tag_Null) {
                throw new InvalidKeyException("Null parameters");
            }
            if (params.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException("Parameters not a SEQUENCE");
            }
            params.data.reset();
            this.p = params.data.getBigInteger();
            this.g = params.data.getBigInteger();
            if (params.data.available() != 0) {
                this.l = params.data.getInteger();
            }
            if (params.data.available() != 0) {
                throw new InvalidKeyException("Extra parameter data");
            }
            this.key = derKeyVal.data.getBitString();
            parseKeyBits();
            if (derKeyVal.data.available() != 0) {
                throw new InvalidKeyException("Excess key data");
            }
            this.encodedKey = (byte[])encodedKey.clone();
        } catch (NumberFormatException e) {
            throw new InvalidKeyException("Private-value length too big");
        } catch (IOException e) {
            throw new InvalidKeyException(
                "Error parsing key encoding: " + e.toString());
        }
    }
    public String getFormat() {
        return "X.509";
    }
    public String getAlgorithm() {
        return "DH";
    }
    public synchronized byte[] getEncoded() {
        if (this.encodedKey == null) {
            try {
                DerOutputStream algid = new DerOutputStream();
                algid.putOID(new ObjectIdentifier(DH_data));
                DerOutputStream params = new DerOutputStream();
                params.putInteger(this.p);
                params.putInteger(this.g);
                if (this.l != 0)
                    params.putInteger(this.l);
                DerValue paramSequence = new DerValue(DerValue.tag_Sequence,
                                                      params.toByteArray());
                algid.putDerValue(paramSequence);
                DerOutputStream tmpDerKey = new DerOutputStream();
                tmpDerKey.write(DerValue.tag_Sequence, algid);
                tmpDerKey.putBitString(this.key);
                DerOutputStream derKey = new DerOutputStream();
                derKey.write(DerValue.tag_Sequence, tmpDerKey);
                this.encodedKey = derKey.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }
        return (byte[])this.encodedKey.clone();
    }
    public BigInteger getY() {
        return this.y;
    }
    public DHParameterSpec getParams() {
        if (this.l != 0)
            return new DHParameterSpec(this.p, this.g, this.l);
        else
            return new DHParameterSpec(this.p, this.g);
    }
    public String toString() {
        String LINE_SEP = System.getProperty("line.separator");
        StringBuffer strbuf
            = new StringBuffer("SunJCE Diffie-Hellman Public Key:"
                               + LINE_SEP + "y:" + LINE_SEP
                               + Debug.toHexString(this.y)
                               + LINE_SEP + "p:" + LINE_SEP
                               + Debug.toHexString(this.p)
                               + LINE_SEP + "g:" + LINE_SEP
                               + Debug.toHexString(this.g));
        if (this.l != 0)
            strbuf.append(LINE_SEP + "l:" + LINE_SEP + "    " + this.l);
        return strbuf.toString();
    }
    private void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(this.key);
            this.y = in.getBigInteger();
        } catch (IOException e) {
            throw new InvalidKeyException(
                "Error parsing key encoding: " + e.toString());
        }
    }
    public int hashCode() {
        int retval = 0;
        byte[] enc = getEncoded();
        for (int i = 1; i < enc.length; i++) {
            retval += enc[i] * i;
        }
        return(retval);
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof PublicKey))
            return false;
        byte[] thisEncoded = this.getEncoded();
        byte[] thatEncoded = ((PublicKey)obj).getEncoded();
        return java.util.Arrays.equals(thisEncoded, thatEncoded);
    }
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
}
