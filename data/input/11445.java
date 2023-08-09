public class ExpressionExecuter implements ExpressionEvaluator {
    private static final boolean debug =
            Boolean.getBoolean("ExpressionEvaluator.debug");
    private MonitoredVm vm;
    private HashMap<String, Object> map = new HashMap<String, Object>();
    ExpressionExecuter(MonitoredVm vm) {
        this.vm = vm;
    }
    public Object evaluate(Expression e) {
        if (e == null) {
            return null;
        }
        if (debug) {
            System.out.println("Evaluating expression: " + e);
        }
        if (e instanceof Literal) {
            return ((Literal)e).getValue();
        }
        if (e instanceof Identifier) {
            Identifier id = (Identifier)e;
            if (map.containsKey(id.getName())) {
                return map.get(id.getName());
            } else {
                Monitor m = (Monitor)id.getValue();
                Object v = m.getValue();
                map.put(id.getName(), v);
                return v;
            }
        }
        Expression l = e.getLeft();
        Expression r = e.getRight();
        Operator op = e.getOperator();
        if (op == null) {
            return evaluate(l);
        } else {
            Double lval = new Double(((Number)evaluate(l)).doubleValue());
            Double rval = new Double(((Number)evaluate(r)).doubleValue());
            double result = op.eval(lval.doubleValue(), rval.doubleValue());
            if (debug) {
                System.out.println("Performed Operation: " + lval + op + rval
                                   + " = " + result);
            }
            return new Double(result);
        }
    }
}
