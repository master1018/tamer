public class DistributionPointName {
    private final GeneralNames fullName;
    private final Name nameRelativeToCRLIssuer;
    public DistributionPointName(GeneralNames fullName) {
        this.fullName = fullName;
        this.nameRelativeToCRLIssuer = null;
    }
    public DistributionPointName(Name nameRelativeToCRLIssuer) {
        this.fullName = null;
        this.nameRelativeToCRLIssuer = nameRelativeToCRLIssuer;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix);
        buffer.append("Distribution Point Name: [\n"); 
        if (fullName != null) {
            fullName.dumpValue(buffer, prefix + "  "); 
        } else {
            buffer.append(prefix);
            buffer.append("  "); 
            buffer.append(nameRelativeToCRLIssuer.getName(
                        X500Principal.RFC2253));
        } 
        buffer.append(prefix);
        buffer.append("]\n"); 
    }
    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
            new ASN1Implicit(0, GeneralNames.ASN1), 
            new ASN1Implicit(1, Name.ASN1_RDN) }) {
        public int getIndex(java.lang.Object object) {
            DistributionPointName dpn = (DistributionPointName) object;
            return (dpn.fullName == null) ? 1 : 0;
        }
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            DistributionPointName result = null;
            if (in.choiceIndex == 0) {
                result = new DistributionPointName((GeneralNames) in.content);
            } else {
                result = new DistributionPointName((Name) in.content);
            }
            return result;
        }
        public Object getObjectToEncode(Object object) {
            DistributionPointName dpn = (DistributionPointName) object;
            if (dpn.fullName == null) {
                return dpn.nameRelativeToCRLIssuer;
            } else {
                return dpn.fullName;
            }
        }
    };
}
