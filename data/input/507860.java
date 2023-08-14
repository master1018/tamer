public class FuncTranslate extends Function3Args
{
    static final long serialVersionUID = -1672834340026116482L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    String theFirstString = m_arg0.execute(xctxt).str();
    String theSecondString = m_arg1.execute(xctxt).str();
    String theThirdString = m_arg2.execute(xctxt).str();
    int theFirstStringLength = theFirstString.length();
    int theThirdStringLength = theThirdString.length();
    StringBuffer sbuffer = new StringBuffer();
    for (int i = 0; i < theFirstStringLength; i++)
    {
      char theCurrentChar = theFirstString.charAt(i);
      int theIndex = theSecondString.indexOf(theCurrentChar);
      if (theIndex < 0)
      {
        sbuffer.append(theCurrentChar);
      }
      else if (theIndex < theThirdStringLength)
      {
        sbuffer.append(theThirdString.charAt(theIndex));
      }
      else
      {
      }
    }
    return new XString(sbuffer.toString());
  }
}
