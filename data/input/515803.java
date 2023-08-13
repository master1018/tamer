public class PolicyInformation {
    private String policyIdentifier;
    private byte[] encoding;
    public PolicyInformation(String policyIdentifier) {
        this.policyIdentifier = policyIdentifier;
    }
    public String getPolicyIdentifier() {
        return policyIdentifier;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer) {
        buffer.append("Policy Identifier [") 
            .append(policyIdentifier).append(']');
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(
            new ASN1Type[] { ASN1Oid.getInstance(), ASN1Any.getInstance() }) {
        {
            setOptional(1);
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new PolicyInformation(ObjectIdentifier
                    .toString((int[]) values[0]));
        }
        protected void getValues(Object object, Object[] values) {
            PolicyInformation pi = (PolicyInformation) object;
            values[0] = ObjectIdentifier.toIntArray(pi.policyIdentifier);
        }
    };
}
