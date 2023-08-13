public abstract class DOMSignatureMethod extends DOMStructure
    implements SignatureMethod {
    private static Logger log =
        Logger.getLogger("org.jcp.xml.dsig.internal.dom");
    final static String RSA_SHA256 =
        "http:
    final static String RSA_SHA384 =
        "http:
    final static String RSA_SHA512 =
        "http:
    final static String HMAC_SHA256 =
        "http:
    final static String HMAC_SHA384 =
        "http:
    final static String HMAC_SHA512 =
        "http:
    private SignatureMethodParameterSpec params;
    private Signature signature;
    DOMSignatureMethod(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null &&
            !(params instanceof SignatureMethodParameterSpec)) {
            throw new InvalidAlgorithmParameterException
                ("params must be of type SignatureMethodParameterSpec");
        }
        checkParams((SignatureMethodParameterSpec) params);
        this.params = (SignatureMethodParameterSpec) params;
    }
    DOMSignatureMethod(Element smElem) throws MarshalException {
        Element paramsElem = DOMUtils.getFirstChildElement(smElem);
        if (paramsElem != null) {
            params = unmarshalParams(paramsElem);
        }
        try {
            checkParams(params);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new MarshalException(iape);
        }
    }
    static SignatureMethod unmarshal(Element smElem) throws MarshalException {
        String alg = DOMUtils.getAttributeValue(smElem, "Algorithm");
        if (alg.equals(SignatureMethod.RSA_SHA1)) {
            return new SHA1withRSA(smElem);
        } else if (alg.equals(RSA_SHA256)) {
            return new SHA256withRSA(smElem);
        } else if (alg.equals(RSA_SHA384)) {
            return new SHA384withRSA(smElem);
        } else if (alg.equals(RSA_SHA512)) {
            return new SHA512withRSA(smElem);
        } else if (alg.equals(SignatureMethod.DSA_SHA1)) {
            return new SHA1withDSA(smElem);
        } else if (alg.equals(SignatureMethod.HMAC_SHA1)) {
            return new DOMHMACSignatureMethod.SHA1(smElem);
        } else if (alg.equals(HMAC_SHA256)) {
            return new DOMHMACSignatureMethod.SHA256(smElem);
        } else if (alg.equals(HMAC_SHA384)) {
            return new DOMHMACSignatureMethod.SHA384(smElem);
        } else if (alg.equals(HMAC_SHA512)) {
            return new DOMHMACSignatureMethod.SHA512(smElem);
        } else {
            throw new MarshalException
                ("unsupported SignatureMethod algorithm: " + alg);
        }
    }
    void checkParams(SignatureMethodParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters " +
                "should be specified for the " + getSignatureAlgorithm()
                 + " SignatureMethod algorithm");
        }
    }
    public final AlgorithmParameterSpec getParameterSpec() {
        return params;
    }
    SignatureMethodParameterSpec
        unmarshalParams(Element paramsElem) throws MarshalException {
        throw new MarshalException("no parameters should " +
            "be specified for the " + getSignatureAlgorithm() +
            " SignatureMethod algorithm");
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element smElem = DOMUtils.createElement
            (ownerDoc, "SignatureMethod", XMLSignature.XMLNS, dsPrefix);
        DOMUtils.setAttribute(smElem, "Algorithm", getAlgorithm());
        if (params != null) {
            marshalParams(smElem, dsPrefix);
        }
        parent.appendChild(smElem);
    }
    boolean verify(Key key, DOMSignedInfo si, byte[] sig,
        XMLValidateContext context) throws InvalidKeyException,
        SignatureException, XMLSignatureException {
        if (key == null || si == null || sig == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof PublicKey)) {
            throw new InvalidKeyException("key must be PublicKey");
        }
        if (signature == null) {
            try {
                Provider p = (Provider) context.getProperty
                    ("org.jcp.xml.dsig.internal.dom.SignatureProvider");
                signature = (p == null)
                    ? Signature.getInstance(getSignatureAlgorithm())
                    : Signature.getInstance(getSignatureAlgorithm(), p);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        signature.initVerify((PublicKey) key);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Signature provider:"+ signature.getProvider());
            log.log(Level.FINE, "verifying with key: " + key);
        }
        si.canonicalize(context, new SignerOutputStream(signature));
        if (getAlgorithm().equals(SignatureMethod.DSA_SHA1)) {
            try {
                return signature.verify(convertXMLDSIGtoASN1(sig));
            } catch (IOException ioe) {
                throw new XMLSignatureException(ioe);
            }
        } else {
            return signature.verify(sig);
        }
    }
    byte[] sign(Key key, DOMSignedInfo si, XMLSignContext context)
        throws InvalidKeyException, XMLSignatureException {
        if (key == null || si == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof PrivateKey)) {
            throw new InvalidKeyException("key must be PrivateKey");
        }
        if (signature == null) {
            try {
                Provider p = (Provider) context.getProperty
                    ("org.jcp.xml.dsig.internal.dom.SignatureProvider");
                signature = (p == null)
                    ? Signature.getInstance(getSignatureAlgorithm())
                    : Signature.getInstance(getSignatureAlgorithm(), p);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        signature.initSign((PrivateKey) key);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Signature provider:" +signature.getProvider());
            log.log(Level.FINE, "Signing with key: " + key);
        }
        si.canonicalize(context, new SignerOutputStream(signature));
        try {
            if (getAlgorithm().equals(SignatureMethod.DSA_SHA1)) {
                return convertASN1toXMLDSIG(signature.sign());
            } else {
                return signature.sign();
            }
        } catch (SignatureException se) {
            throw new XMLSignatureException(se);
        } catch (IOException ioe) {
            throw new XMLSignatureException(ioe);
        }
    }
    void marshalParams(Element parent, String paramsPrefix)
        throws MarshalException {
        throw new MarshalException("no parameters should " +
            "be specified for the " + getSignatureAlgorithm() +
            " SignatureMethod algorithm");
    }
    abstract String getSignatureAlgorithm();
    boolean paramsEqual(AlgorithmParameterSpec spec) {
        return (getParameterSpec() == spec);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureMethod)) {
            return false;
        }
        SignatureMethod osm = (SignatureMethod) o;
        return (getAlgorithm().equals(osm.getAlgorithm()) &&
            paramsEqual(osm.getParameterSpec()));
    }
    private static byte[] convertASN1toXMLDSIG(byte asn1Bytes[])
        throws IOException {
        byte rLength = asn1Bytes[3];
        int i;
        for (i = rLength; (i > 0) && (asn1Bytes[(4 + rLength) - i] == 0); i--);
        byte sLength = asn1Bytes[5 + rLength];
        int j;
        for (j = sLength;
            (j > 0) && (asn1Bytes[(6 + rLength + sLength) - j] == 0); j--);
        if ((asn1Bytes[0] != 48) || (asn1Bytes[1] != asn1Bytes.length - 2)
            || (asn1Bytes[2] != 2) || (i > 20)
            || (asn1Bytes[4 + rLength] != 2) || (j > 20)) {
            throw new IOException("Invalid ASN.1 format of DSA signature");
        } else {
            byte xmldsigBytes[] = new byte[40];
            System.arraycopy(asn1Bytes, (4+rLength)-i, xmldsigBytes, 20-i, i);
            System.arraycopy(asn1Bytes, (6+rLength+sLength)-j, xmldsigBytes,
                          40 - j, j);
            return xmldsigBytes;
        }
    }
    private static byte[] convertXMLDSIGtoASN1(byte xmldsigBytes[])
        throws IOException {
        if (xmldsigBytes.length != 40) {
            throw new IOException("Invalid XMLDSIG format of DSA signature");
        }
        int i;
        for (i = 20; (i > 0) && (xmldsigBytes[20 - i] == 0); i--);
        int j = i;
        if (xmldsigBytes[20 - i] < 0) {
            j += 1;
        }
        int k;
        for (k = 20; (k > 0) && (xmldsigBytes[40 - k] == 0); k--);
        int l = k;
        if (xmldsigBytes[40 - k] < 0) {
            l += 1;
        }
        byte asn1Bytes[] = new byte[6 + j + l];
        asn1Bytes[0] = 48;
        asn1Bytes[1] = (byte) (4 + j + l);
        asn1Bytes[2] = 2;
        asn1Bytes[3] = (byte) j;
        System.arraycopy(xmldsigBytes, 20 - i, asn1Bytes, (4 + j) - i, i);
        asn1Bytes[4 + j] = 2;
        asn1Bytes[5 + j] = (byte) l;
        System.arraycopy(xmldsigBytes, 40 - k, asn1Bytes, (6 + j + l) - k, k);
        return asn1Bytes;
    }
    static final class SHA1withRSA extends DOMSignatureMethod {
        SHA1withRSA(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA1withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return SignatureMethod.RSA_SHA1;
        }
        String getSignatureAlgorithm() {
            return "SHA1withRSA";
        }
    }
    static final class SHA256withRSA extends DOMSignatureMethod {
        SHA256withRSA(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA256withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return RSA_SHA256;
        }
        String getSignatureAlgorithm() {
            return "SHA256withRSA";
        }
    }
    static final class SHA384withRSA extends DOMSignatureMethod {
        SHA384withRSA(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA384withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return RSA_SHA384;
        }
        String getSignatureAlgorithm() {
            return "SHA384withRSA";
        }
    }
    static final class SHA512withRSA extends DOMSignatureMethod {
        SHA512withRSA(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA512withRSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return RSA_SHA512;
        }
        String getSignatureAlgorithm() {
            return "SHA512withRSA";
        }
    }
    static final class SHA1withDSA extends DOMSignatureMethod {
        SHA1withDSA(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA1withDSA(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return SignatureMethod.DSA_SHA1;
        }
        String getSignatureAlgorithm() {
            return "SHA1withDSA";
        }
    }
}
