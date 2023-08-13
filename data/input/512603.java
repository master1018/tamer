public class AVTPartXPath extends AVTPart
{
    static final long serialVersionUID = -4460373807550527675L;
  private XPath m_xpath;
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    m_xpath.fixupVariables(vars, globalsSize);
  }
   public boolean canTraverseOutsideSubtree()
   {
    return m_xpath.getExpression().canTraverseOutsideSubtree();
   }
  public AVTPartXPath(XPath xpath)
  {
    m_xpath = xpath;
  }
  public AVTPartXPath(
          String val, org.apache.xml.utils.PrefixResolver nsNode, 
          XPathParser xpathProcessor, XPathFactory factory, 
          XPathContext liaison)
            throws javax.xml.transform.TransformerException
  {
    m_xpath = new XPath(val, null, nsNode, XPath.SELECT, liaison.getErrorListener());
  }
  public String getSimpleString()
  {
    return "{" + m_xpath.getPatternString() + "}";
  }
  public void evaluate(
          XPathContext xctxt, FastStringBuffer buf, int context, org.apache.xml.utils.PrefixResolver nsNode)
            throws javax.xml.transform.TransformerException
  {
    XObject xobj = m_xpath.execute(xctxt, context, nsNode);
    if (null != xobj)
    {
      xobj.appendToFsb(buf);
    }
  }
  public void callVisitors(XSLTVisitor visitor)
  {
  	m_xpath.getExpression().callVisitors(m_xpath, visitor);
  }
}
