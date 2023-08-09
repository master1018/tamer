public class KeyIterator extends OneStepIteratorForward
{
    static final long serialVersionUID = -1349109910100249661L;
  private QName m_name;
  public QName getName()
  {
    return m_name;
  }
  private Vector m_keyDeclarations;
  public Vector getKeyDeclarations()
  {
    return m_keyDeclarations;
  }
  KeyIterator(QName name, Vector keyDeclarations)
  {
    super(Axis.ALL);
    m_keyDeclarations = keyDeclarations;
    m_name = name;
  }
  public short acceptNode(int testNode)
  {
    boolean foundKey = false;
    KeyIterator ki = (KeyIterator) m_lpi;
    org.apache.xpath.XPathContext xctxt = ki.getXPathContext();
    Vector keys = ki.getKeyDeclarations();
    QName name = ki.getName();
    try
    {
      int nDeclarations = keys.size();
      for (int i = 0; i < nDeclarations; i++)
      {
        KeyDeclaration kd = (KeyDeclaration) keys.elementAt(i);
        if (!kd.getName().equals(name))
          continue;
        foundKey = true;
        XPath matchExpr = kd.getMatch();
        double score = matchExpr.getMatchScore(xctxt, testNode);
        if (score == kd.getMatch().MATCH_SCORE_NONE)
          continue;
        return DTMIterator.FILTER_ACCEPT;
      } 
    }
    catch (TransformerException se)
    {
    }
    if (!foundKey)
      throw new RuntimeException(
        XSLMessages.createMessage(
          XSLTErrorResources.ER_NO_XSLKEY_DECLARATION,
          new Object[] { name.getLocalName()}));
    return DTMIterator.FILTER_REJECT;
  }
}
