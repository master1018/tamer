public class ElemApplyTemplates extends ElemCallTemplate
{
    static final long serialVersionUID = 2903125371542621004L;
  private QName m_mode = null;
  public void setMode(QName mode)
  {
    m_mode = mode;
  }
  public QName getMode()
  {
    return m_mode;
  }
  private boolean m_isDefaultTemplate = false;
  public void setIsDefaultTemplate(boolean b)
  {
    m_isDefaultTemplate = b;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_APPLY_TEMPLATES;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_APPLY_TEMPLATES_STRING;
  }
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    transformer.pushCurrentTemplateRuleIsNull(false);
    boolean pushMode = false;
    try
    {
      QName mode = transformer.getMode();
      if (!m_isDefaultTemplate)
      {
        if (((null == mode) && (null != m_mode))
                || ((null != mode) &&!mode.equals(m_mode)))
        {
          pushMode = true;
          transformer.pushMode(m_mode);
        }
      }
      transformSelectedNodes(transformer);
    }
    finally
    {
      if (pushMode)
        transformer.popMode();
      transformer.popCurrentTemplateRuleIsNull();
    }
  }
  public void transformSelectedNodes(TransformerImpl transformer)
            throws TransformerException
  {
    final XPathContext xctxt = transformer.getXPathContext();
    final int sourceNode = xctxt.getCurrentNode();
    DTMIterator sourceNodes = m_selectExpression.asIterator(xctxt, sourceNode);
    VariableStack vars = xctxt.getVarStack();
    int nParams = getParamElemCount();
    int thisframe = vars.getStackFrame();
    boolean pushContextNodeListFlag = false;
    try
    {
            xctxt.pushCurrentNode(DTM.NULL);
            xctxt.pushCurrentExpressionNode(DTM.NULL);
            xctxt.pushSAXLocatorNull();
            transformer.pushElemTemplateElement(null);
      final Vector keys = (m_sortElems == null)
                          ? null
                          : transformer.processSortKeys(this, sourceNode);
      if (null != keys)
        sourceNodes = sortNodes(xctxt, keys, sourceNodes);
      final SerializationHandler rth = transformer.getSerializationHandler();
      final StylesheetRoot sroot = transformer.getStylesheet();
      final TemplateList tl = sroot.getTemplateListComposed();
      final boolean quiet = transformer.getQuietConflictWarnings();
      DTM dtm = xctxt.getDTM(sourceNode);
      int argsFrame = -1;
      if(nParams > 0)
      {
        argsFrame = vars.link(nParams);
        vars.setStackFrame(thisframe);
        for (int i = 0; i < nParams; i++) 
        {
          ElemWithParam ewp = m_paramElems[i];
          XObject obj = ewp.getValue(transformer, sourceNode);
          vars.setLocalVariable(i, obj, argsFrame);
        }
        vars.setStackFrame(argsFrame);
      }
      xctxt.pushContextNodeList(sourceNodes);
      pushContextNodeListFlag = true;
      IntStack currentNodes = xctxt.getCurrentNodeStack();
      IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();     
      int child;
      while (DTM.NULL != (child = sourceNodes.nextNode()))
      {
        currentNodes.setTop(child);
        currentExpressionNodes.setTop(child);
        if(xctxt.getDTM(child) != dtm)
        {
          dtm = xctxt.getDTM(child);
        }
        final int exNodeType = dtm.getExpandedTypeID(child);
        final int nodeType = dtm.getNodeType(child);
        final QName mode = transformer.getMode();
        ElemTemplate template = tl.getTemplateFast(xctxt, child, exNodeType, mode, 
                                      -1, quiet, dtm);
        if (null == template)
        {
          switch (nodeType)
          {
          case DTM.DOCUMENT_FRAGMENT_NODE :
          case DTM.ELEMENT_NODE :
            template = sroot.getDefaultRule();
            break;
          case DTM.ATTRIBUTE_NODE :
          case DTM.CDATA_SECTION_NODE :
          case DTM.TEXT_NODE :
            transformer.pushPairCurrentMatched(sroot.getDefaultTextRule(), child);
            transformer.setCurrentElement(sroot.getDefaultTextRule());
            dtm.dispatchCharactersEvents(child, rth, false);
            transformer.popCurrentMatched();
            continue;
          case DTM.DOCUMENT_NODE :
            template = sroot.getDefaultRootRule();
            break;
          default :
            continue;
          }
        }
        else
        {
        	transformer.setCurrentElement(template);
        }
        transformer.pushPairCurrentMatched(template, child);
        int currentFrameBottom;  
        if(template.m_frameSize > 0)
        {
          xctxt.pushRTFContext();
          currentFrameBottom = vars.getStackFrame();  
          vars.link(template.m_frameSize);
          if( template.m_inArgsSize > 0)
          {
            int paramIndex = 0;
            for (ElemTemplateElement elem = template.getFirstChildElem(); 
                 null != elem; elem = elem.getNextSiblingElem()) 
            {
              if(Constants.ELEMNAME_PARAMVARIABLE == elem.getXSLToken())
              {
                ElemParam ep = (ElemParam)elem;
                int i;
                for (i = 0; i < nParams; i++) 
                {
                  ElemWithParam ewp = m_paramElems[i];
                  if(ewp.m_qnameID == ep.m_qnameID)
                  {
                    XObject obj = vars.getLocalVariable(i, argsFrame);
                    vars.setLocalVariable(paramIndex, obj);
                    break;
                  }
                }
                if(i == nParams)
                  vars.setLocalVariable(paramIndex, null);
              }
              else
                break;
              paramIndex++;
            }
          }
        }
        else
        	currentFrameBottom = 0;
        for (ElemTemplateElement t = template.m_firstChild; 
             t != null; t = t.m_nextSibling)
        {
          xctxt.setSAXLocator(t);
          try
          {
          	transformer.pushElemTemplateElement(t);
          	t.execute(transformer);
          }
          finally
          {
          	transformer.popElemTemplateElement();
          }
        }
        if(template.m_frameSize > 0)
        {
          vars.unlink(currentFrameBottom);
          xctxt.popRTFContext();
        }
        transformer.popCurrentMatched();
      } 
    }
    catch (SAXException se)
    {
      transformer.getErrorListener().fatalError(new TransformerException(se));
    }
    finally
    {
      if(nParams > 0)
        vars.unlink(thisframe);
      xctxt.popSAXLocator();
      if (pushContextNodeListFlag) xctxt.popContextNodeList();
      transformer.popElemTemplateElement();
      xctxt.popCurrentExpressionNode();
      xctxt.popCurrentNode();
      sourceNodes.detach();
    }
  }
}
