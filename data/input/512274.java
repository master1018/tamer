public class FuncDoclocation extends FunctionDef1Arg
{
    static final long serialVersionUID = 7469213946343568769L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int whereNode = getArg0AsNode(xctxt);
    String fileLocation = null;
    if (DTM.NULL != whereNode)
    {
      DTM dtm = xctxt.getDTM(whereNode);
      if (DTM.DOCUMENT_FRAGMENT_NODE ==  dtm.getNodeType(whereNode))
      {
        whereNode = dtm.getFirstChild(whereNode);
      }
      if (DTM.NULL != whereNode)
      {        
        fileLocation = dtm.getDocumentBaseURI();
      }
    }
    return new XString((null != fileLocation) ? fileLocation : "");
  }
}
