public class String extends UnaryOperation
{
    static final long serialVersionUID = 2973374377453022888L;
  public XObject operate(XObject right) throws javax.xml.transform.TransformerException
  {
    return (XString)right.xstr(); 
  }
}
