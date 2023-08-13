class ArrayExpression extends NaryExpression {
    public ArrayExpression(long where, Expression args[]) {
        super(ARRAY, where, Type.tError, null, args);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        env.error(where, "invalid.array.expr");
        return vset;
    }
    public Vset checkInitializer(Environment env, Context ctx, Vset vset, Type t, Hashtable exp) {
        if (!t.isType(TC_ARRAY)) {
            if (!t.isType(TC_ERROR)) {
                env.error(where, "invalid.array.init", t);
            }
            return vset;
        }
        type = t;
        t = t.getElementType();
        for (int i = 0 ; i < args.length ; i++) {
            vset = args[i].checkInitializer(env, ctx, vset, t, exp);
            args[i] = convert(env, ctx, t, args[i]);
        }
        return vset;
    }
    public Expression inline(Environment env, Context ctx) {
        Expression e = null;
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = args[i].inline(env, ctx);
            if (args[i] != null) {
                e = (e == null) ? args[i] : new CommaExpression(where, e, args[i]);
            }
        }
        return e;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = args[i].inlineValue(env, ctx);
        }
        return this;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        int t = 0;
        asm.add(where, opc_ldc, new Integer(args.length));
        switch (type.getElementType().getTypeCode()) {
          case TC_BOOLEAN:      asm.add(where, opc_newarray, new Integer(T_BOOLEAN));   break;
          case TC_BYTE:         asm.add(where, opc_newarray, new Integer(T_BYTE));      break;
          case TC_SHORT:        asm.add(where, opc_newarray, new Integer(T_SHORT));     break;
          case TC_CHAR:         asm.add(where, opc_newarray, new Integer(T_CHAR));      break;
          case TC_INT:          asm.add(where, opc_newarray, new Integer(T_INT));       break;
          case TC_LONG:         asm.add(where, opc_newarray, new Integer(T_LONG));      break;
          case TC_FLOAT:        asm.add(where, opc_newarray, new Integer(T_FLOAT));     break;
          case TC_DOUBLE:       asm.add(where, opc_newarray, new Integer(T_DOUBLE));    break;
          case TC_ARRAY:
            asm.add(where, opc_anewarray, type.getElementType());
            break;
          case TC_CLASS:
            asm.add(where, opc_anewarray, env.getClassDeclaration(type.getElementType()));
            break;
          default:
            throw new CompilerError("codeValue");
        }
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i].equalsDefault()) continue;
            asm.add(where, opc_dup);
            asm.add(where, opc_ldc, new Integer(i));
            args[i].codeValue(env, ctx, asm);
            switch (type.getElementType().getTypeCode()) {
              case TC_BOOLEAN:
              case TC_BYTE:
                asm.add(where, opc_bastore);
                break;
              case TC_CHAR:
                asm.add(where, opc_castore);
                break;
              case TC_SHORT:
                asm.add(where, opc_sastore);
                break;
              default:
                asm.add(where, opc_iastore + type.getElementType().getTypeCodeOffset());
            }
        }
    }
}
