public class ElemTextLiteral extends ElemTemplateElement
{
    static final long serialVersionUID = -7872620006767660088L;
  private boolean m_preserveSpace;
  public void setPreserveSpace(boolean v)
  {
    m_preserveSpace = v;
  }
  public boolean getPreserveSpace()
  {
    return m_preserveSpace;
  }
  private char m_ch[];
  private String m_str;
  public void setChars(char[] v)
  {
    m_ch = v;
  }
  public char[] getChars()
  {
    return m_ch;
  }
  public synchronized String getNodeValue()
  {
    if(null == m_str)
    {
      m_str = new String(m_ch);
    }
    return m_str;
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
    return Constants.ELEMNAME_TEXTLITERALRESULT;
  }
  public String getNodeName()
  {
    return "#Text";
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    try
    {
      SerializationHandler rth = transformer.getResultTreeHandler();
        if (m_disableOutputEscaping)
      {
        rth.processingInstruction(javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING, "");
      }
      rth.characters(m_ch, 0, m_ch.length);
      if (m_disableOutputEscaping)
      {
        rth.processingInstruction(javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING, "");
      }
    }
    catch(SAXException se)
    {
      throw new TransformerException(se);
    }
  }
}
