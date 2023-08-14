public class DOMSignContext extends DOMCryptoContext implements XMLSignContext {
    private Node parent;
    private Node nextSibling;
    public DOMSignContext(Key signingKey, Node parent) {
        if (signingKey == null) {
            throw new NullPointerException("signingKey cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent = parent;
    }
    public DOMSignContext(Key signingKey, Node parent, Node nextSibling) {
        if (signingKey == null) {
            throw new NullPointerException("signingKey cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        if (nextSibling == null) {
            throw new NullPointerException("nextSibling cannot be null");
        }
        setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent = parent;
        this.nextSibling = nextSibling;
    }
    public DOMSignContext(KeySelector ks, Node parent) {
        if (ks == null) {
            throw new NullPointerException("key selector cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        setKeySelector(ks);
        this.parent = parent;
    }
    public DOMSignContext(KeySelector ks, Node parent, Node nextSibling) {
        if (ks == null) {
            throw new NullPointerException("key selector cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        if (nextSibling == null) {
            throw new NullPointerException("nextSibling cannot be null");
        }
        setKeySelector(ks);
        this.parent = parent;
        this.nextSibling = nextSibling;
    }
    public void setParent(Node parent) {
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        this.parent = parent;
    }
    public void setNextSibling(Node nextSibling) {
        this.nextSibling = nextSibling;
    }
    public Node getParent() {
        return parent;
    }
    public Node getNextSibling() {
        return nextSibling;
    }
}
