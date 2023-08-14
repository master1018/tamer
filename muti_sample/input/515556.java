public class AlternativeName extends ExtensionValue {
    public static final boolean ISSUER = false;
    public static final boolean SUBJECT = true;
    private boolean which;
    private GeneralNames alternativeNames;
    public AlternativeName(boolean which, GeneralNames alternativeNames) {
        this.which = which;
        this.alternativeNames = alternativeNames;
    }
    public AlternativeName(boolean which, byte[] encoding) throws IOException {
        super(encoding);
        this.which = which;
        this.alternativeNames =
            (GeneralNames) GeneralNames.ASN1.decode(encoding);
    }
    public List getAlternativeNames() {
        return alternativeNames.getPairsList();
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = GeneralNames.ASN1.encode(alternativeNames);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append((which) ? "Subject" : "Issuer") 
            .append(" Alternative Names [\n"); 
        alternativeNames.dumpValue(buffer, prefix + "  "); 
        buffer.append(prefix).append("]\n"); 
    }
}
