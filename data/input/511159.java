public class XUnresolvedVariableSimple extends XObject
{
    static final long serialVersionUID = -1224413807443958985L;
  public XUnresolvedVariableSimple(ElemVariable obj)
  {
    super(obj);
  }
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
  	Expression expr = ((ElemVariable)m_obj).getSelect().getExpression();
    XObject xobj = expr.execute(xctxt);
    xobj.allowDetachToRelease(false);
    return xobj;
  }
  public int getType()
  {
    return CLASS_UNRESOLVEDVARIABLE;
  }
  public String getTypeString()
  {
    return "XUnresolvedVariableSimple (" + object().getClass().getName() + ")";
  }
}
