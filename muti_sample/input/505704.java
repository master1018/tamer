class ProcessorStripSpace extends ProcessorPreserveSpace
{
    static final long serialVersionUID = -5594493198637899591L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    Stylesheet thisSheet = handler.getStylesheet();
	WhitespaceInfoPaths paths = new WhitespaceInfoPaths(thisSheet);
    setPropertiesFromAttributes(handler, rawName, attributes, paths);
    Vector xpaths = paths.getElements();
    for (int i = 0; i < xpaths.size(); i++)
    {
      WhiteSpaceInfo wsi = new WhiteSpaceInfo((XPath) xpaths.elementAt(i), true, thisSheet);
      wsi.setUid(handler.nextUid());
      thisSheet.setStripSpaces(wsi);
    }
    paths.clearElements();
  }
}
