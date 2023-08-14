class CastExpression extends BinaryExpression {
    public CastExpression(long where, Expression left, Expression right) {
        super(CAST, where, left.type, left, right);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        type = left.toType(env, ctx);
        vset = right.checkValue(env, ctx, vset, exp);
        if (type.isType(TC_ERROR) || right.type.isType(TC_ERROR)) {
            return vset;
        }
        if (type.equals(right.type)) {
            return vset;
        }
        try {
            if (env.explicitCast(right.type, type)) {
                right = new ConvertExpression(where, type, right);
                return vset;
            }
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
        }
        env.error(where, "invalid.cast", right.type, type);
        return vset;
    }
    public boolean isConstant() {
        if (type.inMask(TM_REFERENCE) && !type.equals(Type.tString)) {
            return false;
        }
        return right.isConstant();
    }
    public Expression inline(Environment env, Context ctx) {
        return right.inline(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        return right.inlineValue(env, ctx);
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (ctx == null) {
            return 1 + right.costInline(thresh, env, ctx);
        }
        ClassDefinition sourceClass = ctx.field.getClassDefinition();
        try {
            if (left.type.isType(TC_ARRAY) ||
                 sourceClass.permitInlinedAccess(env,
                                  env.getClassDeclaration(left.type)))
                return 1 + right.costInline(thresh, env, ctx);
        } catch (ClassNotFound e) {
        }
        return thresh;
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        if (type.isType(TC_ERROR)) {
            left.print(out);
        } else {
            out.print(type);
        }
        out.print(" ");
        right.print(out);
        out.print(")");
    }
}
