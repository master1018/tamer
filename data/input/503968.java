class ProcessorGlobalVariableDecl extends ProcessorTemplateElem
{
    static final long serialVersionUID = -5954332402269819582L;
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
    ElemVariable v = (ElemVariable) handler.getElemTemplateElement();
    handler.getStylesheet().appendChild(v);
    handler.getStylesheet().setVariable(v);
    super.endElement(handler, uri, localName, rawName);
  }
}
