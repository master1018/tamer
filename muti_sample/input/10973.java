public class IntervalTree extends RBTree {
  private Comparator endpointComparator;
  public IntervalTree(Comparator endpointComparator) {
    super(new IntervalComparator(endpointComparator));
    this.endpointComparator = endpointComparator;
  }
  public void insert(Interval interval, Object data) {
    IntervalNode node = new IntervalNode(interval, endpointComparator, data);
    insertNode(node);
  }
  public List findAllNodesIntersecting(Interval interval) {
    List retList = new ArrayList();
    searchForIntersectingNodesFrom((IntervalNode) getRoot(), interval, retList);
    return retList;
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    printFromNode(getRoot(), tty, 0);
  }
  protected Object getNodeValue(RBNode node) {
    return ((IntervalNode) node).getInterval();
  }
  protected void verify() {
    super.verify();
    verifyFromNode(getRoot());
  }
  private void verifyFromNode(RBNode node) {
    if (node == null) {
      return;
    }
    IntervalNode intNode = (IntervalNode) node;
    if (!intNode.getMaxEndpoint().equals(intNode.computeMaxEndpoint())) {
      if (DEBUGGING && VERBOSE) {
        print();
      }
      throw new RuntimeException("Node's max endpoint was not updated properly");
    }
    if (!intNode.getMinEndpoint().equals(intNode.computeMinEndpoint())) {
      if (DEBUGGING && VERBOSE) {
        print();
      }
      throw new RuntimeException("Node's min endpoint was not updated properly");
    }
    verifyFromNode(node.getLeft());
    verifyFromNode(node.getRight());
  }
  static class IntervalComparator implements Comparator {
    private Comparator endpointComparator;
    public IntervalComparator(Comparator endpointComparator) {
      this.endpointComparator = endpointComparator;
    }
    public int compare(Object o1, Object o2) {
      Interval i1 = (Interval) o1;
      Interval i2 = (Interval) o2;
      return endpointComparator.compare(i1.getLowEndpoint(), i2.getLowEndpoint());
    }
  }
  private void searchForIntersectingNodesFrom(IntervalNode node,
                                              Interval interval,
                                              List resultList) {
    if (node == null) {
      return;
    }
    IntervalNode left = (IntervalNode) node.getLeft();
    if ((left != null) &&
        (endpointComparator.compare(left.getMaxEndpoint(),
                                    interval.getLowEndpoint()) > 0)) {
      searchForIntersectingNodesFrom(left, interval, resultList);
    }
    if (node.getInterval().overlaps(interval, endpointComparator)) {
      resultList.add(node);
    }
    IntervalNode right = (IntervalNode) node.getRight();
    if ((right != null) &&
        (endpointComparator.compare(interval.getHighEndpoint(),
                                    right.getMinEndpoint()) > 0)) {
      searchForIntersectingNodesFrom(right, interval, resultList);
    }
  }
  private void printFromNode(RBNode node, PrintStream tty, int indentDepth) {
    for (int i = 0; i < indentDepth; i++) {
      tty.print(" ");
    }
    tty.print("-");
    if (node == null) {
      tty.println();
      return;
    }
    tty.println(" " + node +
                " (min " + ((IntervalNode) node).getMinEndpoint() +
                ", max " + ((IntervalNode) node).getMaxEndpoint() + ")" +
                ((node.getColor() == RBColor.RED) ? " (red)" : " (black)"));
    if (node.getLeft()  != null) printFromNode(node.getLeft(),  tty, indentDepth + 2);
    if (node.getRight() != null) printFromNode(node.getRight(), tty, indentDepth + 2);
  }
}
