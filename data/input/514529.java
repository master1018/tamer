public class FuncExtFunctionAvailable extends FunctionOneArg
{
    static final long serialVersionUID = 5118814314918592241L;
    transient private FunctionTable m_functionTable = null;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    String prefix;
    String namespace;
    String methName;
    String fullName = m_arg0.execute(xctxt).str();
    int indexOfNSSep = fullName.indexOf(':');
    if (indexOfNSSep < 0)
    {
      prefix = "";
      namespace = Constants.S_XSLNAMESPACEURL;
      methName = fullName;
    }
    else
    {
      prefix = fullName.substring(0, indexOfNSSep);
      namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
      if (null == namespace)
        return XBoolean.S_FALSE;
        methName = fullName.substring(indexOfNSSep + 1);
    }
    if (namespace.equals(Constants.S_XSLNAMESPACEURL))
    {
      try
      {
        if (null == m_functionTable) m_functionTable = new FunctionTable();
        return m_functionTable.functionAvailable(methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
      }
      catch (Exception e)
      {
        return XBoolean.S_FALSE;
      }
    }
    else
    {
      ExtensionsProvider extProvider = (ExtensionsProvider)xctxt.getOwnerObject();
      return extProvider.functionAvailable(namespace, methName)
             ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
  }
  public void setFunctionTable(FunctionTable aTable){
          m_functionTable = aTable;
  }
}
