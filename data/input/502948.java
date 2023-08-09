@TestTargetClass(Node.class) 
public final class NodeNormalize extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration1());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "normalize",
        args = {}
    )
    public void testNormalize() throws Throwable {
        Document doc;
        Document newDoc;
        DOMImplementation domImpl;
        DocumentType docTypeNull = null;
        Element documentElement;
        Element element1;
        Element element2;
        Element element3;
        Element element4;
        Element element5;
        Element element6;
        Element element7;
        Text text1;
        Text text2;
        Text text3;
        ProcessingInstruction pi;
        CDATASection cData;
        Comment comment;
        EntityReference entRef;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument("http:
                "dom:root", docTypeNull);
        element1 = newDoc.createElement("element1");
        element2 = newDoc.createElement("element2");
        element3 = newDoc.createElement("element3");
        element4 = newDoc.createElement("element4");
        element5 = newDoc.createElement("element5");
        element6 = newDoc.createElement("element6");
        element7 = newDoc.createElement("element7");
        text1 = newDoc.createTextNode("text1");
        text2 = newDoc.createTextNode("text2");
        text3 = newDoc.createTextNode("text3");
        cData = newDoc.createCDATASection("Cdata");
        comment = newDoc.createComment("comment");
        pi = newDoc.createProcessingInstruction("PITarget", "PIData");
        entRef = newDoc.createEntityReference("EntRef");
        assertNotNull("createdEntRefNotNull", entRef);
        documentElement = newDoc.getDocumentElement();
        documentElement.appendChild(element1);
        element2.appendChild(text1);
        element2.appendChild(text2);
        element2.appendChild(text3);
        element1.appendChild(element2);
        text1 = (Text) text1.cloneNode(false);
        text2 = (Text) text2.cloneNode(false);
        element3.appendChild(entRef);
        element3.appendChild(text1);
        element3.appendChild(text2);
        element1.appendChild(element3);
        text1 = (Text) text1.cloneNode(false);
        text2 = (Text) text2.cloneNode(false);
        element4.appendChild(cData);
        element4.appendChild(text1);
        element4.appendChild(text2);
        element1.appendChild(element4);
        text2 = (Text) text2.cloneNode(false);
        text3 = (Text) text3.cloneNode(false);
        element5.appendChild(comment);
        element5.appendChild(text2);
        element5.appendChild(text3);
        element1.appendChild(element5);
        text2 = (Text) text2.cloneNode(false);
        text3 = (Text) text3.cloneNode(false);
        element6.appendChild(pi);
        element6.appendChild(text2);
        element6.appendChild(text3);
        element1.appendChild(element6);
        entRef = (EntityReference) entRef.cloneNode(false);
        text1 = (Text) text1.cloneNode(false);
        text2 = (Text) text2.cloneNode(false);
        text3 = (Text) text3.cloneNode(false);
        element7.appendChild(entRef);
        element7.appendChild(text1);
        element7.appendChild(text2);
        element7.appendChild(text3);
        element1.appendChild(element7);
        elementList = element1.getChildNodes();
        assertEquals("nodeNormalize01_1Bef", 6, elementList.getLength());
        elementList = element2.getChildNodes();
        assertEquals("nodeNormalize01_2Bef", 3, elementList.getLength());
        elementList = element3.getChildNodes();
        assertEquals("nodeNormalize01_3Bef", 3, elementList.getLength());
        elementList = element4.getChildNodes();
        assertEquals("nodeNormalize01_4Bef", 3, elementList.getLength());
        elementList = element5.getChildNodes();
        assertEquals("nodeNormalize01_5Bef", 3, elementList.getLength());
        elementList = element6.getChildNodes();
        assertEquals("nodeNormalize01_6Bef", 3, elementList.getLength());
        elementList = element7.getChildNodes();
        assertEquals("nodeNormalize01_7Bef", 4, elementList.getLength());
        newDoc.normalize();
        elementList = element1.getChildNodes();
        assertEquals("nodeNormalize01_1Aft", 6, elementList.getLength());
        elementList = element2.getChildNodes();
        assertEquals("nodeNormalize01_2Aft", 1, elementList.getLength());
        elementList = element3.getChildNodes();
        assertEquals("nodeNormalize01_3Aft", 2, elementList.getLength());
        elementList = element4.getChildNodes();
        assertEquals("nodeNormalize01_4Aft", 2, elementList.getLength());
        elementList = element5.getChildNodes();
        assertEquals("nodeNormalize01_5Aft", 2, elementList.getLength());
        elementList = element6.getChildNodes();
        assertEquals("nodeNormalize01_6Aft", 2, elementList.getLength());
        elementList = element7.getChildNodes();
        assertEquals("nodeNormalize01_7Aft", 2, elementList.getLength());
    }
}
