class WTextFieldPeer extends WTextComponentPeer implements TextFieldPeer {
    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(((TextField)target).getFont());
        return new Dimension(fm.stringWidth(getText()) + 24,
                             fm.getHeight() + 8);
    }
    public boolean handleJavaKeyEvent(KeyEvent e) {
        switch (e.getID()) {
           case KeyEvent.KEY_TYPED:
               if ((e.getKeyChar() == '\n') && !e.isAltDown() && !e.isControlDown()) {
                    postEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                              getText(), e.getWhen(), e.getModifiers()));
                    return true;
               }
           break;
        }
        return false;
    }
    public void setEchoChar(char c) {
        setEchoCharacter(c);
    }
    public Dimension getPreferredSize(int cols) {
        return getMinimumSize(cols);
    }
    public Dimension getMinimumSize(int cols) {
        FontMetrics fm = getFontMetrics(((TextField)target).getFont());
        return new Dimension(fm.charWidth('0') * cols + 24, fm.getHeight() + 8);
    }
    public InputMethodRequests getInputMethodRequests() {
           return null;
    }
    WTextFieldPeer(TextField target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    void initialize() {
        TextField tf = (TextField)target;
        if (tf.echoCharIsSet()) {
            setEchoChar(tf.getEchoChar());
        }
        super.initialize();
    }
    public native void setEchoCharacter(char c);
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Dimension minimumSize(int cols) {
        return getMinimumSize(cols);
    }
    public Dimension preferredSize(int cols) {
        return getPreferredSize(cols);
    }
}
