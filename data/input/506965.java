public abstract class AVTPart implements java.io.Serializable, XSLTVisitable
{
    static final long serialVersionUID = -1747749903613916025L;
  public AVTPart(){}
  public abstract String getSimpleString();
  public abstract void evaluate(
    XPathContext xctxt, FastStringBuffer buf, int context,
      org.apache.xml.utils.PrefixResolver nsNode)
        throws javax.xml.transform.TransformerException;
  public void setXPathSupport(XPathContext support){}
   public boolean canTraverseOutsideSubtree()
   {
    return false;
   }
  public abstract void fixupVariables(java.util.Vector vars, int globalsSize);
}
