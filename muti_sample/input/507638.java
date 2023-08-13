public class ElemExtensionScript extends ElemTemplateElement
{
    static final long serialVersionUID = -6995978265966057744L;
  public ElemExtensionScript()
  {
  }
  private String m_lang = null;
  public void setLang(String v)
  {
    m_lang = v;
  }
  public String getLang()
  {
    return m_lang;
  }
  private String m_src = null;
  public void setSrc(String v)
  {
    m_src = v;
  }
  public String getSrc()
  {
    return m_src;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONSCRIPT;
  }
}
