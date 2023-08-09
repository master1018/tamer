public class PasswordView extends FieldView {
    public PasswordView(Element elem) {
        super(elem);
    }
    protected int drawUnselectedText(Graphics g, int x, int y,
                                     int p0, int p1) throws BadLocationException {
        Container c = getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField) c;
            if (! f.echoCharIsSet()) {
                return super.drawUnselectedText(g, x, y, p0, p1);
            }
            if (f.isEnabled()) {
                g.setColor(f.getForeground());
            }
            else {
                g.setColor(f.getDisabledTextColor());
            }
            char echoChar = f.getEchoChar();
            int n = p1 - p0;
            for (int i = 0; i < n; i++) {
                x = drawEchoCharacter(g, x, y, echoChar);
            }
        }
        return x;
    }
    protected int drawSelectedText(Graphics g, int x,
                                   int y, int p0, int p1) throws BadLocationException {
        g.setColor(selected);
        Container c = getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField) c;
            if (! f.echoCharIsSet()) {
                return super.drawSelectedText(g, x, y, p0, p1);
            }
            char echoChar = f.getEchoChar();
            int n = p1 - p0;
            for (int i = 0; i < n; i++) {
                x = drawEchoCharacter(g, x, y, echoChar);
            }
        }
        return x;
    }
    protected int drawEchoCharacter(Graphics g, int x, int y, char c) {
        ONE[0] = c;
        SwingUtilities2.drawChars(Utilities.getJComponent(this),
                                  g, ONE, 0, 1, x, y);
        return x + g.getFontMetrics().charWidth(c);
    }
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Container c = getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField) c;
            if (! f.echoCharIsSet()) {
                return super.modelToView(pos, a, b);
            }
            char echoChar = f.getEchoChar();
            FontMetrics m = f.getFontMetrics(f.getFont());
            Rectangle alloc = adjustAllocation(a).getBounds();
            int dx = (pos - getStartOffset()) * m.charWidth(echoChar);
            alloc.x += dx;
            alloc.width = 1;
            return alloc;
        }
        return null;
    }
    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        bias[0] = Position.Bias.Forward;
        int n = 0;
        Container c = getContainer();
        if (c instanceof JPasswordField) {
            JPasswordField f = (JPasswordField) c;
            if (! f.echoCharIsSet()) {
                return super.viewToModel(fx, fy, a, bias);
            }
            char echoChar = f.getEchoChar();
            int charWidth = f.getFontMetrics(f.getFont()).charWidth(echoChar);
            a = adjustAllocation(a);
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a :
                              a.getBounds();
            n = (charWidth > 0 ?
                 ((int)fx - alloc.x) / charWidth : Integer.MAX_VALUE);
            if (n < 0) {
                n = 0;
            }
            else if (n > (getStartOffset() + getDocument().getLength())) {
                n = getDocument().getLength() - getStartOffset();
            }
        }
        return getStartOffset() + n;
    }
    public float getPreferredSpan(int axis) {
        switch (axis) {
        case View.X_AXIS:
            Container c = getContainer();
            if (c instanceof JPasswordField) {
                JPasswordField f = (JPasswordField) c;
                if (f.echoCharIsSet()) {
                    char echoChar = f.getEchoChar();
                    FontMetrics m = f.getFontMetrics(f.getFont());
                    Document doc = getDocument();
                    return m.charWidth(echoChar) * getDocument().getLength();
                }
            }
        }
        return super.getPreferredSpan(axis);
    }
    static char[] ONE = new char[1];
}
