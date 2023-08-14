public class DistributionPoint {
    private final DistributionPointName distributionPoint;
    private final ReasonFlags reasons;
    private final GeneralNames cRLIssuer;
    public DistributionPoint() {
        distributionPoint = null;
        reasons = null;
        cRLIssuer = null;
    }
    public DistributionPoint(DistributionPointName distributionPoint,
            ReasonFlags reasons, GeneralNames cRLIssuer) {
        if ((reasons != null) && (distributionPoint == null) 
                && (cRLIssuer == null)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.17F")); 
        }
        this.distributionPoint = distributionPoint;
        this.reasons = reasons;
        this.cRLIssuer = cRLIssuer;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix);
        buffer.append("Distribution Point: [\n"); 
        if (distributionPoint != null) {
            distributionPoint.dumpValue(buffer, prefix + "  "); 
        }
        if (reasons != null) {
            reasons.dumpValue(buffer, prefix + "  "); 
        }
        if (cRLIssuer != null) {
            buffer.append(prefix);
            buffer.append("  CRL Issuer: [\n"); 
            cRLIssuer.dumpValue(buffer, prefix + "    "); 
            buffer.append(prefix);
            buffer.append("  ]\n"); 
        }
        buffer.append(prefix);
        buffer.append("]\n"); 
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
                new ASN1Explicit(0, DistributionPointName.ASN1),
                new ASN1Implicit(1, ReasonFlags.ASN1),
                new ASN1Implicit(2, GeneralNames.ASN1)
            }) {
        {
            setOptional(0);
            setOptional(1);
            setOptional(2);
        }
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            return new DistributionPoint((DistributionPointName) values[0], 
                    (ReasonFlags) values[1], (GeneralNames) values[2]);
        }
        protected void getValues(Object object, Object[] values) {
            DistributionPoint dp = (DistributionPoint) object;
            values[0] = dp.distributionPoint;
            values[1] = dp.reasons;
            values[2] = dp.cRLIssuer;
        }
    };
}
