public class FuncNot extends FunctionOneArg
{
    static final long serialVersionUID = 7299699961076329790L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    return m_arg0.execute(xctxt).bool() ? XBoolean.S_FALSE : XBoolean.S_TRUE;
  }
}
