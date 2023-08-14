class ProcessorNamespaceAlias extends XSLTElementProcessor
{
    static final long serialVersionUID = -6309867839007018964L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    final String resultNS;
    NamespaceAlias na = new NamespaceAlias(handler.nextUid());
    setPropertiesFromAttributes(handler, rawName, attributes, na);
    String prefix = na.getStylesheetPrefix();
    if(prefix.equals("#default"))
    {
      prefix = "";
      na.setStylesheetPrefix(prefix);
    }
    String stylesheetNS = handler.getNamespaceForPrefix(prefix);
    na.setStylesheetNamespace(stylesheetNS);
    prefix = na.getResultPrefix();
    if(prefix.equals("#default"))
    {
      prefix = "";
      na.setResultPrefix(prefix);
      resultNS = handler.getNamespaceForPrefix(prefix);
      if(null == resultNS)
        handler.error(XSLTErrorResources.ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT, null, null);
    }
    else
    {
        resultNS = handler.getNamespaceForPrefix(prefix);
        if(null == resultNS)
         handler.error(XSLTErrorResources.ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX, new Object[] {prefix}, null);
    }
    na.setResultNamespace(resultNS);
    handler.getStylesheet().setNamespaceAlias(na);
    handler.getStylesheet().appendChild(na);
  }
}
