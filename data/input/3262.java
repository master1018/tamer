public class MetalComboBoxEditor extends BasicComboBoxEditor {
    public MetalComboBoxEditor() {
        super();
        editor = new JTextField("",9) {
                public void setText(String s) {
                    if (getText().equals(s)) {
                        return;
                    }
                    super.setText(s);
                }
            public Dimension getPreferredSize() {
                Dimension pref = super.getPreferredSize();
                pref.height += 4;
                return pref;
            }
            public Dimension getMinimumSize() {
                Dimension min = super.getMinimumSize();
                min.height += 4;
                return min;
            }
            };
        editor.setBorder( new EditorBorder() );
    }
    protected static Insets editorBorderInsets = new Insets( 2, 2, 2, 0 );
    class EditorBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate( x, y );
            if (MetalLookAndFeel.usingOcean()) {
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.drawRect(0, 0, w, h - 1);
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(1, 1, w - 2, h - 3);
            }
            else {
                g.setColor( MetalLookAndFeel.getControlDarkShadow() );
                g.drawLine( 0, 0, w-1, 0 );
                g.drawLine( 0, 0, 0, h-2 );
                g.drawLine( 0, h-2, w-1, h-2 );
                g.setColor( MetalLookAndFeel.getControlHighlight() );
                g.drawLine( 1, 1, w-1, 1 );
                g.drawLine( 1, 1, 1, h-1 );
                g.drawLine( 1, h-1, w-1, h-1 );
                g.setColor( MetalLookAndFeel.getControl() );
                g.drawLine( 1, h-2, 1, h-2 );
            }
            g.translate( -x, -y );
        }
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(2, 2, 2, 0);
            return insets;
        }
    }
    public static class UIResource extends MetalComboBoxEditor
    implements javax.swing.plaf.UIResource {
    }
}
