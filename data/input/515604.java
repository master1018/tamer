public class FuncLang extends FunctionOneArg
{
    static final long serialVersionUID = -7868705139354872185L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    String lang = m_arg0.execute(xctxt).str();
    int parent = xctxt.getCurrentNode();
    boolean isLang = false;
    DTM dtm = xctxt.getDTM(parent);
    while (DTM.NULL != parent)
    {
      if (DTM.ELEMENT_NODE == dtm.getNodeType(parent))
      {
        int langAttr = dtm.getAttributeNode(parent, "http:
        if (DTM.NULL != langAttr)
        {
          String langVal = dtm.getNodeValue(langAttr);
          if (langVal.toLowerCase().startsWith(lang.toLowerCase()))
          {
            int valLen = lang.length();
            if ((langVal.length() == valLen)
                    || (langVal.charAt(valLen) == '-'))
            {
              isLang = true;
            }
          }
          break;
        }
      }
      parent = dtm.getParent(parent);
    }
    return isLang ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
