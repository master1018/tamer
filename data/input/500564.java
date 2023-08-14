public class ElemValueOf extends ElemTemplateElement
{
    static final long serialVersionUID = 3490728458007586786L;
  private XPath m_selectExpression = null;
  private boolean m_isDot = false;
  public void setSelect(XPath v)
  {
    if (null != v)
    {
      String s = v.getPatternString();
      m_isDot = (null != s) && s.equals(".");
    }
    m_selectExpression = v;
  }
  public XPath getSelect()
  {
    return m_selectExpression;
  }
  private boolean m_disableOutputEscaping = false;
  public void setDisableOutputEscaping(boolean v)
  {
    m_disableOutputEscaping = v;
  }
  public boolean getDisableOutputEscaping()
  {
    return m_disableOutputEscaping;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_VALUEOF;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if (null != m_selectExpression)
      m_selectExpression.fixupVariables(
        vnames, sroot.getComposeState().getGlobalsSize());
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_VALUEOF_STRING;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    XPathContext xctxt = transformer.getXPathContext();
    SerializationHandler rth = transformer.getResultTreeHandler();
    try
    {
        xctxt.pushNamespaceContext(this);
        int current = xctxt.getCurrentNode();
        xctxt.pushCurrentNodeAndExpression(current, current);
        if (m_disableOutputEscaping)
          rth.processingInstruction(
            javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        try
        {
          Expression expr = m_selectExpression.getExpression();
            expr.executeCharsToContentHandler(xctxt, rth);
        }
        finally
        {
          if (m_disableOutputEscaping)
            rth.processingInstruction(
              javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING, "");
          xctxt.popNamespaceContext();
          xctxt.popCurrentNodeAndExpression();
        }
    }
    catch (SAXException se)
    {
      throw new TransformerException(se);
    }
    catch (RuntimeException re) {
    	TransformerException te = new TransformerException(re);
    	te.setLocator(this);
    	throw te;
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_selectExpression.getExpression().callVisitors(m_selectExpression, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
}
