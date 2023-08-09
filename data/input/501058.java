public class FuncKey extends Function2Args
{
    static final long serialVersionUID = 9089293100115347340L;
  static private Boolean ISTRUE = new Boolean(true);
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
    XNodeSet nodes = null;
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int docContext = dtm.getDocumentRoot(context);
    if (DTM.NULL == docContext)
    {
    }
    String xkeyname = getArg0().execute(xctxt).str();
    QName keyname = new QName(xkeyname, xctxt.getNamespaceContext());
    XObject arg = getArg1().execute(xctxt);
    boolean argIsNodeSetDTM = (XObject.CLASS_NODESET == arg.getType());
    KeyManager kmgr = transformer.getKeyManager();
    if(argIsNodeSetDTM)
    {
    	XNodeSet ns = (XNodeSet)arg;
    	ns.setShouldCacheNodes(true);
    	int len = ns.getLength();
    	if(len <= 1)
    		argIsNodeSetDTM = false;
    }
    if (argIsNodeSetDTM)
    {
      Hashtable usedrefs = null;
      DTMIterator ni = arg.iter();
      int pos;
      UnionPathIterator upi = new UnionPathIterator();
      upi.exprSetParent(this);
      while (DTM.NULL != (pos = ni.nextNode()))
      {
        dtm = xctxt.getDTM(pos);
        XMLString ref = dtm.getStringValue(pos);
        if (null == ref)
          continue;
        if (null == usedrefs)
          usedrefs = new Hashtable();
        if (usedrefs.get(ref) != null)
        {
          continue;  
        }
        else
        {
          usedrefs.put(ref, ISTRUE);
        }
        XNodeSet nl =
          kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref,
                               xctxt.getNamespaceContext());
        nl.setRoot(xctxt.getCurrentNode(), xctxt);
          upi.addIterator(nl);
      }
      int current = xctxt.getCurrentNode();
      upi.setRoot(current, xctxt);
      nodes = new XNodeSet(upi);
    }
    else
    {
      XMLString ref = arg.xstr();
      nodes = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname,
                                                ref,
                                                xctxt.getNamespaceContext());
      nodes.setRoot(xctxt.getCurrentNode(), xctxt);
    }
    return nodes;
  }
}
