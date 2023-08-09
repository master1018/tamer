public class IntTrie {
    private Node mHead;
    class Node {
        private Node mFirstChild;
        private Node mNextSibling;
        private char mKey;
        int mValue;
        public final void add(String key, int value) {
            final int len = key.length();
            Node n = this;
            int index = 0;
            while (index < len) {
                n = n.getOrCreateNode(key.charAt(index++));
            }
            n.mValue = value;
        }
        private Node getOrCreateNode(char key) {
            for (Node n = mFirstChild; n != null; n = n.mNextSibling) {
                if (n.mKey == key) {
                    return n;
                }
            }
            Node n = new Node();
            n.mKey = key;
            n.mNextSibling = mFirstChild;
            mFirstChild = n;
            return n;
        }
        Node getNode(char key) {
            for (Node n = mFirstChild; n != null; n = n.mNextSibling) {
                if (n.mKey == key) {
                    return n;
                }
            }
            return null;
        }
    }
    public IntTrie(String[] dictionary, int[] values) {
        final int len = dictionary.length;
        if (len != values.length) {
            throw new IllegalArgumentException("dictionary[] and values[] must be the same length");
        }
        mHead = new Node();
        for (int i = 0; i < len; i++) {
            mHead.add(dictionary[i], values[i]);
        }
    }
    public Node getNode(char key) {
        return mHead.getNode(key);
    }
}
