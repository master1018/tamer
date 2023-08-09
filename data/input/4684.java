class AssignOpExpression extends BinaryAssignExpression {
    protected Type itype;       
    final int NOINC = Integer.MAX_VALUE;
    protected FieldUpdater updater = null;   
    public AssignOpExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left, right);
    }
    final void selectType(Environment env, Context ctx, int tm) {
        Type rtype = null;      
        switch(op) {
            case ASGADD:
                if (left.type == Type.tString) {
                    if (right.type == Type.tVoid) {
                        env.error(where, "incompatible.type",
                                  opNames[op], Type.tVoid, Type.tString);
                        type = Type.tError;
                    } else {
                        type = itype = Type.tString;
                    }
                    return;
                }
            case ASGDIV: case ASGMUL: case ASGSUB: case ASGREM:
                if ((tm & TM_DOUBLE) != 0) {
                    itype = Type.tDouble;
                } else if ((tm & TM_FLOAT) != 0) {
                    itype = Type.tFloat;
                } else if ((tm & TM_LONG) != 0) {
                    itype = Type.tLong;
                } else {
                    itype = Type.tInt;
                }
                break;
            case ASGBITAND: case ASGBITOR: case ASGBITXOR:
                if ((tm & TM_BOOLEAN) != 0) {
                    itype = Type.tBoolean;
                } else if ((tm & TM_LONG) != 0) {
                    itype = Type.tLong;
                } else {
                    itype = Type.tInt;
                }
                break;
            case ASGLSHIFT: case ASGRSHIFT: case ASGURSHIFT:
                rtype = Type.tInt;
                if (right.type.inMask(TM_INTEGER)) {
                    right = new ConvertExpression(where, Type.tInt, right);
                }
                if (left.type == Type.tLong) {
                    itype = Type.tLong;
                } else {
                    itype = Type.tInt;
                }
                break;
            default:
                throw new CompilerError("Bad assignOp type: " + op);
        }
        if (rtype == null) {
            rtype = itype;
        }
        right = convert(env, ctx, rtype, right);
        type = left.type;
    }
    int getIncrement() {
        if ((left.op == IDENT) && type.isType(TC_INT) && (right.op == INTVAL))
            if ((op == ASGADD) || (op == ASGSUB))
                if (((IdentifierExpression)left).field.isLocal()) {
                    int val = ((IntExpression)right).value;
                    if (op == ASGSUB)
                        val = -val;
                    if (val == (short)val)
                        return val;
                }
        return NOINC;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = left.checkAssignOp(env, ctx, vset, exp, this);
        vset = right.checkValue(env, ctx, vset, exp);
        int tm = left.type.getTypeMask() | right.type.getTypeMask();
        if ((tm & TM_ERROR) != 0) {
            return vset;
        }
        selectType(env, ctx, tm);
        if (!type.isType(TC_ERROR)) {
            convert(env, ctx, itype, left);
        }
        updater = left.getUpdater(env, ctx);  
        return vset;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        if (updater != null) {
            updater = updater.inline(env, ctx);
        }
        return this;
    }
    public Expression copyInline(Context ctx) {
        AssignOpExpression e = (AssignOpExpression)clone();
        e.left = left.copyInline(ctx);
        e.right = right.copyInline(ctx);
        if (updater != null) {
            e.updater = updater.copyInline(ctx);
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (updater == null) {
            return (getIncrement() != NOINC)
                ? 3
                : right.costInline(thresh, env, ctx) +
                      left.costInline(thresh, env, ctx) + 4;
        } else {
            return right.costInline(thresh, env, ctx) +
                updater.costInline(thresh, env, ctx, true) + 1;
        }
    }
    void code(Environment env, Context ctx, Assembler asm, boolean valNeeded) {
        int val = getIncrement();
        if (val != NOINC && updater == null) {
            int v = ((LocalMember)((IdentifierExpression)left).field).number;
            int[] operands = { v, val };
            asm.add(where, opc_iinc, operands);
            if (valNeeded) {
                left.codeValue(env, ctx, asm);
            }
            return;
        }
        if (updater == null) {
            int depth = left.codeLValue(env, ctx, asm);
            codeDup(env, ctx, asm, depth, 0);
            left.codeLoad(env, ctx, asm);
            codeConversion(env, ctx, asm, left.type, itype);
            right.codeValue(env, ctx, asm);
            codeOperation(env, ctx, asm);
            codeConversion(env, ctx, asm, itype, type);
            if (valNeeded) {
                codeDup(env, ctx, asm, type.stackSize(), depth);
            }
            left.codeStore(env, ctx, asm);
        } else {
            updater.startUpdate(env, ctx, asm, false);
            codeConversion(env, ctx, asm, left.type, itype);
            right.codeValue(env, ctx, asm);
            codeOperation(env, ctx, asm);
            codeConversion(env, ctx, asm, itype, type);
            updater.finishUpdate(env, ctx, asm, valNeeded);
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        code(env, ctx, asm, true);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        code(env, ctx, asm, false);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        left.print(out);
        out.print(" ");
        right.print(out);
        out.print(")");
    }
}
