public class ElementIterator implements Cloneable {
    private Element root;
    private Stack<StackItem> elementStack = null;
    private class StackItem implements Cloneable {
        Element item;
        int childIndex;
        private StackItem(Element elem) {
            this.item = elem;
            this.childIndex = -1;
        }
        private void incrementIndex() {
            childIndex++;
        }
        private Element getElement() {
            return item;
        }
        private int getIndex() {
            return childIndex;
        }
        protected Object clone() throws java.lang.CloneNotSupportedException {
            return super.clone();
        }
    }
    public ElementIterator(Document document) {
        root = document.getDefaultRootElement();
    }
    public ElementIterator(Element root) {
        this.root = root;
    }
    public synchronized Object clone() {
        try {
            ElementIterator it = new ElementIterator(root);
            if (elementStack != null) {
                it.elementStack = new Stack<StackItem>();
                for (int i = 0; i < elementStack.size(); i++) {
                    StackItem item = elementStack.elementAt(i);
                    StackItem clonee = (StackItem)item.clone();
                    it.elementStack.push(clonee);
                }
            }
            return it;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public Element first() {
        if (root == null) {
            return null;
        }
        elementStack = new Stack<StackItem>();
        if (root.getElementCount() != 0) {
            elementStack.push(new StackItem(root));
        }
        return root;
    }
    public int depth() {
        if (elementStack == null) {
            return 0;
        }
        return elementStack.size();
    }
    public Element current() {
        if (elementStack == null) {
            return first();
        }
        if (! elementStack.empty()) {
            StackItem item = elementStack.peek();
            Element elem = item.getElement();
            int index = item.getIndex();
            if (index == -1) {
                return elem;
            }
            return elem.getElement(index);
        }
        return null;
    }
    public Element next() {
        if (elementStack == null) {
            return first();
        }
        if (elementStack.isEmpty()) {
            return null;
        }
        StackItem item = elementStack.peek();
        Element elem = item.getElement();
        int index = item.getIndex();
        if (index+1 < elem.getElementCount()) {
            Element child = elem.getElement(index+1);
            if (child.isLeaf()) {
                item.incrementIndex();
            } else {
                elementStack.push(new StackItem(child));
            }
            return child;
        } else {
            elementStack.pop();
            if (!elementStack.isEmpty()) {
                StackItem top = elementStack.peek();
                top.incrementIndex();
                return next();
            }
        }
        return null;
    }
    public Element previous() {
        int stackSize;
        if (elementStack == null || (stackSize = elementStack.size()) == 0) {
            return null;
        }
        StackItem item = elementStack.peek();
        Element elem = item.getElement();
        int index = item.getIndex();
        if (index > 0) {
            return getDeepestLeaf(elem.getElement(--index));
        } else if (index == 0) {
            return elem;
        } else if (index == -1) {
            if (stackSize == 1) {
                return null;
            }
            StackItem top = elementStack.pop();
            item = elementStack.peek();
            elementStack.push(top);
            elem = item.getElement();
            index = item.getIndex();
            return ((index == -1) ? elem : getDeepestLeaf(elem.getElement
                                                          (index)));
        }
        return null;
    }
    private Element getDeepestLeaf(Element parent) {
        if (parent.isLeaf()) {
            return parent;
        }
        int childCount = parent.getElementCount();
        if (childCount == 0) {
            return parent;
        }
        return getDeepestLeaf(parent.getElement(childCount - 1));
    }
    private void dumpTree() {
        Element elem;
        while (true) {
            if ((elem = next()) != null) {
                System.out.println("elem: " + elem.getName());
                AttributeSet attr = elem.getAttributes();
                String s = "";
                Enumeration names = attr.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object key = names.nextElement();
                    Object value = attr.getAttribute(key);
                    if (value instanceof AttributeSet) {
                        s = s + key + "=**AttributeSet** ";
                    } else {
                        s = s + key + "=" + value + " ";
                    }
                }
                System.out.println("attributes: " + s);
            } else {
                break;
            }
        }
    }
}
