public class ChildIterator extends LocPathIterator
{
    static final long serialVersionUID = -6935428015142993583L;
  ChildIterator(Compiler compiler, int opPos, int analysis)
          throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis, false);
    initNodeTest(DTMFilter.SHOW_ALL);
  }
  public int asNode(XPathContext xctxt)
    throws javax.xml.transform.TransformerException
  {
    int current = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(current);
    return dtm.getFirstChild(current);
  }
  public int nextNode()
  {
  	if(m_foundLast)
  		return DTM.NULL;
    int next;
    m_lastFetched = next = (DTM.NULL == m_lastFetched)
                           ? m_cdtm.getFirstChild(m_context)
                           : m_cdtm.getNextSibling(m_lastFetched);
    if (DTM.NULL != next)
    {
      m_pos++;
      return next;
    }
    else
    {
      m_foundLast = true;
      return DTM.NULL;
    }
  }
  public int getAxis()
  {
    return org.apache.xml.dtm.Axis.CHILD;
  }
}
