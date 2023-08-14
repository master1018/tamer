public class XMLX509IssuerSerial extends SignatureElementProxy
        implements XMLX509DataContent {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                    XMLX509IssuerSerial.class.getName());
    public XMLX509IssuerSerial(Element element, String baseURI)
           throws XMLSecurityException {
        super(element, baseURI);
    }
    public XMLX509IssuerSerial(Document doc, String x509IssuerName,
                               BigInteger x509SerialNumber) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
        addTextElement(x509IssuerName, Constants._TAG_X509ISSUERNAME);
        addTextElement(x509SerialNumber.toString(), Constants._TAG_X509SERIALNUMBER);
    }
    public XMLX509IssuerSerial(Document doc, String x509IssuerName,
                               String x509SerialNumber) {
        this(doc, x509IssuerName, new BigInteger(x509SerialNumber));
    }
    public XMLX509IssuerSerial(Document doc, String x509IssuerName,
                               int x509SerialNumber) {
        this(doc, x509IssuerName,
             new BigInteger(Integer.toString(x509SerialNumber)));
    }
    public XMLX509IssuerSerial(Document doc, X509Certificate x509certificate) {
        this(doc,
             RFC2253Parser.normalize(x509certificate.getIssuerDN().getName()),
             x509certificate.getSerialNumber());
    }
    public BigInteger getSerialNumber() {
        String text = this.getTextFromChildElement
            (Constants._TAG_X509SERIALNUMBER, Constants.SignatureSpecNS);
        if (log.isLoggable(java.util.logging.Level.FINE))
            log.log(java.util.logging.Level.FINE, "X509SerialNumber text: " + text);
        return new BigInteger(text);
    }
    public int getSerialNumberInteger() {
        return this.getSerialNumber().intValue();
    }
    public String getIssuerName()  {
        return RFC2253Parser
           .normalize(this
              .getTextFromChildElement(Constants._TAG_X509ISSUERNAME,
                                       Constants.SignatureSpecNS));
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        XMLX509IssuerSerial other = (XMLX509IssuerSerial) obj;
        return this.getSerialNumber().equals(other.getSerialNumber())
               && this.getIssuerName().equals(other.getIssuerName());
    }
    public String getBaseLocalName() {
        return Constants._TAG_X509ISSUERSERIAL;
    }
}
