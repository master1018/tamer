class NoPragma extends PragmaHandler
{
  public boolean process (String pragma, String currentToken) throws IOException
  {
    parseException (Util.getMessage ("Preprocessor.unknownPragma", pragma));
    skipToEOL ();
    return true;
  } 
} 
