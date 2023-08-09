public class SynthTextPaneUI extends SynthEditorPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new SynthTextPaneUI();
    }
    @Override
    protected String getPropertyPrefix() {
        return "TextPane";
    }
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        updateForeground(c.getForeground());
        updateFont(c.getFont());
    }
    @Override
    protected void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String name = evt.getPropertyName();
        if (name.equals("foreground")) {
            updateForeground((Color)evt.getNewValue());
        } else if (name.equals("font")) {
            updateFont((Font)evt.getNewValue());
        } else if (name.equals("document")) {
            JComponent comp = getComponent();
            updateForeground(comp.getForeground());
            updateFont(comp.getFont());
        }
    }
    private void updateForeground(Color color) {
        StyledDocument doc = (StyledDocument)getComponent().getDocument();
        Style style = doc.getStyle(StyleContext.DEFAULT_STYLE);
        if (style == null) {
            return;
        }
        if (color == null) {
            style.removeAttribute(StyleConstants.Foreground);
        } else {
            StyleConstants.setForeground(style, color);
        }
    }
    private void updateFont(Font font) {
        StyledDocument doc = (StyledDocument)getComponent().getDocument();
        Style style = doc.getStyle(StyleContext.DEFAULT_STYLE);
        if (style == null) {
            return;
        }
        if (font == null) {
            style.removeAttribute(StyleConstants.FontFamily);
            style.removeAttribute(StyleConstants.FontSize);
            style.removeAttribute(StyleConstants.Bold);
            style.removeAttribute(StyleConstants.Italic);
        } else {
            StyleConstants.setFontFamily(style, font.getName());
            StyleConstants.setFontSize(style, font.getSize());
            StyleConstants.setBold(style, font.isBold());
            StyleConstants.setItalic(style, font.isItalic());
        }
    }
    @Override
    void paintBackground(SynthContext context, Graphics g, JComponent c) {
        context.getPainter().paintTextPaneBackground(context, g, 0, 0,
                                                  c.getWidth(), c.getHeight());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintTextPaneBorder(context, g, x, y, w, h);
    }
}
