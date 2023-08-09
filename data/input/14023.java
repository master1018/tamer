public class Negative extends UnaryExpr
{
  protected Negative (Expression operand)
  {
    super ("-", operand);
  } 
  public Object evaluate () throws EvaluationException
  {
    try
    {
      Number op = (Number)operand ().evaluate ();
      if (op instanceof Float || op instanceof Double)
        value (new Double (-op.doubleValue ()));
      else
      {
        BigInteger tmpOp = (BigInteger)op;
        value (tmpOp.multiply (BigInteger.valueOf (-1)));
     }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.neg"), operand ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } 
} 
