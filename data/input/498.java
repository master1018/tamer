public class Transforms extends SignatureElementProxy {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(Transforms.class.getName());
    public static final String TRANSFORM_C14N_OMIT_COMMENTS
        = Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String TRANSFORM_C14N_WITH_COMMENTS
        = Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String TRANSFORM_C14N11_OMIT_COMMENTS
        = Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS;
    public static final String TRANSFORM_C14N11_WITH_COMMENTS
        = Canonicalizer.ALGO_ID_C14N11_WITH_COMMENTS;
    public static final String TRANSFORM_C14N_EXCL_OMIT_COMMENTS
        = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String TRANSFORM_C14N_EXCL_WITH_COMMENTS
        = Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
    public static final String TRANSFORM_XSLT
        = "http:
    public static final String TRANSFORM_BASE64_DECODE
        = Constants.SignatureSpecNS + "base64";
    public static final String TRANSFORM_XPATH
        = "http:
    public static final String TRANSFORM_ENVELOPED_SIGNATURE
        = Constants.SignatureSpecNS + "enveloped-signature";
    public static final String TRANSFORM_XPOINTER
        = "http:
    public static final String TRANSFORM_XPATH2FILTER04
        = "http:
    public static final String TRANSFORM_XPATH2FILTER
        = "http:
    public static final String TRANSFORM_XPATHFILTERCHGP
        = "http:
    Element []transforms;
    protected Transforms() { };
    public Transforms(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
    }
    public Transforms(Element element, String BaseURI)
           throws DOMException, XMLSignatureException,
                  InvalidTransformException, TransformationException,
                  XMLSecurityException {
        super(element, BaseURI);
        int numberOfTransformElems = this.getLength();
        if (numberOfTransformElems == 0) {
            Object exArgs[] = { Constants._TAG_TRANSFORM,
                                Constants._TAG_TRANSFORMS };
            throw new TransformationException("xml.WrongContent", exArgs);
        }
    }
    public void addTransform(String transformURI)
           throws TransformationException {
        try {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Transforms.addTransform(" + transformURI + ")");
            Transform transform =
                Transform.getInstance(this._doc, transformURI);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }
    public void addTransform(String transformURI, Element contextElement)
           throws TransformationException {
        try {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Transforms.addTransform(" + transformURI + ")");
            Transform transform =
                Transform.getInstance(this._doc, transformURI, contextElement);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }
    public void addTransform(String transformURI, NodeList contextNodes)
           throws TransformationException {
        try {
            Transform transform =
                Transform.getInstance(this._doc, transformURI, contextNodes);
            this.addTransform(transform);
        } catch (InvalidTransformException ex) {
            throw new TransformationException("empty", ex);
        }
    }
    private void addTransform(Transform transform) {
        if (log.isLoggable(java.util.logging.Level.FINE))
            log.log(java.util.logging.Level.FINE, "Transforms.addTransform(" + transform.getURI() + ")");
        Element transformElement = transform.getElement();
        this._constructionElement.appendChild(transformElement);
        XMLUtils.addReturnToElement(this._constructionElement);
    }
    public XMLSignatureInput performTransforms(
        XMLSignatureInput xmlSignatureInput) throws TransformationException {
        return performTransforms(xmlSignatureInput, null);
    }
    public XMLSignatureInput performTransforms(
        XMLSignatureInput xmlSignatureInput, OutputStream os)
        throws TransformationException {
        try {
            int last=this.getLength()-1;
            for (int i = 0; i < last; i++) {
                Transform t = this.item(i);
                if (log.isLoggable(java.util.logging.Level.FINE)) {
                    log.log(java.util.logging.Level.FINE, "Perform the (" + i + ")th " + t.getURI()
                        + " transform");
                }
                xmlSignatureInput = t.performTransform(xmlSignatureInput);
            }
            if (last>=0) {
                Transform t = this.item(last);
                xmlSignatureInput = t.performTransform(xmlSignatureInput, os);
            }
            return xmlSignatureInput;
        } catch (IOException ex) {
            throw new TransformationException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new TransformationException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new TransformationException("empty", ex);
        }
    }
    public int getLength()
    {
        if (transforms == null) {
            transforms = XMLUtils.selectDsNodes
                (this._constructionElement.getFirstChild(), "Transform");
        }
        return transforms.length;
    }
    public Transform item(int i) throws TransformationException {
        try {
            if (transforms == null) {
                transforms = XMLUtils.selectDsNodes
                    (this._constructionElement.getFirstChild(), "Transform");
            }
            return new Transform(transforms[i], this._baseURI);
        } catch (XMLSecurityException ex) {
            throw new TransformationException("empty", ex);
        }
    }
    public String getBaseLocalName() {
        return Constants._TAG_TRANSFORMS;
    }
}
