public class IDLID extends RepositoryID
{
  public IDLID ()
  {
    _prefix  = "";
    _name    = "";
    _version = "1.0";
  } 
  public IDLID (String prefix, String name, String version)
  {
    _prefix  = prefix;
    _name    = name;
    _version = version;
  } 
  public String ID ()
  {
    if (_prefix.equals (""))
      return "IDL:" + _name + ':' + _version;
    else
      return "IDL:" + _prefix + '/' + _name + ':' + _version;
  } 
  public String prefix ()
  {
    return _prefix;
  } 
  void prefix (String prefix)
  {
    if (prefix == null)
      _prefix = "";
    else
      _prefix = prefix;
  } 
  public String name ()
  {
    return _name;
  } 
  void name (String name)
  {
    if (name == null)
      _name = "";
    else
      _name = name;
  } 
  public String version ()
  {
    return _version;
  } 
  void version (String version)
  {
    if (version == null)
      _version = "";
    else
      _version = version;
  } 
  void appendToName (String name)
  {
    if (name != null)
      if (_name.equals (""))
        _name = name;
      else
        _name = _name + '/' + name;
  } 
  void replaceName (String name)
  {
    if (name == null)
      _name = "";
    else
    {
      int index = _name.lastIndexOf ('/');
      if (index < 0)
        _name = name;
      else
        _name = _name.substring (0, index + 1) + name;
    }
  } 
  public Object clone ()
  {
    return new IDLID (_prefix, _name, _version);
  } 
  private String _prefix;
  private String _name;
  private String _version;
} 
