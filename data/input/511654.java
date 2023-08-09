public class ElemExsltFunction extends ElemTemplate
{
    static final long serialVersionUID = 272154954793534771L;
  public int getXSLToken()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION;
  }
  public String getNodeName()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION_STRING;
  }
  public void execute(TransformerImpl transformer, XObject[] args)
          throws TransformerException
  {
    XPathContext xctxt = transformer.getXPathContext();
    VariableStack vars = xctxt.getVarStack();
    int thisFrame = vars.getStackFrame();
    int nextFrame = vars.link(m_frameSize);
    if (m_inArgsSize < args.length) {
      throw new TransformerException ("function called with too many args");
    }
    if (m_inArgsSize > 0) {
      vars.clearLocalSlots(0, m_inArgsSize);
      if (args.length > 0) {
        vars.setStackFrame(thisFrame);
        NodeList children = this.getChildNodes();
        for (int i = 0; i < args.length; i ++) {
          Node child = children.item(i);
          if (children.item(i) instanceof ElemParam) {
            ElemParam param = (ElemParam)children.item(i);
            vars.setLocalVariable(param.getIndex(), args[i], nextFrame);
          }
        }
        vars.setStackFrame(nextFrame);
      }
    }
    vars.setStackFrame(nextFrame);
    transformer.executeChildTemplates(this, true);
    vars.unlink(thisFrame);
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    String namespace = getName().getNamespace();
    String handlerClass = sroot.getExtensionHandlerClass();
    Object[] args ={namespace, sroot};
    ExtensionNamespaceSupport extNsSpt = 
                         new ExtensionNamespaceSupport(namespace, handlerClass, args);
    sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    if (!(namespace.equals(Constants.S_EXSLT_FUNCTIONS_URL)))
    {
      namespace = Constants.S_EXSLT_FUNCTIONS_URL;
      args = new Object[]{namespace, sroot};
      extNsSpt = new ExtensionNamespaceSupport(namespace, handlerClass, args);
      sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    }
  }
}
