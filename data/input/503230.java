@TestTargetClass(NamedNodeMap.class) 
public final class SetNamedItemNS extends DOMTestCase {
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
        notes = "Verifies DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
   public void testSetNamedItemNS1() throws Throwable {
      Document doc;
      NodeList elementList;
      Node anotherElement;
      NamedNodeMap anotherMap;
      Node arg;
      Node testAddress;
      NamedNodeMap map;
      doc = (Document) load("staffNS", builder);
      elementList = doc.getElementsByTagName("address");
      anotherElement = elementList.item(2);
      anotherMap = anotherElement.getAttributes();
      arg = anotherMap.getNamedItemNS("http:
      testAddress = elementList.item(0);
      map = testAddress.getAttributes();
      {
         boolean success = false;
         try {
            map.setNamedItemNS(arg);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("throw_INUSE_ATTRIBUTE_ERR", success);
      }
}
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with WRONG_DOCUMENT_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
   public void testSetNamedItemNS2() throws Throwable {
          String namespaceURI = "http:
          String qualifiedName = "dmstc:domestic";
          Document doc;
          Document anotherDoc;
          Node arg;
          NodeList elementList;
          Node testAddress;
          NamedNodeMap attributes;
          doc = (Document) load("staffNS", builder);
          anotherDoc = (Document) load("staffNS", builder);
          arg = anotherDoc.createAttributeNS(namespaceURI, qualifiedName);
          arg.setNodeValue("Maybe");
          elementList = doc.getElementsByTagName("address");
          testAddress = elementList.item(0);
          attributes = testAddress.getAttributes();
          {
             boolean success = false;
             try {
                attributes.setNamedItemNS(arg);
              } catch (DOMException ex) {
                success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
             }
             assertTrue("throw_WRONG_DOCUMENT_ERR", success);
          }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive fnctionality.",
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
   public void testSetNamedItemNS3() throws Throwable {
          String namespaceURI = "http:
          String qualifiedName = "prefix:newAttr";
          Document doc;
          Node arg;
          NodeList elementList;
          Node testAddress;
          NamedNodeMap attributes;
          Node retnode;
          String value;
          doc = (Document) load("staffNS", builder);
          arg = doc.createAttributeNS(namespaceURI, qualifiedName);
          arg.setNodeValue("newValue");
          elementList = doc.getElementsByTagName("address");
          testAddress = elementList.item(0);
          attributes = testAddress.getAttributes();
          attributes.setNamedItemNS(arg);
          retnode = attributes.getNamedItemNS(namespaceURI, "newAttr");
          value = retnode.getNodeValue();
          assertEquals("throw_Equals", "newValue", value);
          }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive fnctionality.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
   public void testSetNamedItemNS5() throws Throwable {
          String namespaceURI = "http:
          String qualifiedName = "dmstc:domestic";
          Document doc;
          Node arg;
          NodeList elementList;
          Node testAddress;
          NamedNodeMap attributes;
          Node retnode;
          String value;
          doc = (Document) load("staffNS", builder);
          arg = doc.createAttributeNS(namespaceURI, qualifiedName);
          arg.setNodeValue("newValue");
          elementList = doc.getElementsByTagName("address");
          testAddress = elementList.item(0);
          attributes = testAddress.getAttributes();
          retnode = attributes.setNamedItemNS(arg);
          value = retnode.getNodeValue();
          assertEquals("throw_Equals", "Yes", value);
          }
}
