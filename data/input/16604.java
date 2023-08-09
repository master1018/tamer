public abstract class BytecodeWideable extends Bytecode {
  BytecodeWideable(Method method, int bci) {
    super(method, bci);
  }
  public boolean isWide() {
    int prevBci = bci() - 1;
    return (prevBci > -1 && method.getBytecodeOrBPAt(prevBci) == Bytecodes._wide);
  }
  public int getLocalVarIndex() {
    return (isWide()) ? (int) (0xFFFF & javaShortAt(1))
            : (int) (0xFF & javaByteAt(1));
  }
}
