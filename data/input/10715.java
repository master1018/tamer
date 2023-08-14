class ConditionalExpression extends BinaryExpression {
    Expression cond;
    public ConditionalExpression(long where, Expression cond, Expression left, Expression right) {
        super(COND, where, Type.tError, left, right);
        this.cond = cond;
    }
    public Expression order() {
        if (precedence() > cond.precedence()) {
            UnaryExpression e = (UnaryExpression)cond;
            cond = e.right;
            e.right = order();
            return e;
        }
        return this;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        ConditionVars cvars = cond.checkCondition(env, ctx, vset, exp);
        vset = left.checkValue(env, ctx, cvars.vsTrue, exp).join(
               right.checkValue(env, ctx, cvars.vsFalse, exp) );
        cond = convert(env, ctx, Type.tBoolean, cond);
        int tm = left.type.getTypeMask() | right.type.getTypeMask();
        if ((tm & TM_ERROR) != 0) {
            type = Type.tError;
            return vset;
        }
        if (left.type.equals(right.type)) {
            type = left.type;
        } else if ((tm & TM_DOUBLE) != 0) {
            type = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            type = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else if ((tm & TM_REFERENCE) != 0) {
            try {
                type = env.implicitCast(right.type, left.type)
                    ? left.type : right.type;
            } catch (ClassNotFound e) {
                type = Type.tError;
            }
        } else if (((tm & TM_CHAR) != 0) && left.fitsType(env, ctx, Type.tChar) && right.fitsType(env, ctx, Type.tChar)) {
            type = Type.tChar;
        } else if (((tm & TM_SHORT) != 0) && left.fitsType(env, ctx, Type.tShort) && right.fitsType(env, ctx, Type.tShort)) {
            type = Type.tShort;
        } else if (((tm & TM_BYTE) != 0) && left.fitsType(env, ctx, Type.tByte) && right.fitsType(env, ctx, Type.tByte)) {
            type = Type.tByte;
        } else {
            type = Type.tInt;
        }
        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
        return vset;
    }
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = cond.checkValue(env, ctx, vset, exp);
        cond = convert(env, ctx, Type.tBoolean, cond);
        return left.check(env, ctx, vset.copy(), exp).join(right.check(env, ctx, vset, exp));
    }
    public boolean isConstant() {
        return cond.isConstant() && left.isConstant() && right.isConstant();
    }
    Expression simplify() {
        if (cond.equals(true)) {
            return left;
        }
        if (cond.equals(false)) {
            return right;
        }
        return this;
    }
    public Expression inline(Environment env, Context ctx) {
        left = left.inline(env, ctx);
        right = right.inline(env, ctx);
        if ((left == null) && (right == null)) {
            return cond.inline(env, ctx);
        }
        if (left == null) {
            left = right;
            right = null;
            cond = new NotExpression(where, cond);
        }
        cond = cond.inlineValue(env, ctx);
        return simplify();
    }
    public Expression inlineValue(Environment env, Context ctx) {
        cond = cond.inlineValue(env, ctx);
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        return simplify();
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 +
            cond.costInline(thresh, env, ctx) +
            left.costInline(thresh, env, ctx) +
            ((right == null) ? 0 : right.costInline(thresh, env, ctx));
    }
    public Expression copyInline(Context ctx) {
        ConditionalExpression e = (ConditionalExpression)clone();
        e.cond = cond.copyInline(ctx);
        e.left = left.copyInline(ctx);
        e.right = (right == null) ? null : right.copyInline(ctx);
        return e;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        Label l2 = new Label();
        cond.codeBranch(env, ctx, asm, l1, false);
        left.codeValue(env, ctx, asm);
        asm.add(where, opc_goto, l2);
        asm.add(l1);
        right.codeValue(env, ctx, asm);
        asm.add(l2);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        cond.codeBranch(env, ctx, asm, l1, false);
        left.code(env, ctx, asm);
        if (right != null) {
            Label l2 = new Label();
            asm.add(where, opc_goto, l2);
            asm.add(l1);
            right.code(env, ctx, asm);
            asm.add(l2);
        } else {
            asm.add(l1);
        }
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        cond.print(out);
        out.print(" ");
        left.print(out);
        out.print(" ");
        if (right != null) {
            right.print(out);
        } else {
            out.print("<null>");
        }
        out.print(")");
    }
}
