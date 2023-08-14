public final class DOMExcC14NMethod extends ApacheCanonicalizer {
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            if (!(params instanceof ExcC14NParameterSpec)) {
                throw new InvalidAlgorithmParameterException
                    ("params must be of type ExcC14NParameterSpec");
            }
            this.params = (C14NMethodParameterSpec) params;
        }
    }
    public void init(XMLStructure parent, XMLCryptoContext context)
        throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        Element paramsElem = DOMUtils.getFirstChildElement(transformElem);
        if (paramsElem == null) {
            this.params = null;
            this.inclusiveNamespaces = null;
            return;
        }
        unmarshalParams(paramsElem);
    }
    private void unmarshalParams(Element paramsElem) {
        String prefixListAttr = paramsElem.getAttributeNS(null, "PrefixList");
        this.inclusiveNamespaces = prefixListAttr;
        int begin = 0;
        int end = prefixListAttr.indexOf(' ');
        List prefixList = new ArrayList();
        while (end != -1) {
            prefixList.add(prefixListAttr.substring(begin, end));
            begin = end + 1;
            end = prefixListAttr.indexOf(' ', begin);
        }
        if (begin <= prefixListAttr.length()) {
            prefixList.add(prefixListAttr.substring(begin));
        }
        this.params = new ExcC14NParameterSpec(prefixList);
    }
    public void marshalParams(XMLStructure parent, XMLCryptoContext context)
        throws MarshalException {
        super.marshalParams(parent, context);
        AlgorithmParameterSpec spec = getParameterSpec();
        if (spec == null) {
            return;
        }
        String prefix =
            DOMUtils.getNSPrefix(context, CanonicalizationMethod.EXCLUSIVE);
        Element excElem = DOMUtils.createElement
            (ownerDoc, "InclusiveNamespaces",
             CanonicalizationMethod.EXCLUSIVE, prefix);
        if (prefix == null || prefix.length() == 0) {
            excElem.setAttributeNS("http:
                CanonicalizationMethod.EXCLUSIVE);
        } else {
            excElem.setAttributeNS("http:
                "xmlns:" + prefix, CanonicalizationMethod.EXCLUSIVE);
        }
        ExcC14NParameterSpec params = (ExcC14NParameterSpec) spec;
        StringBuffer prefixListAttr = new StringBuffer("");
        List prefixList = params.getPrefixList();
        for (int i = 0, size = prefixList.size(); i < size; i++) {
            prefixListAttr.append((String) prefixList.get(i));
            if (i < size - 1) {
                prefixListAttr.append(" ");
            }
        }
        DOMUtils.setAttribute(excElem, "PrefixList", prefixListAttr.toString());
        this.inclusiveNamespaces = prefixListAttr.toString();
        transformElem.appendChild(excElem);
    }
    public String getParamsNSURI() {
        return CanonicalizationMethod.EXCLUSIVE;
    }
    public Data transform(Data data, XMLCryptoContext xc)
        throws TransformException {
        if (data instanceof DOMSubTreeData) {
            DOMSubTreeData subTree = (DOMSubTreeData) data;
            if (subTree.excludeComments()) {
                try {
                    apacheCanonicalizer = Canonicalizer.getInstance
                        (CanonicalizationMethod.EXCLUSIVE);
                } catch (InvalidCanonicalizerException ice) {
                    throw new TransformException
                        ("Couldn't find Canonicalizer for: " +
                         CanonicalizationMethod.EXCLUSIVE + ": " +
                         ice.getMessage(), ice);
                }
            }
        }
        return canonicalize(data, xc);
    }
}
