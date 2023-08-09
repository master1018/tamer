public class FuncCount extends FunctionOneArg
{
    static final long serialVersionUID = -7116225100474153751L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
	DTMIterator nl = m_arg0.asIterator(xctxt, xctxt.getCurrentNode());
	int i = nl.getLength();	
	nl.detach();
    return new XNumber((double) i);
  }
}
