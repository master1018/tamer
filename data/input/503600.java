public class ProcessorStylesheetElement extends XSLTElementProcessor
{
    static final long serialVersionUID = -877798927447840792L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
		super.startElement(handler, uri, localName, rawName, attributes);
    try
    {
      int stylesheetType = handler.getStylesheetType();
      Stylesheet stylesheet;
      if (stylesheetType == StylesheetHandler.STYPE_ROOT)
      {
        try
        {
          stylesheet = getStylesheetRoot(handler);
        }
        catch(TransformerConfigurationException tfe)
        {
          throw new TransformerException(tfe);
        }
      }
      else
      {
        Stylesheet parent = handler.getStylesheet();
        if (stylesheetType == StylesheetHandler.STYPE_IMPORT)
        {
          StylesheetComposed sc = new StylesheetComposed(parent);
          parent.setImport(sc);
          stylesheet = sc;
        }
        else
        {
          stylesheet = new Stylesheet(parent);
          parent.setInclude(stylesheet);
        }
      }
      stylesheet.setDOMBackPointer(handler.getOriginatingNode());
      stylesheet.setLocaterInfo(handler.getLocator());
      stylesheet.setPrefixes(handler.getNamespaceSupport());
      handler.pushStylesheet(stylesheet);
      setPropertiesFromAttributes(handler, rawName, attributes,
                                  handler.getStylesheet());
      handler.pushElemTemplateElement(handler.getStylesheet());
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  protected Stylesheet getStylesheetRoot(StylesheetHandler handler) throws TransformerConfigurationException
  {
    StylesheetRoot stylesheet;
    stylesheet = new StylesheetRoot(handler.getSchema(), handler.getStylesheetProcessor().getErrorListener());
    if (handler.getStylesheetProcessor().isSecureProcessing())
      stylesheet.setSecureProcessing(true);
    return stylesheet;
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
		super.endElement(handler, uri, localName, rawName);
    handler.popElemTemplateElement();
    handler.popStylesheet();
  }
}
