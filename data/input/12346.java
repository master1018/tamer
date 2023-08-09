class BooleanExpression extends ConstantExpression {
    boolean value;
    public BooleanExpression(long where, boolean value) {
        super(BOOLEANVAL, where, Type.tBoolean);
        this.value = value;
    }
    public Object getValue() {
        return new Integer(value ? 1 : 0);
    }
    public boolean equals(boolean b) {
        return value == b;
    }
    public boolean equalsDefault() {
        return !value;
    }
    public void checkCondition(Environment env, Context ctx,
                               Vset vset, Hashtable exp, ConditionVars cvars) {
        if (value) {
            cvars.vsFalse = Vset.DEAD_END;
            cvars.vsTrue = vset;
        } else {
            cvars.vsFalse = vset;
            cvars.vsTrue = Vset.DEAD_END;
        }
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (value == whenTrue) {
            asm.add(where, opc_goto, lbl);
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, new Integer(value ? 1 : 0));
    }
    public void print(PrintStream out) {
        out.print(value ? "true" : "false");
    }
}
