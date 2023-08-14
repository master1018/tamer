public class BadAddressTreeNodeAdapter extends FieldTreeNodeAdapter {
  private boolean usingAddress;
  private Address addr;
  private long    addrValue;
  public BadAddressTreeNodeAdapter(Address addr, FieldIdentifier id) {
    this(addr, id, false);
  }
  public BadAddressTreeNodeAdapter(Address addr, FieldIdentifier id, boolean treeTableMode) {
    super(id, treeTableMode);
    this.addr = addr;
    usingAddress = true;
  }
  public BadAddressTreeNodeAdapter(long addr, FieldIdentifier id) {
    this(addr, id, false);
  }
  public BadAddressTreeNodeAdapter(long addrValue, FieldIdentifier id, boolean treeTableMode) {
    super(id, treeTableMode);
    this.addrValue = addrValue;
    usingAddress = false;
  }
  public int getChildCount() {
    return 0;
  }
  public SimpleTreeNode getChild(int index) {
    throw new RuntimeException("Should not call this");
  }
  public boolean isLeaf() {
    return true;
  }
  public int getIndexOfChild(SimpleTreeNode child) {
    throw new RuntimeException("Should not call this");
  }
  public String getValue() {
    String addrString = null;
    if (usingAddress) {
      if (addr == null) {
        addrString = "0x0";
      } else {
        addrString = addr.toString();
      }
    } else {
      addrString = "0x" + Long.toHexString(addrValue);
    }
    return "** BAD ADDRESS " + addrString + " **";
  }
}
