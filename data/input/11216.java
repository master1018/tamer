class InstanceOfExpression extends BinaryExpression {
    public InstanceOfExpression(long where, Expression left, Expression right) {
        super(INSTANCEOF, where, Type.tBoolean, left, right);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = left.checkValue(env, ctx, vset, exp);
        right = new TypeExpression(right.where, right.toType(env, ctx));
        if (right.type.isType(TC_ERROR) || left.type.isType(TC_ERROR)) {
            return vset;
        }
        if (!right.type.inMask(TM_CLASS|TM_ARRAY)) {
            env.error(right.where, "invalid.arg.type", right.type, opNames[op]);
            return vset;
        }
        try {
            if (!env.explicitCast(left.type, right.type)) {
                env.error(where, "invalid.instanceof", left.type, right.type);
            }
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
        }
        return vset;
    }
    public Expression inline(Environment env, Context ctx) {
        return left.inline(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (ctx == null) {
            return 1 + left.costInline(thresh, env, ctx);
        }
        ClassDefinition sourceClass = ctx.field.getClassDefinition();
        try {
            if (right.type.isType(TC_ARRAY) ||
                 sourceClass.permitInlinedAccess(env, env.getClassDeclaration(right.type)))
                return 1 + left.costInline(thresh, env, ctx);
        } catch (ClassNotFound e) {
        }
        return thresh;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        left.codeValue(env, ctx, asm);
        if (right.type.isType(TC_CLASS)) {
            asm.add(where, opc_instanceof, env.getClassDeclaration(right.type));
        } else {
            asm.add(where, opc_instanceof, right.type);
        }
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        codeValue(env, ctx, asm);
        asm.add(where, whenTrue ? opc_ifne : opc_ifeq, lbl, whenTrue);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        left.code(env, ctx, asm);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        left.print(out);
        out.print(" ");
        if (right.op == TYPE) {
            out.print(right.type.toString());
        } else {
            right.print(out);
        }
        out.print(")");
    }
}
