class StubBranchElement implements Element {
    Document document = new DefaultStyledDocument();
    String context;
    Element[] children = new StubLeafElement[3];
    public StubBranchElement(String context) {
        this.context = context;
        int len = context.length() / 3;
        for (int i = 0; i < 3; i++) {
            children[i] = new StubLeafElement(
                    context.substring(len * i, len * (i + 1)), this, len * i);
        }
        try {
            document.insertString(0, context, new SimpleAttributeSet());
        } catch (BadLocationException e) {
        }
    }
    public Document getDocument() {
        return document;
    }
    public Element getParentElement() {
        return null;
    }
    public String getName() {
        return "StubBranchElement";
    }
    public AttributeSet getAttributes() {
        return new SimpleAttributeSet();
    }
    public int getStartOffset() {
        return 0;
    }
    public int getEndOffset() {
        return document.getLength();
    }
    public int getElementIndex(int offset) {
        return offset / 3;
    }
    public int getElementCount() {
        return 3;
    }
    public Element getElement(int index) {
        return children[index];
    }
    public boolean isLeaf() {
        return false;
    }
    public Element[] getChildren() {
        return children;
    }
}
