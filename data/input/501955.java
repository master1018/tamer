public class FuncGenerateId extends FunctionDef1Arg
{
    static final long serialVersionUID = 973544842091724273L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int which = getArg0AsNode(xctxt);
    if (DTM.NULL != which)
    {
      return new XString("N" + Integer.toHexString(which).toUpperCase());
    }
    else
      return XString.EMPTYSTRING;
  }
}
