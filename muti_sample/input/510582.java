public class ElemForEach extends ElemTemplateElement implements ExpressionOwner
{
    static final long serialVersionUID = 6018140636363583690L;
  static final boolean DEBUG = false;
  public boolean m_doc_cache_off=false;
  public ElemForEach(){}
  protected Expression m_selectExpression = null;
  protected XPath m_xpath = null;  
  public void setSelect(XPath xpath)
  {
    m_selectExpression = xpath.getExpression();
    m_xpath = xpath;    
  }
  public Expression getSelect()
  {
    return m_selectExpression;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    int length = getSortElemCount();
    for (int i = 0; i < length; i++)
    {
      getSortElem(i).compose(sroot);
    }
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if (null != m_selectExpression)
      m_selectExpression.fixupVariables(
        vnames, sroot.getComposeState().getGlobalsSize());
    else
    {
      m_selectExpression =
        getStylesheetRoot().m_selectDefault.getExpression();
    }
  }
  public void endCompose(StylesheetRoot sroot) throws TransformerException
  {
    int length = getSortElemCount();
    for (int i = 0; i < length; i++)
    {
      getSortElem(i).endCompose(sroot);
    }
    super.endCompose(sroot);
  }
  protected Vector m_sortElems = null;
  public int getSortElemCount()
  {
    return (m_sortElems == null) ? 0 : m_sortElems.size();
  }
  public ElemSort getSortElem(int i)
  {
    return (ElemSort) m_sortElems.elementAt(i);
  }
  public void setSortElem(ElemSort sortElem)
  {
    if (null == m_sortElems)
      m_sortElems = new Vector();
    m_sortElems.addElement(sortElem);
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_FOREACH;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_FOREACH_STRING;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    transformer.pushCurrentTemplateRuleIsNull(true);    
    try
    {
      transformSelectedNodes(transformer);
    }
    finally
    {
      transformer.popCurrentTemplateRuleIsNull();
    }
  }
  protected ElemTemplateElement getTemplateMatch()
  {
    return this;
  }
  public DTMIterator sortNodes(
          XPathContext xctxt, Vector keys, DTMIterator sourceNodes)
            throws TransformerException
  {
    NodeSorter sorter = new NodeSorter(xctxt);
    sourceNodes.setShouldCacheNodes(true);
    sourceNodes.runTo(-1);
    xctxt.pushContextNodeList(sourceNodes);
    try
    {
      sorter.sort(sourceNodes, keys, xctxt);
      sourceNodes.setCurrentPos(0);
    }
    finally
    {
      xctxt.popContextNodeList();
    }
    return sourceNodes;
  }
  public void transformSelectedNodes(TransformerImpl transformer)
          throws TransformerException
  {
    final XPathContext xctxt = transformer.getXPathContext();
    final int sourceNode = xctxt.getCurrentNode();
    DTMIterator sourceNodes = m_selectExpression.asIterator(xctxt,
            sourceNode);
    try
    {
      final Vector keys = (m_sortElems == null)
              ? null
              : transformer.processSortKeys(this, sourceNode);
      if (null != keys)
        sourceNodes = sortNodes(xctxt, keys, sourceNodes);
      xctxt.pushCurrentNode(DTM.NULL);
      IntStack currentNodes = xctxt.getCurrentNodeStack();
      xctxt.pushCurrentExpressionNode(DTM.NULL);
      IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();
      xctxt.pushSAXLocatorNull();
      xctxt.pushContextNodeList(sourceNodes);
      transformer.pushElemTemplateElement(null);
      DTM dtm = xctxt.getDTM(sourceNode);
      int docID = sourceNode & DTMManager.IDENT_DTM_DEFAULT;
      int child;
      while (DTM.NULL != (child = sourceNodes.nextNode()))
      {
        currentNodes.setTop(child);
        currentExpressionNodes.setTop(child);
        if ((child & DTMManager.IDENT_DTM_DEFAULT) != docID)
        {
          dtm = xctxt.getDTM(child);
          docID = child & DTMManager.IDENT_DTM_DEFAULT;
        }
        final int nodeType = dtm.getNodeType(child); 
        for (ElemTemplateElement t = this.m_firstChild; t != null;
             t = t.m_nextSibling)
        {
          xctxt.setSAXLocator(t);
          transformer.setCurrentElement(t);
          t.execute(transformer);
        }
	 	if(m_doc_cache_off)
		{
	 	  if(DEBUG)
	 	    System.out.println("JJK***** CACHE RELEASE *****\n"+
				       "\tdtm="+dtm.getDocumentBaseURI());
	 	  xctxt.getSourceTreeManager().removeDocumentFromCache(dtm.getDocument());
	 	  xctxt.release(dtm,false);
	 	}
      }
    }
    finally
    {
      xctxt.popSAXLocator();
      xctxt.popContextNodeList();
      transformer.popElemTemplateElement();
      xctxt.popCurrentExpressionNode();
      xctxt.popCurrentNode();
      sourceNodes.detach();
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    int type = ((ElemTemplateElement) newChild).getXSLToken();
    if (Constants.ELEMNAME_SORT == type)
    {
      setSortElem((ElemSort) newChild);
      return newChild;
    }
    else
      return super.appendChild(newChild);
  }
  public void callChildVisitors(XSLTVisitor visitor, boolean callAttributes)
  {
  	if(callAttributes && (null != m_selectExpression))
  		m_selectExpression.callVisitors(this, visitor);
    int length = getSortElemCount();
    for (int i = 0; i < length; i++)
    {
      getSortElem(i).callVisitors(visitor);
    }
    super.callChildVisitors(visitor, callAttributes);
  }
  public Expression getExpression()
  {
    return m_selectExpression;
  }
  public void setExpression(Expression exp)
  {
  	exp.exprSetParent(this);
  	m_selectExpression = exp;
  }
   private void readObject(ObjectInputStream os) throws 
        IOException, ClassNotFoundException {
           os.defaultReadObject();
           m_xpath = null;
   }
}
