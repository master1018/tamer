public class XRTreeFrag extends XObject implements Cloneable
{
    static final long serialVersionUID = -3201553822254911567L;
  private DTMXRTreeFrag m_DTMXRTreeFrag;
  private int m_dtmRoot = DTM.NULL;
  protected boolean m_allowRelease = false;
  public XRTreeFrag(int root, XPathContext xctxt, ExpressionNode parent)
  {
    super(null);
    exprSetParent(parent);
    initDTM(root, xctxt);    
  }
  public XRTreeFrag(int root, XPathContext xctxt)
  {
    super(null); 
   initDTM(root, xctxt); 
  }
  private final void initDTM(int root, XPathContext xctxt){
    m_dtmRoot = root;
    final DTM dtm = xctxt.getDTM(root);
    if(dtm != null){
      m_DTMXRTreeFrag = xctxt.getDTMXRTreeFrag(xctxt.getDTMIdentity(dtm));
    }
  }
  public Object object()
  {
    if (m_DTMXRTreeFrag.getXPathContext() != null)
      return new org.apache.xml.dtm.ref.DTMNodeIterator((DTMIterator)(new org.apache.xpath.NodeSetDTM(m_dtmRoot, m_DTMXRTreeFrag.getXPathContext().getDTMManager())));
    else
      return super.object();
  }
  public XRTreeFrag(Expression expr)
  {
    super(expr);
  }
  public void allowDetachToRelease(boolean allowRelease)
  {
    m_allowRelease = allowRelease;
  }
  public void detach(){
    if(m_allowRelease){
    	m_DTMXRTreeFrag.destruct();
      setObject(null);
    }
  }
  public int getType()
  {
    return CLASS_RTREEFRAG;
  }
  public String getTypeString()
  {
    return "#RTREEFRAG";
  }
  public double num()
    throws javax.xml.transform.TransformerException
  {
    XMLString s = xstr();
    return s.toDouble();
  }
  public boolean bool()
  {
    return true;
  }
  private XMLString m_xmlStr = null;
  public XMLString xstr()
  {
    if(null == m_xmlStr)
      m_xmlStr = m_DTMXRTreeFrag.getDTM().getStringValue(m_dtmRoot);
    return m_xmlStr;
  }
  public void appendToFsb(org.apache.xml.utils.FastStringBuffer fsb)
  {
    XString xstring = (XString)xstr();
    xstring.appendToFsb(fsb);
  }
  public String str()
  {
    String str = m_DTMXRTreeFrag.getDTM().getStringValue(m_dtmRoot).toString();
    return (null == str) ? "" : str;
  }
  public int rtf()
  {
    return m_dtmRoot;
  }
  public DTMIterator asNodeIterator()
  {
    return new RTFIterator(m_dtmRoot, m_DTMXRTreeFrag.getXPathContext().getDTMManager());
  }
  public NodeList convertToNodeset()
  {
    if (m_obj instanceof NodeList)
      return (NodeList) m_obj;
    else
      return new org.apache.xml.dtm.ref.DTMNodeList(asNodeIterator());
  }
  public boolean equals(XObject obj2)
  {
    try
    {
      if (XObject.CLASS_NODESET == obj2.getType())
      {
        return obj2.equals(this);
      }
      else if (XObject.CLASS_BOOLEAN == obj2.getType())
      {
        return bool() == obj2.bool();
      }
      else if (XObject.CLASS_NUMBER == obj2.getType())
      {
        return num() == obj2.num();
      }
      else if (XObject.CLASS_NODESET == obj2.getType())
      {
        return xstr().equals(obj2.xstr());
      }
      else if (XObject.CLASS_STRING == obj2.getType())
      {
        return xstr().equals(obj2.xstr());
      }
      else if (XObject.CLASS_RTREEFRAG == obj2.getType())
      {
        return xstr().equals(obj2.xstr());
      }
      else
      {
        return super.equals(obj2);
      }
    }
    catch(javax.xml.transform.TransformerException te)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(te);
    }
  }
}
