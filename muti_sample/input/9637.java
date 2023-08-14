public abstract class EncryptionElementProxy extends ElementProxy {
   public EncryptionElementProxy(Document doc) {
      super(doc);
   }
   public EncryptionElementProxy(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public final String getBaseNamespace() {
      return EncryptionConstants.EncryptionSpecNS;
   }
}
