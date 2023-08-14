public final class cdatasectionnormalize extends DOMTestCase {
   public cdatasectionnormalize(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      Element lChild;
      NodeList childNodes;
      CDATASection cdataN;
      String data;
      doc = (Document) load("staff", true);
      nameList = doc.getElementsByTagName("name");
      lChild = (Element) nameList.item(1);
      lChild.normalize();
      childNodes = lChild.getChildNodes();
      cdataN = (CDATASection) childNodes.item(1);
      assertNotNull("firstCDATASection", cdataN);
      data = cdataN.getData();
      assertEquals("data1", "This is a CDATASection with EntityReference number 2 &ent2;", data);
      cdataN = (CDATASection) childNodes.item(3);
      assertNotNull("secondCDATASection", cdataN);
      data = cdataN.getData();
      assertEquals("data3", "This is an adjacent CDATASection with a reference to a tab &tab;", data);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(cdatasectionnormalize.class, args);
   }
}
