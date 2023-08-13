class InlineMethodExpression extends Expression {
    MemberDefinition field;
    Statement body;
    InlineMethodExpression(long where, Type type, MemberDefinition field, Statement body) {
        super(INLINEMETHOD, where, type);
        this.field = field;
        this.body = body;
    }
    public Expression inline(Environment env, Context ctx) {
        body = body.inline(env, new Context(ctx, this));
        if (body == null) {
            return null;
        } else if (body.op == INLINERETURN) {
            Expression expr = ((InlineReturnStatement)body).expr;
            if (expr != null && type.isType(TC_VOID)) {
                throw new CompilerError("value on inline-void return");
            }
            return expr;
        } else {
            return this;
        }
    }
    public Expression inlineValue(Environment env, Context ctx) {
        return inline(env, ctx);
    }
    public Expression copyInline(Context ctx) {
        InlineMethodExpression e = (InlineMethodExpression)clone();
        if (body != null) {
            e.body = body.copyInline(ctx, true);
        }
        return e;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        super.code(env, ctx, asm);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        body.code(env, newctx, asm);
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + "\n");
        body.print(out, 1);
        out.print(")");
    }
}
