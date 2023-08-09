class ReturnStatement extends Statement {
    Expression expr;
    public ReturnStatement(long where, Expression expr) {
        super(RETURN, where);
        this.expr = expr;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        vset = reach(env, vset);
        if (expr != null) {
            vset = expr.checkValue(env, ctx, vset, exp);
        }
        if (ctx.field.isInitializer()) {
            env.error(where, "return.inside.static.initializer");
            return DEAD_END;
        }
        if (ctx.field.getType().getReturnType().isType(TC_VOID)) {
            if (expr != null) {
                if (ctx.field.isConstructor()) {
                    env.error(where, "return.with.value.constr", ctx.field);
                } else {
                    env.error(where, "return.with.value", ctx.field);
                }
                expr = null;
            }
        } else {
            if (expr == null) {
                env.error(where, "return.without.value", ctx.field);
            } else {
                expr = convert(env, ctx, ctx.field.getType().getReturnType(), expr);
            }
        }
        CheckContext mctx = ctx.getReturnContext();
        if (mctx != null) {
            mctx.vsBreak = mctx.vsBreak.join(vset);
        }
        CheckContext exitctx = ctx.getTryExitContext();
        if (exitctx != null) {
            exitctx.vsTryExit = exitctx.vsTryExit.join(vset);
        }
        if (expr != null) {
            Node outerFinallyNode = null;
            for (Context c = ctx; c != null; c = c.prev) {
                if (c.node == null) {
                    continue;
                }
                if (c.node.op == METHOD) {
                    break;
                }
                if (c.node.op == SYNCHRONIZED) {
                    outerFinallyNode = c.node;
                    break;
                } else if (c.node.op == FINALLY
                           && ((CheckContext)c).vsContinue != null) {
                    outerFinallyNode = c.node;
                }
            }
            if (outerFinallyNode != null) {
                if (outerFinallyNode.op == FINALLY) {
                    ((FinallyStatement)outerFinallyNode).needReturnSlot = true;
                } else {
                    ((SynchronizedStatement)outerFinallyNode).needReturnSlot = true;
                }
            }
        }
        return DEAD_END;
    }
    public Statement inline(Environment env, Context ctx) {
        if (expr != null) {
            expr = expr.inlineValue(env, ctx);
        }
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + ((expr != null) ? expr.costInline(thresh, env, ctx) : 0);
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        Expression e = (expr != null) ? expr.copyInline(ctx) : null;
        if ((!valNeeded) && (e != null)) {
            Statement body[] = {
                new ExpressionStatement(where, e),
                new InlineReturnStatement(where, null)
            };
            return new CompoundStatement(where, body);
        }
        return new InlineReturnStatement(where, e);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        if (expr == null) {
            codeFinally(env, ctx, asm, null, null);
            asm.add(where, opc_return);
        } else {
            expr.codeValue(env, ctx, asm);
            codeFinally(env, ctx, asm, null, expr.type);
            asm.add(where, opc_ireturn + expr.type.getTypeCodeOffset());
        }
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("return");
        if (expr != null) {
            out.print(" ");
            expr.print(out);
        }
        out.print(";");
    }
}
