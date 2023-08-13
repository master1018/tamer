public class KeyValue extends SignatureElementProxy implements KeyInfoContent {
    public KeyValue(Document doc, DSAKeyValue dsaKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
        this._constructionElement.appendChild(dsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }
    public KeyValue(Document doc, RSAKeyValue rsaKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
        this._constructionElement.appendChild(rsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }
    public KeyValue(Document doc, Element unknownKeyValue) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
        this._constructionElement.appendChild(unknownKeyValue);
        XMLUtils.addReturnToElement(this._constructionElement);
    }
    public KeyValue(Document doc, PublicKey pk) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
        if (pk instanceof java.security.interfaces.DSAPublicKey) {
            DSAKeyValue dsa = new DSAKeyValue(this._doc, pk);
            this._constructionElement.appendChild(dsa.getElement());
            XMLUtils.addReturnToElement(this._constructionElement);
        } else if (pk instanceof java.security.interfaces.RSAPublicKey) {
            RSAKeyValue rsa = new RSAKeyValue(this._doc, pk);
            this._constructionElement.appendChild(rsa.getElement());
            XMLUtils.addReturnToElement(this._constructionElement);
        }
    }
    public KeyValue(Element element, String BaseURI)
           throws XMLSecurityException {
        super(element, BaseURI);
    }
    public PublicKey getPublicKey() throws XMLSecurityException {
        Element rsa = XMLUtils.selectDsNode
            (this._constructionElement.getFirstChild(),
             Constants._TAG_RSAKEYVALUE,0);
        if (rsa != null) {
            RSAKeyValue kv = new RSAKeyValue(rsa, this._baseURI);
            return kv.getPublicKey();
        }
        Element dsa = XMLUtils.selectDsNode
            (this._constructionElement.getFirstChild(),
             Constants._TAG_DSAKEYVALUE,0);
        if (dsa != null) {
            DSAKeyValue kv = new DSAKeyValue(dsa, this._baseURI);
            return kv.getPublicKey();
        }
        return null;
    }
    public String getBaseLocalName() {
        return Constants._TAG_KEYVALUE;
    }
}
