public class ResourceBundleUtil
{
  public static String getVersion ()
  {
    String version = getMessage ("Version.product", getMessage ("Version.number"));
    return version;
  } 
  public static String getMessage (String key)
  {
    return fBundle.getString(key);
  } 
  public static String getMessage (String key, String fill)
  {
    Object[] args = { fill };
    return MessageFormat.format(fBundle.getString(key), args);
  } 
  public static String getMessage (String key, String[] fill)
  {
    return MessageFormat.format(fBundle.getString(key), fill);
  } 
  public static void registerResourceBundle (ResourceBundle bundle)
  {
    if (bundle != null)
      fBundle = bundle;
  } 
  public static ResourceBundle getResourceBundle ()
  {
    return fBundle;
  } 
  private static ResourceBundle  fBundle;
  static
  {
    fBundle = ResourceBundle.getBundle("com.sun.tools.corba.se.idl.idl");
  }
} 
