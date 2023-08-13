public class ObjectContainer extends SignatureElementProxy {
   public ObjectContainer(Document doc) {
      super(doc);
   }
   public ObjectContainer(Element element, String BaseURI)
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
   public void setMimeType(String MimeType) {
      if ( (MimeType != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_MIMETYPE,
                                                MimeType);
      }
   }
   public String getMimeType() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_MIMETYPE);
   }
   public void setEncoding(String Encoding) {
      if ((Encoding != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ENCODING,
                                                Encoding);
      }
   }
   public String getEncoding() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ENCODING);
   }
   public Node appendChild(Node node) {
      Node result = null;
      result = this._constructionElement.appendChild(node);
      return result;
   }
   public String getBaseLocalName() {
      return Constants._TAG_OBJECT;
   }
}
