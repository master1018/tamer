public class ElemMessage extends ElemTemplateElement
{
    static final long serialVersionUID = 1530472462155060023L;
  private boolean m_terminate = Constants.ATTRVAL_NO;  
  public void setTerminate(boolean v)
  {
    m_terminate = v;
  }
  public boolean getTerminate()
  {
    return m_terminate;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_MESSAGE;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_MESSAGE_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    String data = transformer.transformToString(this);
    transformer.getMsgMgr().message(this, data, m_terminate);
    if(m_terminate)
      transformer.getErrorListener().fatalError(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_STYLESHEET_DIRECTED_TERMINATION, null))); 
  }
}
