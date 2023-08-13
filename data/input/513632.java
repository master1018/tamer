public class ProcessorText extends ProcessorTemplateElem
{
    static final long serialVersionUID = 5170229307201307523L;
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    ProcessorCharacters charProcessor =
      (ProcessorCharacters) handler.getProcessorFor(null, "text()", "text");
    charProcessor.setXslTextElement((ElemText) elem);
    ElemTemplateElement parent = handler.getElemTemplateElement();
    parent.appendChild(elem);
    elem.setDOMBackPointer(handler.getOriginatingNode());
  }
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    ProcessorCharacters charProcessor 
      = (ProcessorCharacters) handler.getProcessorFor(null, "text()", "text");
    charProcessor.setXslTextElement(null);
  }
}
