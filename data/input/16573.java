public class BasicHTML {
    public static View createHTMLView(JComponent c, String html) {
        BasicEditorKit kit = getFactory();
        Document doc = kit.createDefaultDocument(c.getFont(),
                                                 c.getForeground());
        Object base = c.getClientProperty(documentBaseKey);
        if (base instanceof URL) {
            ((HTMLDocument)doc).setBase((URL)base);
        }
        Reader r = new StringReader(html);
        try {
            kit.read(r, doc, 0);
        } catch (Throwable e) {
        }
        ViewFactory f = kit.getViewFactory();
        View hview = f.create(doc.getDefaultRootElement());
        View v = new Renderer(c, f, hview);
        return v;
    }
    public static int getHTMLBaseline(View view, int w, int h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        if (view instanceof Renderer) {
            return getBaseline(view.getView(0), w, h);
        }
        return -1;
    }
    static int getBaseline(JComponent c, int y, int ascent,
                                  int w, int h) {
        View view = (View)c.getClientProperty(BasicHTML.propertyKey);
        if (view != null) {
            int baseline = getHTMLBaseline(view, w, h);
            if (baseline < 0) {
                return baseline;
            }
            return y + baseline;
        }
        return y + ascent;
    }
    static int getBaseline(View view, int w, int h) {
        if (hasParagraph(view)) {
            view.setSize(w, h);
            return getBaseline(view, new Rectangle(0, 0, w, h));
        }
        return -1;
    }
    private static int getBaseline(View view, Shape bounds) {
        if (view.getViewCount() == 0) {
            return -1;
        }
        AttributeSet attributes = view.getElement().getAttributes();
        Object name = null;
        if (attributes != null) {
            name = attributes.getAttribute(StyleConstants.NameAttribute);
        }
        int index = 0;
        if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
            index++;
        }
        bounds = view.getChildAllocation(index, bounds);
        if (bounds == null) {
            return -1;
        }
        View child = view.getView(index);
        if (view instanceof javax.swing.text.ParagraphView) {
            Rectangle rect;
            if (bounds instanceof Rectangle) {
                rect = (Rectangle)bounds;
            }
            else {
                rect = bounds.getBounds();
            }
            return rect.y + (int)(rect.height *
                                  child.getAlignment(View.Y_AXIS));
        }
        return getBaseline(child, bounds);
    }
    private static boolean hasParagraph(View view) {
        if (view instanceof javax.swing.text.ParagraphView) {
            return true;
        }
        if (view.getViewCount() == 0) {
            return false;
        }
        AttributeSet attributes = view.getElement().getAttributes();
        Object name = null;
        if (attributes != null) {
            name = attributes.getAttribute(StyleConstants.NameAttribute);
        }
        int index = 0;
        if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
            index = 1;
        }
        return hasParagraph(view.getView(index));
    }
    public static boolean isHTMLString(String s) {
        if (s != null) {
            if ((s.length() >= 6) && (s.charAt(0) == '<') && (s.charAt(5) == '>')) {
                String tag = s.substring(1,5);
                return tag.equalsIgnoreCase(propertyKey);
            }
        }
        return false;
    }
    public static void updateRenderer(JComponent c, String text) {
        View value = null;
        View oldValue = (View)c.getClientProperty(BasicHTML.propertyKey);
        Boolean htmlDisabled = (Boolean) c.getClientProperty(htmlDisable);
        if (htmlDisabled != Boolean.TRUE && BasicHTML.isHTMLString(text)) {
            value = BasicHTML.createHTMLView(c, text);
        }
        if (value != oldValue && oldValue != null) {
            for (int i = 0; i < oldValue.getViewCount(); i++) {
                oldValue.getView(i).setParent(null);
            }
        }
        c.putClientProperty(BasicHTML.propertyKey, value);
    }
    private static final String htmlDisable = "html.disable";
    public static final String propertyKey = "html";
    public static final String documentBaseKey = "html.base";
    static BasicEditorKit getFactory() {
        if (basicHTMLFactory == null) {
            basicHTMLViewFactory = new BasicHTMLViewFactory();
            basicHTMLFactory = new BasicEditorKit();
        }
        return basicHTMLFactory;
    }
    private static BasicEditorKit basicHTMLFactory;
    private static ViewFactory basicHTMLViewFactory;
    private static final String styleChanges =
    "p { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }" +
    "body { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }";
    static class BasicEditorKit extends HTMLEditorKit {
        private static StyleSheet defaultStyles;
        public StyleSheet getStyleSheet() {
            if (defaultStyles == null) {
                defaultStyles = new StyleSheet();
                StringReader r = new StringReader(styleChanges);
                try {
                    defaultStyles.loadRules(r, null);
                } catch (Throwable e) {
                }
                r.close();
                defaultStyles.addStyleSheet(super.getStyleSheet());
            }
            return defaultStyles;
        }
        public Document createDefaultDocument(Font defaultFont,
                                              Color foreground) {
            StyleSheet styles = getStyleSheet();
            StyleSheet ss = new StyleSheet();
            ss.addStyleSheet(styles);
            BasicDocument doc = new BasicDocument(ss, defaultFont, foreground);
            doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
            doc.setPreservesUnknownTags(false);
            return doc;
        }
        public ViewFactory getViewFactory() {
            return basicHTMLViewFactory;
        }
    }
    static class BasicHTMLViewFactory extends HTMLEditorKit.HTMLFactory {
        public View create(Element elem) {
            View view = super.create(elem);
            if (view instanceof ImageView) {
                ((ImageView)view).setLoadsSynchronously(true);
            }
            return view;
        }
    }
    static class BasicDocument extends HTMLDocument {
        BasicDocument(StyleSheet s, Font defaultFont, Color foreground) {
            super(s);
            setPreservesUnknownTags(false);
            setFontAndColor(defaultFont, foreground);
        }
        private void setFontAndColor(Font font, Color fg) {
            getStyleSheet().addRule(sun.swing.SwingUtilities2.
                                    displayPropertiesToCSS(font,fg));
        }
    }
    static class Renderer extends View {
        Renderer(JComponent c, ViewFactory f, View v) {
            super(null);
            host = c;
            factory = f;
            view = v;
            view.setParent(this);
            setSize(view.getPreferredSpan(X_AXIS), view.getPreferredSpan(Y_AXIS));
        }
        public AttributeSet getAttributes() {
            return null;
        }
        public float getPreferredSpan(int axis) {
            if (axis == X_AXIS) {
                return width;
            }
            return view.getPreferredSpan(axis);
        }
        public float getMinimumSpan(int axis) {
            return view.getMinimumSpan(axis);
        }
        public float getMaximumSpan(int axis) {
            return Integer.MAX_VALUE;
        }
        public void preferenceChanged(View child, boolean width, boolean height) {
            host.revalidate();
            host.repaint();
        }
        public float getAlignment(int axis) {
            return view.getAlignment(axis);
        }
        public void paint(Graphics g, Shape allocation) {
            Rectangle alloc = allocation.getBounds();
            view.setSize(alloc.width, alloc.height);
            view.paint(g, allocation);
        }
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }
        public int getViewCount() {
            return 1;
        }
        public View getView(int n) {
            return view;
        }
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            return view.modelToView(pos, a, b);
        }
        public Shape modelToView(int p0, Position.Bias b0, int p1,
                                 Position.Bias b1, Shape a) throws BadLocationException {
            return view.modelToView(p0, b0, p1, b1, a);
        }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            return view.viewToModel(x, y, a, bias);
        }
        public Document getDocument() {
            return view.getDocument();
        }
        public int getStartOffset() {
            return view.getStartOffset();
        }
        public int getEndOffset() {
            return view.getEndOffset();
        }
        public Element getElement() {
            return view.getElement();
        }
        public void setSize(float width, float height) {
            this.width = (int) width;
            view.setSize(width, height);
        }
        public Container getContainer() {
            return host;
        }
        public ViewFactory getViewFactory() {
            return factory;
        }
        private int width;
        private View view;
        private ViewFactory factory;
        private JComponent host;
    }
}
