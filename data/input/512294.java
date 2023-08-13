public class FuncNamespace extends FunctionDef1Arg
{
    static final long serialVersionUID = -4695674566722321237L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int context = getArg0AsNode(xctxt);
    String s;
    if(context != DTM.NULL)
    {
      DTM dtm = xctxt.getDTM(context);
      int t = dtm.getNodeType(context);
      if(t == DTM.ELEMENT_NODE)
      {
        s = dtm.getNamespaceURI(context);
      }
      else if(t == DTM.ATTRIBUTE_NODE)
      {
        s = dtm.getNodeName(context);
        if(s.startsWith("xmlns:") || s.equals("xmlns"))
          return XString.EMPTYSTRING;
        s = dtm.getNamespaceURI(context);
      }
      else
        return XString.EMPTYSTRING;
    }
    else 
      return XString.EMPTYSTRING;
    return ((null == s) ? XString.EMPTYSTRING : new XString(s));
  }
}
