public final class DOMXSLTTransform extends ApacheTransform {
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        }
        if (!(params instanceof XSLTTransformParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unrecognized params");
        }
        this.params = params;
    }
    public void init(XMLStructure parent, XMLCryptoContext context)
        throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        unmarshalParams(DOMUtils.getFirstChildElement(transformElem));
    }
    private void unmarshalParams(Element sheet) {
        this.params = new XSLTTransformParameterSpec
            (new javax.xml.crypto.dom.DOMStructure(sheet));
    }
    public void marshalParams(XMLStructure parent, XMLCryptoContext context)
        throws MarshalException {
        super.marshalParams(parent, context);
        XSLTTransformParameterSpec xp =
            (XSLTTransformParameterSpec) getParameterSpec();
        Node xsltElem =
            ((javax.xml.crypto.dom.DOMStructure) xp.getStylesheet()).getNode();
        DOMUtils.appendChild(transformElem, xsltElem);
    }
}
