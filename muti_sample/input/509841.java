class TemplateSubPatternAssociation implements Serializable, Cloneable
{
    static final long serialVersionUID = -8902606755229903350L;
  StepPattern m_stepPattern;
  private String m_pattern;
  private ElemTemplate m_template;
  private TemplateSubPatternAssociation m_next = null;
  private boolean m_wild;
  private String m_targetString;
  TemplateSubPatternAssociation(ElemTemplate template, StepPattern pattern, String pat)
  {
    m_pattern = pat;
    m_template = template;
    m_stepPattern = pattern;
    m_targetString = m_stepPattern.getTargetString();
    m_wild = m_targetString.equals("*");
  }
  public Object clone() throws CloneNotSupportedException
  {
    TemplateSubPatternAssociation tspa =
      (TemplateSubPatternAssociation) super.clone();
    tspa.m_next = null;
    return tspa;
  }
  public final String getTargetString()
  {
    return m_targetString;
  }
  public void setTargetString(String key)
  {
    m_targetString = key;
  }
  boolean matchMode(QName m1)
  {
    return matchModes(m1, m_template.getMode());
  }
  private boolean matchModes(QName m1, QName m2)
  {
    return (((null == m1) && (null == m2))
            || ((null != m1) && (null != m2) && m1.equals(m2)));
  }
  public boolean matches(XPathContext xctxt, int targetNode, QName mode)
          throws TransformerException
  {
    double score = m_stepPattern.getMatchScore(xctxt, targetNode);
    return (XPath.MATCH_SCORE_NONE != score)
           && matchModes(mode, m_template.getMode());
  }
  public final boolean isWild()
  {
    return m_wild;
  }
  public final StepPattern getStepPattern()
  {
    return m_stepPattern;
  }
  public final String getPattern()
  {
    return m_pattern;
  }
  public int getDocOrderPos()
  {
    return m_template.getUid();
  }
  public final int getImportLevel()
  {
    return m_template.getStylesheetComposed().getImportCountComposed();
  }
  public final ElemTemplate getTemplate()
  {
    return m_template;
  }
  public final TemplateSubPatternAssociation getNext()
  {
    return m_next;
  }
  public void setNext(TemplateSubPatternAssociation mp)
  {
    m_next = mp;
  }
}
