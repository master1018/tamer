public final class DOMSignedInfo extends DOMStructure implements SignedInfo {
    private static Logger log = Logger.getLogger("org.jcp.xml.dsig.internal.dom");
    private List references;
    private CanonicalizationMethod canonicalizationMethod;
    private SignatureMethod signatureMethod;
    private String id;
    private Document ownerDoc;
    private Element localSiElem;
    private InputStream canonData;
    public DOMSignedInfo(CanonicalizationMethod cm, SignatureMethod sm,
        List references) {
        if (cm == null || sm == null || references == null) {
            throw new NullPointerException();
        }
        this.canonicalizationMethod = cm;
        this.signatureMethod = sm;
        this.references = Collections.unmodifiableList
            (new ArrayList(references));
        if (this.references.isEmpty()) {
            throw new IllegalArgumentException("list of references must " +
                "contain at least one entry");
        }
        for (int i = 0, size = this.references.size(); i < size; i++) {
            Object obj = this.references.get(i);
            if (!(obj instanceof Reference)) {
                throw new ClassCastException("list of references contains " +
                    "an illegal type");
            }
        }
    }
    public DOMSignedInfo(CanonicalizationMethod cm, SignatureMethod sm,
        List references, String id) {
        this(cm, sm, references);
        this.id = id;
    }
    public DOMSignedInfo(Element siElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        localSiElem = siElem;
        ownerDoc = siElem.getOwnerDocument();
        id = DOMUtils.getAttributeValue(siElem, "Id");
        Element cmElem = DOMUtils.getFirstChildElement(siElem);
        canonicalizationMethod = new DOMCanonicalizationMethod
            (cmElem, context, provider);
        Element smElem = DOMUtils.getNextSiblingElement(cmElem);
        signatureMethod = DOMSignatureMethod.unmarshal(smElem);
        ArrayList refList = new ArrayList(5);
        Element refElem = DOMUtils.getNextSiblingElement(smElem);
        while (refElem != null) {
            refList.add(new DOMReference(refElem, context, provider));
            refElem = DOMUtils.getNextSiblingElement(refElem);
        }
        references = Collections.unmodifiableList(refList);
    }
    public CanonicalizationMethod getCanonicalizationMethod() {
        return canonicalizationMethod;
    }
    public SignatureMethod getSignatureMethod() {
        return signatureMethod;
    }
    public String getId() {
        return id;
    }
    public List getReferences() {
        return references;
    }
    public InputStream getCanonicalizedData() {
        return canonData;
    }
    public void canonicalize(XMLCryptoContext context,ByteArrayOutputStream bos)
        throws XMLSignatureException {
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        OutputStream os = new UnsyncBufferedOutputStream(bos);
        try {
            os.close();
        } catch (IOException e) {
        }
        DOMSubTreeData subTree = new DOMSubTreeData(localSiElem, true);
        try {
            Data data = ((DOMCanonicalizationMethod)
                canonicalizationMethod).canonicalize(subTree, context, os);
        } catch (TransformException te) {
            throw new XMLSignatureException(te);
        }
        byte[] signedInfoBytes = bos.toByteArray();
        if (log.isLoggable(Level.FINE)) {
            InputStreamReader isr = new InputStreamReader
                (new ByteArrayInputStream(signedInfoBytes));
            char[] siBytes = new char[signedInfoBytes.length];
            try {
                isr.read(siBytes);
                log.log(Level.FINE, "Canonicalized SignedInfo:\n"
                    + new String(siBytes));
            } catch (IOException ioex) {
                log.log(Level.FINE, "IOException reading SignedInfo bytes");
            }
            log.log(Level.FINE, "Data to be signed/verified:"
                + Base64.encode(signedInfoBytes));
        }
        this.canonData = new ByteArrayInputStream(signedInfoBytes);
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element siElem = DOMUtils.createElement
            (ownerDoc, "SignedInfo", XMLSignature.XMLNS, dsPrefix);
        DOMCanonicalizationMethod dcm =
            (DOMCanonicalizationMethod) canonicalizationMethod;
        dcm.marshal(siElem, dsPrefix, context);
        ((DOMSignatureMethod) signatureMethod).marshal
            (siElem, dsPrefix, context);
        for (int i = 0, size = references.size(); i < size; i++) {
            DOMReference reference = (DOMReference) references.get(i);
            reference.marshal(siElem, dsPrefix, context);
        }
        DOMUtils.setAttributeID(siElem, "Id", id);
        parent.appendChild(siElem);
        localSiElem = siElem;
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignedInfo)) {
            return false;
        }
        SignedInfo osi = (SignedInfo) o;
        boolean idEqual = (id == null ? osi.getId() == null :
            id.equals(osi.getId()));
        return (canonicalizationMethod.equals(osi.getCanonicalizationMethod())
            && signatureMethod.equals(osi.getSignatureMethod()) &&
            references.equals(osi.getReferences()) && idEqual);
    }
}
