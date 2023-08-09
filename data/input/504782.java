public class ElemWhen extends ElemTemplateElement
{
    static final long serialVersionUID = 5984065730262071360L;
  private XPath m_test;
  public void setTest(XPath v)
  {
    m_test = v;
  }
  public XPath getTest()
  {
    return m_test;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_WHEN;
  }
  public void compose(StylesheetRoot sroot) 
    throws javax.xml.transform.TransformerException
  {
    super.compose(sroot);
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if(null != m_test)
      m_test.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_WHEN_STRING;
  }
  public ElemWhen(){}
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_test.getExpression().callVisitors(m_test, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
}
