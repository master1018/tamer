public class ElemIf extends ElemTemplateElement
{
    static final long serialVersionUID = 2158774632427453022L;
  private XPath m_test = null;
  public void setTest(XPath v)
  {
    m_test = v;
  }
  public XPath getTest()
  {
    return m_test;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if (null != m_test)
      m_test.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_IF;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_IF_STRING;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    XPathContext xctxt = transformer.getXPathContext();
    int sourceNode = xctxt.getCurrentNode();
      if (m_test.bool(xctxt, sourceNode, this)) {
          transformer.executeChildTemplates(this, true);
      }
  }
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_test.getExpression().callVisitors(m_test, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
}
