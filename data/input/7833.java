class AndQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -1081892073854801359L;
    private QueryExp exp1;
    private QueryExp exp2;
    public AndQueryExp() {
    }
    public AndQueryExp(QueryExp q1, QueryExp q2) {
        exp1 = q1;
        exp2 = q2;
    }
    public QueryExp getLeftExp()  {
        return exp1;
    }
    public QueryExp getRightExp()  {
        return exp2;
    }
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        return exp1.apply(name) && exp2.apply(name);
    }
    @Override
    public String toString() {
        return "(" + exp1 + ") and (" + exp2 + ")";
    }
}
