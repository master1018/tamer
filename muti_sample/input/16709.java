public class BooleanNot extends UnaryExpr
{
  protected BooleanNot (Expression operand)
  {
    super ("!", operand);
  } 
  public Object evaluate () throws EvaluationException
  {
    try
    {
      Object tmp = operand ().evaluate ();
      Boolean op;
      if (tmp instanceof Number)
      {
        if (tmp instanceof BigInteger)
          op = new Boolean (((BigInteger)tmp).compareTo (zero) != 0);
        else
          op = new Boolean (((Number)tmp).longValue () != 0);
      }
      else
        op = (Boolean)tmp;
      value (new Boolean (!op.booleanValue ()));
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.booleanNot"), operand ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } 
} 
