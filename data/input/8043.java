public class Not extends UnaryExpr
{
  protected Not (Expression operand)
  {
    super ("~", operand);
  } 
  public Object evaluate () throws EvaluationException
  {
    try
    {
      Number op = (Number)operand ().evaluate ();
      if (op instanceof Float || op instanceof Double)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.not"), operand ().value ().getClass ().getName ()};
        throw new EvaluationException (Util.getMessage ("EvaluationException.2", parameters));
      }
      else
      {
        BigInteger b = (BigInteger)coerceToTarget((BigInteger)op);
        if (type ().equals ("short") || type ().equals ("long") || type ().equals ("long long"))
          value (b.add (one).multiply (negOne));
        else if (type ().equals("unsigned short"))
          value (twoPow16.subtract (one).subtract (b));
        else if (type ().equals ("unsigned long"))
          value (twoPow32.subtract (one).subtract (b));
        else if (type ().equals ("unsigned long long"))
          value (twoPow64.subtract (one).subtract (b));
        else
          value (b.not ());  
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.not"), operand ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } 
} 
