public abstract class JavaVFrame extends VFrame {
  public abstract Method getMethod();
  public abstract int    getBCI();
  public abstract StackValueCollection getLocals();
  public abstract StackValueCollection getExpressions();
  public abstract List   getMonitors();    
  public boolean isJavaFrame() { return true; }
  JavaVFrame(Frame fr, RegisterMap regMap, JavaThread thread) {
    super(fr, regMap, thread);
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    super.printOn(tty);
    tty.print("\t");
    getMethod().printValueOn(tty);
    tty.println();
    tty.println("\tbci:\t" + getBCI());
    printStackValuesOn(tty, "locals",      getLocals());
    printStackValuesOn(tty, "expressions", getExpressions());
  }
  public void printActivation(int index) {
    printActivationOn(System.out, index);
  }
  public void printActivationOn(PrintStream tty, int index) {
    tty.print(index + " - ");
    printValueOn(tty);
    tty.println();
    if (VM.getVM().wizardMode()) {
      printOn(tty);
      tty.println();
    }
  }
  public void verify() {
  }
  public boolean equals(Object o) {
      if (o == null || !(o instanceof JavaVFrame)) {
          return false;
      }
      JavaVFrame other = (JavaVFrame) o;
      if (!getMethod().equals(other.getMethod())) {
          return false;
      }
      if (getBCI() != other.getBCI()) {
          return false;
      }
      if (! getFrame().getFP().equals(other.getFrame().getFP())) {
          return false;
      }
      return true;
  }
  public int hashCode() {
      return getMethod().hashCode() ^ getBCI() ^ getFrame().getFP().hashCode();
  }
  public boolean structuralCompare(JavaVFrame other) {
    if (!getMethod().equals(other.getMethod())) {
      return false;
    }
    if (getBCI() != other.getBCI()) {
      return false;
    }
    StackValueCollection locs      = getLocals();
    StackValueCollection otherLocs = other.getLocals();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(locs.size() == otherLocs.size(), "sanity check");
    }
    for (int i = 0; i < locs.size(); i++) {
      if (      isCompiledFrame() && (locs.get(i)).getType()      == BasicType.getTConflict()) continue;
      if (other.isCompiledFrame() && (otherLocs.get(i)).getType() == BasicType.getTConflict()) continue;
      if (!locs.get(i).equals(otherLocs.get(i))) {
        return false;
      }
    }
    StackValueCollection exprs      = getExpressions();
    StackValueCollection otherExprs = other.getExpressions();
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(exprs.size() == otherExprs.size(), "sanity check");
    }
    for (int i = 0; i < exprs.size(); i++) {
      if (!exprs.get(i).equals(otherExprs.get(i))) {
        return false;
      }
    }
    return true;
  }
  private void printStackValuesOn(PrintStream tty, String title, StackValueCollection values) {
    if (values.isEmpty()) {
      return;
    }
    tty.println("\t" + title + ":");
    for (int index = 0; index < values.size(); index++) {
      tty.print("\t" + index + "\t");
      values.get(index).printOn(tty);
      tty.println();
    }
  }
}
