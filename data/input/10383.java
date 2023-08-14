public class HelperNodeList implements NodeList {
   ArrayList nodes = new ArrayList(20);
   boolean _allNodesMustHaveSameParent = false;
   public HelperNodeList() {
      this(false);
   }
   public HelperNodeList(boolean allNodesMustHaveSameParent) {
      this._allNodesMustHaveSameParent = allNodesMustHaveSameParent;
   }
   public Node item(int index) {
      return (Node) nodes.get(index);
   }
   public int getLength() {
      return nodes.size();
   }
   public void appendChild(Node node) throws IllegalArgumentException {
      if (this._allNodesMustHaveSameParent && this.getLength() > 0) {
         if (this.item(0).getParentNode() != node.getParentNode()) {
            throw new IllegalArgumentException("Nodes have not the same Parent");
         }
      }
      nodes.add(node);
   }
   public Document getOwnerDocument() {
      if (this.getLength() == 0) {
         return null;
      }
      return XMLUtils.getOwnerDocument(this.item(0));
   }
}
