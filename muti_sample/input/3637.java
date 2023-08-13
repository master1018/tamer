public final class DOMPGPData extends DOMStructure implements PGPData {
    private final byte[] keyId;
    private final byte[] keyPacket;
    private final List externalElements;
    public DOMPGPData(byte[] keyPacket, List other) {
        if (keyPacket == null) {
            throw new NullPointerException("keyPacket cannot be null");
        }
        if (other == null || other.isEmpty()) {
            this.externalElements = Collections.EMPTY_LIST;
        } else {
            List otherCopy = new ArrayList(other);
            for (int i = 0, size = otherCopy.size(); i < size; i++) {
                if (!(otherCopy.get(i) instanceof XMLStructure)) {
                    throw new ClassCastException
                        ("other["+i+"] is not a valid PGPData type");
                }
            }
            this.externalElements = Collections.unmodifiableList(otherCopy);
        }
        this.keyPacket = (byte []) keyPacket.clone();
        checkKeyPacket(keyPacket);
        this.keyId = null;
    }
    public DOMPGPData(byte[] keyId, byte[] keyPacket, List other) {
        if (keyId == null) {
            throw new NullPointerException("keyId cannot be null");
        }
        if (keyId.length != 8) {
            throw new IllegalArgumentException("keyId must be 8 bytes long");
        }
        if (other == null || other.isEmpty()) {
            this.externalElements = Collections.EMPTY_LIST;
        } else {
            List otherCopy = new ArrayList(other);
            for (int i = 0, size = otherCopy.size(); i < size; i++) {
                if (!(otherCopy.get(i) instanceof XMLStructure)) {
                    throw new ClassCastException
                        ("other["+i+"] is not a valid PGPData type");
                }
            }
            this.externalElements = Collections.unmodifiableList(otherCopy);
        }
        this.keyId = (byte []) keyId.clone();
        this.keyPacket = keyPacket == null ? null : (byte []) keyPacket.clone();
        if (keyPacket != null) {
            checkKeyPacket(keyPacket);
        }
    }
    public DOMPGPData(Element pdElem) throws MarshalException {
        byte[] keyId = null;
        byte[] keyPacket = null;
        NodeList nl = pdElem.getChildNodes();
        int length = nl.getLength();
        List other = new ArrayList(length);
        for (int x = 0; x < length; x++) {
            Node n = nl.item(x);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element childElem = (Element) n;
                String localName = childElem.getLocalName();
                try {
                    if (localName.equals("PGPKeyID")) {
                        keyId = Base64.decode(childElem);
                    } else if (localName.equals("PGPKeyPacket")){
                        keyPacket = Base64.decode(childElem);
                    } else {
                        other.add
                            (new javax.xml.crypto.dom.DOMStructure(childElem));
                    }
                } catch (Base64DecodingException bde) {
                    throw new MarshalException(bde);
                }
            }
        }
        this.keyId = keyId;
        this.keyPacket = keyPacket;
        this.externalElements = Collections.unmodifiableList(other);
    }
    public byte[] getKeyId() {
        return (keyId == null ? null : (byte []) keyId.clone());
    }
    public byte[] getKeyPacket() {
        return (keyPacket == null ? null : (byte []) keyPacket.clone());
    }
    public List getExternalElements() {
        return externalElements;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element pdElem = DOMUtils.createElement
            (ownerDoc, "PGPData", XMLSignature.XMLNS, dsPrefix);
        if (keyId != null) {
            Element keyIdElem = DOMUtils.createElement
                (ownerDoc, "PGPKeyID", XMLSignature.XMLNS, dsPrefix);
            keyIdElem.appendChild
                (ownerDoc.createTextNode(Base64.encode(keyId)));
            pdElem.appendChild(keyIdElem);
        }
        if (keyPacket != null) {
            Element keyPktElem = DOMUtils.createElement
                (ownerDoc, "PGPKeyPacket", XMLSignature.XMLNS, dsPrefix);
            keyPktElem.appendChild
                (ownerDoc.createTextNode(Base64.encode(keyPacket)));
            pdElem.appendChild(keyPktElem);
        }
        for (int i = 0, size = externalElements.size(); i < size; i++) {
            DOMUtils.appendChild(pdElem, ((javax.xml.crypto.dom.DOMStructure)
                externalElements.get(i)).getNode());
        }
        parent.appendChild(pdElem);
    }
    private void checkKeyPacket(byte[] keyPacket) {
        if (keyPacket.length < 3) {
            throw new IllegalArgumentException("keypacket must be at least " +
                "3 bytes long");
        }
        int tag = keyPacket[0];
        if ((tag & 128) != 128) {
            throw new IllegalArgumentException("keypacket tag is invalid: " +
                "bit 7 is not set");
        }
        if ((tag & 64) != 64) {
            throw new IllegalArgumentException("old keypacket tag format is " +
                "unsupported");
        }
        if (((tag & 6) != 6) && ((tag & 14) != 14) &&
            ((tag & 5) != 5) && ((tag & 7) != 7)) {
            throw new IllegalArgumentException("keypacket tag is invalid: " +
                "must be 6, 14, 5, or 7");
        }
    }
}
