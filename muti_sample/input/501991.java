public class AVTPartSimple extends AVTPart
{
    static final long serialVersionUID = -3744957690598727913L;
  private String m_val;
  public AVTPartSimple(String val)
  {
    m_val = val;
  }
  public String getSimpleString()
  {
    return m_val;
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
  }
  public void evaluate(XPathContext xctxt, FastStringBuffer buf,
                       int context,
                       org.apache.xml.utils.PrefixResolver nsNode)
  {
    buf.append(m_val);
  }
  public void callVisitors(XSLTVisitor visitor)
  {
  }
}
