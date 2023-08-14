public class Neg extends UnaryOperation
{
    static final long serialVersionUID = -6280607702375702291L;
  public XObject operate(XObject right) throws javax.xml.transform.TransformerException
  {
    return new XNumber(-right.num());
  }
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return -(m_right.num(xctxt));
  }
}
