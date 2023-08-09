public class FuncSum extends FunctionOneArg
{
    static final long serialVersionUID = -2719049259574677519L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    DTMIterator nodes = m_arg0.asIterator(xctxt, xctxt.getCurrentNode());
    double sum = 0.0;
    int pos;
    while (DTM.NULL != (pos = nodes.nextNode()))
    {
      DTM dtm = nodes.getDTM(pos);
      XMLString s = dtm.getStringValue(pos);
      if (null != s)
        sum += s.toDouble();
    }
    nodes.detach();
    return new XNumber(sum);
  }
}
