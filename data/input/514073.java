public final class documentgetelementsbytagnameNS02 extends DOMTestCase {
   public documentgetelementsbytagnameNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element docElem;
      Element element;
      NodeList childList;
      Node appendedChild;
      doc = (Document) load("staffNS", true);
      docElem = doc.getDocumentElement();
      element = doc.createElementNS("test", "employeeId");
      appendedChild = docElem.appendChild(element);
      childList = doc.getElementsByTagNameNS("*", "employeeId");
      assertSize("documentgetelementsbytagnameNS02", 6, childList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnameNS02.class, args);
   }
}
