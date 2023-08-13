public class NotEquals extends Operation
{
    static final long serialVersionUID = -7869072863070586900L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return (left.notEquals(right)) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
