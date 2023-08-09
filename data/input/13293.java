class TextAreaDocument extends PlainDocument {
    String initialText;
    void reset() {
        try {
            remove(0, getLength());
            if (initialText != null) {
                insertString(0, initialText, null);
            }
        } catch (BadLocationException e) {
        }
    }
    void storeInitialText() {
        try {
            initialText = getText(0, getLength());
        } catch (BadLocationException e) {
        }
    }
}
