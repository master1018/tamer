public class ExpressionVisitor extends XPathVisitor
{
  private StylesheetRoot m_sroot;
  public ExpressionVisitor (StylesheetRoot sroot)
  {
    m_sroot = sroot;
  }
  public boolean visitFunction(ExpressionOwner owner, Function func)
  {
    if (func instanceof FuncExtFunction)
    {
      String namespace = ((FuncExtFunction)func).getNamespace();
      m_sroot.getExtensionNamespacesManager().registerExtension(namespace);      
    }
    else if (func instanceof FuncExtFunctionAvailable)
    {
      String arg = ((FuncExtFunctionAvailable)func).getArg0().toString();
      if (arg.indexOf(":") > 0)
      {
      	String prefix = arg.substring(0,arg.indexOf(":"));
      	String namespace = this.m_sroot.getNamespaceForPrefix(prefix);
      	m_sroot.getExtensionNamespacesManager().registerExtension(namespace);
      }
    }
    return true;
  }
}
