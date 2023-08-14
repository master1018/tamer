public class FuncCurrent extends Function
{
    static final long serialVersionUID = 5715316804877715008L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    SubContextList subContextList = xctxt.getCurrentNodeList();
    int currentNode = DTM.NULL;
    if (null != subContextList) {
        if (subContextList instanceof PredicatedNodeTest) {
            LocPathIterator iter = ((PredicatedNodeTest)subContextList)
                                                          .getLocPathIterator();
            currentNode = iter.getCurrentContextNode();
         } else if(subContextList instanceof StepPattern) {
           throw new RuntimeException(XSLMessages.createMessage(
              XSLTErrorResources.ER_PROCESSOR_ERROR,null));
         }
    } else {
        currentNode = xctxt.getContextNode();
    }
    return new XNodeSet(currentNode, xctxt.getDTMManager());
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
  }
}
