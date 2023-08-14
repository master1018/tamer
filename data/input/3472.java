class WTextComponentPeer extends WComponentPeer implements TextComponentPeer {
    static {
        initIDs();
    }
    public void setEditable(boolean editable) {
        enableEditing(editable);
        setBackground(((TextComponent)target).getBackground());
    }
    public native String getText();
    public native void setText(String txt);
    public native int getSelectionStart();
    public native int getSelectionEnd();
    public native void select(int selStart, int selEnd);
    WTextComponentPeer(TextComponent target) {
        super(target);
    }
    void initialize() {
        TextComponent tc = (TextComponent)target;
        String text = tc.getText();
        if (text != null) {
            setText(text);
        }
        select(tc.getSelectionStart(), tc.getSelectionEnd());
        setEditable(tc.isEditable());
        super.initialize();
    }
    native void enableEditing(boolean e);
    public boolean isFocusable() {
        return true;
    }
    public void setCaretPosition(int pos) {
        select(pos,pos);
    }
    public int getCaretPosition() {
        return getSelectionStart();
    }
    public void valueChanged() {
        postEvent(new TextEvent(target, TextEvent.TEXT_VALUE_CHANGED));
    }
    private static native void initIDs();
    public int getIndexAtPoint(int x, int y) { return -1; }
    public Rectangle getCharacterBounds(int i) { return null; }
    public long filterEvents(long mask) { return 0; }
    public boolean shouldClearRectBeforePaint() {
        return false;
    }
}
