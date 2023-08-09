public class Terminal extends Expression
{
  protected Terminal (String representation, Character charValue,
    boolean isWide)
  {
    rep (representation);
    value (charValue);
    if (isWide)
        type( "wchar" ) ;
    else
        type( "char" ) ;
  } 
  protected Terminal (String representation, Boolean booleanValue)
  {
    rep (representation);
    value (booleanValue);
  } 
  protected Terminal (String representation, BigInteger bigIntegerValue)
  {
    rep (representation);
    value (bigIntegerValue);
  } 
  protected Terminal (String representation, Long longValue)
  {
    long lv = longValue.longValue ();
    rep (representation);
    if (lv > Integer.MAX_VALUE || lv < Integer.MIN_VALUE)
      value (longValue);
    else
      value (new Integer (longValue.intValue ()));
  } 
  protected Terminal (String representation, Double doubleValue)
  {
    rep (representation);
    value (doubleValue);
  } 
  protected Terminal (String stringValue, boolean isWide )
  {
    rep (stringValue);
    value (stringValue);
    if (isWide)
        type( "wstring" ) ;
    else
        type( "string" ) ;
  } 
  protected Terminal (ConstEntry constReference)
  {
    rep (constReference.fullName ());
    value (constReference);
  } 
  public Object evaluate () throws EvaluationException
  {
    if (value () instanceof ConstEntry)
      return ((ConstEntry)value ()).value ().evaluate ();
    else
      return value ();
  } 
} 
