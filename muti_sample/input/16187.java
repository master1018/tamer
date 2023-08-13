public final class DOMXPathTransform extends ApacheTransform {
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        } else if (!(params instanceof XPathFilterParameterSpec)) {
            throw new InvalidAlgorithmParameterException
                ("params must be of type XPathFilterParameterSpec");
        }
        this.params = params;
    }
    public void init(XMLStructure parent, XMLCryptoContext context)
        throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        unmarshalParams(DOMUtils.getFirstChildElement(transformElem));
    }
    private void unmarshalParams(Element paramsElem) {
        String xPath = paramsElem.getFirstChild().getNodeValue();
        NamedNodeMap attributes = paramsElem.getAttributes();
        if (attributes != null) {
            int length = attributes.getLength();
            Map namespaceMap = new HashMap(length);
            for (int i = 0; i < length; i++) {
                Attr attr = (Attr) attributes.item(i);
                String prefix = attr.getPrefix();
                if (prefix != null && prefix.equals("xmlns")) {
                    namespaceMap.put(attr.getLocalName(), attr.getValue());
                }
            }
            this.params = new XPathFilterParameterSpec(xPath, namespaceMap);
        } else {
            this.params = new XPathFilterParameterSpec(xPath);
        }
    }
    public void marshalParams(XMLStructure parent, XMLCryptoContext context)
        throws MarshalException {
        super.marshalParams(parent, context);
        XPathFilterParameterSpec xp =
            (XPathFilterParameterSpec) getParameterSpec();
        Element xpathElem = DOMUtils.createElement
            (ownerDoc, "XPath", XMLSignature.XMLNS,
             DOMUtils.getSignaturePrefix(context));
        xpathElem.appendChild(ownerDoc.createTextNode(xp.getXPath()));
        Iterator i = xp.getNamespaceMap().entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            xpathElem.setAttributeNS("http:
                + (String) entry.getKey(), (String) entry.getValue());
        }
        transformElem.appendChild(xpathElem);
    }
}
