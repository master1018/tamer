public class WhiteSpaceInfo extends ElemTemplate
{
    static final long serialVersionUID = 6389208261999943836L;
  private boolean m_shouldStripSpace;
  public boolean getShouldStripSpace()
  {
    return m_shouldStripSpace;
  }
  public WhiteSpaceInfo(Stylesheet thisSheet)
  {
  	setStylesheet(thisSheet);
  }
  public WhiteSpaceInfo(XPath matchPattern, boolean shouldStripSpace, Stylesheet thisSheet)
  {
    m_shouldStripSpace = shouldStripSpace;
    setMatch(matchPattern);
    setStylesheet(thisSheet);
  }
  public void recompose(StylesheetRoot root)
  {
    root.recomposeWhiteSpaceInfo(this);
  }
}
