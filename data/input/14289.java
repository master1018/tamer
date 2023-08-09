class PostDecExpression extends IncDecExpression {
    public PostDecExpression(long where, Expression right) {
        super(POSTDEC, where, right);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, false, false, true);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, false, false, false);
    }
}
