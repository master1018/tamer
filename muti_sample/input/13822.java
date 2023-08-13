public class PGPData extends SignatureElementProxy implements KeyInfoContent {
   public PGPData(Element element, String BaseURI) throws XMLSecurityException {
      super(element, BaseURI);
   }
   public String getBaseLocalName() {
      return Constants._TAG_PGPDATA;
   }
}
