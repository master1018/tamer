public class TSTInfo {
    private final int version;
    private final String policy;
    private final MessageImprint messageImprint;
    private final BigInteger serialNumber;
    private final Date genTime;
    private final int [] accuracy;
    private final Boolean ordering;
    private final BigInteger nonce;
    private final GeneralName tsa;
    private final Extensions extensions;
    public TSTInfo(int version, String policy, MessageImprint messageImprint,
            BigInteger serialNumber, Date genTime, int[] accuracy,
            Boolean ordering, BigInteger nonce, GeneralName tsa,
            Extensions extensions) {
        this.version = version;
        this.policy = policy;
        this.messageImprint = messageImprint;
        this.serialNumber = serialNumber;
        this.genTime = genTime;
        this.accuracy = accuracy;
        this.ordering = ordering;
        this.nonce = nonce;
        this.tsa = tsa;
        this.extensions = extensions;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("-- TSTInfo:");
        res.append("\nversion:  ");
        res.append(version);
        res.append("\npolicy:  ");
        res.append(policy);
        res.append("\nmessageImprint:  ");
        res.append(messageImprint);
        res.append("\nserialNumber:  ");
        res.append(serialNumber);
        res.append("\ngenTime:  ");
        res.append(genTime);
        res.append("\naccuracy:  ");
        if (accuracy != null) {
            res.append(accuracy[0] + " sec, " + accuracy[1] + " millis, "
                    + accuracy[2] + " micros");
        }
        res.append("\nordering:  ");
        res.append(ordering);
        res.append("\nnonce:  ");
        res.append(nonce);
        res.append("\ntsa:  ");
        res.append(tsa);
        res.append("\nextensions:  ");
        res.append(extensions);
        res.append("\n-- TSTInfo End\n");
        return res.toString();
    }
    public int[] getAccuracy() {
        return accuracy;
    }
    public Extensions getExtensions() {
        return extensions;
    }
    public Date getGenTime() {
        return genTime;
    }
    public MessageImprint getMessageImprint() {
        return messageImprint;
    }
    public BigInteger getNonce() {
        return nonce;
    }
    public Boolean getOrdering() {
        return ordering;
    }
    public String getPolicy() {
        return policy;
    }
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    public GeneralName getTsa() {
        return tsa;
    }
    public int getVersion() {
        return version;
    }
    public static final ASN1Sequence ACCURACY
            = new ASN1Sequence(new ASN1Type[] {
                    ASN1Integer.getInstance(),
                    ASN1Integer.getInstance(),
                    ASN1Integer.getInstance()
            }) {
        {
            setOptional(0);
            setOptional(1);
            setOptional(2);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            int [] accuracy = new int [3];
            for (int i = 0; i < 3; i++) {
                if (values[i] != null) {
                    accuracy[i] = ASN1Integer.toIntValue(values[i]);
                    if (i > 0 && (accuracy[i] < 0 || accuracy[i] > 999)) {
                        throw new RuntimeException(
                                Messages.getString("security.1A3", accuracy[i]));
                    }
                }
            }
            return accuracy;
        }
        protected void getValues(Object object, Object[] values) {
            int [] accuracy = (int []) object;
            for (int i = 0; i < 3; i++) {
                if (i > 0 && (accuracy[i] < 0 || accuracy[i] > 999)) {
                    throw new RuntimeException(
                            Messages.getString("security.1A3", accuracy[i]));
                }
                values[i] = BigInteger.valueOf(accuracy[i]).toByteArray();
            }
        }
    };
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] { 
            ASN1Integer.getInstance(),              
            ASN1Oid.getInstance(),                  
            MessageImprint.ASN1,                    
            ASN1Integer.getInstance(),              
            ASN1GeneralizedTime.getInstance(),      
            ACCURACY,                               
            ASN1Boolean.getInstance(),              
            ASN1Integer.getInstance(),              
            new ASN1Explicit(0, GeneralName.ASN1),  
            new ASN1Implicit(1, Extensions.ASN1) }) {
        {
            setOptional(5);
            setDefault(Boolean.FALSE, 6);
            setOptional(7);
            setOptional(8);
            setOptional(9);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            BigInteger nonce = (values[7] == null) ? null : new BigInteger(
                    (byte[]) values[7]);
            return new TSTInfo(
                    ASN1Integer.toIntValue(values[0]),
                    ObjectIdentifier.toString((int[]) values[1]),
                    (MessageImprint) values[2],
                    new BigInteger((byte[]) values[3]),
                    (Date) values[4],
                    (int []) values[5],
                    (Boolean) values[6],
                    nonce,
                    (GeneralName) values[8],
                    (Extensions) values[9]);
        }
        protected void getValues(Object object, Object[] values) {
            TSTInfo info = (TSTInfo) object;
            values[0] = ASN1Integer.fromIntValue(info.version);
            values[1] = ObjectIdentifier.toIntArray(info.policy);
            values[2] = info.messageImprint;
            values[3] = info.serialNumber.toByteArray();
            values[4] = info.genTime;
            values[5] = info.accuracy;
            values[6] = info.ordering;
            values[7] = (info.nonce == null) ? null : info.nonce.toByteArray();
            values[8] = info.tsa;
            values[9] = info.extensions;
        }
    };
}
