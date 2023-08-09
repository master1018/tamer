public class Or extends BinaryExpr
{
  protected Or (Expression leftOperand, Expression rightOperand)
  {
    super ("|", leftOperand, rightOperand);
  } 
  public Object evaluate () throws EvaluationException
  {
    try
    {
      Number l = (Number)left ().evaluate ();
      Number r = (Number)right ().evaluate ();
      if (l instanceof Float || l instanceof Double || r instanceof Float || r instanceof Double)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.or"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
        throw new EvaluationException (Util.getMessage ("EvaluationException.1", parameters));
      }
      else
      {
        BigInteger uL = (BigInteger)toUnsigned((BigInteger)l);
        BigInteger uR = (BigInteger)toUnsigned((BigInteger)r);
        value((BigInteger)coerceToTarget(uL.or (uR)));
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.or"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.1", parameters));
    }
    return value ();
  } 
} 
