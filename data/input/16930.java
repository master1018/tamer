public class XmlErrorHandler extends DefaultHandler {
       public int errorCounter = 0;
       public void error(SAXParseException e) throws SAXException {
           errorCounter++;
       }
       public void fatalError(SAXParseException e) throws SAXException {
           errorCounter++;
       }
       public void warning(SAXParseException exception) throws SAXException {
       }
}
