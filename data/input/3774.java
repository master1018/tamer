public class Canonicalizer {
    public static final String ENCODING = "UTF8";
    public static final String XPATH_C14N_WITH_COMMENTS_SINGLE_NODE =
        "(.
    public static final String ALGO_ID_C14N_OMIT_COMMENTS =
        "http:
    public static final String ALGO_ID_C14N_WITH_COMMENTS =
        ALGO_ID_C14N_OMIT_COMMENTS + "#WithComments";
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS =
        "http:
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS =
        ALGO_ID_C14N_EXCL_OMIT_COMMENTS + "WithComments";
    public static final String ALGO_ID_C14N11_OMIT_COMMENTS =
        "http:
    public static final String ALGO_ID_C14N11_WITH_COMMENTS =
        ALGO_ID_C14N11_OMIT_COMMENTS + "#WithComments";
    static boolean _alreadyInitialized = false;
    static Map _canonicalizerHash = null;
    protected CanonicalizerSpi canonicalizerSpi = null;
    public static void init() {
        if (!Canonicalizer._alreadyInitialized) {
            Canonicalizer._canonicalizerHash = new HashMap(10);
            Canonicalizer._alreadyInitialized = true;
        }
    }
    private Canonicalizer(String algorithmURI)
           throws InvalidCanonicalizerException {
        try {
            Class implementingClass = getImplementingClass(algorithmURI);
            this.canonicalizerSpi =
                (CanonicalizerSpi) implementingClass.newInstance();
            this.canonicalizerSpi.reset=true;
        } catch (Exception e) {
            Object exArgs[] = { algorithmURI };
            throw new InvalidCanonicalizerException(
               "signature.Canonicalizer.UnknownCanonicalizer", exArgs);
        }
    }
    public static final Canonicalizer getInstance(String algorithmURI)
           throws InvalidCanonicalizerException {
        Canonicalizer c14nizer = new Canonicalizer(algorithmURI);
        return c14nizer;
    }
    public static void register(String algorithmURI, String implementingClass)
           throws AlgorithmAlreadyRegisteredException {
        Class registeredClass = getImplementingClass(algorithmURI);
        if (registeredClass != null)  {
            Object exArgs[] = { algorithmURI, registeredClass };
            throw new AlgorithmAlreadyRegisteredException(
                "algorithm.alreadyRegistered", exArgs);
        }
        try {
            _canonicalizerHash.put(algorithmURI, Class.forName(implementingClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("c14n class not found");
        }
    }
    public final String getURI() {
        return this.canonicalizerSpi.engineGetURI();
    }
    public boolean getIncludeComments() {
        return this.canonicalizerSpi.engineGetIncludeComments();
    }
    public byte[] canonicalize(byte[] inputBytes)
           throws javax.xml.parsers.ParserConfigurationException,
                  java.io.IOException, org.xml.sax.SAXException,
                  CanonicalizationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        InputSource in = new InputSource(bais);
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        dfactory.setValidating(true);
        DocumentBuilder db = dfactory.newDocumentBuilder();
        db.setErrorHandler(new com.sun.org.apache.xml.internal.security.utils
            .IgnoreAllErrorHandler());
        Document document = db.parse(in);
        byte result[] = this.canonicalizeSubtree(document);
        return result;
    }
    public byte[] canonicalizeSubtree(Node node)
           throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeSubTree(node);
    }
    public byte[] canonicalizeSubtree(Node node, String inclusiveNamespaces)
           throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeSubTree(node,
              inclusiveNamespaces);
    }
    public byte[] canonicalizeXPathNodeSet(NodeList xpathNodeSet)
           throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }
    public byte[] canonicalizeXPathNodeSet(
           NodeList xpathNodeSet, String inclusiveNamespaces)
              throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet,
              inclusiveNamespaces);
    }
    public byte[] canonicalizeXPathNodeSet(Set xpathNodeSet)
           throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet);
    }
    public byte[] canonicalizeXPathNodeSet(Set xpathNodeSet,
        String inclusiveNamespaces) throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeXPathNodeSet(xpathNodeSet,
            inclusiveNamespaces);
    }
    public void setWriter(OutputStream os) {
        this.canonicalizerSpi.setWriter(os);
    }
    public String getImplementingCanonicalizerClass() {
        return this.canonicalizerSpi.getClass().getName();
    }
    private static Class getImplementingClass(String URI) {
        return (Class) _canonicalizerHash.get(URI);
    }
    public void notReset() {
        this.canonicalizerSpi.reset = false;
    }
}
