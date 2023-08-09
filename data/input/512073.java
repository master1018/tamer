public class Variable extends Expression implements PathComponent
{
    static final long serialVersionUID = -4334975375609297049L;
  private boolean m_fixUpWasCalled = false;
  protected QName m_qname;
  protected int m_index;
  public void setIndex(int index)
  {
  	m_index = index;
  }
  public int getIndex()
  {
  	return m_index;
  }
  public void setIsGlobal(boolean isGlobal)
  {
  	m_isGlobal = isGlobal;
  }
  public boolean getGlobal()
  {
  	return m_isGlobal;
  }
  protected boolean m_isGlobal = false;
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    m_fixUpWasCalled = true;
    int sz = vars.size();
    for (int i = vars.size()-1; i >= 0; i--) 
    {
      QName qn = (QName)vars.elementAt(i);
      if(qn.equals(m_qname))
      {
        if(i < globalsSize)
        {
          m_isGlobal = true;
          m_index = i;
        }
        else
        {
          m_index = i-globalsSize;
        }
        return;
      }
    }
    java.lang.String msg = XSLMessages.createXPATHMessage(XPATHErrorResources.ER_COULD_NOT_FIND_VAR, 
                                             new Object[]{m_qname.toString()});
    TransformerException te = new TransformerException(msg, this);
    throw new org.apache.xml.utils.WrappedRuntimeException(te);
  }
  public void setQName(QName qname)
  {
    m_qname = qname;
  }
  public QName getQName()
  {
    return m_qname;
  }
  public XObject execute(XPathContext xctxt)
    throws javax.xml.transform.TransformerException
  {
  	return execute(xctxt, false);
  }
  public XObject execute(XPathContext xctxt, boolean destructiveOK) throws javax.xml.transform.TransformerException
  {
    org.apache.xml.utils.PrefixResolver xprefixResolver = xctxt.getNamespaceContext();
    XObject result;
    if(m_fixUpWasCalled)
    {    
      if(m_isGlobal)
        result = xctxt.getVarStack().getGlobalVariable(xctxt, m_index, destructiveOK);
      else
        result = xctxt.getVarStack().getLocalVariable(xctxt, m_index, destructiveOK);
    } 
    else {  
    	result = xctxt.getVarStack().getVariableOrParam(xctxt,m_qname);
    }
      if (null == result)
      {
        warn(xctxt, XPATHErrorResources.WG_ILLEGAL_VARIABLE_REFERENCE,
             new Object[]{ m_qname.getLocalPart() });  
        result = new XNodeSet(xctxt.getDTMManager());
      }
      return result;
  }
  public org.apache.xalan.templates.ElemVariable getElemVariable()
  {
    org.apache.xalan.templates.ElemVariable vvar = null;	
    org.apache.xpath.ExpressionNode owner = getExpressionOwner();
    if (null != owner && owner instanceof org.apache.xalan.templates.ElemTemplateElement)
    {
      org.apache.xalan.templates.ElemTemplateElement prev = 
        (org.apache.xalan.templates.ElemTemplateElement) owner;
      if (!(prev instanceof org.apache.xalan.templates.Stylesheet))
      {            
        while ( prev != null && !(prev.getParentNode() instanceof org.apache.xalan.templates.Stylesheet) )
        {
          org.apache.xalan.templates.ElemTemplateElement savedprev = prev;
          while (null != (prev = prev.getPreviousSiblingElem()))
          {
            if(prev instanceof org.apache.xalan.templates.ElemVariable)
            {
              vvar = (org.apache.xalan.templates.ElemVariable) prev;
              if (vvar.getName().equals(m_qname))
              {
                return vvar;
              }
              vvar = null; 	 	
            }
          }
          prev = savedprev.getParentElem();
        }
      }
      if (prev != null)
        vvar = prev.getStylesheetRoot().getVariableOrParamComposed(m_qname);
    }
    return vvar;
  }
  public boolean isStableNumber()
  {
    return true;
  }
  public int getAnalysisBits()
  {
  	org.apache.xalan.templates.ElemVariable vvar = getElemVariable();
  	if(null != vvar)
  	{
  		XPath xpath = vvar.getSelect();
  		if(null != xpath)
  		{
	  		Expression expr = xpath.getExpression();
	  		if(null != expr && expr instanceof PathComponent)
	  		{
	  			return ((PathComponent)expr).getAnalysisBits();
	  		}
  		}
  	}
    return WalkerFactory.BIT_FILTER;
  }
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor)
  {
  	visitor.visitVariableRef(owner, this);
  }
  public boolean deepEquals(Expression expr)
  {
  	if(!isSameClass(expr))
  		return false;
  	if(!m_qname.equals(((Variable)expr).m_qname))
  		return false;
    if(getElemVariable() != ((Variable)expr).getElemVariable())
    	return false;
  	return true;
  }
  static final java.lang.String PSUEDOVARNAMESPACE = "http:
  public boolean isPsuedoVarRef()
  {
  	java.lang.String ns = m_qname.getNamespaceURI();
  	if((null != ns) && ns.equals(PSUEDOVARNAMESPACE))
  	{
  		if(m_qname.getLocalName().startsWith("#"))
  			return true;
  	}
  	return false;
  }
}
