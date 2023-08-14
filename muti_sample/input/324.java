class InQueryExp extends QueryEval implements QueryExp {
    private static final long serialVersionUID = -5801329450358952434L;
    private ValueExp val;
    private ValueExp[]  valueList;
    public InQueryExp() {
    }
    public InQueryExp(ValueExp v1, ValueExp items[]) {
        val       = v1;
        valueList = items;
    }
    public ValueExp getCheckedValue()  {
        return val;
    }
    public ValueExp[] getExplicitValues()  {
        return valueList;
    }
    public boolean apply(ObjectName name)
    throws BadStringOperationException, BadBinaryOpValueExpException,
        BadAttributeValueExpException, InvalidApplicationException  {
        if (valueList != null) {
            ValueExp v      = val.apply(name);
            boolean numeric = v instanceof NumericValueExp;
            for (ValueExp element : valueList) {
                element = element.apply(name);
                if (numeric) {
                    if (((NumericValueExp) element).doubleValue() ==
                        ((NumericValueExp) v).doubleValue()) {
                        return true;
                    }
                } else {
                    if (((StringValueExp) element).getValue().equals(
                        ((StringValueExp) v).getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public String toString()  {
        return val + " in (" + generateValueList() + ")";
    }
    private String generateValueList() {
        if (valueList == null || valueList.length == 0) {
            return "";
        }
        final StringBuilder result =
                new StringBuilder(valueList[0].toString());
        for (int i = 1; i < valueList.length; i++) {
            result.append(", ");
            result.append(valueList[i]);
        }
        return result.toString();
    }
 }
