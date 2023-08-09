public class XPathFuncHereAPI {
   public static Node selectSingleNode(Node contextNode, Node xpathnode)
           throws TransformerException {
      return selectSingleNode(contextNode, xpathnode, contextNode);
   }
   public static Node selectSingleNode(
           Node contextNode, Node xpathnode, Node namespaceNode)
              throws TransformerException {
      NodeIterator nl = selectNodeIterator(contextNode, xpathnode,
                                           namespaceNode);
      return nl.nextNode();
   }
   public static NodeIterator selectNodeIterator(
           Node contextNode, Node xpathnode) throws TransformerException {
      return selectNodeIterator(contextNode, xpathnode, contextNode);
   }
   public static NodeIterator selectNodeIterator(
           Node contextNode, Node xpathnode, Node namespaceNode)
              throws TransformerException {
      XObject list = eval(contextNode, xpathnode, namespaceNode);
      return list.nodeset();
   }
   public static NodeList selectNodeList(Node contextNode, Node xpathnode)
           throws TransformerException {
      return selectNodeList(contextNode, xpathnode, contextNode);
   }
   public static NodeList selectNodeList(
           Node contextNode, Node xpathnode, Node namespaceNode)
              throws TransformerException {
      XObject list = eval(contextNode, xpathnode, namespaceNode);
      return list.nodelist();
   }
   public static XObject eval(Node contextNode, Node xpathnode)
           throws TransformerException {
      return eval(contextNode, xpathnode, contextNode);
   }
   public static XObject eval(
           Node contextNode, Node xpathnode, Node namespaceNode)
              throws TransformerException {
      FuncHereContext xpathSupport = new FuncHereContext(xpathnode);
      PrefixResolverDefault prefixResolver =
         new PrefixResolverDefault((namespaceNode.getNodeType()
                                    == Node.DOCUMENT_NODE)
                                   ? ((Document) namespaceNode)
                                      .getDocumentElement()
                                   : namespaceNode);
      String str = getStrFromNode(xpathnode);
      XPath xpath = new XPath(str, null, prefixResolver, XPath.SELECT, null);
      int ctxtNode = xpathSupport.getDTMHandleFromNode(contextNode);
      return xpath.execute(xpathSupport, ctxtNode, prefixResolver);
   }
   public static XObject eval(
           Node contextNode, Node xpathnode, PrefixResolver prefixResolver)
              throws TransformerException {
      String str = getStrFromNode(xpathnode);
      XPath xpath = new XPath(str, null, prefixResolver, XPath.SELECT, null);
      FuncHereContext xpathSupport = new FuncHereContext(xpathnode);
      int ctxtNode = xpathSupport.getDTMHandleFromNode(contextNode);
      return xpath.execute(xpathSupport, ctxtNode, prefixResolver);
   }
   private static String getStrFromNode(Node xpathnode) {
      if (xpathnode.getNodeType() == Node.TEXT_NODE) {
         return ((Text) xpathnode).getData();
      } else if (xpathnode.getNodeType() == Node.ATTRIBUTE_NODE) {
         return ((Attr) xpathnode).getNodeValue();
      } else if (xpathnode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
         return ((ProcessingInstruction) xpathnode).getNodeValue();
      }
      return "";
   }
}
