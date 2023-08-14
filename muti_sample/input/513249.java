public class DTMDOMException extends org.w3c.dom.DOMException
{
    static final long serialVersionUID = 1895654266613192414L;
  public DTMDOMException(short code, String message)
  {
    super(code, message);
  }
  public DTMDOMException(short code)
  {
    super(code, "");
  }
}
