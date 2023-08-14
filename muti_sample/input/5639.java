public abstract class Operator {
    private static int nextOrdinal = 0;
    private static HashMap<String, Operator> map = new HashMap<String, Operator>();
    private final String name;
    private final int ordinal = nextOrdinal++;
    private Operator(String name) {
        this.name = name;
        map.put(name, this);
    }
    protected abstract double eval(double x, double y);
    public static final Operator PLUS = new Operator("+") {
        protected double eval(double x, double y) {
            return x + y;
        }
    };
    public static final Operator MINUS = new Operator("-") {
        protected double eval(double x, double y) {
            return x - y;
        }
    };
    public static final Operator DIVIDE = new Operator("/") {
        protected double eval(double x, double y) {
            if (y == 0) {
                return Double.NaN;
            }
            return x / y;
        }
    };
    public static final Operator MULTIPLY = new Operator("*") {
        protected double eval(double x, double y) {
            return x * y;
        }
    };
    public String toString() {
        return name;
    }
    public static Operator toOperator(String s) {
        return map.get(s);
    }
    protected static Set keySet() {
        return map.keySet();
    }
}
