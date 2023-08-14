public class KeyName extends SignatureElementProxy implements KeyInfoContent {
   public KeyName(Element element, String BaseURI) throws XMLSecurityException {
      super(element, BaseURI);
   }
   public KeyName(Document doc, String keyName) {
      super(doc);
      this.addText(keyName);
   }
   public String getKeyName() {
      return this.getTextFromTextChild();
   }
   public String getBaseLocalName() {
      return Constants._TAG_KEYNAME;
   }
}
