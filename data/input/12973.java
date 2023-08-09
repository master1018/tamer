public class MgmtData extends SignatureElementProxy implements KeyInfoContent {
   public MgmtData(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public MgmtData(Document doc, String mgmtData) {
      super(doc);
      this.addText(mgmtData);
   }
   public String getMgmtData() {
      return this.getTextFromTextChild();
   }
   public String getBaseLocalName() {
      return Constants._TAG_MGMTDATA;
   }
}
