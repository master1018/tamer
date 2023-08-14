class BitNotExpression extends UnaryExpression {
    public BitNotExpression(long where, Expression right) {
        super(BITNOT, where, right.type, right);
    }
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        right = convert(env, ctx, type, right);
    }
    Expression eval(int a) {
        return new IntExpression(where, ~a);
    }
    Expression eval(long a) {
        return new LongExpression(where, ~a);
    }
    Expression simplify() {
        if (right.op == BITNOT) {
            return ((BitNotExpression)right).right;
        }
        return this;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        if (type.isType(TC_INT)) {
            asm.add(where, opc_ldc, new Integer(-1));
            asm.add(where, opc_ixor);
        } else {
            asm.add(where, opc_ldc2_w, new Long(-1));
            asm.add(where, opc_lxor);
        }
    }
}
