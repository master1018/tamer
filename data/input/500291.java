public class ElemVariable extends ElemTemplateElement
{
    static final long serialVersionUID = 9111131075322790061L;
  public ElemVariable(){}
  protected int m_index;
  int m_frameSize = -1;
  public void setIndex(int index)
  {
    m_index = index;
  }
  public int getIndex()
  {
    return m_index;
  }
  private XPath m_selectPattern;
  public void setSelect(XPath v)
  {
    m_selectPattern = v;
  }
  public XPath getSelect()
  {
    return m_selectPattern;
  }
  protected QName m_qname;
  public void setName(QName v)
  {
    m_qname = v;
  }
  public QName getName()
  {
    return m_qname;
  }
  private boolean m_isTopLevel = false;
  public void setIsTopLevel(boolean v)
  {
    m_isTopLevel = v;
  }
  public boolean getIsTopLevel()
  {
    return m_isTopLevel;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_VARIABLE;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_VARIABLE_STRING;
  }
  public ElemVariable(ElemVariable param) throws TransformerException
  {
    m_selectPattern = param.m_selectPattern;
    m_qname = param.m_qname;
    m_isTopLevel = param.m_isTopLevel;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    int sourceNode = transformer.getXPathContext().getCurrentNode();
    XObject var = getValue(transformer, sourceNode);
    transformer.getXPathContext().getVarStack().setLocalVariable(m_index, var);
  }
  public XObject getValue(TransformerImpl transformer, int sourceNode)
          throws TransformerException
  {
    XObject var;
    XPathContext xctxt = transformer.getXPathContext();
    xctxt.pushCurrentNode(sourceNode);
    try
    {
      if (null != m_selectPattern)
      {
        var = m_selectPattern.execute(xctxt, sourceNode, this);
        var.allowDetachToRelease(false);
      }
      else if (null == getFirstChildElem())
      {
        var = XString.EMPTYSTRING;
      }
      else
      {
        int df;
		try
		{
			if(m_parentNode instanceof Stylesheet) 
				df = transformer.transformToGlobalRTF(this);
			else
				df = transformer.transformToRTF(this);
    	}
		finally{ 
			}
        var = new XRTreeFrag(df, xctxt, this);
      }
    }
    finally
    {      
      xctxt.popCurrentNode();
    }
    return var;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    if(null == m_selectPattern  
       && sroot.getOptimizer())
    {
      XPath newSelect = rewriteChildToExpression(this);
      if(null != newSelect)
        m_selectPattern = newSelect;
    }
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_selectPattern)
      m_selectPattern.fixupVariables(vnames, cstate.getGlobalsSize());
    if(!(m_parentNode instanceof Stylesheet) && m_qname != null)
    {
      m_index = cstate.addVariableName(m_qname) - cstate.getGlobalsSize();
    }
    else if (m_parentNode instanceof Stylesheet)
    {
		cstate.resetStackFrameSize();
    }
    super.compose(sroot);
  }
  public void endCompose(StylesheetRoot sroot) throws TransformerException
  {
    super.endCompose(sroot);
    if(m_parentNode instanceof Stylesheet)
    {
    	StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    	m_frameSize = cstate.getFrameSize();
    	cstate.resetStackFrameSize();
    }
  }
  static XPath rewriteChildToExpression(ElemTemplateElement varElem)
          throws TransformerException
  {
    ElemTemplateElement t = varElem.getFirstChildElem();
    if (null != t && null == t.getNextSiblingElem())
    {
      int etype = t.getXSLToken();
      if (Constants.ELEMNAME_VALUEOF == etype)
      {
        ElemValueOf valueof = (ElemValueOf) t;
        if (valueof.getDisableOutputEscaping() == false
                && valueof.getDOMBackPointer() == null)
        {
          varElem.m_firstChild = null;
          return new XPath(new XRTreeFragSelectWrapper(valueof.getSelect().getExpression()));
        }
      }
      else if (Constants.ELEMNAME_TEXTLITERALRESULT == etype)
      {
        ElemTextLiteral lit = (ElemTextLiteral) t;
        if (lit.getDisableOutputEscaping() == false
                && lit.getDOMBackPointer() == null)
        {
          String str = lit.getNodeValue();
          XString xstr = new XString(str);
          varElem.m_firstChild = null;
          return new XPath(new XRTreeFragSelectWrapper(xstr));
        }
      }
    }
    return null;
  }
  public void recompose(StylesheetRoot root)
  {
    root.recomposeVariables(this);
  }
  public void setParentElem(ElemTemplateElement p)
  {
    super.setParentElem(p);
    p.m_hasVariableDecl = true;
  }
  protected boolean accept(XSLTVisitor visitor)
  {
  	return visitor.visitVariableOrParamDecl(this);
  }
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(null != m_selectPattern)
  		m_selectPattern.getExpression().callVisitors(m_selectPattern, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
  public boolean isPsuedoVar()
  {
  	java.lang.String ns = m_qname.getNamespaceURI();
  	if((null != ns) && ns.equals(RedundentExprEliminator.PSUEDOVARNAMESPACE))
  	{
  		if(m_qname.getLocalName().startsWith("#"))
  			return true;
  	}
  	return false;
  }
  public ElemTemplateElement appendChild(ElemTemplateElement elem)
  {
    if (m_selectPattern != null)
    {
      error(XSLTErrorResources.ER_CANT_HAVE_CONTENT_AND_SELECT, 
          new Object[]{"xsl:" + this.getNodeName()});
      return null;
    }
    return super.appendChild(elem);
  }
}
