public class HTMLEditorKit extends StyledEditorKit implements Accessible {
    private JEditorPane theEditor;
    public HTMLEditorKit() {
    }
    public String getContentType() {
        return "text/html";
    }
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
    public Document createDefaultDocument() {
        StyleSheet styles = getStyleSheet();
        StyleSheet ss = new StyleSheet();
        ss.addStyleSheet(styles);
        HTMLDocument doc = new HTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }
    private Parser ensureParser(HTMLDocument doc) throws IOException {
        Parser p = doc.getParser();
        if (p == null) {
            p = getParser();
        }
        if (p == null) {
            throw new IOException("Can't load parser");
        }
        return p;
    }
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        if (doc instanceof HTMLDocument) {
            HTMLDocument hdoc = (HTMLDocument) doc;
            if (pos > doc.getLength()) {
                throw new BadLocationException("Invalid location", pos);
            }
            Parser p = ensureParser(hdoc);
            ParserCallback receiver = hdoc.getReader(pos);
            Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
            p.parse(in, receiver, (ignoreCharset == null) ? false : ignoreCharset.booleanValue());
            receiver.flush();
        } else {
            super.read(in, doc, pos);
        }
    }
    public void insertHTML(HTMLDocument doc, int offset, String html,
                           int popDepth, int pushDepth,
                           HTML.Tag insertTag) throws
                       BadLocationException, IOException {
        if (offset > doc.getLength()) {
            throw new BadLocationException("Invalid location", offset);
        }
        Parser p = ensureParser(doc);
        ParserCallback receiver = doc.getReader(offset, popDepth, pushDepth,
                                                insertTag);
        Boolean ignoreCharset = (Boolean)doc.getProperty
                                ("IgnoreCharsetDirective");
        p.parse(new StringReader(html), receiver, (ignoreCharset == null) ?
                false : ignoreCharset.booleanValue());
        receiver.flush();
    }
    public void write(Writer out, Document doc, int pos, int len)
        throws IOException, BadLocationException {
        if (doc instanceof HTMLDocument) {
            HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len);
            w.write();
        } else if (doc instanceof StyledDocument) {
            MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len);
            w.write();
        } else {
            super.write(out, doc, pos, len);
        }
    }
    public void install(JEditorPane c) {
        c.addMouseListener(linkHandler);
        c.addMouseMotionListener(linkHandler);
        c.addCaretListener(nextLinkAction);
        super.install(c);
        theEditor = c;
    }
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(linkHandler);
        c.removeMouseMotionListener(linkHandler);
        c.removeCaretListener(nextLinkAction);
        super.deinstall(c);
        theEditor = null;
    }
    public static final String DEFAULT_CSS = "default.css";
    public void setStyleSheet(StyleSheet s) {
        if (s == null) {
            AppContext.getAppContext().remove(DEFAULT_STYLES_KEY);
        } else {
            AppContext.getAppContext().put(DEFAULT_STYLES_KEY, s);
        }
    }
    public StyleSheet getStyleSheet() {
        AppContext appContext = AppContext.getAppContext();
        StyleSheet defaultStyles = (StyleSheet) appContext.get(DEFAULT_STYLES_KEY);
        if (defaultStyles == null) {
            defaultStyles = new StyleSheet();
            appContext.put(DEFAULT_STYLES_KEY, defaultStyles);
            try {
                InputStream is = HTMLEditorKit.getResourceAsStream(DEFAULT_CSS);
                Reader r = new BufferedReader(
                        new InputStreamReader(is, "ISO-8859-1"));
                defaultStyles.loadRules(r, null);
                r.close();
            } catch (Throwable e) {
            }
        }
        return defaultStyles;
    }
    static InputStream getResourceAsStream(String name) {
        try {
            return ResourceLoader.getResourceAsStream(name);
        } catch (Throwable e) {
            return HTMLEditorKit.class.getResourceAsStream(name);
        }
    }
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), this.defaultActions);
    }
    protected void createInputAttributes(Element element,
                                         MutableAttributeSet set) {
        set.removeAttributes(set);
        set.addAttributes(element.getAttributes());
        set.removeAttribute(StyleConstants.ComposedTextAttribute);
        Object o = set.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag tag = (HTML.Tag)o;
            if(tag == HTML.Tag.IMG) {
                set.removeAttribute(HTML.Attribute.SRC);
                set.removeAttribute(HTML.Attribute.HEIGHT);
                set.removeAttribute(HTML.Attribute.WIDTH);
                set.addAttribute(StyleConstants.NameAttribute,
                                 HTML.Tag.CONTENT);
            }
            else if (tag == HTML.Tag.HR || tag == HTML.Tag.BR) {
                set.addAttribute(StyleConstants.NameAttribute,
                                 HTML.Tag.CONTENT);
            }
            else if (tag == HTML.Tag.COMMENT) {
                set.addAttribute(StyleConstants.NameAttribute,
                                 HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Attribute.COMMENT);
            }
            else if (tag == HTML.Tag.INPUT) {
                set.addAttribute(StyleConstants.NameAttribute,
                                 HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Tag.INPUT);
            }
            else if (tag instanceof HTML.UnknownTag) {
                set.addAttribute(StyleConstants.NameAttribute,
                                 HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Attribute.ENDTAG);
            }
        }
    }
    public MutableAttributeSet getInputAttributes() {
        if (input == null) {
            input = getStyleSheet().addStyle(null, null);
        }
        return input;
    }
    public void setDefaultCursor(Cursor cursor) {
        defaultCursor = cursor;
    }
    public Cursor getDefaultCursor() {
        return defaultCursor;
    }
    public void setLinkCursor(Cursor cursor) {
        linkCursor = cursor;
    }
    public Cursor getLinkCursor() {
        return linkCursor;
    }
    public boolean isAutoFormSubmission() {
        return isAutoFormSubmission;
    }
    public void setAutoFormSubmission(boolean isAuto) {
        isAutoFormSubmission = isAuto;
    }
    public Object clone() {
        HTMLEditorKit o = (HTMLEditorKit)super.clone();
        if (o != null) {
            o.input = null;
            o.linkHandler = new LinkController();
        }
        return o;
    }
    protected Parser getParser() {
        if (defaultParser == null) {
            try {
                Class c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
                defaultParser = (Parser) c.newInstance();
            } catch (Throwable e) {
            }
        }
        return defaultParser;
    }
    private AccessibleContext accessibleContext;
    public AccessibleContext getAccessibleContext() {
        if (theEditor == null) {
            return null;
        }
        if (accessibleContext == null) {
            AccessibleHTML a = new AccessibleHTML(theEditor);
            accessibleContext = a.getAccessibleContext();
        }
        return accessibleContext;
    }
    private static final Cursor MoveCursor = Cursor.getPredefinedCursor
                                    (Cursor.HAND_CURSOR);
    private static final Cursor DefaultCursor = Cursor.getPredefinedCursor
                                    (Cursor.DEFAULT_CURSOR);
    private static final ViewFactory defaultFactory = new HTMLFactory();
    MutableAttributeSet input;
    private static final Object DEFAULT_STYLES_KEY = new Object();
    private LinkController linkHandler = new LinkController();
    private static Parser defaultParser = null;
    private Cursor defaultCursor = DefaultCursor;
    private Cursor linkCursor = MoveCursor;
    private boolean isAutoFormSubmission = true;
    public static class LinkController extends MouseAdapter implements MouseMotionListener, Serializable {
        private Element curElem = null;
        private boolean curElemImage = false;
        private String href = null;
        private transient Position.Bias[] bias = new Position.Bias[1];
        private int curOffset;
        public void mouseClicked(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (! editor.isEditable() && editor.isEnabled() &&
                    SwingUtilities.isLeftMouseButton(e)) {
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.viewToModel(pt);
                if (pos >= 0) {
                    activateLink(pos, editor, e);
                }
            }
        }
        public void mouseDragged(MouseEvent e) {
        }
        public void mouseMoved(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (!editor.isEnabled()) {
                return;
            }
            HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
            boolean adjustCursor = true;
            Cursor newCursor = kit.getDefaultCursor();
            if (!editor.isEditable()) {
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.getUI().viewToModel(editor, pt, bias);
                if (bias[0] == Position.Bias.Backward && pos > 0) {
                    pos--;
                }
                if (pos >= 0 &&(editor.getDocument() instanceof HTMLDocument)){
                    HTMLDocument hdoc = (HTMLDocument)editor.getDocument();
                    Element elem = hdoc.getCharacterElement(pos);
                    if (!doesElementContainLocation(editor, elem, pos,
                                                    e.getX(), e.getY())) {
                        elem = null;
                    }
                    if (curElem != elem || curElemImage) {
                        Element lastElem = curElem;
                        curElem = elem;
                        String href = null;
                        curElemImage = false;
                        if (elem != null) {
                            AttributeSet a = elem.getAttributes();
                            AttributeSet anchor = (AttributeSet)a.
                                                   getAttribute(HTML.Tag.A);
                            if (anchor == null) {
                                curElemImage = (a.getAttribute(StyleConstants.
                                            NameAttribute) == HTML.Tag.IMG);
                                if (curElemImage) {
                                    href = getMapHREF(editor, hdoc, elem, a,
                                                      pos, e.getX(), e.getY());
                                }
                            }
                            else {
                                href = (String)anchor.getAttribute
                                    (HTML.Attribute.HREF);
                            }
                        }
                        if (href != this.href) {
                            fireEvents(editor, hdoc, href, lastElem, e);
                            this.href = href;
                            if (href != null) {
                                newCursor = kit.getLinkCursor();
                            }
                        }
                        else {
                            adjustCursor = false;
                        }
                    }
                    else {
                        adjustCursor = false;
                    }
                    curOffset = pos;
                }
            }
            if (adjustCursor && editor.getCursor() != newCursor) {
                editor.setCursor(newCursor);
            }
        }
        private String getMapHREF(JEditorPane html, HTMLDocument hdoc,
                                  Element elem, AttributeSet attr, int offset,
                                  int x, int y) {
            Object useMap = attr.getAttribute(HTML.Attribute.USEMAP);
            if (useMap != null && (useMap instanceof String)) {
                Map m = hdoc.getMap((String)useMap);
                if (m != null && offset < hdoc.getLength()) {
                    Rectangle bounds;
                    TextUI ui = html.getUI();
                    try {
                        Shape lBounds = ui.modelToView(html, offset,
                                                   Position.Bias.Forward);
                        Shape rBounds = ui.modelToView(html, offset + 1,
                                                   Position.Bias.Backward);
                        bounds = lBounds.getBounds();
                        bounds.add((rBounds instanceof Rectangle) ?
                                    (Rectangle)rBounds : rBounds.getBounds());
                    } catch (BadLocationException ble) {
                        bounds = null;
                    }
                    if (bounds != null) {
                        AttributeSet area = m.getArea(x - bounds.x,
                                                      y - bounds.y,
                                                      bounds.width,
                                                      bounds.height);
                        if (area != null) {
                            return (String)area.getAttribute(HTML.Attribute.
                                                             HREF);
                        }
                    }
                }
            }
            return null;
        }
        private boolean doesElementContainLocation(JEditorPane editor,
                                                   Element e, int offset,
                                                   int x, int y) {
            if (e != null && offset > 0 && e.getStartOffset() == offset) {
                try {
                    TextUI ui = editor.getUI();
                    Shape s1 = ui.modelToView(editor, offset,
                                              Position.Bias.Forward);
                    if (s1 == null) {
                        return false;
                    }
                    Rectangle r1 = (s1 instanceof Rectangle) ? (Rectangle)s1 :
                                    s1.getBounds();
                    Shape s2 = ui.modelToView(editor, e.getEndOffset(),
                                              Position.Bias.Backward);
                    if (s2 != null) {
                        Rectangle r2 = (s2 instanceof Rectangle) ? (Rectangle)s2 :
                                    s2.getBounds();
                        r1.add(r2);
                    }
                    return r1.contains(x, y);
                } catch (BadLocationException ble) {
                }
            }
            return true;
        }
        protected void activateLink(int pos, JEditorPane editor) {
            activateLink(pos, editor, null);
        }
        void activateLink(int pos, JEditorPane html, MouseEvent mouseEvent) {
            Document doc = html.getDocument();
            if (doc instanceof HTMLDocument) {
                HTMLDocument hdoc = (HTMLDocument) doc;
                Element e = hdoc.getCharacterElement(pos);
                AttributeSet a = e.getAttributes();
                AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
                HyperlinkEvent linkEvent = null;
                String description;
                int x = -1;
                int y = -1;
                if (mouseEvent != null) {
                    x = mouseEvent.getX();
                    y = mouseEvent.getY();
                }
                if (anchor == null) {
                    href = getMapHREF(html, hdoc, e, a, pos, x, y);
                }
                else {
                    href = (String)anchor.getAttribute(HTML.Attribute.HREF);
                }
                if (href != null) {
                    linkEvent = createHyperlinkEvent(html, hdoc, href, anchor,
                                                     e, mouseEvent);
                }
                if (linkEvent != null) {
                    html.fireHyperlinkUpdate(linkEvent);
                }
            }
        }
        HyperlinkEvent createHyperlinkEvent(JEditorPane html,
                                            HTMLDocument hdoc, String href,
                                            AttributeSet anchor,
                                            Element element,
                                            MouseEvent mouseEvent) {
            URL u;
            try {
                URL base = hdoc.getBase();
                u = new URL(base, href);
                if (href != null && "file".equals(u.getProtocol()) &&
                    href.startsWith("#")) {
                    String baseFile = base.getFile();
                    String newFile = u.getFile();
                    if (baseFile != null && newFile != null &&
                        !newFile.startsWith(baseFile)) {
                        u = new URL(base, baseFile + href);
                    }
                }
            } catch (MalformedURLException m) {
                u = null;
            }
            HyperlinkEvent linkEvent;
            if (!hdoc.isFrameDocument()) {
                linkEvent = new HyperlinkEvent(
                        html, HyperlinkEvent.EventType.ACTIVATED, u, href,
                        element, mouseEvent);
            } else {
                String target = (anchor != null) ?
                    (String)anchor.getAttribute(HTML.Attribute.TARGET) : null;
                if ((target == null) || (target.equals(""))) {
                    target = hdoc.getBaseTarget();
                }
                if ((target == null) || (target.equals(""))) {
                    target = "_self";
                }
                    linkEvent = new HTMLFrameHyperlinkEvent(
                        html, HyperlinkEvent.EventType.ACTIVATED, u, href,
                        element, mouseEvent, target);
            }
            return linkEvent;
        }
        void fireEvents(JEditorPane editor, HTMLDocument doc, String href,
                        Element lastElem, MouseEvent mouseEvent) {
            if (this.href != null) {
                URL u;
                try {
                    u = new URL(doc.getBase(), this.href);
                } catch (MalformedURLException m) {
                    u = null;
                }
                HyperlinkEvent exit = new HyperlinkEvent(editor,
                                 HyperlinkEvent.EventType.EXITED, u, this.href,
                                 lastElem, mouseEvent);
                editor.fireHyperlinkUpdate(exit);
            }
            if (href != null) {
                URL u;
                try {
                    u = new URL(doc.getBase(), href);
                } catch (MalformedURLException m) {
                    u = null;
                }
                HyperlinkEvent entered = new HyperlinkEvent(editor,
                                            HyperlinkEvent.EventType.ENTERED,
                                            u, href, curElem, mouseEvent);
                editor.fireHyperlinkUpdate(entered);
            }
        }
    }
    public static abstract class Parser {
        public abstract void parse(Reader r, ParserCallback cb, boolean ignoreCharSet) throws IOException;
    }
    public static class ParserCallback {
        public static final Object IMPLIED = "_implied_";
        public void flush() throws BadLocationException {
        }
        public void handleText(char[] data, int pos) {
        }
        public void handleComment(char[] data, int pos) {
        }
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        }
        public void handleEndTag(HTML.Tag t, int pos) {
        }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        }
        public void handleError(String errorMsg, int pos){
        }
        public void handleEndOfLineString(String eol) {
        }
    }
    public static class HTMLFactory implements ViewFactory {
        public View create(Element elem) {
            AttributeSet attrs = elem.getAttributes();
            Object elementName =
                attrs.getAttribute(AbstractDocument.ElementNameAttribute);
            Object o = (elementName != null) ?
                null : attrs.getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                HTML.Tag kind = (HTML.Tag) o;
                if (kind == HTML.Tag.CONTENT) {
                    return new InlineView(elem);
                } else if (kind == HTML.Tag.IMPLIED) {
                    String ws = (String) elem.getAttributes().getAttribute(
                        CSS.Attribute.WHITE_SPACE);
                    if ((ws != null) && ws.equals("pre")) {
                        return new LineView(elem);
                    }
                    return new javax.swing.text.html.ParagraphView(elem);
                } else if ((kind == HTML.Tag.P) ||
                           (kind == HTML.Tag.H1) ||
                           (kind == HTML.Tag.H2) ||
                           (kind == HTML.Tag.H3) ||
                           (kind == HTML.Tag.H4) ||
                           (kind == HTML.Tag.H5) ||
                           (kind == HTML.Tag.H6) ||
                           (kind == HTML.Tag.DT)) {
                    return new javax.swing.text.html.ParagraphView(elem);
                } else if ((kind == HTML.Tag.MENU) ||
                           (kind == HTML.Tag.DIR) ||
                           (kind == HTML.Tag.UL)   ||
                           (kind == HTML.Tag.OL)) {
                    return new ListView(elem);
                } else if (kind == HTML.Tag.BODY) {
                    return new BodyBlockView(elem);
                } else if (kind == HTML.Tag.HTML) {
                    return new BlockView(elem, View.Y_AXIS);
                } else if ((kind == HTML.Tag.LI) ||
                           (kind == HTML.Tag.CENTER) ||
                           (kind == HTML.Tag.DL) ||
                           (kind == HTML.Tag.DD) ||
                           (kind == HTML.Tag.DIV) ||
                           (kind == HTML.Tag.BLOCKQUOTE) ||
                           (kind == HTML.Tag.PRE) ||
                           (kind == HTML.Tag.FORM)) {
                    return new BlockView(elem, View.Y_AXIS);
                } else if (kind == HTML.Tag.NOFRAMES) {
                    return new NoFramesView(elem, View.Y_AXIS);
                } else if (kind==HTML.Tag.IMG) {
                    return new ImageView(elem);
                } else if (kind == HTML.Tag.ISINDEX) {
                    return new IsindexView(elem);
                } else if (kind == HTML.Tag.HR) {
                    return new HRuleView(elem);
                } else if (kind == HTML.Tag.BR) {
                    return new BRView(elem);
                } else if (kind == HTML.Tag.TABLE) {
                    return new javax.swing.text.html.TableView(elem);
                } else if ((kind == HTML.Tag.INPUT) ||
                           (kind == HTML.Tag.SELECT) ||
                           (kind == HTML.Tag.TEXTAREA)) {
                    return new FormView(elem);
                } else if (kind == HTML.Tag.OBJECT) {
                    return new ObjectView(elem);
                } else if (kind == HTML.Tag.FRAMESET) {
                     if (elem.getAttributes().isDefined(HTML.Attribute.ROWS)) {
                         return new FrameSetView(elem, View.Y_AXIS);
                     } else if (elem.getAttributes().isDefined(HTML.Attribute.COLS)) {
                         return new FrameSetView(elem, View.X_AXIS);
                     }
                     throw new RuntimeException("Can't build a"  + kind + ", " + elem + ":" +
                                     "no ROWS or COLS defined.");
                } else if (kind == HTML.Tag.FRAME) {
                    return new FrameView(elem);
                } else if (kind instanceof HTML.UnknownTag) {
                    return new HiddenTagView(elem);
                } else if (kind == HTML.Tag.COMMENT) {
                    return new CommentView(elem);
                } else if (kind == HTML.Tag.HEAD) {
                    return new BlockView(elem, View.X_AXIS) {
                        public float getPreferredSpan(int axis) {
                            return 0;
                        }
                        public float getMinimumSpan(int axis) {
                            return 0;
                        }
                        public float getMaximumSpan(int axis) {
                            return 0;
                        }
                        protected void loadChildren(ViewFactory f) {
                        }
                        public Shape modelToView(int pos, Shape a,
                               Position.Bias b) throws BadLocationException {
                            return a;
                        }
                        public int getNextVisualPositionFrom(int pos,
                                     Position.Bias b, Shape a,
                                     int direction, Position.Bias[] biasRet) {
                            return getElement().getEndOffset();
                        }
                    };
                } else if ((kind == HTML.Tag.TITLE) ||
                           (kind == HTML.Tag.META) ||
                           (kind == HTML.Tag.LINK) ||
                           (kind == HTML.Tag.STYLE) ||
                           (kind == HTML.Tag.SCRIPT) ||
                           (kind == HTML.Tag.AREA) ||
                           (kind == HTML.Tag.MAP) ||
                           (kind == HTML.Tag.PARAM) ||
                           (kind == HTML.Tag.APPLET)) {
                    return new HiddenTagView(elem);
                }
            }
            String nm = (elementName != null) ? (String)elementName :
                                                elem.getName();
            if (nm != null) {
                if (nm.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (nm.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (nm.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (nm.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (nm.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
        static class BodyBlockView extends BlockView implements ComponentListener {
            public BodyBlockView(Element elem) {
                super(elem,View.Y_AXIS);
            }
            protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
                r = super.calculateMajorAxisRequirements(axis, r);
                r.maximum = Integer.MAX_VALUE;
                return r;
            }
            protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
                Container container = getContainer();
                Container parentContainer;
                if (container != null
                    && (container instanceof javax.swing.JEditorPane)
                    && (parentContainer = container.getParent()) != null
                    && (parentContainer instanceof javax.swing.JViewport)) {
                    JViewport viewPort = (JViewport)parentContainer;
                    if (cachedViewPort != null) {
                        JViewport cachedObject = cachedViewPort.get();
                        if (cachedObject != null) {
                            if (cachedObject != viewPort) {
                                cachedObject.removeComponentListener(this);
                            }
                        } else {
                            cachedViewPort = null;
                        }
                    }
                    if (cachedViewPort == null) {
                        viewPort.addComponentListener(this);
                        cachedViewPort = new WeakReference<JViewport>(viewPort);
                    }
                    componentVisibleWidth = viewPort.getExtentSize().width;
                    if (componentVisibleWidth > 0) {
                    Insets insets = container.getInsets();
                    viewVisibleWidth = componentVisibleWidth - insets.left - getLeftInset();
                    targetSpan = Math.min(targetSpan, viewVisibleWidth);
                    }
                } else {
                    if (cachedViewPort != null) {
                        JViewport cachedObject = cachedViewPort.get();
                        if (cachedObject != null) {
                            cachedObject.removeComponentListener(this);
                        }
                        cachedViewPort = null;
                    }
                }
                super.layoutMinorAxis(targetSpan, axis, offsets, spans);
            }
            public void setParent(View parent) {
                if (parent == null) {
                    if (cachedViewPort != null) {
                        Object cachedObject;
                        if ((cachedObject = cachedViewPort.get()) != null) {
                            ((JComponent)cachedObject).removeComponentListener(this);
                        }
                        cachedViewPort = null;
                    }
                }
                super.setParent(parent);
            }
            public void componentResized(ComponentEvent e) {
                if ( !(e.getSource() instanceof JViewport) ) {
                    return;
                }
                JViewport viewPort = (JViewport)e.getSource();
                if (componentVisibleWidth != viewPort.getExtentSize().width) {
                    Document doc = getDocument();
                    if (doc instanceof AbstractDocument) {
                        AbstractDocument document = (AbstractDocument)getDocument();
                        document.readLock();
                        try {
                            layoutChanged(X_AXIS);
                            preferenceChanged(null, true, true);
                        } finally {
                            document.readUnlock();
                        }
                    }
                }
            }
            public void componentHidden(ComponentEvent e) {
            }
            public void componentMoved(ComponentEvent e) {
            }
            public void componentShown(ComponentEvent e) {
            }
            private Reference<JViewport> cachedViewPort = null;
            private boolean isListening = false;
            private int viewVisibleWidth = Integer.MAX_VALUE;
            private int componentVisibleWidth = Integer.MAX_VALUE;
        }
    }
    public static final String  BOLD_ACTION = "html-bold-action";
    public static final String  ITALIC_ACTION = "html-italic-action";
    public static final String  PARA_INDENT_LEFT = "html-para-indent-left";
    public static final String  PARA_INDENT_RIGHT = "html-para-indent-right";
    public static final String  FONT_CHANGE_BIGGER = "html-font-bigger";
    public static final String  FONT_CHANGE_SMALLER = "html-font-smaller";
    public static final String  COLOR_ACTION = "html-color-action";
    public static final String  LOGICAL_STYLE_ACTION = "html-logical-style-action";
    public static final String  IMG_ALIGN_TOP = "html-image-align-top";
    public static final String  IMG_ALIGN_MIDDLE = "html-image-align-middle";
    public static final String  IMG_ALIGN_BOTTOM = "html-image-align-bottom";
    public static final String  IMG_BORDER = "html-image-border";
    private static final String INSERT_TABLE_HTML = "<table border=1><tr><td></td></tr></table>";
    private static final String INSERT_UL_HTML = "<ul><li></li></ul>";
    private static final String INSERT_OL_HTML = "<ol><li></li></ol>";
    private static final String INSERT_HR_HTML = "<hr>";
    private static final String INSERT_PRE_HTML = "<pre></pre>";
    private static final NavigateLinkAction nextLinkAction =
        new NavigateLinkAction("next-link-action");
    private static final NavigateLinkAction previousLinkAction =
        new NavigateLinkAction("previous-link-action");
    private static final ActivateLinkAction activateLinkAction =
        new ActivateLinkAction("activate-link-action");
    private static final Action[] defaultActions = {
        new InsertHTMLTextAction("InsertTable", INSERT_TABLE_HTML,
                                 HTML.Tag.BODY, HTML.Tag.TABLE),
        new InsertHTMLTextAction("InsertTableRow", INSERT_TABLE_HTML,
                                 HTML.Tag.TABLE, HTML.Tag.TR,
                                 HTML.Tag.BODY, HTML.Tag.TABLE),
        new InsertHTMLTextAction("InsertTableDataCell", INSERT_TABLE_HTML,
                                 HTML.Tag.TR, HTML.Tag.TD,
                                 HTML.Tag.BODY, HTML.Tag.TABLE),
        new InsertHTMLTextAction("InsertUnorderedList", INSERT_UL_HTML,
                                 HTML.Tag.BODY, HTML.Tag.UL),
        new InsertHTMLTextAction("InsertUnorderedListItem", INSERT_UL_HTML,
                                 HTML.Tag.UL, HTML.Tag.LI,
                                 HTML.Tag.BODY, HTML.Tag.UL),
        new InsertHTMLTextAction("InsertOrderedList", INSERT_OL_HTML,
                                 HTML.Tag.BODY, HTML.Tag.OL),
        new InsertHTMLTextAction("InsertOrderedListItem", INSERT_OL_HTML,
                                 HTML.Tag.OL, HTML.Tag.LI,
                                 HTML.Tag.BODY, HTML.Tag.OL),
        new InsertHRAction(),
        new InsertHTMLTextAction("InsertPre", INSERT_PRE_HTML,
                                 HTML.Tag.BODY, HTML.Tag.PRE),
        nextLinkAction, previousLinkAction, activateLinkAction,
        new BeginAction(beginAction, false),
        new BeginAction(selectionBeginAction, true)
    };
    private boolean foundLink = false;
    private int prevHypertextOffset = -1;
    private Object linkNavigationTag;
    public static abstract class HTMLTextAction extends StyledTextAction {
        public HTMLTextAction(String name) {
            super(name);
        }
        protected HTMLDocument getHTMLDocument(JEditorPane e) {
            Document d = e.getDocument();
            if (d instanceof HTMLDocument) {
                return (HTMLDocument) d;
            }
            throw new IllegalArgumentException("document must be HTMLDocument");
        }
        protected HTMLEditorKit getHTMLEditorKit(JEditorPane e) {
            EditorKit k = e.getEditorKit();
            if (k instanceof HTMLEditorKit) {
                return (HTMLEditorKit) k;
            }
            throw new IllegalArgumentException("EditorKit must be HTMLEditorKit");
        }
        protected Element[] getElementsAt(HTMLDocument doc, int offset) {
            return getElementsAt(doc.getDefaultRootElement(), offset, 0);
        }
        private Element[] getElementsAt(Element parent, int offset,
                                        int depth) {
            if (parent.isLeaf()) {
                Element[] retValue = new Element[depth + 1];
                retValue[depth] = parent;
                return retValue;
            }
            Element[] retValue = getElementsAt(parent.getElement
                          (parent.getElementIndex(offset)), offset, depth + 1);
            retValue[depth] = parent;
            return retValue;
        }
        protected int elementCountToTag(HTMLDocument doc, int offset,
                                        HTML.Tag tag) {
            int depth = -1;
            Element e = doc.getCharacterElement(offset);
            while (e != null && e.getAttributes().getAttribute
                   (StyleConstants.NameAttribute) != tag) {
                e = e.getParentElement();
                depth++;
            }
            if (e == null) {
                return -1;
            }
            return depth;
        }
        protected Element findElementMatchingTag(HTMLDocument doc, int offset,
                                                 HTML.Tag tag) {
            Element e = doc.getDefaultRootElement();
            Element lastMatch = null;
            while (e != null) {
                if (e.getAttributes().getAttribute
                   (StyleConstants.NameAttribute) == tag) {
                    lastMatch = e;
                }
                e = e.getElement(e.getElementIndex(offset));
            }
            return lastMatch;
        }
    }
    public static class InsertHTMLTextAction extends HTMLTextAction {
        public InsertHTMLTextAction(String name, String html,
                                    HTML.Tag parentTag, HTML.Tag addTag) {
            this(name, html, parentTag, addTag, null, null);
        }
        public InsertHTMLTextAction(String name, String html,
                                    HTML.Tag parentTag,
                                    HTML.Tag addTag,
                                    HTML.Tag alternateParentTag,
                                    HTML.Tag alternateAddTag) {
            this(name, html, parentTag, addTag, alternateParentTag,
                 alternateAddTag, true);
        }
        InsertHTMLTextAction(String name, String html,
                                    HTML.Tag parentTag,
                                    HTML.Tag addTag,
                                    HTML.Tag alternateParentTag,
                                    HTML.Tag alternateAddTag,
                                    boolean adjustSelection) {
            super(name);
            this.html = html;
            this.parentTag = parentTag;
            this.addTag = addTag;
            this.alternateParentTag = alternateParentTag;
            this.alternateAddTag = alternateAddTag;
            this.adjustSelection = adjustSelection;
        }
        protected void insertHTML(JEditorPane editor, HTMLDocument doc,
                                  int offset, String html, int popDepth,
                                  int pushDepth, HTML.Tag addTag) {
            try {
                getHTMLEditorKit(editor).insertHTML(doc, offset, html,
                                                    popDepth, pushDepth,
                                                    addTag);
            } catch (IOException ioe) {
                throw new RuntimeException("Unable to insert: " + ioe);
            } catch (BadLocationException ble) {
                throw new RuntimeException("Unable to insert: " + ble);
            }
        }
        protected void insertAtBoundary(JEditorPane editor, HTMLDocument doc,
                                        int offset, Element insertElement,
                                        String html, HTML.Tag parentTag,
                                        HTML.Tag addTag) {
            insertAtBoundry(editor, doc, offset, insertElement, html,
                            parentTag, addTag);
        }
        @Deprecated
        protected void insertAtBoundry(JEditorPane editor, HTMLDocument doc,
                                       int offset, Element insertElement,
                                       String html, HTML.Tag parentTag,
                                       HTML.Tag addTag) {
            Element e;
            Element commonParent;
            boolean isFirst = (offset == 0);
            if (offset > 0 || insertElement == null) {
                e = doc.getDefaultRootElement();
                while (e != null && e.getStartOffset() != offset &&
                       !e.isLeaf()) {
                    e = e.getElement(e.getElementIndex(offset));
                }
                commonParent = (e != null) ? e.getParentElement() : null;
            }
            else {
                commonParent = insertElement;
            }
            if (commonParent != null) {
                int pops = 0;
                int pushes = 0;
                if (isFirst && insertElement != null) {
                    e = commonParent;
                    while (e != null && !e.isLeaf()) {
                        e = e.getElement(e.getElementIndex(offset));
                        pops++;
                    }
                }
                else {
                    e = commonParent;
                    offset--;
                    while (e != null && !e.isLeaf()) {
                        e = e.getElement(e.getElementIndex(offset));
                        pops++;
                    }
                    e = commonParent;
                    offset++;
                    while (e != null && e != insertElement) {
                        e = e.getElement(e.getElementIndex(offset));
                        pushes++;
                    }
                }
                pops = Math.max(0, pops - 1);
                insertHTML(editor, doc, offset, html, pops, pushes, addTag);
            }
        }
        boolean insertIntoTag(JEditorPane editor, HTMLDocument doc,
                              int offset, HTML.Tag tag, HTML.Tag addTag) {
            Element e = findElementMatchingTag(doc, offset, tag);
            if (e != null && e.getStartOffset() == offset) {
                insertAtBoundary(editor, doc, offset, e, html,
                                 tag, addTag);
                return true;
            }
            else if (offset > 0) {
                int depth = elementCountToTag(doc, offset - 1, tag);
                if (depth != -1) {
                    insertHTML(editor, doc, offset, html, depth, 0, addTag);
                    return true;
                }
            }
            return false;
        }
        void adjustSelection(JEditorPane pane, HTMLDocument doc,
                             int startOffset, int oldLength) {
            int newLength = doc.getLength();
            if (newLength != oldLength && startOffset < newLength) {
                if (startOffset > 0) {
                    String text;
                    try {
                        text = doc.getText(startOffset - 1, 1);
                    } catch (BadLocationException ble) {
                        text = null;
                    }
                    if (text != null && text.length() > 0 &&
                        text.charAt(0) == '\n') {
                        pane.select(startOffset, startOffset);
                    }
                    else {
                        pane.select(startOffset + 1, startOffset + 1);
                    }
                }
                else {
                    pane.select(1, 1);
                }
            }
        }
        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                HTMLDocument doc = getHTMLDocument(editor);
                int offset = editor.getSelectionStart();
                int length = doc.getLength();
                boolean inserted;
                if (!insertIntoTag(editor, doc, offset, parentTag, addTag) &&
                    alternateParentTag != null) {
                    inserted = insertIntoTag(editor, doc, offset,
                                             alternateParentTag,
                                             alternateAddTag);
                }
                else {
                    inserted = true;
                }
                if (adjustSelection && inserted) {
                    adjustSelection(editor, doc, offset, length);
                }
            }
        }
        protected String html;
        protected HTML.Tag parentTag;
        protected HTML.Tag addTag;
        protected HTML.Tag alternateParentTag;
        protected HTML.Tag alternateAddTag;
        boolean adjustSelection;
    }
    static class InsertHRAction extends InsertHTMLTextAction {
        InsertHRAction() {
            super("InsertHR", "<hr>", null, HTML.Tag.IMPLIED, null, null,
                  false);
        }
        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                HTMLDocument doc = getHTMLDocument(editor);
                int offset = editor.getSelectionStart();
                Element paragraph = doc.getParagraphElement(offset);
                if (paragraph.getParentElement() != null) {
                    parentTag = (HTML.Tag)paragraph.getParentElement().
                                  getAttributes().getAttribute
                                  (StyleConstants.NameAttribute);
                    super.actionPerformed(ae);
                }
            }
        }
    }
    static private Object getAttrValue(AttributeSet attr, HTML.Attribute key) {
        Enumeration names = attr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object nextKey = names.nextElement();
            Object nextVal = attr.getAttribute(nextKey);
            if (nextVal instanceof AttributeSet) {
                Object value = getAttrValue((AttributeSet)nextVal, key);
                if (value != null) {
                    return value;
                }
            } else if (nextKey == key) {
                return nextVal;
            }
        }
        return null;
    }
    static class NavigateLinkAction extends TextAction implements CaretListener {
        private static final FocusHighlightPainter focusPainter =
            new FocusHighlightPainter(null);
        private final boolean focusBack;
        public NavigateLinkAction(String actionName) {
            super(actionName);
            focusBack = "previous-link-action".equals(actionName);
        }
        public void caretUpdate(CaretEvent e) {
            Object src = e.getSource();
            if (src instanceof JTextComponent) {
                JTextComponent comp = (JTextComponent) src;
                HTMLEditorKit kit = getHTMLEditorKit(comp);
                if (kit != null && kit.foundLink) {
                    kit.foundLink = false;
                    comp.getAccessibleContext().firePropertyChange(
                        AccessibleContext.ACCESSIBLE_HYPERTEXT_OFFSET,
                        Integer.valueOf(kit.prevHypertextOffset),
                        Integer.valueOf(e.getDot()));
                }
            }
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent comp = getTextComponent(e);
            if (comp == null || comp.isEditable()) {
                return;
            }
            Document doc = comp.getDocument();
            HTMLEditorKit kit = getHTMLEditorKit(comp);
            if (doc == null || kit == null) {
                return;
            }
            ElementIterator ei = new ElementIterator(doc);
            int currentOffset = comp.getCaretPosition();
            int prevStartOffset = -1;
            int prevEndOffset = -1;
            Element nextElement;
            while ((nextElement = ei.next()) != null) {
                String name = nextElement.getName();
                AttributeSet attr = nextElement.getAttributes();
                Object href = getAttrValue(attr, HTML.Attribute.HREF);
                if (!(name.equals(HTML.Tag.OBJECT.toString())) && href == null) {
                    continue;
                }
                int elementOffset = nextElement.getStartOffset();
                if (focusBack) {
                    if (elementOffset >= currentOffset &&
                        prevStartOffset >= 0) {
                        kit.foundLink = true;
                        comp.setCaretPosition(prevStartOffset);
                        moveCaretPosition(comp, kit, prevStartOffset,
                                          prevEndOffset);
                        kit.prevHypertextOffset = prevStartOffset;
                        return;
                    }
                } else { 
                    if (elementOffset > currentOffset) {
                        kit.foundLink = true;
                        comp.setCaretPosition(elementOffset);
                        moveCaretPosition(comp, kit, elementOffset,
                                          nextElement.getEndOffset());
                        kit.prevHypertextOffset = elementOffset;
                        return;
                    }
                }
                prevStartOffset = nextElement.getStartOffset();
                prevEndOffset = nextElement.getEndOffset();
            }
            if (focusBack && prevStartOffset >= 0) {
                kit.foundLink = true;
                comp.setCaretPosition(prevStartOffset);
                moveCaretPosition(comp, kit, prevStartOffset, prevEndOffset);
                kit.prevHypertextOffset = prevStartOffset;
            }
        }
        private void moveCaretPosition(JTextComponent comp, HTMLEditorKit kit,
                                       int mark, int dot) {
            Highlighter h = comp.getHighlighter();
            if (h != null) {
                int p0 = Math.min(dot, mark);
                int p1 = Math.max(dot, mark);
                try {
                    if (kit.linkNavigationTag != null) {
                        h.changeHighlight(kit.linkNavigationTag, p0, p1);
                    } else {
                        kit.linkNavigationTag =
                                h.addHighlight(p0, p1, focusPainter);
                    }
                } catch (BadLocationException e) {
                }
            }
        }
        private HTMLEditorKit getHTMLEditorKit(JTextComponent comp) {
            if (comp instanceof JEditorPane) {
                EditorKit kit = ((JEditorPane) comp).getEditorKit();
                if (kit instanceof HTMLEditorKit) {
                    return (HTMLEditorKit) kit;
                }
            }
            return null;
        }
        static class FocusHighlightPainter extends
            DefaultHighlighter.DefaultHighlightPainter {
            FocusHighlightPainter(Color color) {
                super(color);
            }
            public Shape paintLayer(Graphics g, int offs0, int offs1,
                                    Shape bounds, JTextComponent c, View view) {
                Color color = getColor();
                if (color == null) {
                    g.setColor(c.getSelectionColor());
                }
                else {
                    g.setColor(color);
                }
                if (offs0 == view.getStartOffset() &&
                    offs1 == view.getEndOffset()) {
                    Rectangle alloc;
                    if (bounds instanceof Rectangle) {
                        alloc = (Rectangle)bounds;
                    }
                    else {
                        alloc = bounds.getBounds();
                    }
                    g.drawRect(alloc.x, alloc.y, alloc.width - 1, alloc.height);
                    return alloc;
                }
                else {
                    try {
                        Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                                                       offs1,Position.Bias.Backward,
                                                       bounds);
                        Rectangle r = (shape instanceof Rectangle) ?
                            (Rectangle)shape : shape.getBounds();
                        g.drawRect(r.x, r.y, r.width - 1, r.height);
                        return r;
                    } catch (BadLocationException e) {
                    }
                }
                return null;
            }
        }
    }
    static class ActivateLinkAction extends TextAction {
        public ActivateLinkAction(String actionName) {
            super(actionName);
        }
        private void activateLink(String href, HTMLDocument doc,
                                  JEditorPane editor, int offset) {
            try {
                URL page =
                    (URL)doc.getProperty(Document.StreamDescriptionProperty);
                URL url = new URL(page, href);
                HyperlinkEvent linkEvent = new HyperlinkEvent
                    (editor, HyperlinkEvent.EventType.
                     ACTIVATED, url, url.toExternalForm(),
                     doc.getCharacterElement(offset));
                editor.fireHyperlinkUpdate(linkEvent);
            } catch (MalformedURLException m) {
            }
        }
        private void doObjectAction(JEditorPane editor, Element elem) {
            View view = getView(editor, elem);
            if (view != null && view instanceof ObjectView) {
                Component comp = ((ObjectView)view).getComponent();
                if (comp != null && comp instanceof Accessible) {
                    AccessibleContext ac = comp.getAccessibleContext();
                    if (ac != null) {
                        AccessibleAction aa = ac.getAccessibleAction();
                        if (aa != null) {
                            aa.doAccessibleAction(0);
                        }
                    }
                }
            }
        }
        private View getRootView(JEditorPane editor) {
            return editor.getUI().getRootView(editor);
        }
        private View getView(JEditorPane editor, Element elem) {
            Object lock = lock(editor);
            try {
                View rootView = getRootView(editor);
                int start = elem.getStartOffset();
                if (rootView != null) {
                    return getView(rootView, elem, start);
                }
                return null;
            } finally {
                unlock(lock);
            }
        }
        private View getView(View parent, Element elem, int start) {
            if (parent.getElement() == elem) {
                return parent;
            }
            int index = parent.getViewIndex(start, Position.Bias.Forward);
            if (index != -1 && index < parent.getViewCount()) {
                return getView(parent.getView(index), elem, start);
            }
            return null;
        }
        private Object lock(JEditorPane editor) {
            Document document = editor.getDocument();
            if (document instanceof AbstractDocument) {
                ((AbstractDocument)document).readLock();
                return document;
            }
            return null;
        }
        private void unlock(Object key) {
            if (key != null) {
                ((AbstractDocument)key).readUnlock();
            }
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent c = getTextComponent(e);
            if (c.isEditable() || !(c instanceof JEditorPane)) {
                return;
            }
            JEditorPane editor = (JEditorPane)c;
            Document d = editor.getDocument();
            if (d == null || !(d instanceof HTMLDocument)) {
                return;
            }
            HTMLDocument doc = (HTMLDocument)d;
            ElementIterator ei = new ElementIterator(doc);
            int currentOffset = editor.getCaretPosition();
            String urlString = null;
            String objString = null;
            Element currentElement;
            while ((currentElement = ei.next()) != null) {
                String name = currentElement.getName();
                AttributeSet attr = currentElement.getAttributes();
                Object href = getAttrValue(attr, HTML.Attribute.HREF);
                if (href != null) {
                    if (currentOffset >= currentElement.getStartOffset() &&
                        currentOffset <= currentElement.getEndOffset()) {
                        activateLink((String)href, doc, editor, currentOffset);
                        return;
                    }
                } else if (name.equals(HTML.Tag.OBJECT.toString())) {
                    Object obj = getAttrValue(attr, HTML.Attribute.CLASSID);
                    if (obj != null) {
                        if (currentOffset >= currentElement.getStartOffset() &&
                            currentOffset <= currentElement.getEndOffset()) {
                            doObjectAction(editor, currentElement);
                            return;
                        }
                    }
                }
            }
        }
    }
    private static int getBodyElementStart(JTextComponent comp) {
        Element rootElement = comp.getDocument().getRootElements()[0];
        for (int i = 0; i < rootElement.getElementCount(); i++) {
            Element currElement = rootElement.getElement(i);
            if("body".equals(currElement.getName())) {
                return currElement.getStartOffset();
            }
        }
        return 0;
    }
    static class BeginAction extends TextAction {
        BeginAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            int bodyStart = getBodyElementStart(target);
            if (target != null) {
                if (select) {
                    target.moveCaretPosition(bodyStart);
                } else {
                    target.setCaretPosition(bodyStart);
                }
            }
        }
        private boolean select;
    }
}
