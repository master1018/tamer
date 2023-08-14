public class ShiftLeft extends BinaryExpr
{
  protected ShiftLeft (Expression leftOperand, Expression rightOperand)
  {
    super ("<<", leftOperand, rightOperand);
  } 
  public Object evaluate () throws EvaluationException
  {
    try
    {
      Number l = (Number)left ().evaluate ();
      Number r = (Number)right ().evaluate ();
      if (l instanceof Float || l instanceof Double || r instanceof Float || r instanceof Double)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.left"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
        throw new EvaluationException (Util.getMessage ("EvaluationException.1", parameters));
      }
      else
      {
        BigInteger bL = (BigInteger)coerceToTarget (l);
        BigInteger bR = (BigInteger)r;
        BigInteger ls  = bL.shiftLeft (bR.intValue ());
        if (type ().indexOf ("short") >= 0)
          ls = ls.mod (twoPow16);
        else if (type ().indexOf ("long") >= 0)
          ls = ls.mod (twoPow32);
        else if (type ().indexOf ("long long") >= 0)
          ls = ls.mod (twoPow64);
        value (coerceToTarget (ls));
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.left"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.1", parameters));
    }
    return value ();
  } 
} 
