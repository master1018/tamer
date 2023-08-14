class LengthExpression extends UnaryExpression {
    public LengthExpression(long where, Expression right) {
        super(LENGTH, where, Type.tInt, right);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = right.checkValue(env, ctx, vset, exp);
        if (!right.type.isType(TC_ARRAY)) {
            env.error(where, "invalid.length", right.type);
        }
        return vset;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        asm.add(where, opc_arraylength);
    }
}
