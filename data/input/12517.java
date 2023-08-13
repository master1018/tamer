public class SignatureProperty extends SignatureElementProxy {
   public SignatureProperty(Document doc, String Target) {
      this(doc, Target, null);
   }
   public SignatureProperty(Document doc, String Target, String Id) {
      super(doc);
      this.setTarget(Target);
      this.setId(Id);
   }
   public SignatureProperty(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
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
   public void setTarget(String Target) {
      if ((Target != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_TARGET, Target);
      }
   }
   public String getTarget() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_TARGET);
   }
   public Node appendChild(Node node) {
      return this._constructionElement.appendChild(node);
   }
   public String getBaseLocalName() {
      return Constants._TAG_SIGNATUREPROPERTY;
   }
}
