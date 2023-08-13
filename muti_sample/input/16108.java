class PostIncExpression extends IncDecExpression {
    public PostIncExpression(long where, Expression right) {
        super(POSTINC, where, right);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, true, false, true);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        codeIncDec(env, ctx, asm, true, false, false);
    }
}
