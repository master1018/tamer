public class ElemCopy extends ElemUse
{
    static final long serialVersionUID = 5478580783896941384L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
                XPathContext xctxt = transformer.getXPathContext();
    try
    {
      int sourceNode = xctxt.getCurrentNode();
      xctxt.pushCurrentNode(sourceNode);
      DTM dtm = xctxt.getDTM(sourceNode);
      short nodeType = dtm.getNodeType(sourceNode);
      if ((DTM.DOCUMENT_NODE != nodeType) && (DTM.DOCUMENT_FRAGMENT_NODE != nodeType))
      {
        SerializationHandler rthandler = transformer.getSerializationHandler();
        ClonerToResultTree.cloneToResultTree(sourceNode, nodeType, dtm, 
                                             rthandler, false);
        if (DTM.ELEMENT_NODE == nodeType)
        {
          super.execute(transformer);
          SerializerUtils.processNSDecls(rthandler, sourceNode, nodeType, dtm);
          transformer.executeChildTemplates(this, true);
          String ns = dtm.getNamespaceURI(sourceNode);
          String localName = dtm.getLocalName(sourceNode);
          transformer.getResultTreeHandler().endElement(ns, localName,
                                                        dtm.getNodeName(sourceNode));
        }
      }
      else
      {
        super.execute(transformer);
        transformer.executeChildTemplates(this, true);
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      xctxt.popCurrentNode();
    }
  }
}
