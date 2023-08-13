public final class DOMCanonicalXMLC14NMethod extends ApacheCanonicalizer {
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters " +
                "should be specified for Canonical XML C14N algorithm");
        }
    }
    public Data transform(Data data, XMLCryptoContext xc)
        throws TransformException {
        if (data instanceof DOMSubTreeData) {
            DOMSubTreeData subTree = (DOMSubTreeData) data;
            if (subTree.excludeComments()) {
                try {
                    apacheCanonicalizer = Canonicalizer.getInstance
                        (CanonicalizationMethod.INCLUSIVE);
                } catch (InvalidCanonicalizerException ice) {
                    throw new TransformException
                        ("Couldn't find Canonicalizer for: " +
                         CanonicalizationMethod.INCLUSIVE + ": " +
                         ice.getMessage(), ice);
                }
            }
        }
        return canonicalize(data, xc);
    }
}
