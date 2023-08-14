public class SdkRepositoryTest extends TestCase {
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
    private Validator getValidator(int version, CaptureErrorHandler handler) throws SAXException {
        InputStream xsdStream = SdkRepository.getXsdStream(version);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        Validator validator = schema.newValidator();
        if (handler != null) {
            validator.setErrorHandler(handler);
        }
        return validator;
    }
    private void assertRegex(String expectedRegexp, String actualString) {
        assertNotNull(actualString);
        assertTrue(
                String.format("Regexp Assertion Failed:\nExpected: %s\nActual: %s\n",
                        expectedRegexp, actualString),
                actualString.matches(expectedRegexp));
    }
    public void testValidateLocalRepositoryFile1() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_1.xml");
        Source source = new StreamSource(xmlStream);
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(1, handler);
        validator.validate(source);
        handler.verify();
    }
    public void testValidateLocalRepositoryFile2() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_2.xml");
        Source source = new StreamSource(xmlStream);
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(2, handler);
        validator.validate(source);
        handler.verify();
    }
    public void testEmptyXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>";
        Source source = new StreamSource(new StringReader(document));
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, handler);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("Premature end of file.*", e.getMessage());
            return;
        }
        handler.verify();
        fail();
    }
    private static String OPEN_TAG =
        "<r:sdk-repository xmlns:r=\"http:
        Integer.toString(SdkRepository.NS_LATEST_VERSION) +
        "\">";
    private static String CLOSE_TAG = "</r:sdk-repository>";
    public void testEmptyRootXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        CaptureErrorHandler handler = new CaptureErrorHandler();
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, handler);
        validator.validate(source);
        handler.verify();
    }
    public void testUnknownContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            "<r:unknown />" +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("cvc-complex-type.2.4.a: Invalid content was found.*", e.getMessage());
            return;
        }
        fail();
    }
    public void testIncompleteContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            "<r:platform> <r:api-level>1</r:api-level> <r:libs /> </r:platform>" +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("cvc-complex-type.2.4.a: Invalid content was found.*", e.getMessage());
            return;
        }
        fail();
    }
    public void testWrongTypeContentXml() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            "<r:platform> <r:api-level>NotAnInteger</r:api-level> <r:libs /> </r:platform>" +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("cvc-datatype-valid.1.2.1: 'NotAnInteger' is not a valid value.*",
                    e.getMessage());
            return;
        }
        fail();
    }
    public void testLicenseIdNotFound() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            "<r:license id=\"lic1\"> some license </r:license> " +
            "<r:tool> <r:uses-license ref=\"lic2\" /> <r:revision>1</r:revision> " +
            "<r:archives> <r:archive os=\"any\"> <r:size>1</r:size> <r:checksum>2822ae37115ebf13412bbef91339ee0d9454525e</r:checksum> " +
            "<r:url>url</r:url> </r:archive> </r:archives> </r:tool>" +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("cvc-id.1: There is no ID/IDREF binding for IDREF 'lic2'.*",
                    e.getMessage());
            return;
        }
        fail();
    }
    public void testExtraPathWithSlash() throws Exception {
        String document = "<?xml version=\"1.0\"?>" +
            OPEN_TAG +
            "<r:extra> <r:revision>1</r:revision> <r:path>path/cannot\\contain\\segments</r:path> " +
            "<r:archives> <r:archive os=\"any\"> <r:size>1</r:size> <r:checksum>2822ae37115ebf13412bbef91339ee0d9454525e</r:checksum> " +
            "<r:url>url</r:url> </r:archive> </r:archives> </r:extra>" +
            CLOSE_TAG;
        Source source = new StreamSource(new StringReader(document));
        Validator validator = getValidator(SdkRepository.NS_LATEST_VERSION, null);
        try {
            validator.validate(source);
        } catch (SAXParseException e) {
            assertRegex("cvc-pattern-valid: Value 'path/cannot\\\\contain\\\\segments' is not facet-valid with respect to pattern.*",
                    e.getMessage());
            return;
        }
        fail();
    }
}
