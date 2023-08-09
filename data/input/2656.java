public class SPKIData extends SignatureElementProxy implements KeyInfoContent {
   public SPKIData(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public String getBaseLocalName() {
      return Constants._TAG_SPKIDATA;
   }
}
