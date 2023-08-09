@TestTargetClass(Element.class) 
public final class Normalize extends DOMTestCase {
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
      Element root;
      NodeList elementList;
      Node firstChild;
      NodeList textList;
      CharacterData textNode;
      String data;
      doc = (Document) load("staff", builder);
      root = doc.getDocumentElement();
      root.normalize();
      elementList = root.getElementsByTagName("name");
      firstChild = elementList.item(2);
      textList = firstChild.getChildNodes();
      textNode = (CharacterData) textList.item(0);
      data = textNode.getData();
      assertEquals("data", "Roger\n Jones", data);
      }
}
