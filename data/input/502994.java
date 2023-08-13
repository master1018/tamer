@TestTargetClass(Document.class) 
public final class GetElementsByTagNameNS extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration2());
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
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS method with * as parameters.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS1() throws Throwable {
        String namespaceURI = "*";
        String localName = "*";
        Document doc;
        NodeList newList;
        doc = (Document) load("staffNS", builder);
        newList = doc.getElementsByTagNameNS(namespaceURI, localName);
        assertEquals("throw_Size", 36, newList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS with '*' as the first parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS2() throws Throwable {
        Document doc;
        NodeList newList;
        Element newElement;
        String prefix;
        String lname;
        doc = (Document) load("staffNS", builder);
        newList = doc.getElementsByTagNameNS("*", "employee");
        assertEquals("employeeCount", 5, newList.getLength());
        newElement = (Element) newList.item(3);
        prefix = newElement.getPrefix();
        assertEquals("prefix", "emp", prefix);
        lname = newElement.getLocalName();
        assertEquals("lname", "employee", lname);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS with '*' as the second parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS3() throws Throwable {
        Document doc;
        NodeList elementList;
        Node child;
        String childName;
        List<String> result = new ArrayList<String>();
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("employee");
        expectedResult.add("employeeId");
        expectedResult.add("name");
        expectedResult.add("position");
        expectedResult.add("salary");
        expectedResult.add("gender");
        expectedResult.add("address");
        expectedResult.add("emp:employee");
        expectedResult.add("emp:employeeId");
        expectedResult.add("emp:position");
        expectedResult.add("emp:salary");
        expectedResult.add("emp:gender");
        expectedResult.add("emp:address");
        expectedResult.add("address");
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
        for (int indexN10076 = 0; indexN10076 < elementList.getLength(); indexN10076++) {
            child = (Node) elementList.item(indexN10076);
            childName = child.getNodeName();
            result.add(childName);
        }
        assertEquals("nodeNames", expectedResult, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS with '*' as the first parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node child;
        String childName;
        List<String> result = new ArrayList<String>();
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("emp:address");
        expectedResult.add("address");
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        for (int indexN10059 = 0; indexN10059 < elementList.getLength(); indexN10059++) {
            child = (Node) elementList.item(indexN10059);
            childName = child.getNodeName();
            result.add(childName);
        }
        assertEquals("nodeNames", expectedResult, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative case of getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS5() throws Throwable {
        String namespaceURI = "http:
        String localName = "nomatch";
        Document doc;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS(namespaceURI, localName);
        assertEquals("throw_Size", 0, elementList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative case of getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS6() throws Throwable {
        Document doc;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        assertEquals("matchSize", 0, elementList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case of getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS7() throws Throwable {
        Document doc;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        assertEquals("addresses", 3, elementList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS method with '*' as parameters; positive case.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS8() throws Throwable {
        Document doc;
        Element docElem;
        NodeList newList;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        newList = docElem.getElementsByTagNameNS("*", "*");
        assertEquals("listSize", 36, newList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS method with '*' as the first parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS9() throws Throwable {
        Document doc;
        NodeList newList;
        Element newElement;
        String prefix;
        String lname;
        Element docElem;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        newList = docElem.getElementsByTagNameNS("*", "employee");
        assertEquals("employeeCount", 5, newList.getLength());
        newElement = (Element) newList.item(3);
        prefix = newElement.getPrefix();
        assertEquals("prefix", "emp", prefix);
        lname = newElement.getLocalName();
        assertEquals("lname", "employee", lname);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS method with '*' as the second parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS10() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        Node child;
        String childName;
        List<String> result = new ArrayList<String>();
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("employee");
        expectedResult.add("employeeId");
        expectedResult.add("name");
        expectedResult.add("position");
        expectedResult.add("salary");
        expectedResult.add("gender");
        expectedResult.add("address");
        expectedResult.add("emp:employee");
        expectedResult.add("emp:employeeId");
        expectedResult.add("emp:position");
        expectedResult.add("emp:salary");
        expectedResult.add("emp:gender");
        expectedResult.add("emp:address");
        expectedResult.add("address");
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        elementList = docElem
                .getElementsByTagNameNS("http:
        for (int indexN1007E = 0; indexN1007E < elementList.getLength(); indexN1007E++) {
            child = (Node) elementList.item(indexN1007E);
            childName = child.getNodeName();
            result.add(childName);
        }
        assertEquals("nodeNames", expectedResult, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getElementsByTagNameNS method with '*' as the first parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS11() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        Node child;
        String childName;
        List<String> result = new ArrayList<String>();
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("emp:address");
        expectedResult.add("address");
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        elementList = docElem.getElementsByTagNameNS("*", "address");
        for (int indexN1005E = 0; indexN1005E < elementList.getLength(); indexN1005E++) {
            child = (Node) elementList.item(indexN1005E);
            childName = child.getNodeName();
            result.add(childName);
        }
        assertEquals("nodeNames", expectedResult, result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative case for getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS12() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        elementList = docElem.getElementsByTagNameNS("http:
                "nomatch");
        assertEquals("size", 0, elementList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative case for getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS13() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        elementList = docElem.getElementsByTagNameNS("http:
                "address");
        assertEquals("matchSize", 0, elementList.getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case for getElementsByTagNameNS method.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS14() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        elementList = docElem.getElementsByTagNameNS("http:
                "address");
        assertEquals("addresses", 3, elementList.getLength());
    }
}
