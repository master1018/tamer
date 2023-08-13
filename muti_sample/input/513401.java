public class SimpleNode extends org.apache.james.mime4j.field.address.parser.BaseNode implements Node {
  protected Node parent;
  protected Node[] children;
  protected int id;
  protected AddressListParser parser;
  public SimpleNode(int i) {
    id = i;
  }
  public SimpleNode(AddressListParser p, int i) {
    this(i);
    parser = p;
  }
  public void jjtOpen() {
  }
  public void jjtClose() {
  }
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }
  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }
  public Node jjtGetChild(int i) {
    return children[i];
  }
  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }
  public Object jjtAccept(AddressListParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  public Object childrenAccept(AddressListParserVisitor visitor, Object data) {
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        children[i].jjtAccept(visitor, data);
      }
    }
    return data;
  }
  public String toString() { return AddressListParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }
  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }
}
