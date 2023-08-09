class PreDecExpression extends IncDecExpression {
    public PreDecExpression(long where, Expression right) {
        super(PREDEC, where, right);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, false, true, true);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, false, true, false);
    }
}
