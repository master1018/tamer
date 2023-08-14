public abstract class Algorithm extends SignatureElementProxy {
   public Algorithm(Document doc, String algorithmURI) {
      super(doc);
      this.setAlgorithmURI(algorithmURI);
   }
   public Algorithm(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public String getAlgorithmURI() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ALGORITHM);
   }
   protected void setAlgorithmURI(String algorithmURI) {
      if ( (algorithmURI != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ALGORITHM,
                                                algorithmURI);
      }
   }
}
