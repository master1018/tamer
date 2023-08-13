public class FuncSubstringAfter extends Function2Args
{
    static final long serialVersionUID = -8119731889862512194L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    XMLString s1 = m_arg0.execute(xctxt).xstr();
    XMLString s2 = m_arg1.execute(xctxt).xstr();
    int index = s1.indexOf(s2);
    return (-1 == index)
           ? XString.EMPTYSTRING
           : (XString)s1.substring(index + s2.length());
  }
}
