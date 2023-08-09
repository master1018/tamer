public class PKCS10 {
    public PKCS10(PublicKey publicKey) {
        subjectPublicKeyInfo = publicKey;
        attributeSet = new PKCS10Attributes();
    }
    public PKCS10(PublicKey publicKey, PKCS10Attributes attributes) {
        subjectPublicKeyInfo = publicKey;
        attributeSet = attributes;
    }
    public PKCS10(byte[] data)
    throws IOException, SignatureException, NoSuchAlgorithmException {
        DerInputStream  in;
        DerValue[]      seq;
        AlgorithmId     id;
        byte[]          sigData;
        Signature       sig;
        encoded = data;
        in = new DerInputStream(data);
        seq = in.getSequence(3);
        if (seq.length != 3)
            throw new IllegalArgumentException("not a PKCS #10 request");
        data = seq[0].toByteArray();            
        id = AlgorithmId.parse(seq[1]);
        sigData = seq[2].getBitString();
        BigInteger      serial;
        DerValue        val;
        serial = seq[0].data.getBigInteger();
        if (!serial.equals(BigInteger.ZERO))
            throw new IllegalArgumentException("not PKCS #10 v1");
        subject = new X500Name(seq[0].data);
        subjectPublicKeyInfo = X509Key.parse(seq[0].data.getDerValue());
        if (seq[0].data.available() != 0)
            attributeSet = new PKCS10Attributes(seq[0].data);
        else
            attributeSet = new PKCS10Attributes();
        if (seq[0].data.available() != 0)
            throw new IllegalArgumentException("illegal PKCS #10 data");
        try {
            sig = Signature.getInstance(id.getName());
            sig.initVerify(subjectPublicKeyInfo);
            sig.update(data);
            if (!sig.verify(sigData))
                throw new SignatureException("Invalid PKCS #10 signature");
        } catch (InvalidKeyException e) {
            throw new SignatureException("invalid key");
        }
    }
    public void encodeAndSign(X500Name subject, Signature signature)
    throws CertificateException, IOException, SignatureException {
        DerOutputStream out, scratch;
        byte[]          certificateRequestInfo;
        byte[]          sig;
        if (encoded != null)
            throw new SignatureException("request is already signed");
        this.subject = subject;
        scratch = new DerOutputStream();
        scratch.putInteger(BigInteger.ZERO);            
        subject.encode(scratch);                        
        scratch.write(subjectPublicKeyInfo.getEncoded()); 
        attributeSet.encode(scratch);
        out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, scratch);      
        certificateRequestInfo = out.toByteArray();
        scratch = out;
        signature.update(certificateRequestInfo, 0,
                certificateRequestInfo.length);
        sig = signature.sign();
        AlgorithmId algId = null;
        try {
            algId = AlgorithmId.getAlgorithmId(signature.getAlgorithm());
        } catch (NoSuchAlgorithmException nsae) {
            throw new SignatureException(nsae);
        }
        algId.encode(scratch);     
        scratch.putBitString(sig);                      
        out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, scratch);
        encoded = out.toByteArray();
    }
    public X500Name getSubjectName() { return subject; }
    public PublicKey getSubjectPublicKeyInfo()
        { return subjectPublicKeyInfo; }
    public PKCS10Attributes getAttributes()
        { return attributeSet; }
    public byte[] getEncoded() {
        if (encoded != null)
            return encoded.clone();
        else
            return null;
    }
    public void print(PrintStream out)
    throws IOException, SignatureException {
        if (encoded == null)
            throw new SignatureException("Cert request was not signed");
        BASE64Encoder   encoder = new BASE64Encoder();
        out.println("-----BEGIN NEW CERTIFICATE REQUEST-----");
        encoder.encodeBuffer(encoded, out);
        out.println("-----END NEW CERTIFICATE REQUEST-----");
    }
    public String toString() {
        return "[PKCS #10 certificate request:\n"
            + subjectPublicKeyInfo.toString()
            + " subject: <" + subject + ">" + "\n"
            + " attributes: " + attributeSet.toString()
            + "\n]";
    }
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof PKCS10))
            return false;
        if (encoded == null) 
            return false;
        byte[] otherEncoded = ((PKCS10)other).getEncoded();
        if (otherEncoded == null)
            return false;
        return java.util.Arrays.equals(encoded, otherEncoded);
    }
    public int hashCode() {
        int     retval = 0;
        if (encoded != null)
            for (int i = 1; i < encoded.length; i++)
             retval += encoded[i] * i;
        return(retval);
    }
    private X500Name            subject;
    private PublicKey           subjectPublicKeyInfo;
    private PKCS10Attributes    attributeSet;
    private byte[]              encoded;        
}
