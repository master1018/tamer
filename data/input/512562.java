public class XRTreeFragSelectWrapper extends XRTreeFrag implements Cloneable
{
    static final long serialVersionUID = -6526177905590461251L;
  public XRTreeFragSelectWrapper(Expression expr)
  {
    super(expr);
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    ((Expression)m_obj).fixupVariables(vars, globalsSize);
  }
  public XObject execute(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
	 XObject m_selected;
     m_selected = ((Expression)m_obj).execute(xctxt);
     m_selected.allowDetachToRelease(m_allowRelease);
     if (m_selected.getType() == CLASS_STRING)
       return m_selected;
     else
       return new XString(m_selected.str());
  }
  public void detach()
  {
	throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
  public double num()
    throws javax.xml.transform.TransformerException
  {
	throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
  public XMLString xstr()
  {
	throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
  public String str()
  {
	throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
  public int getType()
  {
    return CLASS_STRING;
  }
  public int rtf()
  {
    throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
  public DTMIterator asNodeIterator()
  {
    throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null)); 
  }
}
