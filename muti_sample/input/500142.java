public class XPathAPI
{
  public static Node selectSingleNode(Node contextNode, String str)
          throws TransformerException
  {
    return selectSingleNode(contextNode, str, contextNode);
  }
  public static Node selectSingleNode(
          Node contextNode, String str, Node namespaceNode)
            throws TransformerException
  {
    NodeIterator nl = selectNodeIterator(contextNode, str, namespaceNode);
    return nl.nextNode();
  }
  public static NodeIterator selectNodeIterator(Node contextNode, String str)
          throws TransformerException
  {
    return selectNodeIterator(contextNode, str, contextNode);
  }
  public static NodeIterator selectNodeIterator(
          Node contextNode, String str, Node namespaceNode)
            throws TransformerException
  {
    XObject list = eval(contextNode, str, namespaceNode);
    return list.nodeset();
  }
  public static NodeList selectNodeList(Node contextNode, String str)
          throws TransformerException
  {
    return selectNodeList(contextNode, str, contextNode);
  }
  public static NodeList selectNodeList(
          Node contextNode, String str, Node namespaceNode)
            throws TransformerException
  {
    XObject list = eval(contextNode, str, namespaceNode);
    return list.nodelist();
  }
  public static XObject eval(Node contextNode, String str)
          throws TransformerException
  {
    return eval(contextNode, str, contextNode);
  }
  public static XObject eval(Node contextNode, String str, Node namespaceNode)
          throws TransformerException
  {
    XPathContext xpathSupport = new XPathContext(false);
    PrefixResolverDefault prefixResolver = new PrefixResolverDefault(
      (namespaceNode.getNodeType() == Node.DOCUMENT_NODE)
      ? ((Document) namespaceNode).getDocumentElement() : namespaceNode);
    XPath xpath = new XPath(str, null, prefixResolver, XPath.SELECT, null);
    int ctxtNode = xpathSupport.getDTMHandleFromNode(contextNode);
    return xpath.execute(xpathSupport, ctxtNode, prefixResolver);
  }
  public static XObject eval(
          Node contextNode, String str, PrefixResolver prefixResolver)
            throws TransformerException
  {
    XPath xpath = new XPath(str, null, prefixResolver, XPath.SELECT, null);
    XPathContext xpathSupport = new XPathContext(false);
    int ctxtNode = xpathSupport.getDTMHandleFromNode(contextNode);
    return xpath.execute(xpathSupport, ctxtNode, prefixResolver);
  }
}
