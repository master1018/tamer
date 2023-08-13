public class SignatureProperties extends SignatureElementProxy {
   public SignatureProperties(Document doc) {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   public SignatureProperties(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public int getLength() {
         Element[] propertyElems =
            XMLUtils.selectDsNodes(this._constructionElement,
                                     Constants._TAG_SIGNATUREPROPERTY
                                    );
         return propertyElems.length;
   }
   public SignatureProperty item(int i) throws XMLSignatureException {
          try {
         Element propertyElem =
            XMLUtils.selectDsNode(this._constructionElement,
                                 Constants._TAG_SIGNATUREPROPERTY,
                                 i );
         if (propertyElem == null) {
            return null;
         }
         return new SignatureProperty(propertyElem, this._baseURI);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public void setId(String Id) {
      if ((Id != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ID, Id);
         IdResolver.registerElementById(this._constructionElement, Id);
      }
   }
   public String getId() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ID);
   }
   public void addSignatureProperty(SignatureProperty sp) {
      this._constructionElement.appendChild(sp.getElement());
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   public String getBaseLocalName() {
      return Constants._TAG_SIGNATUREPROPERTIES;
   }
}
