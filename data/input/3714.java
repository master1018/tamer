public class HTMLPane extends JEditorPane {
    private boolean hasSelection = false;
    public HTMLPane() {
        setContentType("text/html");
        setEditable(false);
        ((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                setHasSelection(e.getDot() != e.getMark());
            }
        });
    }
    public synchronized void setHasSelection(boolean b) {
        hasSelection = b;
    }
    public synchronized boolean getHasSelection() {
        return hasSelection;
    }
    public void setText(String text) {
        if (!getHasSelection()) {
            String textColor =
                String.format("%06x", getForeground().getRGB() & 0xFFFFFF);
            super.setText("<html><body text=#"+textColor+">" + text + "</body></html>");
        }
    }
}
