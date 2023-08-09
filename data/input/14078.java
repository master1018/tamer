public abstract class PragmaHandler
{
  public abstract boolean process (String pragma, String currentToken) throws IOException;
  void init (Preprocessor p)
  {
    preprocessor = p;
  } 
  protected String currentToken ()
  {
    return preprocessor.currentToken ();
  } 
  protected SymtabEntry getEntryForName (String string)
  {
    return preprocessor.getEntryForName (string);
  } 
  protected String getStringToEOL () throws IOException
  {
    return preprocessor.getStringToEOL ();
  } 
  protected String getUntil (char c) throws IOException
  {
    return preprocessor.getUntil (c);
  } 
  protected String nextToken () throws IOException
  {
    return preprocessor.nextToken ();
  } 
  protected SymtabEntry scopedName () throws IOException
  {
    return preprocessor.scopedName ();
  } 
  protected void skipToEOL () throws IOException
  {
    preprocessor.skipToEOL ();
  } 
  protected String skipUntil (char c) throws IOException
  {
    return preprocessor.skipUntil (c);
  } 
  protected void parseException (String message)
  {
    preprocessor.parseException (message);
  } 
  protected void openScope (SymtabEntry entry)
  {
  } 
  protected void closeScope (SymtabEntry entry)
  {
  } 
  private Preprocessor preprocessor = null;
} 
