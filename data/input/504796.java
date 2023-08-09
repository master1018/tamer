public final class elementgetelementsbytagnamens04 extends DOMTestCase {
   public elementgetelementsbytagnamens04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      Element child1;
      Element child2;
      Element child3;
      Node appendedChild;
      NodeList elementList;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      element = doc.createElementNS("http:
      child1 = doc.createElementNS("http:
      child2 = doc.createElementNS(nullNS, "child");
      child3 = doc.createElementNS("http:
      appendedChild = element.appendChild(child1);
      appendedChild = element.appendChild(child2);
      appendedChild = element.appendChild(child3);
      elementList = element.getElementsByTagNameNS(nullNS, "child");
      assertSize("elementgetelementsbytagnamens04_1", 1, elementList);
      elementList = element.getElementsByTagNameNS("*", "child");
      assertSize("elementgetelementsbytagnamens04_2", 3, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementsbytagnamens04.class, args);
   }
}
