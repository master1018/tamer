public class BitwiseNot<T extends Number> extends UnaryFunctor<T, T> {
    static final long serialVersionUID = 6788747054561165216L;
    private transient IntegerArithmetic<T> _math;
    private Class<T> _type;
    public BitwiseNot(Class<T> c) {
        _type = c;
        getMath();
    }
    public Class<T> getType() {
        return _type;
    }
    public T fn(T x) {
        try {
            return getMath().not(x);
        } catch (ClassCastException ex) {
            String msg = "ClassCastException: Cannot compute ~{0}[{1}]";
            String err = MessageFormat.format(msg, new Object[] { x.getClass(), x });
            throw new EvaluationException(err, ex);
        }
    }
    public void accept(net.sf.jga.fn.Visitor v) {
        if (v instanceof BitwiseNot.Visitor) ((BitwiseNot.Visitor) v).visit(this); else v.visit(this);
    }
    private IntegerArithmetic<T> getMath() {
        if (_math == null) {
            _math = ArithmeticFactory.getIntegralArithmetic(_type);
            if (_math == null) {
                String msg = "No implementation of IntegerArithmetic registered for {0}";
                throw new IllegalArgumentException(MessageFormat.format(msg, new Object[] { _type }));
            }
        }
        return _math;
    }
    public String toString() {
        return "BitwiseNot";
    }
    public interface Visitor extends net.sf.jga.fn.Visitor {
        public void visit(BitwiseNot<? extends Number> host);
    }
}
