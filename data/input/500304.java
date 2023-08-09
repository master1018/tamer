public class XMLMessages
{
  protected Locale fLocale = Locale.getDefault();
  private static ListResourceBundle XMLBundle = new XMLErrorResources(); 
  protected static final String BAD_CODE = "BAD_CODE";
  protected static final String FORMAT_FAILED = "FORMAT_FAILED";
   public void setLocale(Locale locale)
  {
    fLocale = locale;
  }
  public Locale getLocale()
  {
    return fLocale;
  }
  public static final String createXMLMessage(String msgKey, Object args[])
  {
      return createMsg(XMLBundle, msgKey, args);
  }
  public static final String createMsg(ListResourceBundle fResourceBundle,
	String msgKey, Object args[])  
  {
    String fmsg = null;
    boolean throwex = false;
    String msg = null;
    if (msgKey != null)
      msg = fResourceBundle.getString(msgKey);
    if (msg == null)
    {
      msg = fResourceBundle.getString(BAD_CODE);
      throwex = true;
    }
    if (args != null)
    {
      try
      {
        int n = args.length;
        for (int i = 0; i < n; i++)
        {
          if (null == args[i])
            args[i] = "";
        }
        fmsg = java.text.MessageFormat.format(msg, args);
      }
      catch (Exception e)
      {
        fmsg = fResourceBundle.getString(FORMAT_FAILED);
        fmsg += " " + msg;
      }
    }
    else
      fmsg = msg;
    if (throwex)
    {
      throw new RuntimeException(fmsg);
    }
    return fmsg;
  }
}
