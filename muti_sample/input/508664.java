public class XSLMessages extends XPATHMessages
{
  private static ListResourceBundle XSLTBundle = new XSLTErrorResources(); 
  public static final String createMessage(String msgKey, Object args[])  
  {
      return createMsg(XSLTBundle, msgKey, args);
  }
  public static final String createWarning(String msgKey, Object args[])  
  {
      return createMsg(XSLTBundle, msgKey, args);
  }
}
