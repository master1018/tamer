public class FuncStartsWith extends Function2Args
{
    static final long serialVersionUID = 2194585774699567928L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    return m_arg0.execute(xctxt).xstr().startsWith(m_arg1.execute(xctxt).xstr())
           ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
