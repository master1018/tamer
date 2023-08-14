class SwitchStatement extends Statement {
    Expression expr;
    Statement args[];
    public SwitchStatement(long where, Expression expr, Statement args[]) {
        super(SWITCH, where);
        this.expr = expr;
        this.args = args;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        vset = expr.checkValue(env, newctx, reach(env, vset), exp);
        Type switchType = expr.type;
        expr = convert(env, newctx, Type.tInt, expr);
        Hashtable tab = new Hashtable();
        boolean hasDefault = false;
        Vset vs = DEAD_END;
        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if (s.op == CASE) {
                vs = s.check(env, newctx, vs.join(vset.copy()), exp);
                Expression lbl = ((CaseStatement)s).expr;
                if (lbl != null) {
                    if (lbl instanceof IntegerExpression) {
                        Integer Ivalue =
                            (Integer)(((IntegerExpression)lbl).getValue());
                        int ivalue = Ivalue.intValue();
                        if (tab.get(lbl) != null) {
                            env.error(s.where, "duplicate.label", Ivalue);
                        } else {
                            tab.put(lbl, s);
                            boolean overflow;
                            switch (switchType.getTypeCode()) {
                                case TC_BYTE:
                                    overflow = (ivalue != (byte)ivalue); break;
                                case TC_SHORT:
                                    overflow = (ivalue != (short)ivalue); break;
                                case TC_CHAR:
                                    overflow = (ivalue != (char)ivalue); break;
                                default:
                                    overflow = false;
                            }
                            if (overflow) {
                                env.error(s.where, "switch.overflow",
                                          Ivalue, switchType);
                            }
                        }
                    } else {
                        if (!lbl.isConstant() ||
                            lbl.getType() != Type.tInt) {
                            env.error(s.where, "const.expr.required");
                        }
                    }
                } else {
                    if (hasDefault) {
                        env.error(s.where, "duplicate.default");
                    }
                    hasDefault = true;
                }
            } else {
                vs = s.checkBlockStatement(env, newctx, vs, exp);
            }
        }
        if (!vs.isDeadEnd()) {
            newctx.vsBreak = newctx.vsBreak.join(vs);
        }
        if (hasDefault)
            vset = newctx.vsBreak;
        return ctx.removeAdditionalVars(vset);
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        expr = expr.inlineValue(env, ctx);
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                args[i] = args[i].inline(env, ctx);
            }
        }
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        SwitchStatement s = (SwitchStatement)clone();
        s.expr = expr.copyInline(ctx);
        s.args = new Statement[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                s.args[i] = args[i].copyInline(ctx, valNeeded);
            }
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = expr.costInline(thresh, env, ctx);
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            if (args[i] != null) {
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        expr.codeValue(env, newctx, asm);
        SwitchData sw = new SwitchData();
        boolean hasDefault = false;
        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if ((s != null) && (s.op == CASE)) {
                Expression e = ((CaseStatement)s).expr;
                if (e != null) {
                    sw.add(((IntegerExpression)e).value, new Label());
                }
                else {
                    hasDefault = true;
                }
            }
        }
        if (env.coverage())
            sw.initTableCase();
        asm.add(where, opc_tableswitch, sw);
        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if (s != null) {
                if (s.op == CASE) {
                    Expression e = ((CaseStatement)s).expr;
                    if (e != null) {
                        asm.add(sw.get(((IntegerExpression)e).value));
                        sw.addTableCase(((IntegerExpression)e).value, s.where);
                    } else {
                        asm.add(sw.getDefaultLabel());
                        sw.addTableDefault(s.where);
                    }
                } else {
                    s.code(env, newctx, asm);
                }
            }
        }
        if (!hasDefault) {
            asm.add(sw.getDefaultLabel());
        }
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("switch (");
        expr.print(out);
        out.print(") {\n");
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                printIndent(out, indent + 1);
                args[i].print(out, indent + 1);
                out.print("\n");
            }
        }
        printIndent(out, indent);
        out.print("}");
    }
}
