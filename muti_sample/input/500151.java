public final class documentimportnode21 extends DOMTestCase {
   public documentimportnode21(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.notExpandEntityReferences
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docTypeNull = null;
      Document docImp;
      DOMImplementation domImpl;
      NodeList addressList;
      NodeList addressChildList;
      Element element;
      EntityReference entRef2;
      EntityReference entRefImp2;
      EntityReference entRef3;
      EntityReference entRefImp3;
      String nodeName2;
      String nodeName3;
      String nodeNameImp2;
      String nodeNameImp3;
      NodeList nodes;
      Node nodeImp3;
      Node nodeImp2;
      String nodeValueImp2;
      String nodeValueImp3;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      docImp = domImpl.createDocument("http:
      addressList = doc.getElementsByTagName("address");
      element = (Element) addressList.item(1);
      addressChildList = element.getChildNodes();
      entRef2 = (EntityReference) addressChildList.item(0);
      entRef3 = (EntityReference) addressChildList.item(2);
      entRefImp2 = (EntityReference) docImp.importNode(entRef2, true);
      entRefImp3 = (EntityReference) docImp.importNode(entRef3, false);
      nodeName2 = entRef2.getNodeName();
      nodeName3 = entRef3.getNodeName();
      nodeNameImp2 = entRefImp2.getNodeName();
      nodeNameImp3 = entRefImp3.getNodeName();
      assertEquals("documentimportnode21_Ent2NodeName", nodeName2, nodeNameImp2);
      assertEquals("documentimportnode21_Ent3NodeName", nodeName3, nodeNameImp3);
      entRefImp2 = (EntityReference) doc.importNode(entRef2, true);
      entRefImp3 = (EntityReference) doc.importNode(entRef3, false);
      nodes = entRefImp2.getChildNodes();
      nodeImp2 = nodes.item(0);
      nodeValueImp2 = nodeImp2.getNodeValue();
      nodes = entRefImp3.getChildNodes();
      nodeImp3 = nodes.item(0);
      nodeValueImp3 = nodeImp3.getNodeValue();
      assertEquals("documentimportnode21_Ent2NodeValue", "1900 Dallas Road", nodeValueImp2);
      assertEquals("documentimportnode21_Ent3Nodevalue", "Texas", nodeValueImp3);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode21.class, args);
   }
}
