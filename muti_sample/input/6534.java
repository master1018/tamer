public class XMLX509Certificate extends SignatureElementProxy
        implements XMLX509DataContent {
   public static final String JCA_CERT_ID = "X.509";
   public XMLX509Certificate(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public XMLX509Certificate(Document doc, byte[] certificateBytes) {
      super(doc);
      this.addBase64Text(certificateBytes);
   }
   public XMLX509Certificate(Document doc, X509Certificate x509certificate)
           throws XMLSecurityException {
      super(doc);
      try {
         this.addBase64Text(x509certificate.getEncoded());
      } catch (java.security.cert.CertificateEncodingException ex) {
         throw new XMLSecurityException("empty", ex);
      }
   }
   public byte[] getCertificateBytes() throws XMLSecurityException {
      return this.getBytesFromTextChild();
   }
   public X509Certificate getX509Certificate() throws XMLSecurityException {
      try {
         byte certbytes[] = this.getCertificateBytes();
         CertificateFactory certFact =
            CertificateFactory.getInstance(XMLX509Certificate.JCA_CERT_ID);
         X509Certificate cert =
            (X509Certificate) certFact
               .generateCertificate(new ByteArrayInputStream(certbytes));
         if (cert != null) {
            return cert;
         }
         return null;
      } catch (CertificateException ex) {
         throw new XMLSecurityException("empty", ex);
      }
   }
   public PublicKey getPublicKey() throws XMLSecurityException {
      X509Certificate cert = this.getX509Certificate();
      if (cert != null) {
         return cert.getPublicKey();
      }
      return null;
   }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        XMLX509Certificate other = (XMLX509Certificate) obj;
        try {
            return java.security.MessageDigest.isEqual
                (other.getCertificateBytes(), this.getCertificateBytes());
        } catch (XMLSecurityException ex) {
            return false;
        }
    }
   public String getBaseLocalName() {
      return Constants._TAG_X509CERTIFICATE;
   }
}
