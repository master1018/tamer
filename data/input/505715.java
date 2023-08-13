public class FunctionDef1Arg extends FunctionOneArg
{
    static final long serialVersionUID = 2325189412814149264L;
  protected int getArg0AsNode(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return (null == m_arg0)
           ? xctxt.getCurrentNode() : m_arg0.asNode(xctxt);
  }
  public boolean Arg0IsNodesetExpr()
  {
    return (null == m_arg0) ? true : m_arg0.isNodesetExpr();
  }
  protected XMLString getArg0AsString(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    if(null == m_arg0)
    {
      int currentNode = xctxt.getCurrentNode();
      if(DTM.NULL == currentNode)
        return XString.EMPTYSTRING;
      else
      {
        DTM dtm = xctxt.getDTM(currentNode);
        return dtm.getStringValue(currentNode);
      }
    }
    else
      return m_arg0.execute(xctxt).xstr();   
  }
  protected double getArg0AsNumber(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    if(null == m_arg0)
    {
      int currentNode = xctxt.getCurrentNode();
      if(DTM.NULL == currentNode)
        return 0;
      else
      {
        DTM dtm = xctxt.getDTM(currentNode);
        XMLString str = dtm.getStringValue(currentNode);
        return str.toDouble();
      }
    }
    else
      return m_arg0.execute(xctxt).num();
  }
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if (argNum > 1)
      reportWrongNumberArgs();
  }
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ZERO_OR_ONE, null)); 
  }
  public boolean canTraverseOutsideSubtree()
  {
    return (null == m_arg0) ? false : super.canTraverseOutsideSubtree();
  }
}
