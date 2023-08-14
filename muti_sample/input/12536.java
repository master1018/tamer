class ConvertExpression extends UnaryExpression {
    public ConvertExpression(long where, Type type, Expression right) {
        super(CONVERT, where, type, right);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        return right.checkValue(env, ctx, vset, exp);
    }
    Expression simplify() {
        switch (right.op) {
          case BYTEVAL:
          case CHARVAL:
          case SHORTVAL:
          case INTVAL: {
            int value = ((IntegerExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case LONGVAL: {
            long value = ((LongExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case FLOATVAL: {
            float value = ((FloatExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case DOUBLEVAL: {
            double value = ((DoubleExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
            }
            break;
          }
        }
        return this;
    }
    public boolean equals(int i) {
        return right.equals(i);
    }
    public boolean equals(boolean b) {
        return right.equals(b);
    }
    public Expression inline(Environment env, Context ctx) {
        if (right.type.inMask(TM_REFERENCE) && type.inMask(TM_REFERENCE)) {
            try {
                if (!env.implicitCast(right.type, type))
                    return inlineValue(env, ctx);
            } catch (ClassNotFound e) {
                throw new CompilerError(e);
            }
        }
        return super.inline(env, ctx);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        codeConversion(env, ctx, asm, right.type, type);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " " + type.toString() + " ");
        right.print(out);
        out.print(")");
    }
}
