class ProcessorGlobalParamDecl extends ProcessorTemplateElem
{
    static final long serialVersionUID = 1900450872353587350L;
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    handler.pushElemTemplateElement(elem);
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    ElemParam v = (ElemParam) handler.getElemTemplateElement();
    handler.getStylesheet().appendChild(v);
    handler.getStylesheet().setParam(v);
    super.endElement(handler, uri, localName, rawName);
  }
}
