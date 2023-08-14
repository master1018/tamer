public class Plus extends Operation
{
    static final long serialVersionUID = -4492072861616504256L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return new XNumber(left.num() + right.num());
  }
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return (m_right.num(xctxt) + m_left.num(xctxt));
  }
}
