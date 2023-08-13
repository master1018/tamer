class TryStatement extends Statement {
    Statement body;
    Statement args[];
    long arrayCloneWhere;       
    public TryStatement(long where, Statement body, Statement args[]) {
        super(TRY, where);
        this.body = body;
        this.args = args;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        try {
            vset = reach(env, vset);
            Hashtable newexp = new Hashtable();
            CheckContext newctx =  new CheckContext(ctx, this);
            Vset vs = body.check(env, newctx, vset.copy(), newexp);
            Vset cvs = Vset.firstDAandSecondDU(vset, vs.copy().join(newctx.vsTryExit));
            for (int i = 0 ; i < args.length ; i++) {
                vs = vs.join(args[i].check(env, newctx, cvs.copy(), exp));
            }
            for (int i = 1 ; i < args.length ; i++) {
                CatchStatement cs = (CatchStatement)args[i];
                if (cs.field == null) {
                    continue;
                }
                Type type = cs.field.getType();
                ClassDefinition def = env.getClassDefinition(type);
                for (int j = 0 ; j < i ; j++) {
                    CatchStatement cs2 = (CatchStatement)args[j];
                    if (cs2.field == null) {
                        continue;
                    }
                    Type t = cs2.field.getType();
                    ClassDeclaration c = env.getClassDeclaration(t);
                    if (def.subClassOf(env, c)) {
                        env.error(args[i].where, "catch.not.reached");
                        break;
                    }
                }
            }
            ClassDeclaration ignore1 = env.getClassDeclaration(idJavaLangError);
            ClassDeclaration ignore2 = env.getClassDeclaration(idJavaLangRuntimeException);
            for (int i = 0 ; i < args.length ; i++) {
                CatchStatement cs = (CatchStatement)args[i];
                if (cs.field == null) {
                    continue;
                }
                Type type = cs.field.getType();
                if (!type.isType(TC_CLASS)) {
                    continue;
                }
                ClassDefinition def = env.getClassDefinition(type);
                if (def.subClassOf(env, ignore1) || def.superClassOf(env, ignore1) ||
                    def.subClassOf(env, ignore2) || def.superClassOf(env, ignore2)) {
                    continue;
                }
                boolean ok = false;
                for (Enumeration e = newexp.keys() ; e.hasMoreElements() ; ) {
                    ClassDeclaration c = (ClassDeclaration)e.nextElement();
                    if (def.superClassOf(env, c) || def.subClassOf(env, c)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok && arrayCloneWhere != 0
                    && def.getName().toString().equals("java.lang.CloneNotSupportedException")) {
                    env.error(arrayCloneWhere, "warn.array.clone.supported", def.getName());
                }
                if (!ok) {
                    env.error(cs.where, "catch.not.thrown", def.getName());
                }
            }
            for (Enumeration e = newexp.keys() ; e.hasMoreElements() ; ) {
                ClassDeclaration c = (ClassDeclaration)e.nextElement();
                ClassDefinition def = c.getClassDefinition(env);
                boolean add = true;
                for (int i = 0 ; i < args.length ; i++) {
                    CatchStatement cs = (CatchStatement)args[i];
                    if (cs.field == null) {
                        continue;
                    }
                    Type type = cs.field.getType();
                    if (type.isType(TC_ERROR))
                        continue;
                    if (def.subClassOf(env, env.getClassDeclaration(type))) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    exp.put(c, newexp.get(c));
                }
            }
            return ctx.removeAdditionalVars(vs.join(newctx.vsBreak));
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
            return vset;
        }
    }
    public Statement inline(Environment env, Context ctx) {
        if (body != null) {
            body = body.inline(env, new Context(ctx, this));
        }
        if (body == null) {
            return null;
        }
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                args[i] = args[i].inline(env, new Context(ctx, this));
            }
        }
        return (args.length == 0) ? eliminate(env, body) : this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        TryStatement s = (TryStatement)clone();
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        s.args = new Statement[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                s.args[i] = args[i].copyInline(ctx, valNeeded);
            }
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx){
        return thresh;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        TryData td = new TryData();
        for (int i = 0 ; i < args.length ; i++) {
            Type t = ((CatchStatement)args[i]).field.getType();
            if (t.isType(TC_CLASS)) {
                td.add(env.getClassDeclaration(t));
            } else {
                td.add(t);
            }
        }
        asm.add(where, opc_try, td);
        if (body != null) {
            body.code(env, newctx, asm);
        }
        asm.add(td.getEndLabel());
        asm.add(where, opc_goto, newctx.breakLabel);
        for (int i = 0 ; i < args.length ; i++) {
            CatchData cd = td.getCatch(i);
            asm.add(cd.getLabel());
            args[i].code(env, newctx, asm);
            asm.add(where, opc_goto, newctx.breakLabel);
        }
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("try ");
        if (body != null) {
            body.print(out, indent);
        } else {
            out.print("<empty>");
        }
        for (int i = 0 ; i < args.length ; i++) {
            out.print(" ");
            args[i].print(out, indent);
        }
    }
}
