public class ElemText extends ElemTemplateElement
{
    static final long serialVersionUID = 1383140876182316711L;
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
    return Constants.ELEMNAME_TEXT;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_TEXT_STRING;
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    int type = ((ElemTemplateElement) newChild).getXSLToken();
    switch (type)
    {
    case Constants.ELEMNAME_TEXTLITERALRESULT :
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  
    }
    return super.appendChild(newChild);
  }
}
