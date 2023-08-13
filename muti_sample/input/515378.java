public class ElemApplyImport extends ElemTemplateElement
{
    static final long serialVersionUID = 3764728663373024038L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.currentTemplateRuleIsNull())
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NO_APPLY_IMPORT_IN_FOR_EACH);  
    }
    int sourceNode = transformer.getXPathContext().getCurrentNode();
    if (DTM.NULL != sourceNode)
    {
      ElemTemplate matchTemplate = transformer.getMatchedTemplate();
      transformer.applyTemplateToNode(this, matchTemplate, sourceNode);
    }
    else  
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NULL_SOURCENODE_APPLYIMPORTS);  
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
}
