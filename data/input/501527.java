class ProcessorDecimalFormat extends XSLTElementProcessor
{
    static final long serialVersionUID = -5052904382662921627L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    DecimalFormatProperties dfp = new DecimalFormatProperties(handler.nextUid());
    dfp.setDOMBackPointer(handler.getOriginatingNode());
    dfp.setLocaterInfo(handler.getLocator());
    setPropertiesFromAttributes(handler, rawName, attributes, dfp);
    handler.getStylesheet().setDecimalFormat(dfp);
    handler.getStylesheet().appendChild(dfp);
  }
}
