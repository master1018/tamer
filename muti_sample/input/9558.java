public abstract class BinaryExpr extends Expression
{
  public BinaryExpr (String operation, Expression leftOperand, Expression rightOperand)
  {
    _op    = operation;
    _left  = leftOperand;
    _right = rightOperand;
  } 
  public void op (String op) {_op = (op == null)? "": op;}
  public String op () {return _op;}
  public void       left (Expression left) {_left = left;}
  public Expression left () {return _left;}
  public void       right (Expression right) {_right = right;}
  public Expression right () {return _right;}
  private String     _op    = "";
  private Expression _left  = null;
  private Expression _right = null;
} 
