public final class Transform extends SignatureElementProxy {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(Transform.class.getName());
    private static boolean alreadyInitialized = false;
    private static HashMap transformClassHash = null;
    private static HashMap transformSpiHash = new HashMap();
    private TransformSpi transformSpi = null;
    public Transform(Document doc, String algorithmURI, NodeList contextNodes)
        throws InvalidTransformException {
        super(doc);
        this._constructionElement.setAttributeNS
            (null, Constants._ATT_ALGORITHM, algorithmURI);
        transformSpi = getTransformSpi(algorithmURI);
        if (transformSpi == null) {
             Object exArgs[] = { algorithmURI };
             throw new InvalidTransformException(
                "signature.Transform.UnknownTransform", exArgs);
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Create URI \"" + algorithmURI + "\" class \""
                   + transformSpi.getClass() + "\"");
            log.log(java.util.logging.Level.FINE, "The NodeList is " + contextNodes);
        }
        if (contextNodes != null) {
            for (int i = 0; i < contextNodes.getLength(); i++) {
               this._constructionElement.appendChild
                   (contextNodes.item(i).cloneNode(true));
            }
        }
    }
    public Transform(Element element, String BaseURI)
        throws InvalidTransformException, TransformationException,
               XMLSecurityException {
        super(element, BaseURI);
        String algorithmURI = element.getAttributeNS(null, Constants._ATT_ALGORITHM);
        if (algorithmURI == null || algorithmURI.length() == 0) {
            Object exArgs[] = { Constants._ATT_ALGORITHM,
                                Constants._TAG_TRANSFORM };
            throw new TransformationException("xml.WrongContent", exArgs);
        }
        transformSpi = getTransformSpi(algorithmURI);
        if (transformSpi == null) {
            Object exArgs[] = { algorithmURI };
            throw new InvalidTransformException(
                "signature.Transform.UnknownTransform", exArgs);
        }
    }
    public static Transform getInstance(
        Document doc, String algorithmURI) throws InvalidTransformException {
        return getInstance(doc, algorithmURI, (NodeList) null);
    }
    public static Transform getInstance(
        Document doc, String algorithmURI, Element contextChild)
        throws InvalidTransformException {
        HelperNodeList contextNodes = new HelperNodeList();
        XMLUtils.addReturnToElement(doc, contextNodes);
        contextNodes.appendChild(contextChild);
        XMLUtils.addReturnToElement(doc, contextNodes);
        return getInstance(doc, algorithmURI, contextNodes);
    }
    public static Transform getInstance(
        Document doc, String algorithmURI, NodeList contextNodes)
        throws InvalidTransformException {
        return new Transform(doc, algorithmURI, contextNodes);
    }
    public static void init() {
        if (!alreadyInitialized) {
            transformClassHash = new HashMap(10);
            com.sun.org.apache.xml.internal.security.Init.init();
            alreadyInitialized = true;
        }
    }
    public static void register(String algorithmURI, String implementingClass)
        throws AlgorithmAlreadyRegisteredException {
        Class registeredClass = getImplementingClass(algorithmURI);
        if ((registeredClass != null) ) {
            Object exArgs[] = { algorithmURI, registeredClass };
            throw new AlgorithmAlreadyRegisteredException(
               "algorithm.alreadyRegistered", exArgs);
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            transformClassHash.put
                (algorithmURI, Class.forName(implementingClass, true, cl));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public String getURI() {
        return this._constructionElement.getAttributeNS
            (null, Constants._ATT_ALGORITHM);
    }
    public XMLSignatureInput performTransform(XMLSignatureInput input)
         throws IOException, CanonicalizationException,
                InvalidCanonicalizerException, TransformationException {
        XMLSignatureInput result = null;
        try {
            result = transformSpi.enginePerformTransform(input, this);
        } catch (ParserConfigurationException ex) {
            Object exArgs[] = { this.getURI(), "ParserConfigurationException" };
            throw new CanonicalizationException(
                "signature.Transform.ErrorDuringTransform", exArgs, ex);
        } catch (SAXException ex) {
            Object exArgs[] = { this.getURI(), "SAXException" };
            throw new CanonicalizationException(
                "signature.Transform.ErrorDuringTransform", exArgs, ex);
        }
        return result;
    }
    public XMLSignatureInput performTransform(XMLSignatureInput input,
        OutputStream os) throws IOException, CanonicalizationException,
        InvalidCanonicalizerException, TransformationException {
        XMLSignatureInput result = null;
        try {
            result = transformSpi.enginePerformTransform(input, os, this);
        } catch (ParserConfigurationException ex) {
            Object exArgs[] = { this.getURI(), "ParserConfigurationException" };
            throw new CanonicalizationException(
                "signature.Transform.ErrorDuringTransform", exArgs, ex);
        } catch (SAXException ex) {
            Object exArgs[] = { this.getURI(), "SAXException" };
            throw new CanonicalizationException(
                "signature.Transform.ErrorDuringTransform", exArgs, ex);
        }
        return result;
    }
    private static Class getImplementingClass(String URI) {
        return (Class) transformClassHash.get(URI);
    }
    private static TransformSpi getTransformSpi(String URI)
        throws InvalidTransformException {
        try {
            Object value = transformSpiHash.get(URI);
            if (value != null) {
                return (TransformSpi) value;
            }
            Class cl = (Class) transformClassHash.get(URI);
            if (cl != null) {
                TransformSpi tr = (TransformSpi) cl.newInstance();
                transformSpiHash.put(URI, tr);
                return tr;
            }
        } catch (InstantiationException ex) {
            Object exArgs[] = { URI };
            throw new InvalidTransformException(
                "signature.Transform.UnknownTransform", exArgs, ex);
        } catch (IllegalAccessException ex) {
            Object exArgs[] = { URI };
            throw new InvalidTransformException(
                "signature.Transform.UnknownTransform", exArgs, ex);
        }
        return null;
    }
    public String getBaseLocalName() {
        return Constants._TAG_TRANSFORM;
    }
}
