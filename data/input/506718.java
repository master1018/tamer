class ProcessorAttributeSet extends XSLTElementProcessor
{
    static final long serialVersionUID = -6473739251316787552L;
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    ElemAttributeSet eat = new ElemAttributeSet();
    eat.setLocaterInfo(handler.getLocator());
    try
    {
      eat.setPrefixes(handler.getNamespaceSupport());
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
    eat.setDOMBackPointer(handler.getOriginatingNode());
    setPropertiesFromAttributes(handler, rawName, attributes, eat);
    handler.getStylesheet().setAttributeSet(eat);
    ElemTemplateElement parent = handler.getElemTemplateElement();
    parent.appendChild(eat);
    handler.pushElemTemplateElement(eat);
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    handler.popElemTemplateElement();
  }
}
