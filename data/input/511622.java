public class GeneralSubtrees {
    private List generalSubtrees;
    private byte[] encoding;
    public GeneralSubtrees() {}
    public GeneralSubtrees(List generalSubtrees) {
        this.generalSubtrees = generalSubtrees;
    }
    public List getSubtrees() {
        return generalSubtrees;
    }
    public GeneralSubtrees addSubtree(GeneralSubtree subtree) {
        encoding = null;
        if (generalSubtrees == null) {
            generalSubtrees = new ArrayList();
        }
        generalSubtrees.add(subtree);
        return this;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Type ASN1 = new ASN1SequenceOf(GeneralSubtree.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new GeneralSubtrees((List)in.content);
        }
        public Collection getValues(Object object) {
            GeneralSubtrees gss = (GeneralSubtrees) object;
            return (gss.generalSubtrees == null) 
                ? new ArrayList() : gss.generalSubtrees;
        }
    };
}
