public class XMLX509SubjectName extends SignatureElementProxy
        implements XMLX509DataContent {
   public XMLX509SubjectName(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public XMLX509SubjectName(Document doc, String X509SubjectNameString) {
      super(doc);
      this.addText(X509SubjectNameString);
   }
   public XMLX509SubjectName(Document doc, X509Certificate x509certificate) {
      this(doc,
           RFC2253Parser.normalize(x509certificate.getSubjectDN().getName()));
   }
   public String getSubjectName() {
      return RFC2253Parser.normalize(this.getTextFromTextChild());
   }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        XMLX509SubjectName other = (XMLX509SubjectName) obj;
        String otherSubject = other.getSubjectName();
        String thisSubject = this.getSubjectName();
        return thisSubject.equals(otherSubject);
   }
   public String getBaseLocalName() {
      return Constants._TAG_X509SUBJECTNAME;
   }
}
