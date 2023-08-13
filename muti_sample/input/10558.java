public class BytecodeLoadConstant extends BytecodeWithCPIndex {
  BytecodeLoadConstant(Method method, int bci) {
    super(method, bci);
  }
  public boolean hasCacheIndex() {
    return javaCode() != code();
  }
  public int index() {
    int i = javaCode() == Bytecodes._ldc ?
                 (int) (0xFF & javaByteAt(1))
               : (int) (0xFFFF & javaShortAt(1));
    if (hasCacheIndex()) {
      return (0xFFFF & VM.getVM().getBytes().swapShort((short) i));
    } else {
      return i;
    }
  }
  public int poolIndex() {
    int i = index();
    if (hasCacheIndex()) {
      ConstantPoolCache cpCache = method().getConstants().getCache();
      return cpCache.getEntryAt(i).getConstantPoolIndex();
    } else {
      return i;
    }
  }
  public int cacheIndex() {
    if (hasCacheIndex()) {
      return index();
    } else {
      return -1;  
    }
  }
  private Oop getCachedConstant() {
    int i = cacheIndex();
    if (i >= 0) {
      ConstantPoolCache cpCache = method().getConstants().getCache();
      return cpCache.getEntryAt(i).getF1();
    }
    return null;
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check load constant");
    }
  }
  public boolean isValid() {
    int jcode = javaCode();
    boolean codeOk = jcode == Bytecodes._ldc || jcode == Bytecodes._ldc_w ||
           jcode == Bytecodes._ldc2_w;
    if (! codeOk) return false;
    ConstantTag ctag = method().getConstants().getTagAt(index());
    if (jcode == Bytecodes._ldc2_w) {
       return (ctag.isDouble() || ctag.isLong()) ? true: false;
    } else {
       return (ctag.isUnresolvedString() || ctag.isString()
               || ctag.isUnresolvedKlass() || ctag.isKlass()
               || ctag.isMethodHandle() || ctag.isMethodType()
               || ctag.isInt() || ctag.isFloat())? true: false;
    }
  }
  public boolean isKlassConstant() {
    int jcode = javaCode();
    if (jcode == Bytecodes._ldc2_w) {
       return false;
    }
    ConstantTag ctag = method().getConstants().getTagAt(index());
    return ctag.isKlass() || ctag.isUnresolvedKlass();
  }
  public Object getKlass() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isKlassConstant(), "not a klass literal");
    }
    ConstantPool cpool = method().getConstants();
    int cpIndex = index();
    ConstantPool.CPSlot oop = cpool.getSlotAt(cpIndex);
    if (oop.isOop()) {
      return (Klass) oop.getOop();
    } else if (oop.isMetaData()) {
      return oop.getSymbol();
    } else {
       throw new RuntimeException("should not reach here");
    }
  }
  public static BytecodeLoadConstant at(Method method, int bci) {
    BytecodeLoadConstant b = new BytecodeLoadConstant(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeLoadConstant atCheck(Method method, int bci) {
    BytecodeLoadConstant b = new BytecodeLoadConstant(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeLoadConstant at(BytecodeStream bcs) {
    return new BytecodeLoadConstant(bcs.method(), bcs.bci());
  }
  public String getConstantValue() {
    ConstantPool cpool = method().getConstants();
    int cpIndex = poolIndex();
    ConstantTag ctag = cpool.getTagAt(cpIndex);
    if (ctag.isInt()) {
       return "<int " + Integer.toString(cpool.getIntAt(cpIndex)) +">";
    } else if (ctag.isLong()) {
       return "<long " + Long.toString(cpool.getLongAt(cpIndex)) + "L>";
    } else if (ctag.isFloat()) {
       return "<float " + Float.toString(cpool.getFloatAt(cpIndex)) + "F>";
    } else if (ctag.isDouble()) {
       return "<double " + Double.toString(cpool.getDoubleAt(cpIndex)) + "D>";
    } else if (ctag.isString() || ctag.isUnresolvedString()) {
       ConstantPool.CPSlot obj = cpool.getSlotAt(cpIndex);
       if (obj.isMetaData()) {
         Symbol sym = obj.getSymbol();
         return "<String \"" + sym.asString() + "\">";
       } else if (obj.isOop()) {
         return "<String \"" + OopUtilities.stringOopToString(obj.getOop()) + "\">";
       } else {
          throw new RuntimeException("should not reach here");
       }
    } else if (ctag.isKlass() || ctag.isUnresolvedKlass()) {
       ConstantPool.CPSlot obj = cpool.getSlotAt(cpIndex);
       if (obj.isOop()) {
         Klass k = (Klass) obj.getOop();
         return "<Class " + k.getName().asString() + "@" + k.getHandle() + ">";
       } else if (obj.isMetaData()) {
         Symbol sym = obj.getSymbol();
         return "<Class " + sym.asString() + ">";
       } else {
          throw new RuntimeException("should not reach here");
       }
    } else if (ctag.isMethodHandle()) {
       Oop x = getCachedConstant();
       int refidx = cpool.getMethodHandleIndexAt(cpIndex);
       int refkind = cpool.getMethodHandleRefKindAt(cpIndex);
       return "<MethodHandle kind=" + Integer.toString(refkind) +
           " ref=" + Integer.toString(refidx)
           + (x == null ? "" : " @" + x.getHandle()) + ">";
    } else if (ctag.isMethodType()) {
       Oop x = getCachedConstant();
       int refidx = cpool.getMethodTypeIndexAt(cpIndex);
       return "<MethodType " + cpool.getSymbolAt(refidx).asString()
           + (x == null ? "" : " @" + x.getHandle()) + ">";
    } else {
       if (Assert.ASSERTS_ENABLED) {
         Assert.that(false, "invalid load constant type");
       }
       return null;
    }
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(poolIndex()));
    if (hasCacheIndex()) {
       buf.append('(');
       buf.append(Integer.toString(cacheIndex()));
       buf.append(')');
    }
    buf.append(spaces);
    buf.append(getConstantValue());
    if (code() != javaCode()) {
       buf.append(spaces);
       buf.append('[');
       buf.append(getBytecodeName());
       buf.append(']');
    }
    return buf.toString();
  }
}
