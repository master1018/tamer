class StubLeafElement implements Element {
    Document document = new DefaultStyledDocument();
    String context;
    Element parent;
    int position;
    public StubLeafElement(String context, Element parent, int position) {
        this.context = context;
        this.parent = parent;
        this.position = position;
        try {
            document.insertString(0, context, new SimpleAttributeSet());
        } catch (BadLocationException e) {
        }
    }
    public Document getDocument() {
        return document;
    }
    public Element getParentElement() {
        return parent;
    }
    public String getName() {
        return "StubLeafElement";
    }
    public AttributeSet getAttributes() {
        return new SimpleAttributeSet();
    }
    public int getStartOffset() {
        return position;
    }
    public int getEndOffset() {
        return position + document.getLength();
    }
    public int getElementIndex(int offset) {
        return 0;
    }
    public int getElementCount() {
        return 0;
    }
    public Element getElement(int index) {
        return this;
    }
    public boolean isLeaf() {
        return true;
    }
}
