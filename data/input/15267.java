final class DHPrivateKey implements PrivateKey,
javax.crypto.interfaces.DHPrivateKey, Serializable {
    static final long serialVersionUID = 7565477590005668886L;
    private static final BigInteger PKCS8_VERSION = BigInteger.ZERO;
    private BigInteger x;
    private byte[] key;
    private byte[] encodedKey;
    private BigInteger p;
    private BigInteger g;
    private int l;
    private int DH_data[] = { 1, 2, 840, 113549, 1, 3, 1 };
    DHPrivateKey(BigInteger x, BigInteger p, BigInteger g)
        throws InvalidKeyException {
        this(x, p, g, 0);
    }
    DHPrivateKey(BigInteger x, BigInteger p, BigInteger g, int l) {
        this.x = x;
        this.p = p;
        this.g = g;
        this.l = l;
        try {
            this.key = new DerValue(DerValue.tag_Integer,
                                    this.x.toByteArray()).toByteArray();
            this.encodedKey = getEncoded();
        } catch (IOException e) {
            throw new ProviderException("Cannot produce ASN.1 encoding", e);
        }
    }
    DHPrivateKey(byte[] encodedKey) throws InvalidKeyException {
        InputStream inStream = new ByteArrayInputStream(encodedKey);
        try {
            DerValue val = new DerValue(inStream);
            if (val.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException ("Key not a SEQUENCE");
            }
            BigInteger parsedVersion = val.data.getBigInteger();
            if (!parsedVersion.equals(PKCS8_VERSION)) {
                throw new IOException("version mismatch: (supported: " +
                                      PKCS8_VERSION + ", parsed: " +
                                      parsedVersion);
            }
            DerValue algid = val.data.getDerValue();
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
            this.key = val.data.getOctetString();
            parseKeyBits();
            this.encodedKey = (byte[])encodedKey.clone();
        } catch (NumberFormatException e) {
            InvalidKeyException ike = new InvalidKeyException(
                "Private-value length too big");
            ike.initCause(e);
            throw ike;
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException(
                "Error parsing key encoding: " + e.getMessage());
            ike.initCause(e);
            throw ike;
        }
    }
    public String getFormat() {
        return "PKCS#8";
    }
    public String getAlgorithm() {
        return "DH";
    }
    public synchronized byte[] getEncoded() {
        if (this.encodedKey == null) {
            try {
                DerOutputStream tmp = new DerOutputStream();
                tmp.putInteger(PKCS8_VERSION);
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
                tmp.write(DerValue.tag_Sequence, algid);
                tmp.putOctetString(this.key);
                DerOutputStream derKey = new DerOutputStream();
                derKey.write(DerValue.tag_Sequence, tmp);
                this.encodedKey = derKey.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }
        return (byte[])this.encodedKey.clone();
    }
    public BigInteger getX() {
        return this.x;
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
            = new StringBuffer("SunJCE Diffie-Hellman Private Key:"
                               + LINE_SEP + "x:" + LINE_SEP
                               + Debug.toHexString(this.x)
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
            this.x = in.getBigInteger();
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException(
                "Error parsing key encoding: " + e.getMessage());
            ike.initCause(e);
            throw ike;
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
        if (!(obj instanceof PrivateKey))
            return false;
        byte[] thisEncoded = this.getEncoded();
        byte[] thatEncoded = ((PrivateKey)obj).getEncoded();
        return java.util.Arrays.equals(thisEncoded, thatEncoded);
    }
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PRIVATE,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
}
