public class XMLX509SKI extends SignatureElementProxy
        implements XMLX509DataContent {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(XMLX509SKI.class.getName());
    public static final String SKI_OID = "2.5.29.14";
    public XMLX509SKI(Document doc, byte[] skiBytes) {
        super(doc);
        this.addBase64Text(skiBytes);
    }
    public XMLX509SKI(Document doc, X509Certificate x509certificate)
           throws XMLSecurityException {
        super(doc);
        this.addBase64Text(XMLX509SKI.getSKIBytesFromCert(x509certificate));
    }
    public XMLX509SKI(Element element, String BaseURI)
           throws XMLSecurityException {
        super(element, BaseURI);
    }
    public byte[] getSKIBytes() throws XMLSecurityException {
        return this.getBytesFromTextChild();
    }
    public static byte[] getSKIBytesFromCert(X509Certificate cert)
        throws XMLSecurityException {
        if (cert.getVersion() < 3) {
            Object exArgs[] = { new Integer(cert.getVersion()) };
            throw new XMLSecurityException("certificate.noSki.lowVersion",
                                           exArgs);
        }
        byte[] extensionValue = cert.getExtensionValue(XMLX509SKI.SKI_OID);
        if (extensionValue == null) {
            throw new XMLSecurityException("certificate.noSki.null");
        }
        byte skidValue[] = new byte[extensionValue.length - 4];
        System.arraycopy(extensionValue, 4, skidValue, 0, skidValue.length);
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Base64 of SKI is " + Base64.encode(skidValue));
        }
        return skidValue;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        XMLX509SKI other = (XMLX509SKI) obj;
        try {
            return java.security.MessageDigest.isEqual(other.getSKIBytes(),
                                        this.getSKIBytes());
        } catch (XMLSecurityException ex) {
            return false;
        }
    }
    public String getBaseLocalName() {
        return Constants._TAG_X509SKI;
    }
}
