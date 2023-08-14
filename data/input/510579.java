@TestTargetClass(SAXParser.class) 
public class SAXParserTest extends TestCase {
    private class MockSAXParser extends SAXParser {
        public MockSAXParser() {
            super();
        }
        @Override
        public Parser getParser() throws SAXException {
            return null;
        }
        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException,
                SAXNotSupportedException {
            return null;
        }
        @Override
        public XMLReader getXMLReader() throws SAXException {
            return null;
        }
        @Override
        public boolean isNamespaceAware() {
            return false;
        }
        @Override
        public boolean isValidating() {
            return false;
        }
        @Override
        public void setProperty(String name, Object value) throws
                SAXNotRecognizedException, SAXNotSupportedException {
        }
    }
    private static final String LEXICAL_HANDLER_PROPERTY
            = "http:
    SAXParserFactory spf;
    SAXParser parser;
    static HashMap<String, String> ns;
    static Vector<String> el;
    static HashMap<String, String> attr;
    SAXParserTestSupport sp = new SAXParserTestSupport();
    File [] list_wf;
    File [] list_nwf;
    File [] list_out_dh;
    File [] list_out_hb;
    boolean validating = false;
    private InputStream getResource(String name) {
        return this.getClass().getResourceAsStream(name);        
    }
    public void initFiles() throws Exception {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            validating = parser.isValidating();
        } catch (Exception e) {
            fail("could not obtain a SAXParser");
        }
        String tmpPath = System.getProperty("java.io.tmpdir");
        list_wf = new File[] {new File(tmpPath + "/" + 
                SAXParserTestSupport.XML_WF + "staff.xml")};
        list_nwf = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_NWF + "staff.xml")};
        list_out_dh = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_WF_OUT_DH + "staff.out")};
        list_out_hb = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_WF_OUT_HB + "staff.out")};
        list_wf[0].deleteOnExit();
        list_nwf[0].deleteOnExit();
        list_out_hb[0].deleteOnExit();
        list_out_dh[0].deleteOnExit();
        Support_Resources.copyLocalFileto(list_wf[0],
                getResource(SAXParserTestSupport.XML_WF + "staff.xml"));
        Support_Resources.copyLocalFileto(new File(
                tmpPath + "/" + SAXParserTestSupport.XML_WF + "staff.dtd"),
                getResource(SAXParserTestSupport.XML_WF + "staff.dtd"));
        Support_Resources.copyLocalFileto(list_nwf[0],
                getResource(SAXParserTestSupport.XML_NWF + "staff.xml"));
        Support_Resources.copyLocalFileto(new File(
                tmpPath + "/" + SAXParserTestSupport.XML_NWF + "staff.dtd"),
                getResource(SAXParserTestSupport.XML_NWF + "staff.dtd"));
        Support_Resources.copyLocalFileto(list_out_dh[0],
                getResource(SAXParserTestSupport.XML_WF_OUT_DH + "staff.out"));
        Support_Resources.copyLocalFileto(list_out_hb[0],
                getResource(SAXParserTestSupport.XML_WF_OUT_HB + "staff.out"));
    }
    @Override
    protected void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser(); 
        assertNotNull(parser);
        ns = new HashMap<String, String>();
        attr = new HashMap<String, String>();
        el = new Vector<String>();
        initFiles();
    }
    @Override
    protected void tearDown() throws Exception {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SAXParser",
        args = {}
    )
    public void testSAXParser() {
        try {
            new MockSAXParser();
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isNamespaceAware",
        args = {}
    )
    public void testIsNamespaceAware() {
        try {
            spf.setNamespaceAware(true);
            assertTrue(spf.newSAXParser().isNamespaceAware());
            spf.setNamespaceAware(false);
            assertFalse(spf.newSAXParser().isNamespaceAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No validating parser in Android, hence not tested",
        method = "isValidating",
        args = {}
    )
    public void testIsValidating() {
        try {
            spf.setValidating(false);
            assertFalse(spf.newSAXParser().isValidating());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No XInclude-aware parser in Android, hence not tested",
        method = "isXIncludeAware",
        args = {}
    )
    @KnownFailure("Should handle XIncludeAware flag more gracefully")
    public void testIsXIncludeAware() {
        try {
            spf.setXIncludeAware(false);
            assertFalse(spf.newSAXParser().isXIncludeAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify positive functionality properly; not all exceptions are verified.",
        method = "parse",
        args = {java.io.File.class, org.xml.sax.helpers.DefaultHandler.class}
    )
    public void test_parseLjava_io_FileLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm =
                new SAXParserTestSupport().readFile(list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i], dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            }
        }
        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        }
        try {
            parser.parse(list_wf[0], (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Sufficient while XML parser situation is still unclear",
        method = "parse",
        args = {java.io.File.class, org.xml.sax.HandlerBase.class}
    )
    public void testParseFileHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                parser.parse(list_wf[i], dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyHandler dh = new MyHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            parser.parse(list_wf[0], (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify IOException.",
        method = "parse",
        args = {org.xml.sax.InputSource.class, org.xml.sax.helpers.DefaultHandler.class}
    )
    public void test_parseLorg_xml_sax_InputSourceLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputSource is = new InputSource(new FileInputStream(list_wf[i]));
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputSource is = new InputSource(
                        new FileInputStream(list_nwf[i]));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            }
        }
        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        }
        try {
            InputSource is = new InputSource(new FileInputStream(list_wf[0]));
            parser.parse(is, (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
        try {
            InputSource is = new InputSource(new BrokenInputStream(new FileInputStream(list_wf[0]), 10));
            parser.parse(is, (DefaultHandler) null);
            fail("IOException expected");
        } catch(IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Sufficient while XML parser situation is still unclear",
        method = "parse",
        args = {org.xml.sax.InputSource.class, org.xml.sax.HandlerBase.class}
    )
    public void testParseInputSourceHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                InputSource is = new InputSource(new FileInputStream(list_wf[i]));
                parser.parse(is, dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputSource is = new InputSource(new FileInputStream(list_nwf[i]));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputSource is = new InputSource(new FileInputStream(list_wf[0]));
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputSource is = new InputSource(new InputStreamReader(
                    new FileInputStream(list_wf[0])));
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputSource is = new InputSource(list_wf[0].toURI().toString());
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new BrokenInputStream(
                    new FileInputStream(list_wf[0]), 10);
            parser.parse(is, (HandlerBase) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("IOException expected");
        } catch(IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify IOException.",
        method = "parse",
        args = {java.io.InputStream.class, org.xml.sax.helpers.DefaultHandler.class}
    )
    public void test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            }
        }
        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        }
        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify  IOException.",
        method = "parse",
        args = {java.io.InputStream.class, org.xml.sax.helpers.DefaultHandler.class, java.lang.String.class}
    )
    @KnownFailure("We supply optional qnames, but this test doesn't expect them")
    public void test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandlerLjava_lang_String() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_wf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                assertEquals(hm, dh.createData());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Sufficient while XML parser situation is still unclear",
        method = "parse",
        args = {java.io.InputStream.class, org.xml.sax.HandlerBase.class}
    )
    public void testParseInputStreamHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_wf[i]);
                parser.parse(is, dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new BrokenInputStream(
                    new FileInputStream(list_wf[0]), 10);
            parser.parse(is, (HandlerBase) null);
            fail("IOException expected");
        } catch(IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Sufficient while XML parser situation is still unclear",
        method = "parse",
        args = {java.io.InputStream.class, org.xml.sax.HandlerBase.class, java.lang.String.class}
    )
    public void testParseInputStreamHandlerBaseString() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_wf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputStream) null, dh, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (HandlerBase) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            InputStream is = new BrokenInputStream(
                    new FileInputStream(list_wf[0]), 10);
            parser.parse(is, (HandlerBase) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("IOException expected");
        } catch(IOException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify IOException.",
        method = "parse",
        args = {java.lang.String.class, org.xml.sax.helpers.DefaultHandler.class}
    )
    public void test_parseLjava_lang_StringLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i].toURI().toString(), dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i].toURI().toString(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            }
        }
        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        }
        try {
            parser.parse(list_wf[0].toURI().toString(), (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Sufficient while XML parser situation is still unclear",
        method = "parse",
        args = {java.lang.String.class, org.xml.sax.HandlerBase.class}
    )
    public void testParseStringHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                parser.parse(list_wf[i].toURI().toString(), dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }
        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i].toURI().toString(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }
        try {
            MyHandler dh = new MyHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            parser.parse(list_wf[0].toURI().toString(), (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "reset",
        args = { }
    )
    @KnownFailure("Android DocumentBuilder should implement reset() properly")
    public void testReset() {
        try {
            spf = SAXParserFactory.newInstance();
            parser = spf.newSAXParser();
            parser.setProperty(LEXICAL_HANDLER_PROPERTY, new MockHandler(new MethodLogger()));
            parser.reset();
            assertEquals(null, parser.getProperty(LEXICAL_HANDLER_PROPERTY));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getParser",
        args = { }
    )
    public void testGetParser() {
        spf = SAXParserFactory.newInstance();
        try {
            Parser parser = spf.newSAXParser().getParser();
            assertNotNull(parser);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getXMLReader",
        args = { }
    )
    public void testGetReader() {
        spf = SAXParserFactory.newInstance();
        try {
            XMLReader reader = spf.newSAXParser().getXMLReader();
            assertNotNull(reader);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProperty",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProperty",
            args = { String.class, Object.class }
        )
    })
    @KnownFailure("ExpatParser should allow to clear properties")
    public void testSetGetProperty() {
        String validName = "http:
        LexicalHandler validValue = new MockHandler(new MethodLogger());            
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty(validName, validValue);
            assertEquals(validValue, parser.getProperty(validName));
            parser.setProperty(validName, null);
            assertEquals(null, parser.getProperty(validName));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty("foo", "bar");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            SAXParser parser = spf.newSAXParser();
            parser.getProperty("foo");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty(null, "bar");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            SAXParser parser = spf.newSAXParser();
            parser.getProperty(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
