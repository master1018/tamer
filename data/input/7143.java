public abstract class DOMDigestMethod extends DOMStructure
    implements DigestMethod {
    final static String SHA384 =
        "http:
    private DigestMethodParameterSpec params;
    DOMDigestMethod(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof DigestMethodParameterSpec)) {
            throw new InvalidAlgorithmParameterException
                ("params must be of type DigestMethodParameterSpec");
        }
        checkParams((DigestMethodParameterSpec) params);
        this.params = (DigestMethodParameterSpec) params;
    }
    DOMDigestMethod(Element dmElem) throws MarshalException {
        Element paramsElem = DOMUtils.getFirstChildElement(dmElem);
        if (paramsElem != null) {
            params = unmarshalParams(paramsElem);
        }
        try {
            checkParams(params);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new MarshalException(iape);
        }
    }
    static DigestMethod unmarshal(Element dmElem) throws MarshalException {
        String alg = DOMUtils.getAttributeValue(dmElem, "Algorithm");
        if (alg.equals(DigestMethod.SHA1)) {
            return new SHA1(dmElem);
        } else if (alg.equals(DigestMethod.SHA256)) {
            return new SHA256(dmElem);
        } else if (alg.equals(SHA384)) {
            return new SHA384(dmElem);
        } else if (alg.equals(DigestMethod.SHA512)) {
            return new SHA512(dmElem);
        } else {
            throw new MarshalException
                ("unsupported DigestMethod algorithm: " + alg);
        }
    }
    void checkParams(DigestMethodParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters " +
                "should be specified for the " + getMessageDigestAlgorithm()
                 + " DigestMethod algorithm");
        }
    }
    public final AlgorithmParameterSpec getParameterSpec() {
        return params;
    }
    DigestMethodParameterSpec
        unmarshalParams(Element paramsElem) throws MarshalException {
        throw new MarshalException("no parameters should " +
            "be specified for the " + getMessageDigestAlgorithm() +
            " DigestMethod algorithm");
    }
    public void marshal(Node parent, String prefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element dmElem = DOMUtils.createElement
            (ownerDoc, "DigestMethod", XMLSignature.XMLNS, prefix);
        DOMUtils.setAttribute(dmElem, "Algorithm", getAlgorithm());
        if (params != null) {
            marshalParams(dmElem, prefix);
        }
        parent.appendChild(dmElem);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DigestMethod)) {
            return false;
        }
        DigestMethod odm = (DigestMethod) o;
        boolean paramsEqual = (params == null ? odm.getParameterSpec() == null :
            params.equals(odm.getParameterSpec()));
        return (getAlgorithm().equals(odm.getAlgorithm()) && paramsEqual);
    }
    void marshalParams(Element parent, String prefix)
        throws MarshalException {
        throw new MarshalException("no parameters should " +
            "be specified for the " + getMessageDigestAlgorithm() +
            " DigestMethod algorithm");
    }
    abstract String getMessageDigestAlgorithm();
    static final class SHA1 extends DOMDigestMethod {
        SHA1(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA1(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return DigestMethod.SHA1;
        }
        String getMessageDigestAlgorithm() {
            return "SHA-1";
        }
    }
    static final class SHA256 extends DOMDigestMethod {
        SHA256(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA256(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return DigestMethod.SHA256;
        }
        String getMessageDigestAlgorithm() {
            return "SHA-256";
        }
    }
    static final class SHA384 extends DOMDigestMethod {
        SHA384(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA384(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return SHA384;
        }
        String getMessageDigestAlgorithm() {
            return "SHA-384";
        }
    }
    static final class SHA512 extends DOMDigestMethod {
        SHA512(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
            super(params);
        }
        SHA512(Element dmElem) throws MarshalException {
            super(dmElem);
        }
        public String getAlgorithm() {
            return DigestMethod.SHA512;
        }
        String getMessageDigestAlgorithm() {
            return "SHA-512";
        }
    }
}
