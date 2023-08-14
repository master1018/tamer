class BetweenQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -2933597532866307444L;
    private ValueExp exp1;
    private ValueExp exp2;
    private ValueExp exp3;
    public BetweenQueryExp() {
    }
    public BetweenQueryExp(ValueExp v1, ValueExp v2, ValueExp v3) {
        exp1  = v1;
        exp2  = v2;
        exp3  = v3;
    }
    public ValueExp getCheckedValue()  {
        return exp1;
    }
    public ValueExp getLowerBound()  {
        return exp2;
    }
    public ValueExp getUpperBound()  {
        return exp3;
    }
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        ValueExp val1 = exp1.apply(name);
        ValueExp val2 = exp2.apply(name);
        ValueExp val3 = exp3.apply(name);
        boolean numeric = val1 instanceof NumericValueExp;
        if (numeric) {
            if (((NumericValueExp)val1).isLong()) {
                long lval1 = ((NumericValueExp)val1).longValue();
                long lval2 = ((NumericValueExp)val2).longValue();
                long lval3 = ((NumericValueExp)val3).longValue();
                return lval2 <= lval1 && lval1 <= lval3;
            } else {
                double dval1 = ((NumericValueExp)val1).doubleValue();
                double dval2 = ((NumericValueExp)val2).doubleValue();
                double dval3 = ((NumericValueExp)val3).doubleValue();
                return dval2 <= dval1 && dval1 <= dval3;
            }
        } else {
            String sval1 = ((StringValueExp)val1).getValue();
            String sval2 = ((StringValueExp)val2).getValue();
            String sval3 = ((StringValueExp)val3).getValue();
            return sval2.compareTo(sval1) <= 0 && sval1.compareTo(sval3) <= 0;
        }
    }
    @Override
    public String toString()  {
        return "(" + exp1 + ") between (" + exp2 + ") and (" + exp3 + ")";
    }
}
