class AssignRemainderExpression extends AssignOpExpression {
    public AssignRemainderExpression(long where, Expression left, Expression right) {
        super(ASGREM, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_irem + itype.getTypeCodeOffset());
    }
}
