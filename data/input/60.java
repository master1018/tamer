public class RBTree {
  private RBNode root;
  private Comparator comparator;
  protected static final boolean DEBUGGING = true;
  protected static final boolean VERBOSE   = true;
  protected static final boolean REALLY_VERBOSE = false;
  public RBTree(Comparator comparator) {
    this.comparator = comparator;
  }
  public RBNode getRoot() {
    return root;
  }
  public void insertNode(RBNode x) {
    treeInsert(x);
    x.setColor(RBColor.RED);
    boolean shouldPropagate = x.update();
    if (DEBUGGING && REALLY_VERBOSE) {
      System.err.println("RBTree.insertNode");
    }
    RBNode propagateStart = x;
    while ((x != root) && (x.getParent().getColor() == RBColor.RED)) {
      if (x.getParent() == x.getParent().getParent().getLeft()) {
        RBNode y = x.getParent().getParent().getRight();
        if ((y != null) && (y.getColor() == RBColor.RED)) {
          if (DEBUGGING && REALLY_VERBOSE) {
            System.err.println("  Case 1/1");
          }
          x.getParent().setColor(RBColor.BLACK);
          y.setColor(RBColor.BLACK);
          x.getParent().getParent().setColor(RBColor.RED);
          x.getParent().update();
          x = x.getParent().getParent();
          shouldPropagate = x.update();
          propagateStart = x;
        } else {
          if (x == x.getParent().getRight()) {
            if (DEBUGGING && REALLY_VERBOSE) {
              System.err.println("  Case 1/2");
            }
            x = x.getParent();
            leftRotate(x);
          }
          if (DEBUGGING && REALLY_VERBOSE) {
            System.err.println("  Case 1/3");
          }
          x.getParent().setColor(RBColor.BLACK);
          x.getParent().getParent().setColor(RBColor.RED);
          shouldPropagate = rightRotate(x.getParent().getParent());
          propagateStart = x.getParent();
        }
      } else {
        RBNode y = x.getParent().getParent().getLeft();
        if ((y != null) && (y.getColor() == RBColor.RED)) {
          if (DEBUGGING && REALLY_VERBOSE) {
            System.err.println("  Case 2/1");
          }
          x.getParent().setColor(RBColor.BLACK);
          y.setColor(RBColor.BLACK);
          x.getParent().getParent().setColor(RBColor.RED);
          x.getParent().update();
          x = x.getParent().getParent();
          shouldPropagate = x.update();
          propagateStart = x;
        } else {
          if (x == x.getParent().getLeft()) {
            if (DEBUGGING && REALLY_VERBOSE) {
              System.err.println("  Case 2/2");
            }
            x = x.getParent();
            rightRotate(x);
          }
          if (DEBUGGING && REALLY_VERBOSE) {
            System.err.println("  Case 2/3");
          }
          x.getParent().setColor(RBColor.BLACK);
          x.getParent().getParent().setColor(RBColor.RED);
          shouldPropagate = leftRotate(x.getParent().getParent());
          propagateStart = x.getParent();
        }
      }
    }
    while (shouldPropagate && (propagateStart != root)) {
      if (DEBUGGING && REALLY_VERBOSE) {
        System.err.println("  Propagating");
      }
      propagateStart = propagateStart.getParent();
      shouldPropagate = propagateStart.update();
    }
    root.setColor(RBColor.BLACK);
    if (DEBUGGING) {
      verify();
    }
  }
  public void deleteNode(RBNode z) {
    RBNode y;
    if ((z.getLeft() == null) || (z.getRight() == null)) {
      y = z;
    } else {
      y = treeSuccessor(z);
    }
    RBNode x;
    if (y.getLeft() != null) {
      x = y.getLeft();
    } else {
      x = y.getRight();
    }
    RBNode xParent;
    if (x != null) {
      x.setParent(y.getParent());
      xParent = x.getParent();
    } else {
      xParent = y.getParent();
    }
    if (y.getParent() == null) {
      root = x;
    } else {
      if (y == y.getParent().getLeft()) {
        y.getParent().setLeft(x);
      } else {
        y.getParent().setRight(x);
      }
    }
    if (y != z) {
      z.copyFrom(y);
    }
    if (y.getColor() == RBColor.BLACK) {
      deleteFixup(x, xParent);
    }
    if (DEBUGGING) {
      verify();
    }
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    printFromNode(root, tty, 0);
  }
  protected Object getNodeValue(RBNode node) {
    return node.getData();
  }
  protected void verify() {
    verifyFromNode(root);
  }
  private void treeInsert(RBNode z) {
    RBNode y = null;
    RBNode x = root;
    while (x != null) {
      y = x;
      if (comparator.compare(getNodeValue(z), getNodeValue(x)) < 0) {
        x = x.getLeft();
      } else {
        x = x.getRight();
      }
    }
    z.setParent(y);
    if (y == null) {
      root = z;
    } else {
      if (comparator.compare(getNodeValue(z), getNodeValue(y)) < 0) {
        y.setLeft(z);
      } else {
        y.setRight(z);
      }
    }
  }
  private RBNode treeSuccessor(RBNode x) {
    if (x.getRight() != null) {
      return treeMinimum(x.getRight());
    }
    RBNode y = x.getParent();
    while ((y != null) && (x == y.getRight())) {
      x = y;
      y = y.getParent();
    }
    return y;
  }
  private RBNode treeMinimum(RBNode x) {
    while (x.getLeft() != null) {
      x = x.getLeft();
    }
    return x;
  }
  private boolean leftRotate(RBNode x) {
    RBNode y = x.getRight();
    x.setRight(y.getLeft());
    if (y.getLeft() != null) {
      y.getLeft().setParent(x);
    }
    y.setParent(x.getParent());
    if (x.getParent() == null) {
      root = y;
    } else {
      if (x == x.getParent().getLeft()) {
        x.getParent().setLeft(y);
      } else {
        x.getParent().setRight(y);
      }
    }
    y.setLeft(x);
    x.setParent(y);
    boolean res = x.update();
    res = y.update() || res;
    return res;
  }
  private boolean rightRotate(RBNode y) {
    RBNode x = y.getLeft();
    y.setLeft(x.getRight());
    if (x.getRight() != null) {
      x.getRight().setParent(y);
    }
    x.setParent(y.getParent());
    if (y.getParent() == null) {
      root = x;
    } else {
      if (y == y.getParent().getLeft()) {
        y.getParent().setLeft(x);
      } else {
        y.getParent().setRight(x);
      }
    }
    x.setRight(y);
    y.setParent(x);
    boolean res = y.update();
    res = x.update() || res;
    return res;
  }
  private void deleteFixup(RBNode x, RBNode xParent) {
    while ((x != root) && ((x == null) || (x.getColor() == RBColor.BLACK))) {
      if (x == xParent.getLeft()) {
        RBNode w = xParent.getRight();
        if (DEBUGGING) {
          if (w == null) {
            throw new RuntimeException("x's sibling should not be null");
          }
        }
        if (w.getColor() == RBColor.RED) {
          w.setColor(RBColor.BLACK);
          xParent.setColor(RBColor.RED);
          leftRotate(xParent);
          w = xParent.getRight();
        }
        if (((w.getLeft()  == null) || (w.getLeft().getColor()  == RBColor.BLACK)) &&
            ((w.getRight() == null) || (w.getRight().getColor() == RBColor.BLACK))) {
          w.setColor(RBColor.RED);
          x = xParent;
          xParent = x.getParent();
        } else {
          if ((w.getRight() == null) || (w.getRight().getColor() == RBColor.BLACK)) {
            w.getLeft().setColor(RBColor.BLACK);
            w.setColor(RBColor.RED);
            rightRotate(w);
            w = xParent.getRight();
          }
          w.setColor(xParent.getColor());
          xParent.setColor(RBColor.BLACK);
          if (w.getRight() != null) {
            w.getRight().setColor(RBColor.BLACK);
          }
          leftRotate(xParent);
          x = root;
          xParent = x.getParent();
        }
      } else {
        RBNode w = xParent.getLeft();
        if (DEBUGGING) {
          if (w == null) {
            throw new RuntimeException("x's sibling should not be null");
          }
        }
        if (w.getColor() == RBColor.RED) {
          w.setColor(RBColor.BLACK);
          xParent.setColor(RBColor.RED);
          rightRotate(xParent);
          w = xParent.getLeft();
        }
        if (((w.getRight() == null) || (w.getRight().getColor() == RBColor.BLACK)) &&
            ((w.getLeft()  == null) || (w.getLeft().getColor()  == RBColor.BLACK))) {
          w.setColor(RBColor.RED);
          x = xParent;
          xParent = x.getParent();
        } else {
          if ((w.getLeft() == null) || (w.getLeft().getColor() == RBColor.BLACK)) {
            w.getRight().setColor(RBColor.BLACK);
            w.setColor(RBColor.RED);
            leftRotate(w);
            w = xParent.getLeft();
          }
          w.setColor(xParent.getColor());
          xParent.setColor(RBColor.BLACK);
          if (w.getLeft() != null) {
            w.getLeft().setColor(RBColor.BLACK);
          }
          rightRotate(xParent);
          x = root;
          xParent = x.getParent();
        }
      }
    }
    if (x != null) {
      x.setColor(RBColor.BLACK);
    }
  }
  private int verifyFromNode(RBNode node) {
    if (node == null) {
      return 1;
    }
    if (!((node.getColor() == RBColor.RED) ||
          (node.getColor() == RBColor.BLACK))) {
      if (DEBUGGING) {
        System.err.println("Verify failed:");
        printOn(System.err);
      }
      throw new RuntimeException("Verify failed (1)");
    }
    if (node.getColor() == RBColor.RED) {
      if (node.getLeft() != null) {
        if (node.getLeft().getColor() != RBColor.BLACK) {
          if (DEBUGGING) {
            System.err.println("Verify failed:");
            printOn(System.err);
          }
          throw new RuntimeException("Verify failed (2)");
        }
      }
      if (node.getRight() != null) {
        if (node.getRight().getColor() != RBColor.BLACK) {
          if (DEBUGGING) {
            System.err.println("Verify failed:");
            printOn(System.err);
          }
          throw new RuntimeException("Verify failed (3)");
        }
      }
    }
    int i = verifyFromNode(node.getLeft());
    int j = verifyFromNode(node.getRight());
    if (i != j) {
      if (DEBUGGING) {
        System.err.println("Verify failed:");
        printOn(System.err);
      }
      throw new RuntimeException("Verify failed (4) (left black count = " +
                                 i + ", right black count = " + j + ")");
    }
    return i + ((node.getColor() == RBColor.RED) ? 0 : 1);
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
    tty.println(" " + getNodeValue(node) +
                ((node.getColor() == RBColor.RED) ? " (red)" : " (black)"));
    printFromNode(node.getLeft(), tty, indentDepth + 2);
    printFromNode(node.getRight(), tty, indentDepth + 2);
  }
  public static void main(String[] args) {
    int treeSize = 10000;
    int maxVal = treeSize;
    System.err.println("Building tree...");
    RBTree tree = new RBTree(new Comparator() {
        public int compare(Object o1, Object o2) {
          Integer i1 = (Integer) o1;
          Integer i2 = (Integer) o2;
          if (i1.intValue() < i2.intValue()) {
            return -1;
          } else if (i1.intValue() == i2.intValue()) {
            return 0;
          }
          return 1;
        }
      });
    Random rand = new Random(System.currentTimeMillis());
    for (int i = 0; i < treeSize; i++) {
      Integer val = new Integer(rand.nextInt(maxVal) + 1);
      try {
        tree.insertNode(new RBNode(val));
        if ((i > 0) && (i % 100 == 0)) {
          System.err.print(i + "...");
          System.err.flush();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        System.err.println("While inserting value " + val);
        tree.printOn(System.err);
        System.exit(1);
      }
    }
    System.err.println();
    System.err.println("Churning tree...");
    for (int i = 0; i < treeSize; i++) {
      if (DEBUGGING && VERBOSE) {
        System.err.println("Iteration " + i + ":");
        tree.printOn(System.err);
      }
      RBNode xParent = null;
      RBNode x = tree.getRoot();
      int depth = 0;
      while (x != null) {
        xParent = x;
        if (rand.nextBoolean()) {
          x = x.getLeft();
        } else {
          x = x.getRight();
        }
        ++depth;
      }
      int height = rand.nextInt(depth);
      if (DEBUGGING) {
        if (height >= depth) {
          throw new RuntimeException("bug in java.util.Random");
        }
      }
      while (height > 0) {
        xParent = xParent.getParent();
        --height;
      }
      if (DEBUGGING && VERBOSE) {
        System.err.println("(Removing value " + tree.getNodeValue(xParent) + ")");
      }
      tree.deleteNode(xParent);
      Integer newVal = new Integer(rand.nextInt(maxVal) + 1);
      if (DEBUGGING && VERBOSE) {
        System.err.println("(Inserting value " + newVal + ")");
      }
      tree.insertNode(new RBNode(newVal));
    }
    System.err.println("All tests passed.");
  }
}
