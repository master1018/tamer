public class TestLayoutDevicesXsd extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    private static class CaptureErrorHandler implements ErrorHandler {
        private String mWarnings = "";
        private String mErrors = "";
        @SuppressWarnings("unused")
        public String getErrors() {
            return mErrors;
        }
        @SuppressWarnings("unused")
        public String getWarnings() {
            return mWarnings;
        }
        public void verify() {
            if (mWarnings.length() > 0) {
                System.err.println(mWarnings);
            }
            if (mErrors.length() > 0) {
                System.err.println(mErrors);
                fail(mErrors);
            }
        }
        public void error(SAXParseException ex) throws SAXException {
            mErrors += "Error: " + ex.getMessage() + "\n";
        }
        public void fatalError(SAXParseException ex) throws SAXException {
            mErrors += "Fatal Error: " + ex.getMessage() + "\n";
        }
        public void warning(SAXParseException ex) throws SAXException {
            mWarnings += "Warning: " + ex.getMessage() + "\n";
        }
    }
    private void assertRegex(String expectedRegexp, String actualString) {
        assertNotNull(actualString);
        assertTrue(
                String.format("Regexp Assertion Failed:\nExpected: %s\nActual: %s\n",
                        expectedRegexp, actualString),
                actualString.matches(expectedRegexp));
    }
    public void checkFailure(String document, String regexp) throws Exception {
        Source source = new StreamSource(new StringReader(document));
        Validator validator = LayoutDevicesXsd.getValidator(null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex(regexp, e.getMessage());
            return;
        }
        fail();
    }
    public void checkSuccess(String document) throws Exception {
        Source source = new StreamSource(new StringReader(document));
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = LayoutDevicesXsd.getValidator(null);
        validator.validate(source);
        handler.verify();
    }
    public void testValidateLocalRepositoryFile() throws Exception {
        InputStream xmlStream =
            TestLayoutDevicesXsd.class.getResourceAsStream("config_sample.xml");
        Source source = new StreamSource(xmlStream);
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = LayoutDevicesXsd.getValidator(handler);
        validator.validate(source);
        handler.verify();
    }
    public void testEmptyXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>",
                "Premature end of file.*");
    }
    public void testUnknownContentXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:unknown />" +
                "</d:layout-devices>",
                "cvc-complex-type.2.4.a: Invalid content was found.*");
    }
    public void testIncompleteContentXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device />" +
                "</d:layout-devices>",
                "cvc-complex-type.4: Attribute 'name' must appear on element 'd:device'.");
    }
    public void testEmptyRootXml() throws Exception {
        checkSuccess(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
    }
    public void testEmptyDeviceXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\"/>" +
                "</d:layout-devices>",
                "cvc-complex-type.2.4.b: The content of element 'd:device' is not complete.*");
    }
    public void testTwoDefaultsXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:default />" +
                "  <d:default />" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'd:default'.*");
    }
    public void testDefaultConfigOrderXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:config name=\"must-be-after-default\" />" +
                "  <d:default />" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'd:default'.*");
    }
    public void testScreenDimZeroXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:default>" +
                "    <d:screen-dimension> <d:size>0</d:size> <d:size>1</d:size> </d:screen-dimension>" +
                "  </d:default>" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-minInclusive-valid: Value '0' is not facet-valid with respect to minInclusive '1'.*");
    }
    public void testScreenDimNegativeXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:default>" +
                "    <d:screen-dimension> <d:size>-5</d:size> <d:size>1</d:size> </d:screen-dimension>" +
                "  </d:default>" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-minInclusive-valid: Value '-5' is not facet-valid with respect to minInclusive '1'.*");
    }
    public void testXDpiZeroXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:default>" +
                "    <d:xdpi>0</d:xdpi>" +
                "  </d:default>" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-minExclusive-valid: Value '0' is not facet-valid with respect to minExclusive '0.0E1'.*");
    }
    public void testXDpiNegativeXml() throws Exception {
        checkFailure(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:default>" +
                "    <d:xdpi>-3.1415926538</d:xdpi>" +
                "  </d:default>" +
                "</d:device>" +
                "</d:layout-devices>",
                "cvc-minExclusive-valid: Value '-3.1415926538' is not facet-valid with respect to minExclusive '0.0E1'.*");
    }
    public void testTokenWhitespaceXml() throws Exception {
        checkSuccess(
                "<?xml version=\"1.0\"?>" +
                "<d:layout-devices xmlns:d=\"http:
                "<d:device name=\"foo\">" +
                "  <d:config name='foo'>" +
                "    <d:screen-ratio>  \n long \r </d:screen-ratio>" +
                "  </d:config>" +
                "</d:device>" +
                "</d:layout-devices>");
    }
}
