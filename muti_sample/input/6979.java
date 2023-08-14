public final class DOMXPathFilter2Transform extends ApacheTransform {
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        } else if (!(params instanceof XPathFilter2ParameterSpec)) {
            throw new InvalidAlgorithmParameterException
                ("params must be of type XPathFilter2ParameterSpec");
        }
        this.params = params;
    }
    public void init(XMLStructure parent, XMLCryptoContext context)
        throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        try {
            unmarshalParams(DOMUtils.getFirstChildElement(transformElem));
        } catch (MarshalException me) {
            throw (InvalidAlgorithmParameterException)
                new InvalidAlgorithmParameterException().initCause(me);
        }
    }
    private void unmarshalParams(Element curXPathElem) throws MarshalException {
        List list = new ArrayList();
        while (curXPathElem != null) {
            String xPath = curXPathElem.getFirstChild().getNodeValue();
            String filterVal =
                DOMUtils.getAttributeValue(curXPathElem, "Filter");
            if (filterVal == null) {
                throw new MarshalException("filter cannot be null");
            }
            XPathType.Filter filter = null;
            if (filterVal.equals("intersect")) {
                filter = XPathType.Filter.INTERSECT;
            } else if (filterVal.equals("subtract")) {
                filter = XPathType.Filter.SUBTRACT;
            } else if (filterVal.equals("union")) {
                filter = XPathType.Filter.UNION;
            } else {
                throw new MarshalException("Unknown XPathType filter type"
                    + filterVal);
            }
            NamedNodeMap attributes = curXPathElem.getAttributes();
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
                list.add(new XPathType(xPath, filter, namespaceMap));
            } else {
                list.add(new XPathType(xPath, filter));
            }
            curXPathElem = DOMUtils.getNextSiblingElement(curXPathElem);
        }
        this.params = new XPathFilter2ParameterSpec(list);
    }
    public void marshalParams(XMLStructure parent, XMLCryptoContext context)
        throws MarshalException {
        super.marshalParams(parent, context);
        XPathFilter2ParameterSpec xp =
            (XPathFilter2ParameterSpec) getParameterSpec();
        String prefix = DOMUtils.getNSPrefix(context, Transform.XPATH2);
        String qname = (prefix == null || prefix.length() == 0)
                       ? "xmlns" : "xmlns:" + prefix;
        List list = xp.getXPathList();
        for (int i = 0, size = list.size(); i < size; i++) {
            XPathType xpathType = (XPathType) list.get(i);
            Element elem = DOMUtils.createElement
                (ownerDoc, "XPath", Transform.XPATH2, prefix);
            elem.appendChild
                (ownerDoc.createTextNode(xpathType.getExpression()));
            DOMUtils.setAttribute
                (elem, "Filter", xpathType.getFilter().toString());
            elem.setAttributeNS("http:
                Transform.XPATH2);
            Iterator it = xpathType.getNamespaceMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                elem.setAttributeNS("http:
                    + (String) entry.getKey(), (String) entry.getValue());
            }
            transformElem.appendChild(elem);
        }
    }
}
