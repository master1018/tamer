public class UiElementPullParserTest extends TestCase {
    private UiElementNode ui;
    private HashMap<String, String> button1Map;
    private HashMap<String, String> button2Map;
    private HashMap<String, String> textMap;
    @Override
    protected void setUp() throws Exception {
        ElementDescriptor buttonDescriptor = new ElementDescriptor("Button", "Button", "", "",
                new AttributeDescriptor[] {
                    new TextAttributeDescriptor("name", "name", SdkConstants.NS_RESOURCES, ""),
                    new TextAttributeDescriptor("text", "text", SdkConstants.NS_RESOURCES, ""),
                    },
                new ElementDescriptor[] {}, false);
        ElementDescriptor textDescriptor = new ElementDescriptor("TextView", "TextView", "", "",
                new AttributeDescriptor[] {
                new TextAttributeDescriptor("name", "name", SdkConstants.NS_RESOURCES, ""),
                new TextAttributeDescriptor("text", "text", SdkConstants.NS_RESOURCES, ""), },
                new ElementDescriptor[] {}, false);
        ElementDescriptor linearDescriptor = new ElementDescriptor("LinearLayout", "Linear Layout",
                "", "",
                new AttributeDescriptor[] {
                    new TextAttributeDescriptor("orientation", "orientation",
                            SdkConstants.NS_RESOURCES, ""),
                },
                new ElementDescriptor[] { }, false);
        ElementDescriptor relativeDescriptor = new ElementDescriptor("RelativeLayout",
                "Relative Layout", "", "",
                new AttributeDescriptor[] {
                    new TextAttributeDescriptor("orientation", "orientation",
                            SdkConstants.NS_RESOURCES, ""),
                },
                new ElementDescriptor[] { }, false);
        ElementDescriptor[] a = new ElementDescriptor[] {
                buttonDescriptor, textDescriptor, linearDescriptor, relativeDescriptor
        };
        linearDescriptor.setChildren(a);
        relativeDescriptor.setChildren(a);
        ElementDescriptor rootDescriptor = new ElementDescriptor("root", "", "", "",
                new AttributeDescriptor[] { }, a, false);
        ui = new UiElementNode(rootDescriptor);
        MockXmlNode button1 = new MockXmlNode(null , "Button", Node.ELEMENT_NODE,
                null);
        button1.addAttributes(SdkConstants.NS_RESOURCES, "name", "button1");
        button1.addAttributes(SdkConstants.NS_RESOURCES, "text", "button1text");
        button1Map = new HashMap<String, String>();
        button1Map.put("name", "button1");
        button1Map.put("text", "button1text");
        MockXmlNode button2 = new MockXmlNode(null , "Button", Node.ELEMENT_NODE,
                null);
        button2.addAttributes(SdkConstants.NS_RESOURCES, "name", "button2");
        button2.addAttributes(SdkConstants.NS_RESOURCES, "text", "button2text");
        button2Map = new HashMap<String, String>();
        button2Map.put("name", "button2");
        button2Map.put("text", "button2text");
        MockXmlNode text = new MockXmlNode(null , "TextView", Node.ELEMENT_NODE,
                null);
        text.addAttributes(SdkConstants.NS_RESOURCES, "name", "text1");
        text.addAttributes(SdkConstants.NS_RESOURCES, "text", "text1text");
        textMap = new HashMap<String, String>();
        textMap.put("name", "text1");
        textMap.put("text", "text1text");
        MockXmlNode relative = new MockXmlNode(null , "RelativeLayout",
                Node.ELEMENT_NODE, new MockXmlNode[] { button2, text });
        relative.addAttributes(SdkConstants.NS_RESOURCES, "orientation", "toto");
        MockXmlNode linear = new MockXmlNode(null , "LinearLayout",
                Node.ELEMENT_NODE, new MockXmlNode[] { button1, relative });
        linear.addAttributes(SdkConstants.NS_RESOURCES, "orientation", "vertical");
        MockXmlNode root = new MockXmlNode(null , "root", Node.ELEMENT_NODE,
                new MockXmlNode[] { linear });
        root.setPrefix(SdkConstants.NS_RESOURCES, "android");
        ui.loadFromXmlNode(root);
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testParser() {
        try {
            UiElementPullParser parser = new UiElementPullParser(
                    ui, 
                    false, 
                    Density.MEDIUM.getDpiValue(), 
                    Density.MEDIUM.getDpiValue(), 
                    null 
                    );
            assertEquals(XmlPullParser.START_DOCUMENT, parser.getEventType());
            assertEquals(XmlPullParser.START_TAG, parser.next());
            assertEquals("LinearLayout", parser.getName());
            assertEquals(1, parser.getAttributeCount());
            assertEquals("orientation", parser.getAttributeName(0));
            assertEquals(SdkConstants.NS_RESOURCES, parser.getAttributeNamespace(0));
            assertEquals("android", parser.getAttributePrefix(0));
            assertEquals("vertical", parser.getAttributeValue(0));
            assertEquals(XmlPullParser.START_TAG, parser.next());
            assertEquals("Button", parser.getName());
            assertEquals(2, parser.getAttributeCount());
            check(parser, 0, button1Map);
            check(parser, 1, button1Map);
            assertEquals(XmlPullParser.END_TAG, parser.next());
            assertEquals(XmlPullParser.START_TAG, parser.next());
            assertEquals("RelativeLayout", parser.getName());
            assertEquals(1, parser.getAttributeCount());
            assertEquals("orientation", parser.getAttributeName(0));
            assertEquals(SdkConstants.NS_RESOURCES, parser.getAttributeNamespace(0));
            assertEquals("android", parser.getAttributePrefix(0));
            assertEquals("toto", parser.getAttributeValue(0));
            assertEquals(XmlPullParser.START_TAG, parser.next());
            assertEquals("Button", parser.getName());
            assertEquals(2, parser.getAttributeCount());
            check(parser, 0, button2Map);
            check(parser, 1, button2Map);
            assertEquals(XmlPullParser.END_TAG, parser.next());
            assertEquals(XmlPullParser.START_TAG, parser.next());
            assertEquals("TextView", parser.getName());
            assertEquals(2, parser.getAttributeCount());
            check(parser, 0, textMap);
            check(parser, 1, textMap);
            assertEquals(XmlPullParser.END_TAG, parser.next());
            assertEquals(XmlPullParser.END_TAG, parser.next());
            assertEquals(XmlPullParser.END_TAG, parser.next());
            assertEquals(XmlPullParser.END_DOCUMENT, parser.next());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    private void check(UiElementPullParser parser, int i, HashMap<String, String> map) {
        String name = parser.getAttributeName(i);
        String value = parser.getAttributeValue(i);
        String referenceValue = map.get(name);
        assertNotNull(referenceValue);
        assertEquals(referenceValue, value);
        assertEquals(SdkConstants.NS_RESOURCES, parser.getAttributeNamespace(i));
        assertEquals("android", parser.getAttributePrefix(i));
    }
}
