class ProcessorTemplate extends ProcessorTemplateElem
{
    static final long serialVersionUID = -8457812845473603860L;
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    super.appendAndPush(handler, elem);
    elem.setDOMBackPointer(handler.getOriginatingNode());
    handler.getStylesheet().setTemplate((ElemTemplate) elem);
  }
}
