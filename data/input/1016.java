public class WindowsTextFieldUI extends BasicTextFieldUI
{
    public static ComponentUI createUI(JComponent c) {
        return new WindowsTextFieldUI();
    }
    protected void paintBackground(Graphics g) {
        super.paintBackground(g);
    }
    protected Caret createCaret() {
        return new WindowsFieldCaret();
    }
    static class WindowsFieldCaret extends DefaultCaret implements UIResource {
        public WindowsFieldCaret() {
            super();
        }
        protected void adjustVisibility(Rectangle r) {
            SwingUtilities.invokeLater(new SafeScroller(r));
        }
        protected Highlighter.HighlightPainter getSelectionPainter() {
            return WindowsTextUI.WindowsPainter;
        }
        private class SafeScroller implements Runnable {
            SafeScroller(Rectangle r) {
                this.r = r;
            }
            public void run() {
                JTextField field = (JTextField) getComponent();
                if (field != null) {
                    TextUI ui = field.getUI();
                    int dot = getDot();
                    Position.Bias bias = Position.Bias.Forward;
                    Rectangle startRect = null;
                    try {
                        startRect = ui.modelToView(field, dot, bias);
                    } catch (BadLocationException ble) {}
                    Insets i = field.getInsets();
                    BoundedRangeModel vis = field.getHorizontalVisibility();
                    int x = r.x + vis.getValue() - i.left;
                    int quarterSpan = vis.getExtent() / 4;
                    if (r.x < i.left) {
                        vis.setValue(x - quarterSpan);
                    } else if (r.x + r.width > i.left + vis.getExtent()) {
                        vis.setValue(x - (3 * quarterSpan));
                    }
                    if (startRect != null) {
                        try {
                            Rectangle endRect;
                            endRect = ui.modelToView(field, dot, bias);
                            if (endRect != null && !endRect.equals(startRect)){
                                damage(endRect);
                            }
                        } catch (BadLocationException ble) {}
                    }
                }
            }
            private Rectangle r;
        }
    }
}
