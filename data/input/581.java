class NotQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = 5269643775896723397L;
    private QueryExp exp;
    public NotQueryExp() {
    }
    public NotQueryExp(QueryExp q) {
        exp = q;
    }
    public QueryExp getNegatedExp()  {
        return exp;
    }
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        return exp.apply(name) == false;
    }
    @Override
    public String toString()  {
        return "not (" + exp + ")";
    }
 }
