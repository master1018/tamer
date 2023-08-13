public class DOMTransform extends DOMStructure implements Transform {
    protected TransformService spi;
    public DOMTransform(TransformService spi) {
        this.spi = spi;
    }
    public DOMTransform(Element transElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        String algorithm = DOMUtils.getAttributeValue(transElem, "Algorithm");
        try {
            spi = TransformService.getInstance(algorithm, "DOM");
        } catch (NoSuchAlgorithmException e1) {
            try {
                spi = TransformService.getInstance(algorithm, "DOM", provider);
            } catch (NoSuchAlgorithmException e2) {
                throw new MarshalException(e2);
            }
        }
        try {
            spi.init(new javax.xml.crypto.dom.DOMStructure(transElem), context);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new MarshalException(iape);
        }
    }
    public final AlgorithmParameterSpec getParameterSpec() {
        return spi.getParameterSpec();
    }
    public final String getAlgorithm() {
        return spi.getAlgorithm();
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element transformElem = null;
        if (parent.getLocalName().equals("Transforms")) {
            transformElem = DOMUtils.createElement
                (ownerDoc, "Transform", XMLSignature.XMLNS, dsPrefix);
        } else {
            transformElem = DOMUtils.createElement
            (ownerDoc, "CanonicalizationMethod", XMLSignature.XMLNS, dsPrefix);
        }
        DOMUtils.setAttribute(transformElem, "Algorithm", getAlgorithm());
        spi.marshalParams
            (new javax.xml.crypto.dom.DOMStructure(transformElem), context);
        parent.appendChild(transformElem);
    }
    public Data transform(Data data, XMLCryptoContext xc)
        throws TransformException {
        return spi.transform(data, xc);
    }
    public Data transform(Data data, XMLCryptoContext xc, OutputStream os)
        throws TransformException {
        return spi.transform(data, xc, os);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transform)) {
            return false;
        }
        Transform otransform = (Transform) o;
        return (getAlgorithm().equals(otransform.getAlgorithm()) &&
            DOMUtils.paramsEqual
                (getParameterSpec(), otransform.getParameterSpec()));
    }
    Data transform(Data data, XMLCryptoContext xc, DOMSignContext context)
        throws MarshalException, TransformException {
        marshal(context.getParent(),
            DOMUtils.getSignaturePrefix(context), context);
        return transform(data, xc);
    }
}
