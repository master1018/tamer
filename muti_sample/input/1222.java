class BinaryExpression extends UnaryExpression {
    Expression left;
    BinaryExpression(int op, long where, Type type, Expression left, Expression right) {
        super(op, where, type, right);
        this.left = left;
    }
    public Expression order() {
        if (precedence() > left.precedence()) {
            UnaryExpression e = (UnaryExpression)left;
            left = e.right;
            e.right = order();
            return e;
        }
        return this;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = left.checkValue(env, ctx, vset, exp);
        vset = right.checkValue(env, ctx, vset, exp);
        int tm = left.type.getTypeMask() | right.type.getTypeMask();
        if ((tm & TM_ERROR) != 0) {
            return vset;
        }
        selectType(env, ctx, tm);
        if (type.isType(TC_ERROR)) {
            env.error(where, "invalid.args", opNames[op]);
        }
        return vset;
    }
    public boolean isConstant() {
        switch (op) {
        case MUL:
        case DIV:
        case REM:
        case ADD:
        case SUB:
        case LSHIFT:
        case RSHIFT:
        case URSHIFT:
        case LT:
        case LE:
        case GT:
        case GE:
        case EQ:
        case NE:
        case BITAND:
        case BITXOR:
        case BITOR:
        case AND:
        case OR:
            return left.isConstant() && right.isConstant();
        }
        return false;
    }
    Expression eval(int a, int b) {
        return this;
    }
    Expression eval(long a, long b) {
        return this;
    }
    Expression eval(float a, float b) {
        return this;
    }
    Expression eval(double a, double b) {
        return this;
    }
    Expression eval(boolean a, boolean b) {
        return this;
    }
    Expression eval(String a, String b) {
        return this;
    }
    Expression eval() {
        if (left.op == right.op) {
            switch (left.op) {
              case BYTEVAL:
              case CHARVAL:
              case SHORTVAL:
              case INTVAL:
                return eval(((IntegerExpression)left).value, ((IntegerExpression)right).value);
              case LONGVAL:
                return eval(((LongExpression)left).value, ((LongExpression)right).value);
              case FLOATVAL:
                return eval(((FloatExpression)left).value, ((FloatExpression)right).value);
              case DOUBLEVAL:
                return eval(((DoubleExpression)left).value, ((DoubleExpression)right).value);
              case BOOLEANVAL:
                return eval(((BooleanExpression)left).value, ((BooleanExpression)right).value);
              case STRINGVAL:
                return eval(((StringExpression)left).value, ((StringExpression)right).value);
            }
        }
        return this;
    }
    public Expression inline(Environment env, Context ctx) {
        left = left.inline(env, ctx);
        right = right.inline(env, ctx);
        return (left == null) ? right : new CommaExpression(where, left, right);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        try {
            return eval().simplify();
        } catch (ArithmeticException e) {
            return this;
        }
    }
    public Expression copyInline(Context ctx) {
        BinaryExpression e = (BinaryExpression)clone();
        if (left != null) {
            e.left = left.copyInline(ctx);
        }
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + ((left != null) ? left.costInline(thresh, env, ctx) : 0) +
                   ((right != null) ? right.costInline(thresh, env, ctx) : 0);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        throw new CompilerError("codeOperation: " + opNames[op]);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        if (type.isType(TC_BOOLEAN)) {
            Label l1 = new Label();
            Label l2 = new Label();
            codeBranch(env, ctx, asm, l1, true);
            asm.add(true, where, opc_ldc, new Integer(0));
            asm.add(true, where, opc_goto, l2);
            asm.add(l1);
            asm.add(true, where, opc_ldc, new Integer(1));
            asm.add(l2);
        } else {
            left.codeValue(env, ctx, asm);
            right.codeValue(env, ctx, asm);
            codeOperation(env, ctx, asm);
        }
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        if (left != null) {
            left.print(out);
        } else {
            out.print("<null>");
        }
        out.print(" ");
        if (right != null) {
            right.print(out);
        } else {
            out.print("<null>");
        }
        out.print(")");
    }
}
