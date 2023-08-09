public class FuncCeiling extends FunctionOneArg
{
    static final long serialVersionUID = -1275988936390464739L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    return new XNumber(Math.ceil(m_arg0.execute(xctxt).num()));
  }
}
