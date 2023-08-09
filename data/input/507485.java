public final class importNode01 extends DOMTestCase {
   public importNode01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      Attr newAttr;
      Text importedChild;
      Node aNode;
      Document ownerDocument;
      Element attrOwnerElement;
      DocumentType docType;
      String system;
      boolean specified;
      NodeList childList;
      String nodeName;
      Node child;
      String childValue;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedResult = new java.util.ArrayList();
      expectedResult.add("elem:attr1");
      expectedResult.add("importedText");
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      newAttr = aNewDoc.createAttribute("elem:attr1");
      importedChild = aNewDoc.createTextNode("importedText");
      aNode = newAttr.appendChild(importedChild);
      aNode = doc.importNode(newAttr, false);
      ownerDocument = aNode.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertNotNull("aNode", aNode);
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
attrOwnerElement = ((Attr) aNode).getOwnerElement();
      assertNull("ownerElement", attrOwnerElement);
      specified = ((Attr) aNode).getSpecified();
      assertTrue("specified", specified);
      childList = aNode.getChildNodes();
      assertSize("childList", 1, childList);
      nodeName = aNode.getNodeName();
      assertEquals("nodeName", "elem:attr1", nodeName);
      child = aNode.getFirstChild();
      childValue = child.getNodeValue();
      assertEquals("childValue", "importedText", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode01.class, args);
   }
}
