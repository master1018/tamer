public class StopParseException extends org.xml.sax.SAXException
{
        static final long serialVersionUID = 210102479218258961L;
  StopParseException()
  {
    super("Stylesheet PIs found, stop the parse");
  }
}
