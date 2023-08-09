public class InlineView extends LabelView {
    public InlineView(Element elem) {
        super(elem);
        StyleSheet sheet = getStyleSheet();
        attr = sheet.getViewAttributes(this);
    }
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
    }
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
    }
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        StyleSheet sheet = getStyleSheet();
        attr = sheet.getViewAttributes(this);
        preferenceChanged(null, true, true);
    }
    public AttributeSet getAttributes() {
        return attr;
    }
    public int getBreakWeight(int axis, float pos, float len) {
        if (nowrap) {
            return BadBreakWeight;
        }
        return super.getBreakWeight(axis, pos, len);
    }
    public View breakView(int axis, int offset, float pos, float len) {
        return super.breakView(axis, offset, pos, len);
    }
    protected void setPropertiesFromAttributes() {
        super.setPropertiesFromAttributes();
        AttributeSet a = getAttributes();
        Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
        boolean u = (decor != null) ?
          (decor.toString().indexOf("underline") >= 0) : false;
        setUnderline(u);
        boolean s = (decor != null) ?
          (decor.toString().indexOf("line-through") >= 0) : false;
        setStrikeThrough(s);
        Object vAlign = a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
        s = (vAlign != null) ? (vAlign.toString().indexOf("sup") >= 0) : false;
        setSuperscript(s);
        s = (vAlign != null) ? (vAlign.toString().indexOf("sub") >= 0) : false;
        setSubscript(s);
        Object whitespace = a.getAttribute(CSS.Attribute.WHITE_SPACE);
        if ((whitespace != null) && whitespace.equals("nowrap")) {
            nowrap = true;
        } else {
            nowrap = false;
        }
        HTMLDocument doc = (HTMLDocument)getDocument();
        Color bg = doc.getBackground(a);
        if (bg != null) {
            setBackground(bg);
        }
    }
    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument) getDocument();
        return doc.getStyleSheet();
    }
    private boolean nowrap;
    private AttributeSet attr;
}
