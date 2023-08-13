public class FuncId extends FunctionOneArg
{
    static final long serialVersionUID = 8930573966143567310L;
  private StringVector getNodesByID(XPathContext xctxt, int docContext,
                                    String refval, StringVector usedrefs,
                                    NodeSetDTM nodeSet, boolean mayBeMore)
  {
    if (null != refval)
    {
      String ref = null;
      StringTokenizer tokenizer = new StringTokenizer(refval);
      boolean hasMore = tokenizer.hasMoreTokens();
      DTM dtm = xctxt.getDTM(docContext);
      while (hasMore)
      {
        ref = tokenizer.nextToken();
        hasMore = tokenizer.hasMoreTokens();
        if ((null != usedrefs) && usedrefs.contains(ref))
        {
          ref = null;
          continue;
        }
        int node = dtm.getElementById(ref);
        if (DTM.NULL != node)
          nodeSet.addNodeInDocOrder(node, xctxt);
        if ((null != ref) && (hasMore || mayBeMore))
        {
          if (null == usedrefs)
            usedrefs = new StringVector();
          usedrefs.addElement(ref);
        }
      }
    }
    return usedrefs;
  }
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int docContext = dtm.getDocument();
    if (DTM.NULL == docContext)
      error(xctxt, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC, null);
    XObject arg = m_arg0.execute(xctxt);
    int argType = arg.getType();
    XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
    NodeSetDTM nodeSet = nodes.mutableNodeset();
    if (XObject.CLASS_NODESET == argType)
    {
      DTMIterator ni = arg.iter();
      StringVector usedrefs = null;
      int pos = ni.nextNode();
      while (DTM.NULL != pos)
      {
        DTM ndtm = ni.getDTM(pos);
        String refval = ndtm.getStringValue(pos).toString();
        pos = ni.nextNode();
        usedrefs = getNodesByID(xctxt, docContext, refval, usedrefs, nodeSet,
                                DTM.NULL != pos);
      }
    }
    else if (XObject.CLASS_NULL == argType)
    {
      return nodes;
    }
    else
    {
      String refval = arg.str();
      getNodesByID(xctxt, docContext, refval, null, nodeSet, false);
    }
    return nodes;
  }
}
