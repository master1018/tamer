public class MockNodeList implements NodeList {
    ArrayList<MockXmlNode> mChildren;
    public MockNodeList(MockXmlNode[] children) {
        mChildren = new ArrayList<MockXmlNode>();
        if (children != null) {
            for (MockXmlNode n : children) {
                mChildren.add(n);
            }
        }
    }
    public int getLength() {
        return mChildren.size();
    }
    public Node item(int index) {
        if (index >= 0 && index < mChildren.size()) {
            return mChildren.get(index);
        }
        return null;
    }
    public ArrayList<MockXmlNode> getArrayList() {
        return mChildren;
    }
}
