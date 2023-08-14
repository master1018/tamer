public class Bool extends UnaryOperation
{
    static final long serialVersionUID = 44705375321914635L;
  public XObject operate(XObject right) throws javax.xml.transform.TransformerException
  {
    if (XObject.CLASS_BOOLEAN == right.getType())
      return right;
    else
      return right.bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
  public boolean bool(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return m_right.bool(xctxt);
  }
}
