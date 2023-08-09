public class DefaultExprFactory implements ExprFactory
{
  public And and (Expression left, Expression right)
  {
    return new And (left, right);
  } 
  public BooleanAnd booleanAnd (Expression left, Expression right)
  {
    return new BooleanAnd (left, right);
  } 
  public BooleanNot booleanNot (Expression operand)
  {
    return new BooleanNot (operand);
  } 
  public BooleanOr booleanOr (Expression left, Expression right)
  {
    return new BooleanOr (left, right);
  } 
  public Divide divide (Expression left, Expression right)
  {
    return new Divide (left, right);
  } 
  public Equal equal (Expression left, Expression right)
  {
    return new Equal (left, right);
  } 
  public GreaterEqual greaterEqual (Expression left, Expression right)
  {
    return new GreaterEqual (left, right);
  } 
  public GreaterThan greaterThan (Expression left, Expression right)
  {
    return new GreaterThan (left, right);
  } 
  public LessEqual lessEqual (Expression left, Expression right)
  {
    return new LessEqual (left, right);
  } 
  public LessThan lessThan (Expression left, Expression right)
  {
    return new LessThan (left, right);
  } 
  public Minus minus (Expression left, Expression right)
  {
    return new Minus (left, right);
  } 
  public Modulo modulo (Expression left, Expression right)
  {
    return new Modulo (left, right);
  } 
  public Negative negative (Expression operand)
  {
    return new Negative (operand);
  } 
  public Not not (Expression operand)
  {
    return new Not (operand);
  } 
  public NotEqual notEqual (Expression left, Expression right)
  {
    return new NotEqual (left, right);
  } 
  public Or or (Expression left, Expression right)
  {
    return new Or (left, right);
  } 
  public Plus plus (Expression left, Expression right)
  {
    return new Plus (left, right);
  } 
  public Positive positive (Expression operand)
  {
    return new Positive (operand);
  } 
  public ShiftLeft shiftLeft (Expression left, Expression right)
  {
    return new ShiftLeft (left, right);
  } 
  public ShiftRight shiftRight (Expression left, Expression right)
  {
    return new ShiftRight (left, right);
  } 
  public Terminal terminal (String representation, Character charValue,
    boolean isWide )
  {
    return new Terminal (representation, charValue, isWide );
  } 
  public Terminal terminal (String representation, Boolean booleanValue)
  {
    return new Terminal (representation, booleanValue);
  } 
  public Terminal terminal (String representation, BigInteger bigIntegerValue)
  {
    return new Terminal (representation, bigIntegerValue);
  } 
  public Terminal terminal (String representation, Double doubleValue)
  {
    return new Terminal (representation, doubleValue);
  } 
  public Terminal terminal (String stringValue, boolean isWide )
  {
    return new Terminal (stringValue, isWide);
  } 
  public Terminal terminal (ConstEntry constReference)
  {
    return new Terminal (constReference);
  } 
  public Times times (Expression left, Expression right)
  {
    return new Times (left, right);
  } 
  public Xor xor (Expression left, Expression right)
  {
    return new Xor (left, right);
  } 
} 
