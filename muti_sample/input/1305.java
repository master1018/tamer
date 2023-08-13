public class RBNode {
  private Object data;
  private RBNode left;
  private RBNode right;
  private RBNode parent;
  private RBColor color;
  public RBNode(Object data) {
    this.data  = data;
    color = RBColor.RED;
  }
  public Object getData() {
    return data;
  }
  public void copyFrom(RBNode arg) {
    this.data  = arg.data;
  }
  public boolean update() {
    return false;
  }
  public RBColor getColor()            { return color;         }
  public void setColor(RBColor color)  { this.color = color;   }
  public RBNode getParent()            { return parent;        }
  public void setParent(RBNode parent) { this.parent = parent; }
  public RBNode getLeft()              { return left;          }
  public void setLeft(RBNode left)     { this.left = left;     }
  public RBNode getRight()             { return right;         }
  public void setRight(RBNode right)   { this.right = right;   }
}
