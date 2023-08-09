public class LivenessPath {
  LivenessPath() {
    stack = new Stack();
  }
  public int size() {
    return stack.size();
  }
  public LivenessPathElement get(int index) throws ArrayIndexOutOfBoundsException {
    return (LivenessPathElement) stack.get(index);
  }
  public void printOn(PrintStream tty) {
    for (int j = 0; j < size(); j++) {
      LivenessPathElement el = get(j);
      tty.print("  - ");
      if (el.getObj() != null) {
        Oop.printOopValueOn(el.getObj(), tty);
      }
      if (el.getField() != null) {
        if (el.getObj() != null) {
          tty.print(", field ");
        }
        tty.print(el.getField().getName());
      }
      tty.println();
    }
  }
  boolean isComplete() {
    if (size() == 0)
      return false;
    return peek().isRoot();
  }
  LivenessPathElement peek() {
    return (LivenessPathElement) stack.peek();
  }
  void push(LivenessPathElement el) {
    stack.push(el);
  }
  void pop() {
    stack.pop();
  }
  LivenessPath copy() {
    LivenessPath dup = new LivenessPath();
    for (int i = 0; i < stack.size(); i++) {
      dup.stack.push(stack.get(i));
    }
    return dup;
  }
  private Stack stack;
}
