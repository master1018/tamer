public class XMLX509CRL extends SignatureElementProxy
        implements XMLX509DataContent {
   public XMLX509CRL(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public XMLX509CRL(Document doc, byte[] crlBytes) {
      super(doc);
      this.addBase64Text(crlBytes);
   }
   public byte[] getCRLBytes() throws XMLSecurityException {
      return this.getBytesFromTextChild();
   }
   public String getBaseLocalName() {
      return Constants._TAG_X509CRL;
   }
}
