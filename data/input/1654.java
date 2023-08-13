public final class DOMCanonicalXMLC14N11Method extends ApacheCanonicalizer {
    public static final String C14N_11 = "http:
    public static final String C14N_11_WITH_COMMENTS
        = "http:
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters " +
                "should be specified for Canonical XML 1.1 algorithm");
        }
    }
    public Data transform(Data data, XMLCryptoContext xc)
        throws TransformException {
        if (data instanceof DOMSubTreeData) {
            DOMSubTreeData subTree = (DOMSubTreeData) data;
            if (subTree.excludeComments()) {
                try {
                    apacheCanonicalizer = Canonicalizer.getInstance(C14N_11);
                } catch (InvalidCanonicalizerException ice) {
                    throw new TransformException
                        ("Couldn't find Canonicalizer for: " +
                         C14N_11 + ": " + ice.getMessage(), ice);
                }
            }
        }
        return canonicalize(data, xc);
    }
}
