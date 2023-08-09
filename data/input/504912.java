public class PolicyConstraints extends ExtensionValue {
    private final BigInteger requireExplicitPolicy;
    private final BigInteger inhibitPolicyMapping;
    private byte[] encoding;
    public PolicyConstraints() {
        this(null, null);
    }
    public PolicyConstraints(BigInteger requireExplicitPolicy,
            BigInteger inhibitPolicyMapping) {
        this.requireExplicitPolicy = requireExplicitPolicy;
        this.inhibitPolicyMapping = inhibitPolicyMapping;
    }
    public PolicyConstraints(int requireExplicitPolicy,
            int inhibitPolicyMapping) {
        this.requireExplicitPolicy = BigInteger.valueOf(requireExplicitPolicy);
        this.inhibitPolicyMapping = BigInteger.valueOf(inhibitPolicyMapping);
    }
    public PolicyConstraints(byte[] encoding) throws IOException {
        super(encoding);
        PolicyConstraints pc = (PolicyConstraints) ASN1.decode(encoding);
        this.requireExplicitPolicy = pc.requireExplicitPolicy;
        this.inhibitPolicyMapping = pc.inhibitPolicyMapping;
    }
    private PolicyConstraints(BigInteger requireExplicitPolicy, 
                            BigInteger inhibitPolicyMapping, byte[] encoding) {
        this(requireExplicitPolicy, inhibitPolicyMapping);
        this.encoding = encoding;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("PolicyConstraints: [\n"); 
        if (requireExplicitPolicy != null) {
            buffer.append(prefix).append("  requireExplicitPolicy: ") 
                .append(requireExplicitPolicy).append('\n');
        }
        if (inhibitPolicyMapping != null) {
            buffer.append(prefix).append("  inhibitPolicyMapping: ") 
                .append(inhibitPolicyMapping).append('\n');
        }
        buffer.append(prefix).append("]\n"); 
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Implicit(0, ASN1Integer.getInstance()), 
            new ASN1Implicit(1, ASN1Integer.getInstance()) }) {
        {
            setOptional(0);
            setOptional(1);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            BigInteger requireExplicitPolicy = null;
            BigInteger inhibitPolicyMapping = null;
            if (values[0] != null) {
                requireExplicitPolicy = new BigInteger((byte[]) values[0]);
            }
            if (values[1] != null) {
                inhibitPolicyMapping = new BigInteger((byte[]) values[1]);
            }
            return new PolicyConstraints(
                requireExplicitPolicy, inhibitPolicyMapping,
                    in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            PolicyConstraints pc = (PolicyConstraints) object;
            values[0] = pc.requireExplicitPolicy.toByteArray();
            values[1] = pc.inhibitPolicyMapping.toByteArray();
        }
    };
}
