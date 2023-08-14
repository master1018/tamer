public class X509Data extends SignatureElementProxy implements KeyInfoContent {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(X509Data.class.getName());
   public X509Data(Document doc) {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   public X509Data(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
      Node sibling=this._constructionElement.getFirstChild();
      while (sibling!=null) {
         if (sibling.getNodeType()!=Node.ELEMENT_NODE) {
                sibling=sibling.getNextSibling();
            continue;
         }
         return;
      }
      Object exArgs[] = { "Elements", Constants._TAG_X509DATA };
      throw new XMLSecurityException("xml.WrongContent", exArgs);
   }
   public void addIssuerSerial(String X509IssuerName,
                               BigInteger X509SerialNumber) {
      this.add(new XMLX509IssuerSerial(this._doc, X509IssuerName,
                                       X509SerialNumber));
   }
   public void addIssuerSerial(String X509IssuerName, String X509SerialNumber) {
      this.add(new XMLX509IssuerSerial(this._doc, X509IssuerName,
                                       X509SerialNumber));
   }
   public void addIssuerSerial(String X509IssuerName, int X509SerialNumber) {
      this.add(new XMLX509IssuerSerial(this._doc, X509IssuerName,
                                       X509SerialNumber));
   }
   public void add(XMLX509IssuerSerial xmlX509IssuerSerial) {
         this._constructionElement
            .appendChild(xmlX509IssuerSerial.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void addSKI(byte[] skiBytes) {
      this.add(new XMLX509SKI(this._doc, skiBytes));
   }
   public void addSKI(X509Certificate x509certificate)
           throws XMLSecurityException {
      this.add(new XMLX509SKI(this._doc, x509certificate));
   }
   public void add(XMLX509SKI xmlX509SKI) {
         this._constructionElement.appendChild(xmlX509SKI.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void addSubjectName(String subjectName) {
      this.add(new XMLX509SubjectName(this._doc, subjectName));
   }
   public void addSubjectName(X509Certificate x509certificate) {
      this.add(new XMLX509SubjectName(this._doc, x509certificate));
   }
   public void add(XMLX509SubjectName xmlX509SubjectName) {
         this._constructionElement.appendChild(xmlX509SubjectName.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void addCertificate(X509Certificate x509certificate)
           throws XMLSecurityException {
      this.add(new XMLX509Certificate(this._doc, x509certificate));
   }
   public void addCertificate(byte[] x509certificateBytes) {
      this.add(new XMLX509Certificate(this._doc, x509certificateBytes));
   }
   public void add(XMLX509Certificate xmlX509Certificate) {
         this._constructionElement.appendChild(xmlX509Certificate.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void addCRL(byte[] crlBytes) {
      this.add(new XMLX509CRL(this._doc, crlBytes));
   }
   public void add(XMLX509CRL xmlX509CRL) {
         this._constructionElement.appendChild(xmlX509CRL.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public void addUnknownElement(Element element) {
         this._constructionElement.appendChild(element);
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public int lengthIssuerSerial() {
      return this.length(Constants.SignatureSpecNS,
                         Constants._TAG_X509ISSUERSERIAL);
   }
   public int lengthSKI() {
      return this.length(Constants.SignatureSpecNS, Constants._TAG_X509SKI);
   }
   public int lengthSubjectName() {
      return this.length(Constants.SignatureSpecNS,
                         Constants._TAG_X509SUBJECTNAME);
   }
   public int lengthCertificate() {
      return this.length(Constants.SignatureSpecNS,
                         Constants._TAG_X509CERTIFICATE);
   }
   public int lengthCRL() {
      return this.length(Constants.SignatureSpecNS, Constants._TAG_X509CRL);
   }
   public int lengthUnknownElement() {
      int result = 0;
      Node n=this._constructionElement.getFirstChild();
      while (n!=null){
         if ((n.getNodeType() == Node.ELEMENT_NODE)
                 &&!n.getNamespaceURI().equals(Constants.SignatureSpecNS)) {
            result += 1;
         }
         n=n.getNextSibling();
      }
      return result;
   }
   public XMLX509IssuerSerial itemIssuerSerial(int i)
           throws XMLSecurityException {
      Element e =
        XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                       Constants._TAG_X509ISSUERSERIAL,i);
      if (e != null) {
         return new XMLX509IssuerSerial(e, this._baseURI);
      }
      return null;
   }
   public XMLX509SKI itemSKI(int i) throws XMLSecurityException {
      Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                                Constants._TAG_X509SKI,i);
      if (e != null) {
         return new XMLX509SKI(e, this._baseURI);
      }
      return null;
   }
   public XMLX509SubjectName itemSubjectName(int i)
           throws XMLSecurityException {
      Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                                Constants._TAG_X509SUBJECTNAME,i);
      if (e != null) {
         return new XMLX509SubjectName(e, this._baseURI);
      }
       return null;
   }
   public XMLX509Certificate itemCertificate(int i)
           throws XMLSecurityException {
      Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                                Constants._TAG_X509CERTIFICATE,i);
      if (e != null) {
         return new XMLX509Certificate(e, this._baseURI);
      }
       return null;
   }
   public XMLX509CRL itemCRL(int i) throws XMLSecurityException {
      Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                                Constants._TAG_X509CRL,i);
      if (e != null) {
         return new XMLX509CRL(e, this._baseURI);
      }
       return null;
   }
   public Element itemUnknownElement(int i) {
          log.log(java.util.logging.Level.FINE, "itemUnknownElement not implemented:"+i);
      return null;
   }
   public boolean containsIssuerSerial() {
      return this.lengthIssuerSerial() > 0;
   }
   public boolean containsSKI() {
      return this.lengthSKI() > 0;
   }
   public boolean containsSubjectName() {
      return this.lengthSubjectName() > 0;
   }
   public boolean containsCertificate() {
      return this.lengthCertificate() > 0;
   }
   public boolean containsCRL() {
      return this.lengthCRL() > 0;
   }
   public boolean containsUnknownElement() {
      return this.lengthUnknownElement() > 0;
   }
   public String getBaseLocalName() {
      return Constants._TAG_X509DATA;
   }
}
