public class Comment
{
  static final int UNKNOWN  = -1;
  static final int JAVA_DOC =  0;
  static final int C_BLOCK  =  1;
  static final int CPP_LINE =  2;
  private static String _eol = System.getProperty ("line.separator");
  private String _text  = new String ("");
  private int    _style = UNKNOWN;
  Comment () {_text = new String (""); _style = UNKNOWN;} 
  Comment (String text) {_text = text; _style = style (_text);} 
  public void text (String string) {_text = string; _style = style (_text);}
  public String text () {return _text;}
  private int style (String text)
  {
    if (text == null)
      return UNKNOWN;
    else if (text.startsWith (""))
      return JAVA_DOC;
    else if (text.startsWith (""))
      return C_BLOCK;
    else if (text.startsWith ("
      return CPP_LINE;
    else
      return UNKNOWN;
  } 
  public void write () {System.out.println (_text);}
  public void generate (String indent, PrintWriter printStream)
  {
    if (_text == null || printStream == null)
      return;
    if (indent == null)
      indent = new String ("");
    switch (_style)
    {
      case JAVA_DOC:
        print (indent, printStream);
        break;
      case C_BLOCK:
        print (indent, printStream);
        break;
      case CPP_LINE:
        print (indent, printStream);
        break;
      default:
        break;
    }
  } 
  private void print (String indent, PrintWriter stream)
  {
    String text = _text.trim () + _eol;
    String line = null;
    int iLineStart = 0;
    int iLineEnd   = text.indexOf (_eol);
    int iTextEnd   = text.length () - 1;
    stream.println ();
    while (iLineStart < iTextEnd)
    {
      line = text.substring (iLineStart, iLineEnd);
      stream.println (indent + line);
      iLineStart = iLineEnd + _eol.length ();
      iLineEnd = iLineStart + text.substring (iLineStart).indexOf (_eol);
    }
  } 
  private void printJavaDoc (String indent, PrintWriter stream)
  {
    String text = _text.substring (3, (_text.length () - 2)).trim () + _eol;
    String line = null;
    int iLineStart = 0;
    int iLineEnd   = text.indexOf (_eol);
    int iTextEnd   = text.length () - 1;   
    stream.println (_eol + indent + "");
  } 
  private void printCBlock (String indent, PrintWriter stream)
  {
    String text = _text.substring (2, (_text.length () - 2)).trim () + _eol;
    String line = null;
    int iLineStart = 0;
    int iLineEnd   = text.indexOf (_eol);
    int iTextEnd   = text.length () - 1;   
    stream.println (indent + "");
  } 
  private void printCppLine (String indent, PrintWriter stream)
  {
    stream.println (indent + "
    stream.println (indent + "
    stream.println (indent + "
  } 
} 
