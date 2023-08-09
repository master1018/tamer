public class NodeListImpl implements NodeList {
    private ArrayList<Node> mSearchNodes;
    private ArrayList<Node> mStaticNodes;
    private Node mRootNode;
    private String mTagName;
    private boolean mDeepSearch;
    public NodeListImpl(Node rootNode, String tagName, boolean deepSearch) {
        mRootNode = rootNode;
        mTagName  = tagName;
        mDeepSearch = deepSearch;
    }
    public NodeListImpl(ArrayList<Node> nodes) {
        mStaticNodes = nodes;
    }
    public int getLength() {
        if (mStaticNodes == null) {
            fillList(mRootNode);
            return mSearchNodes.size();
        } else {
            return mStaticNodes.size();
        }
    }
    public Node item(int index) {
        Node node = null;
        if (mStaticNodes == null) {
            fillList(mRootNode);
            try {
                node = mSearchNodes.get(index);
            } catch (IndexOutOfBoundsException e) {
            }
        } else {
            try {
                node = mStaticNodes.get(index);
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return node;
    }
    private void fillList(Node node) {
        if (node == mRootNode) {
            mSearchNodes = new ArrayList<Node>();
        } else {
            if ((mTagName == null) || node.getNodeName().equals(mTagName)) {
                mSearchNodes.add(node);
            }
        }
        node = node.getFirstChild();
        while (node != null) {
            if (mDeepSearch) {
                fillList(node);
            } else {
                if ((mTagName == null) || node.getNodeName().equals(mTagName)) {
                    mSearchNodes.add(node);
                }
            }
            node = node.getNextSibling();
        }
    }
}
