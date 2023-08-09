public class ElemExsltFuncResult extends ElemVariable
{
    static final long serialVersionUID = -3478311949388304563L;
    private boolean m_isResultSet = false;
    private XObject m_result = null;
    private int m_callerFrameSize = 0;
  public void execute(TransformerImpl transformer) throws TransformerException
  {    
    XPathContext context = transformer.getXPathContext();
    if (transformer.currentFuncResultSeen()) {
        throw new TransformerException("An EXSLT function cannot set more than one result!");
    }
    int sourceNode = context.getCurrentNode();
    XObject var = getValue(transformer, sourceNode);
    transformer.popCurrentFuncResult();
    transformer.pushCurrentFuncResult(var);
  }
  public int getXSLToken()
  {
    return Constants.EXSLT_ELEMNAME_FUNCRESULT;
  }
   public String getNodeName()
  {
    return Constants.EXSLT_ELEMNAME_FUNCRESULT_STRING;
  }
}
