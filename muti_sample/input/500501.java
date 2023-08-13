public final class hc_elementretrievetagname extends DOMTestCase {
   public hc_elementretrievetagname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      String strong;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("code");
      testEmployee = elementList.item(1);
      strong = testEmployee.getNodeName();
      assertEqualsAutoCase("element", "nodename", "code", strong);
        strong = ((Element) testEmployee).getTagName();
      assertEqualsAutoCase("element", "tagname", "code", strong);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementretrievetagname.class, args);
   }
}
