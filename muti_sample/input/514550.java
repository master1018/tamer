public class GeneralNames {
    private List generalNames;
    private byte[] encoding;
    public GeneralNames() {
        generalNames = new ArrayList();
    }
    public GeneralNames(List generalNames) {
        this.generalNames = generalNames;
    }
    private GeneralNames(List generalNames, byte[] encoding) {
        this.generalNames = generalNames;
        this.encoding = encoding;
    }
    public List getNames() {
        if ((generalNames == null) || (generalNames.size() == 0)) {
            return null;
        }
        return new ArrayList(generalNames);
    }
    public List getPairsList() {
        ArrayList result = new ArrayList();
        if (generalNames == null) {
            return result;
        }
        Iterator it = generalNames.iterator();
        while (it.hasNext()) {
            result.add(((GeneralName) it.next()).getAsList());
        }
        return result;
    }
    public void addName(GeneralName name) {
        encoding = null;
        if (generalNames == null) {
            generalNames = new ArrayList();
        }
        generalNames.add(name);
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        if (generalNames == null) {
            return;
        }
        for (Iterator it=generalNames.iterator(); it.hasNext();) {
            buffer.append(prefix);
            buffer.append(it.next());
            buffer.append('\n');
        }
    }
    public static final ASN1Type ASN1 = new ASN1SequenceOf(GeneralName.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new GeneralNames((List)in.content, in.getEncoded());
        }
        public Collection getValues(Object object) {
            GeneralNames gns = (GeneralNames) object;
            return gns.generalNames;
        }
    };
}
