public class ProgressiveHeapVisitor implements HeapVisitor {
  private HeapVisitor userHeapVisitor;
  private HeapProgressThunk thunk;
  private long usedSize;
  private long visitedSize;
  private double lastNotificationFraction;
  private static double MINIMUM_NOTIFICATION_FRACTION = 0.01;
  public ProgressiveHeapVisitor(HeapVisitor userHeapVisitor,
                                HeapProgressThunk thunk) {
    this.userHeapVisitor = userHeapVisitor;
    this.thunk = thunk;
  }
  public void prologue(long usedSize) {
    this.usedSize = usedSize;
    visitedSize = 0;
    userHeapVisitor.prologue(usedSize);
  }
  public boolean doObj(Oop obj) {
    userHeapVisitor.doObj(obj);
    visitedSize += obj.getObjectSize();
    double curFrac = (double) visitedSize / (double) usedSize;
    if (curFrac > lastNotificationFraction + MINIMUM_NOTIFICATION_FRACTION) {
      thunk.heapIterationFractionUpdate(curFrac);
      lastNotificationFraction = curFrac;
    }
    return false;
  }
  public void epilogue() {
    userHeapVisitor.epilogue();
    thunk.heapIterationComplete();
  }
}
