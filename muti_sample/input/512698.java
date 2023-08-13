public class TimeStampReq {
    private final int version;
    private final MessageImprint messageImprint;
    private final String reqPolicy;
    private final BigInteger nonce;
    private final Boolean certReq;
    private final Extensions extensions;
    private byte [] encoding;
    public TimeStampReq(int version, MessageImprint messageImprint,
            String reqPolicy, BigInteger nonce, Boolean certReq,
            Extensions extensions) {
        this.version = version;
        this.messageImprint = messageImprint;
        this.reqPolicy = reqPolicy;
        this.nonce = nonce;
        this.certReq = certReq;
        this.extensions = extensions;
    }
    private TimeStampReq(int version, MessageImprint messageImprint,
            String reqPolicy, BigInteger nonce, Boolean certReq,
            Extensions extensions, byte [] encoding) {
        this (version, messageImprint, reqPolicy, nonce, certReq, extensions);
        this.encoding = encoding;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("-- TimeStampReq:");
        res.append("\nversion : ");
        res.append(version);
        res.append("\nmessageImprint:  ");
        res.append(messageImprint);
        res.append("\nreqPolicy:  ");
        res.append(reqPolicy);
        res.append("\nnonce:  ");
        res.append(nonce);
        res.append("\ncertReq:  ");
        res.append(certReq);
        res.append("\nextensions:  ");
        res.append(extensions);
        res.append("\n-- TimeStampReq End\n");
        return res.toString();
    }
    public byte [] getEncoded(){
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public Boolean getCertReq() {
        return certReq;
    }
    public Extensions getExtensions() {
        return extensions;
    }
    public MessageImprint getMessageImprint() {
        return messageImprint;
    }
    public BigInteger getNonce() {
        return nonce;
    }
    public String getReqPolicy() {
        return reqPolicy;
    }
    public int getVersion() {
        return version;
    }    
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(),              
            MessageImprint.ASN1,                    
            ASN1Oid.getInstance(),                  
            ASN1Integer.getInstance(),              
            ASN1Boolean.getInstance(),              
            new ASN1Implicit(0, Extensions.ASN1)}) {
        {
            setDefault(Boolean.FALSE, 4);
            setOptional(2);
            setOptional(3);
            setOptional(5);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            String objID = (values[2] == null) ? null : ObjectIdentifier
                    .toString((int[]) values[2]);
            BigInteger nonce = (values[3] == null) ? null : new BigInteger(
                    (byte[]) values[3]);
            if (values[5] == null) {
                return new TimeStampReq(
                        ASN1Integer.toIntValue(values[0]),
                        (MessageImprint) values[1],
                        objID,
                        nonce,
                        (Boolean) values[4],
                        null,
                        in.getEncoded()
                   );
            } else {
                return new TimeStampReq(
                        ASN1Integer.toIntValue(values[0]),
                        (MessageImprint) values[1],
                        objID,
                        nonce,
                        (Boolean) values[4],
                        (Extensions) values[5],
                        in.getEncoded()
                   );
            }
        }
        protected void getValues(Object object, Object[] values) {
            TimeStampReq req = (TimeStampReq) object;
            values[0] = ASN1Integer.fromIntValue(req.version);
            values[1] = req.messageImprint;
            values[2] = (req.reqPolicy == null) ? null : ObjectIdentifier
                    .toIntArray(req.reqPolicy);
            values[3] = (req.nonce == null) ? null : req.nonce.toByteArray();
            values[4] = (req.certReq == null) ? Boolean.FALSE : req.certReq;
            values[5] = req.extensions;
        }
    };
}
