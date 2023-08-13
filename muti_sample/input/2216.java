public class DOMCanonicalizationMethod extends DOMTransform
    implements CanonicalizationMethod {
    public DOMCanonicalizationMethod(TransformService spi)
        throws InvalidAlgorithmParameterException {
        super(spi);
    }
    public DOMCanonicalizationMethod(Element cmElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        super(cmElem, context, provider);
    }
    public Data canonicalize(Data data, XMLCryptoContext xc)
        throws TransformException {
        return transform(data, xc);
    }
    public Data canonicalize(Data data, XMLCryptoContext xc, OutputStream os)
        throws TransformException {
        return transform(data, xc, os);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CanonicalizationMethod)) {
            return false;
        }
        CanonicalizationMethod ocm = (CanonicalizationMethod) o;
        return (getAlgorithm().equals(ocm.getAlgorithm()) &&
            DOMUtils.paramsEqual(getParameterSpec(), ocm.getParameterSpec()));
    }
}
