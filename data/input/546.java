class PreIncExpression extends IncDecExpression {
    public PreIncExpression(long where, Expression right) {
        super(PREINC, where, right);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, true, true, true);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, true, true, false);
    }
}
