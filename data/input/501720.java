public final class elementretrievetagname extends DOMTestCase {
   public elementretrievetagname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      String name;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("position");
      testEmployee = elementList.item(1);
      name = testEmployee.getNodeName();
      assertEquals("nodename", "position", name);
      name = ((Element) testEmployee).getTagName();
      assertEquals("tagname", "position", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementretrievetagname.class, args);
   }
}
