public abstract class DOMHMACSignatureMethod extends DOMSignatureMethod {
    private static Logger log =
        Logger.getLogger("org.jcp.xml.dsig.internal.dom");
    private Mac hmac;
    private int outputLength;
    private boolean outputLengthSet;
    DOMHMACSignatureMethod(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        super(params);
    }
    DOMHMACSignatureMethod(Element smElem) throws MarshalException {
        super(smElem);
    }
    void checkParams(SignatureMethodParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            if (!(params instanceof HMACParameterSpec)) {
                throw new InvalidAlgorithmParameterException
                    ("params must be of type HMACParameterSpec");
            }
            outputLength = ((HMACParameterSpec) params).getOutputLength();
            outputLengthSet = true;
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                    "Setting outputLength from HMACParameterSpec to: "
                    + outputLength);
            }
        } else {
            outputLength = -1;
        }
    }
    SignatureMethodParameterSpec unmarshalParams(Element paramsElem)
        throws MarshalException {
        outputLength = new Integer
            (paramsElem.getFirstChild().getNodeValue()).intValue();
        outputLengthSet = true;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "unmarshalled outputLength: " + outputLength);
        }
        return new HMACParameterSpec(outputLength);
    }
    void marshalParams(Element parent, String prefix)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element hmacElem = DOMUtils.createElement(ownerDoc, "HMACOutputLength",
            XMLSignature.XMLNS, prefix);
        hmacElem.appendChild(ownerDoc.createTextNode
           (String.valueOf(outputLength)));
        parent.appendChild(hmacElem);
    }
    boolean verify(Key key, DOMSignedInfo si, byte[] sig,
        XMLValidateContext context)
        throws InvalidKeyException, SignatureException, XMLSignatureException {
        if (key == null || si == null || sig == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("key must be SecretKey");
        }
        if (hmac == null) {
            try {
                hmac = Mac.getInstance(getSignatureAlgorithm());
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        if (outputLengthSet && outputLength < getDigestLength()) {
            throw new XMLSignatureException
                ("HMACOutputLength must not be less than " + getDigestLength());
        }
        hmac.init((SecretKey) key);
        si.canonicalize(context, new MacOutputStream(hmac));
        byte[] result = hmac.doFinal();
        return MessageDigest.isEqual(sig, result);
    }
    byte[] sign(Key key, DOMSignedInfo si, XMLSignContext context)
        throws InvalidKeyException, XMLSignatureException {
        if (key == null || si == null) {
            throw new NullPointerException();
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("key must be SecretKey");
        }
        if (hmac == null) {
            try {
                hmac = Mac.getInstance(getSignatureAlgorithm());
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLSignatureException(nsae);
            }
        }
        if (outputLengthSet && outputLength < getDigestLength()) {
            throw new XMLSignatureException
                ("HMACOutputLength must not be less than " + getDigestLength());
        }
        hmac.init((SecretKey) key);
        si.canonicalize(context, new MacOutputStream(hmac));
        return hmac.doFinal();
    }
    boolean paramsEqual(AlgorithmParameterSpec spec) {
        if (getParameterSpec() == spec) {
            return true;
        }
        if (!(spec instanceof HMACParameterSpec)) {
            return false;
        }
        HMACParameterSpec ospec = (HMACParameterSpec) spec;
        return (outputLength == ospec.getOutputLength());
    }
    abstract int getDigestLength();
    static final class SHA1 extends DOMHMACSignatureMethod {
        SHA1(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA1(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return SignatureMethod.HMAC_SHA1;
        }
        String getSignatureAlgorithm() {
            return "HmacSHA1";
        }
        int getDigestLength() {
            return 160;
        }
    }
    static final class SHA256 extends DOMHMACSignatureMethod {
        SHA256(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA256(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return HMAC_SHA256;
        }
        String getSignatureAlgorithm() {
            return "HmacSHA256";
        }
        int getDigestLength() {
            return 256;
        }
    }
    static final class SHA384 extends DOMHMACSignatureMethod {
        SHA384(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA384(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return HMAC_SHA384;
        }
        String getSignatureAlgorithm() {
            return "HmacSHA384";
        }
        int getDigestLength() {
            return 384;
        }
    }
    static final class SHA512 extends DOMHMACSignatureMethod {
        SHA512(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA512(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return HMAC_SHA512;
        }
        String getSignatureAlgorithm() {
            return "HmacSHA512";
        }
        int getDigestLength() {
            return 512;
        }
    }
}
