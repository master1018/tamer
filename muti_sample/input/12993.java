public class FuncHere extends Function {
        private static final long serialVersionUID = 1L;
   public XObject execute(XPathContext xctxt)
           throws javax.xml.transform.TransformerException {
      Node xpathOwnerNode = (Node) xctxt.getOwnerObject();
      if (xpathOwnerNode == null) {
         return null;
      }
      int xpathOwnerNodeDTM = xctxt.getDTMHandleFromNode(xpathOwnerNode);
      int currentNode = xctxt.getCurrentNode();
      DTM dtm = xctxt.getDTM(currentNode);
      int docContext = dtm.getDocument();
      if (DTM.NULL == docContext) {
         error(xctxt, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC, null);
      }
      {
         Document currentDoc =
            XMLUtils.getOwnerDocument(dtm.getNode(currentNode));
         Document xpathOwnerDoc = XMLUtils.getOwnerDocument(xpathOwnerNode);
         if (currentDoc != xpathOwnerDoc) {
            throw new TransformerException(I18n
               .translate("xpath.funcHere.documentsDiffer"));
         }
      }
      XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
      NodeSetDTM nodeSet = nodes.mutableNodeset();
      {
         int hereNode = DTM.NULL;
         switch (dtm.getNodeType(xpathOwnerNodeDTM)) {
         case Node.ATTRIBUTE_NODE : {
            hereNode = xpathOwnerNodeDTM;
            nodeSet.addNode(hereNode);
            break;
         }
         case Node.PROCESSING_INSTRUCTION_NODE : {
            hereNode = xpathOwnerNodeDTM;
            nodeSet.addNode(hereNode);
            break;
         }
         case Node.TEXT_NODE : {
            hereNode = dtm.getParent(xpathOwnerNodeDTM);
            nodeSet.addNode(hereNode);
            break;
         }
         default :
            break;
         }
      }
      nodeSet.detach();
      return nodes;
   }
   public void fixupVariables(java.util.Vector vars, int globalsSize) {
   }
}
