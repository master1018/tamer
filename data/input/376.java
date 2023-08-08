public class PLSQLSimpleSearchCondition extends PLSQLSearchCondition {
    public PLSQLSimpleSearchCondition(TokenReference ref, PLSQLPredicate expr) {
        super(ref);
        this.expr = expr;
    }
    public void accept(PLVisitor visitor) throws PositionedError {
        visitor.visitSQLSimpleSearchCondition(this, expr);
    }
    public void analyse(PLSQLContext context, Environment env) throws PositionedError {
        expr.analyse(context, env);
    }
    public SearchCondition toXKopi(PLSQLContext context, Environment env) throws PositionedError {
        return new SimpleSearchCondition(getTokenReference(), expr.toXKopi(context, env));
    }
    private PLSQLPredicate expr;
}
