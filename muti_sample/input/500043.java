public class CRLDistributionPoints extends ExtensionValue {
    private List distributionPoints;
    private byte[] encoding;
    public CRLDistributionPoints(List distributionPoints) {
        if ((distributionPoints == null) 
                || (distributionPoints.size() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.17D")); 
        }
        this.distributionPoints = distributionPoints;
    }
    public CRLDistributionPoints(List distributionPoints, byte[] encoding) {
        if ((distributionPoints == null) 
                || (distributionPoints.size() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.17D")); 
        }
        this.distributionPoints = distributionPoints;
        this.encoding = encoding;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static CRLDistributionPoints decode(byte[] encoding) 
            throws IOException {
        CRLDistributionPoints cdp = (CRLDistributionPoints) ASN1.decode(encoding);
        return cdp;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("CRL Distribution Points: [\n"); 
        int number = 0;
        for (Iterator it=distributionPoints.iterator();
                it.hasNext();) {
            buffer.append(prefix).append("  [").append(++number).append("]\n"); 
            ((DistributionPoint) it.next()).dumpValue(buffer, prefix + "  "); 
        }
        buffer.append(prefix).append("]\n"); 
    }
    public static final ASN1Type ASN1 = 
        new ASN1SequenceOf(DistributionPoint.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new CRLDistributionPoints((List)in.content, 
                    in.getEncoded());
        }
        public Collection getValues(Object object) {
            CRLDistributionPoints dps = (CRLDistributionPoints) object;
            return dps.distributionPoints;
        }
    };
}
