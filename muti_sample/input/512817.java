public abstract class ExtensionHandler
{
  protected String m_namespaceUri; 
  protected String m_scriptLang;
  static Class getClassForName(String className)
      throws ClassNotFoundException
  {
    if(className.equals("org.apache.xalan.xslt.extensions.Redirect")) {
      className = "org.apache.xalan.lib.Redirect";
    }
    return ObjectFactory.findProviderClass(
        className, ObjectFactory.findClassLoader(), true);
  }
  protected ExtensionHandler(String namespaceUri, String scriptLang)
  {
    m_namespaceUri = namespaceUri;
    m_scriptLang = scriptLang;
  }
  public abstract boolean isFunctionAvailable(String function);
  public abstract boolean isElementAvailable(String element);
  public abstract Object callFunction(
    String funcName, Vector args, Object methodKey,
      ExpressionContext exprContext) throws TransformerException;
  public abstract Object callFunction(
    FuncExtFunction extFunction, Vector args,
      ExpressionContext exprContext) throws TransformerException;
  public abstract void processElement(
    String localPart, ElemTemplateElement element, TransformerImpl transformer,
      Stylesheet stylesheetTree, Object methodKey) throws TransformerException, IOException;
}
