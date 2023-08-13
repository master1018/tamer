class BinaryRelQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -5690656271650491000L;
    private int relOp;
    private ValueExp exp1;
    private ValueExp exp2;
    public BinaryRelQueryExp() {
    }
    public BinaryRelQueryExp(int op, ValueExp v1, ValueExp v2) {
        relOp = op;
        exp1  = v1;
        exp2  = v2;
    }
    public int getOperator()  {
        return relOp;
    }
    public ValueExp getLeftValue()  {
        return exp1;
    }
    public ValueExp getRightValue()  {
        return exp2;
    }
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        Object val1 = exp1.apply(name);
        Object val2 = exp2.apply(name);
        boolean numeric = val1 instanceof NumericValueExp;
        boolean bool = val1 instanceof BooleanValueExp;
        if (numeric) {
            if (((NumericValueExp)val1).isLong()) {
                long lval1 = ((NumericValueExp)val1).longValue();
                long lval2 = ((NumericValueExp)val2).longValue();
                switch (relOp) {
                case Query.GT:
                    return lval1 > lval2;
                case Query.LT:
                    return lval1 < lval2;
                case Query.GE:
                    return lval1 >= lval2;
                case Query.LE:
                    return lval1 <= lval2;
                case Query.EQ:
                    return lval1 == lval2;
                }
            } else {
                double dval1 = ((NumericValueExp)val1).doubleValue();
                double dval2 = ((NumericValueExp)val2).doubleValue();
                switch (relOp) {
                case Query.GT:
                    return dval1 > dval2;
                case Query.LT:
                    return dval1 < dval2;
                case Query.GE:
                    return dval1 >= dval2;
                case Query.LE:
                    return dval1 <= dval2;
                case Query.EQ:
                    return dval1 == dval2;
                }
            }
        } else if (bool) {
            boolean bval1 = ((BooleanValueExp)val1).getValue().booleanValue();
            boolean bval2 = ((BooleanValueExp)val2).getValue().booleanValue();
            switch (relOp) {
            case Query.GT:
                return bval1 && !bval2;
            case Query.LT:
                return !bval1 && bval2;
            case Query.GE:
                return bval1 || !bval2;
            case Query.LE:
                return !bval1 || bval2;
            case Query.EQ:
                return bval1 == bval2;
            }
        } else {
            String sval1 = ((StringValueExp)val1).getValue();
            String sval2 = ((StringValueExp)val2).getValue();
            switch (relOp) {
            case Query.GT:
                return sval1.compareTo(sval2) > 0;
            case Query.LT:
                return sval1.compareTo(sval2) < 0;
            case Query.GE:
                return sval1.compareTo(sval2) >= 0;
            case Query.LE:
                return sval1.compareTo(sval2) <= 0;
            case Query.EQ:
                return sval1.compareTo(sval2) == 0;
            }
        }
        return false;
    }
    @Override
    public String toString()  {
        return "(" + exp1 + ") " + relOpString() + " (" + exp2 + ")";
    }
    private String relOpString() {
        switch (relOp) {
        case Query.GT:
            return ">";
        case Query.LT:
            return "<";
        case Query.GE:
            return ">=";
        case Query.LE:
            return "<=";
        case Query.EQ:
            return "=";
        }
        return "=";
    }
 }
