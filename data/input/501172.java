public class ElemAttribute extends ElemElement
{
    static final long serialVersionUID = 8817220961566919187L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_ATTRIBUTE;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_ATTRIBUTE_STRING;
  }
  protected String resolvePrefix(SerializationHandler rhandler,
                                 String prefix, String nodeNamespace)
    throws TransformerException
  {
    if (null != prefix && (prefix.length() == 0 || prefix.equals("xmlns")))
    {
      prefix = rhandler.getPrefix(nodeNamespace);
      if (null == prefix || prefix.length() == 0 || prefix.equals("xmlns"))
      {
        if(nodeNamespace.length() > 0)
        {
            NamespaceMappings prefixMapping = rhandler.getNamespaceMappings();
            prefix = prefixMapping.generateNextPrefix();
        }
        else
          prefix = "";
      }
    }
    return prefix;
  }
   protected boolean validateNodeName(String nodeName)
   {
      if(null == nodeName)
        return false;
      if(nodeName.equals("xmlns"))
        return false;
      return XML11Char.isXML11ValidQName(nodeName);
   }
  void constructNode(
          String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer)
            throws TransformerException
  {
    if(null != nodeName && nodeName.length() > 0)
    {
      SerializationHandler rhandler = transformer.getSerializationHandler();
      String val = transformer.transformToString(this);
      try 
      {
        String localName = QName.getLocalPart(nodeName);
        if(prefix != null && prefix.length() > 0){
            rhandler.addAttribute(nodeNamespace, localName, nodeName, "CDATA", val, true);
        }else{
            rhandler.addAttribute("", localName, nodeName, "CDATA", val, true);
        }
      }
      catch (SAXException e)
      {
      }
    }
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    int type = ((ElemTemplateElement) newChild).getXSLToken();
    switch (type)
    {
    case Constants.ELEMNAME_TEXTLITERALRESULT :
    case Constants.ELEMNAME_APPLY_TEMPLATES :
    case Constants.ELEMNAME_APPLY_IMPORTS :
    case Constants.ELEMNAME_CALLTEMPLATE :
    case Constants.ELEMNAME_FOREACH :
    case Constants.ELEMNAME_VALUEOF :
    case Constants.ELEMNAME_COPY_OF :
    case Constants.ELEMNAME_NUMBER :
    case Constants.ELEMNAME_CHOOSE :
    case Constants.ELEMNAME_IF :
    case Constants.ELEMNAME_TEXT :
    case Constants.ELEMNAME_COPY :
    case Constants.ELEMNAME_VARIABLE :
    case Constants.ELEMNAME_MESSAGE :
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  
    }
    return super.appendChild(newChild);
  }
	public void setName(AVT v) {
        if (v.isSimple())
        {
            if (v.getSimpleString().equals("xmlns"))
            {
                throw new IllegalArgumentException();
            }
        }
		super.setName(v);
	}
}
