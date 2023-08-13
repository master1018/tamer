public final class DOMX509IssuerSerial extends DOMStructure
    implements X509IssuerSerial {
    private final String issuerName;
    private final BigInteger serialNumber;
    public DOMX509IssuerSerial(String issuerName, BigInteger serialNumber) {
        if (issuerName == null) {
            throw new NullPointerException("issuerName cannot be null");
        }
        if (serialNumber == null) {
            throw new NullPointerException("serialNumber cannot be null");
        }
        new X500Principal(issuerName);
        this.issuerName = issuerName;
        this.serialNumber = serialNumber;
    }
    public DOMX509IssuerSerial(Element isElem) {
        Element iNElem = DOMUtils.getFirstChildElement(isElem);
        Element sNElem = DOMUtils.getNextSiblingElement(iNElem);
        issuerName = iNElem.getFirstChild().getNodeValue();
        serialNumber = new BigInteger(sNElem.getFirstChild().getNodeValue());
    }
    public String getIssuerName() {
        return issuerName;
    }
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element isElem = DOMUtils.createElement
            (ownerDoc, "X509IssuerSerial", XMLSignature.XMLNS, dsPrefix);
        Element inElem = DOMUtils.createElement
            (ownerDoc, "X509IssuerName", XMLSignature.XMLNS, dsPrefix);
        Element snElem = DOMUtils.createElement
            (ownerDoc, "X509SerialNumber", XMLSignature.XMLNS, dsPrefix);
        inElem.appendChild(ownerDoc.createTextNode(issuerName));
        snElem.appendChild(ownerDoc.createTextNode(serialNumber.toString()));
        isElem.appendChild(inElem);
        isElem.appendChild(snElem);
        parent.appendChild(isElem);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof X509IssuerSerial)) {
            return false;
        }
        X509IssuerSerial ois = (X509IssuerSerial) obj;
        return (issuerName.equals(ois.getIssuerName()) &&
            serialNumber.equals(ois.getSerialNumber()));
    }
}
