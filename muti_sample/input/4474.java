class WTextAreaPeer extends WTextComponentPeer implements TextAreaPeer {
    public Dimension getMinimumSize() {
        return getMinimumSize(10, 60);
    }
    public void insert(String txt, int pos) {
        insertText(txt, pos);
    }
    public void replaceRange(String txt, int start, int end) {
        replaceText(txt, start, end);
    }
    public Dimension getPreferredSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }
    public Dimension getMinimumSize(int rows, int cols) {
        FontMetrics fm = getFontMetrics(((TextArea)target).getFont());
        return new Dimension(fm.charWidth('0') * cols + 20, fm.getHeight() * rows + 20);
    }
    public InputMethodRequests getInputMethodRequests() {
           return null;
    }
    WTextAreaPeer(TextArea target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    public native void insertText(String txt, int pos);
    public native void replaceText(String txt, int start, int end);
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }
    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }
}
