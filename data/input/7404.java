class UnaryExpression extends Expression {
    Expression right;
    UnaryExpression(int op, long where, Type type, Expression right) {
        super(op, where, type);
        this.right = right;
    }
    public Expression order() {
        if (precedence() > right.precedence()) {
            UnaryExpression e = (UnaryExpression)right;
            right = e.right;
            e.right = order();
            return e;
        }
        return this;
    }
    void selectType(Environment env, Context ctx, int tm) {
        throw new CompilerError("selectType: " + opNames[op]);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = right.checkValue(env, ctx, vset, exp);
        int tm = right.type.getTypeMask();
        selectType(env, ctx, tm);
        if (((tm & TM_ERROR) == 0) && type.isType(TC_ERROR)) {
            env.error(where, "invalid.arg", opNames[op]);
        }
        return vset;
    }
    public boolean isConstant() {
        switch (op) {
        case POS:
        case NEG:
        case BITNOT:
        case NOT:
        case EXPR:
        case CONVERT: 
            return right.isConstant();
        }
        return false;
    }
    Expression eval(int a) {
        return this;
    }
    Expression eval(long a) {
        return this;
    }
    Expression eval(float a) {
        return this;
    }
    Expression eval(double a) {
        return this;
    }
    Expression eval(boolean a) {
        return this;
    }
    Expression eval(String a) {
        return this;
    }
    Expression eval() {
        switch (right.op) {
          case BYTEVAL:
          case CHARVAL:
          case SHORTVAL:
          case INTVAL:
            return eval(((IntegerExpression)right).value);
          case LONGVAL:
            return eval(((LongExpression)right).value);
          case FLOATVAL:
            return eval(((FloatExpression)right).value);
          case DOUBLEVAL:
            return eval(((DoubleExpression)right).value);
          case BOOLEANVAL:
            return eval(((BooleanExpression)right).value);
          case STRINGVAL:
            return eval(((StringExpression)right).value);
        }
        return this;
    }
    public Expression inline(Environment env, Context ctx) {
        return right.inline(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        right = right.inlineValue(env, ctx);
        try {
            return eval().simplify();
        } catch (ArithmeticException e) {
            return this;
        }
    }
    public Expression copyInline(Context ctx) {
        UnaryExpression e = (UnaryExpression)clone();
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + right.costInline(thresh, env, ctx);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        right.print(out);
        out.print(")");
    }
}
