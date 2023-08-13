public class MyTransform extends TransformSpi {
    public static final String URI =
        "http:
    public MyTransform() {
        try {
            System.out.println("Registering Transform");
            Transform.init();
            Transform.register(URI, "MyTransform");
        } catch (AlgorithmAlreadyRegisteredException e) {
        }
    }
    protected String engineGetURI() {
        return URI;
    }
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input)
        throws IOException, CanonicalizationException,
               InvalidCanonicalizerException, TransformationException,
               ParserConfigurationException, SAXException {
        throw new TransformationException("Unsupported Operation");
    }
}
