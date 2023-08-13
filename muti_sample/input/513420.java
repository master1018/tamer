public class JaxenXPathTestSuite {
    private static final File DEFAULT_JAXEN_HOME
            = new File("/home/dalvik-prebuild/jaxen");
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: JaxenXPathTestSuite <jaxen-home>");
            return;
        }
        File jaxenHome = new File(args[0]);
        TestRunner.run(suite(jaxenHome));
    }
    public static Test suite() throws Exception {
        return suite(DEFAULT_JAXEN_HOME);
    }
    public static Test suite(File jaxenHome)
            throws ParserConfigurationException, IOException, SAXException {
        File testsXml = new File(jaxenHome + "/xml/test/tests.xml");
        Element tests = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(testsXml).getDocumentElement();
        TestSuite result = new TestSuite();
        for (Element document : elementsOf(tests.getElementsByTagName("document"))) {
            String url = document.getAttribute("url");
            InputSource inputSource = new InputSource("file:" + jaxenHome + "/" + url);
            for (final Element context : elementsOf(document.getElementsByTagName("context"))) {
                contextToTestSuite(result, url, inputSource, context);
            }
        }
        return result;
    }
    private static void contextToTestSuite(TestSuite suite, String url,
            InputSource inputSource, Element element) {
        String select = element.getAttribute("select");
        Context context = new Context(inputSource, url, select);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setXPathVariableResolver(new ElementVariableResolver(element));
        for (Element test : elementsOf(element.getChildNodes())) {
            if (test.getTagName().equals("test")) {
                suite.addTest(createFromTest(xpath, context, test));
            } else if (test.getTagName().equals("valueOf")) {
                suite.addTest(createFromValueOf(xpath, context, test));
            } else {
                throw new UnsupportedOperationException("Unsupported test: " + context);
            }
        }
    }
    private static TestCase createFromTest(
            final XPath xpath, final Context context, final Element element) {
        final String select = element.getAttribute("select");
        if (element.getAttribute("exception").equals("true")) {
            return new XPathTest(context, select) {
                @Override void test(Node contextNode) {
                    try {
                        xpath.evaluate(select, contextNode);
                        fail("Expected exception!");
                    } catch (XPathExpressionException expected) {
                    }
                }
            };
        }
        NodeList valueOfElements = element.getElementsByTagName("valueOf");
        if (valueOfElements.getLength() == 1) {
            final Element valueOf = (Element) valueOfElements.item(0);
            final String valueOfSelect = valueOf.getAttribute("select");
            return new XPathTest(context, select) {
                @Override void test(Node contextNode) throws XPathExpressionException {
                    Node newContext = (Node) xpath.evaluate(
                            select, contextNode, XPathConstants.NODE);
                    assertEquals(valueOf.getTextContent(),
                            xpath.evaluate(valueOfSelect, newContext, XPathConstants.STRING));
                }
            };
        }
        final String count = element.getAttribute("count");
        if (count.length() > 0) {
            return new XPathTest(context, select) {
                @Override void test(Node contextNode) throws XPathExpressionException {
                    NodeList result = (NodeList) xpath.evaluate(
                            select, contextNode, XPathConstants.NODESET);
                    assertEquals(Integer.parseInt(count), result.getLength());
                }
            };
        }
        throw new UnsupportedOperationException("Unsupported test: " + context);
    }
    private static TestCase createFromValueOf(
            final XPath xpath, final Context context, final Element element) {
        final String select = element.getAttribute("select");
        return new XPathTest(context, select) {
            @Override void test(Node contextNode) throws XPathExpressionException {
                assertEquals(element.getTextContent(),
                        xpath.evaluate(select, contextNode, XPathConstants.STRING));
            }
        };
    }
    static class Context {
        private final InputSource inputSource;
        private final String url;
        private final String select;
        Context(InputSource inputSource, String url, String select) {
            this.inputSource = inputSource;
            this.url = url;
            this.select = select;
        }
        Node getNode() {
            XPath xpath = XPathFactory.newInstance().newXPath();
            try {
                return (Node) xpath.evaluate(select, inputSource, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                Error error = new AssertionFailedError("Failed to get context");
                error.initCause(e);
                throw error;
            }
        }
        @Override public String toString() {
            return url + " " + select;
        }
    }
    public abstract static class XPathTest extends TestCase {
        private final Context context;
        private final String select;
        public XPathTest(Context context, String select) {
            super("test");
            this.context = context;
            this.select = select;
        }
        abstract void test(Node contextNode) throws XPathExpressionException;
        public final void test() throws XPathExpressionException {
            try {
                test(context.getNode());
            } catch (XPathExpressionException e) {
                if (isMissingFunction(e)) {
                    fail(e.getCause().getMessage());
                } else {
                    throw e;
                }
            }
        }
        private boolean isMissingFunction(XPathExpressionException e) {
            return e.getCause() != null
                    && e.getCause().getMessage().startsWith("Could not find function");
        }
        @Override public String getName() {
            return context + " " + select;
        }
    }
    private static class ElementVariableResolver implements XPathVariableResolver {
        private final Element element;
        public ElementVariableResolver(Element element) {
            this.element = element;
        }
        public Object resolveVariable(QName variableName) {
            return element.getAttribute("var:" + variableName.getLocalPart());
        }
    }
    private static List<Element> elementsOf(NodeList nodeList) {
        List<Element> result = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                result.add((Element) node);
            }
        }
        return result;
    }
}
