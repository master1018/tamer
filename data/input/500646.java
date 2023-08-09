public final class hc_textparseintolistofelements extends DOMTestCase {
   public hc_textparseintolistofelements(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node addressNode;
      NodeList childList;
      Node child;
      String value;
      Node grandChild;
      int length;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedNormal = new java.util.ArrayList();
      expectedNormal.add("\u03b2"); 
      expectedNormal.add(" Dallas, ");
      expectedNormal.add("\u03b3"); 
      expectedNormal.add("\n 98554");
      java.util.List expectedExpanded = new java.util.ArrayList();
      expectedExpanded.add("\u03b2 Dallas, \u03b3\n 98554"); 
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      addressNode = elementList.item(1);
      childList = addressNode.getChildNodes();
      length = (int) childList.getLength();
      for (int indexN1007C = 0; indexN1007C < childList.getLength(); indexN1007C++) {
          child = (Node) childList.item(indexN1007C);
    value = child.getNodeValue();
      if ((value == null)) {
          grandChild = child.getFirstChild();
      assertNotNull("grandChildNotNull", grandChild);
      value = grandChild.getNodeValue();
      result.add(value);
      } else {
          result.add(value);
      }
      }
      if (equals(1, length)) {
          assertEquals("assertEqCoalescing", expectedExpanded, result);
      } else {
          assertEquals("assertEqNormal", expectedNormal, result);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_textparseintolistofelements.class, args);
   }
}
