public final class XMLDSigRI extends Provider {
    static final long serialVersionUID = -5049765099299494554L;
    private static final String INFO = "XMLDSig " +
    "(DOM XMLSignatureFactory; DOM KeyInfoFactory)";
    public XMLDSigRI() {
        super("XMLDSig", 1.0, INFO);
        final Map map = new HashMap();
        map.put("XMLSignatureFactory.DOM",
                "org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory");
        map.put("KeyInfoFactory.DOM",
                "org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory");
        map.put((String)"TransformService." + CanonicalizationMethod.INCLUSIVE,
                "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod");
        map.put("Alg.Alias.TransformService.INCLUSIVE",
                CanonicalizationMethod.INCLUSIVE);
        map.put((String)"TransformService." + CanonicalizationMethod.INCLUSIVE +
                " MechanismType", "DOM");
        map.put((String) "TransformService." +
                CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod");
        map.put("Alg.Alias.TransformService.INCLUSIVE_WITH_COMMENTS",
                CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS);
        map.put((String) "TransformService." +
                CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS +
                " MechanismType", "DOM");
        map.put((String)"TransformService." +
                "http:
                "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method");
        map.put((String)"TransformService." +
                "http:
                " MechanismType", "DOM");
        map.put((String)"TransformService." +
                "http:
                "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method");
        map.put((String)"TransformService." +
                "http:
                " MechanismType", "DOM");
        map.put((String) "TransformService." + CanonicalizationMethod.EXCLUSIVE,
                "org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod");
        map.put("Alg.Alias.TransformService.EXCLUSIVE",
                CanonicalizationMethod.EXCLUSIVE);
        map.put((String)"TransformService." + CanonicalizationMethod.EXCLUSIVE +
                " MechanismType", "DOM");
        map.put((String) "TransformService." +
                CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
                "org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod");
        map.put("Alg.Alias.TransformService.EXCLUSIVE_WITH_COMMENTS",
                CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS);
        map.put((String) "TransformService." +
                CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS +
                " MechanismType", "DOM");
        map.put((String) "TransformService." + Transform.BASE64,
                "org.jcp.xml.dsig.internal.dom.DOMBase64Transform");
        map.put("Alg.Alias.TransformService.BASE64", Transform.BASE64);
        map.put((String) "TransformService." + Transform.BASE64 +
                " MechanismType", "DOM");
        map.put((String) "TransformService." + Transform.ENVELOPED,
                "org.jcp.xml.dsig.internal.dom.DOMEnvelopedTransform");
        map.put("Alg.Alias.TransformService.ENVELOPED", Transform.ENVELOPED);
        map.put((String) "TransformService." + Transform.ENVELOPED +
                " MechanismType", "DOM");
        map.put((String) "TransformService." + Transform.XPATH2,
                "org.jcp.xml.dsig.internal.dom.DOMXPathFilter2Transform");
        map.put("Alg.Alias.TransformService.XPATH2", Transform.XPATH2);
        map.put((String) "TransformService." + Transform.XPATH2 +
                " MechanismType", "DOM");
        map.put((String) "TransformService." + Transform.XPATH,
                "org.jcp.xml.dsig.internal.dom.DOMXPathTransform");
        map.put("Alg.Alias.TransformService.XPATH", Transform.XPATH);
        map.put((String) "TransformService." + Transform.XPATH +
                " MechanismType", "DOM");
        map.put((String) "TransformService." + Transform.XSLT,
                "org.jcp.xml.dsig.internal.dom.DOMXSLTTransform");
        map.put("Alg.Alias.TransformService.XSLT", Transform.XSLT);
        map.put((String) "TransformService." + Transform.XSLT +
                " MechanismType", "DOM");
        AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                putAll(map);
                return null;
            }
        });
    }
}
