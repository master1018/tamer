public abstract class BasicTextUI extends TextUI implements ViewFactory {
    public BasicTextUI() {
        painted = false;
    }
    protected Caret createCaret() {
        return new BasicCaret();
    }
    protected Highlighter createHighlighter() {
        return new BasicHighlighter();
    }
    protected String getKeymapName() {
        String nm = getClass().getName();
        int index = nm.lastIndexOf('.');
        if (index >= 0) {
            nm = nm.substring(index+1, nm.length());
        }
        return nm;
    }
    protected Keymap createKeymap() {
        String nm = getKeymapName();
        Keymap map = JTextComponent.getKeymap(nm);
        if (map == null) {
            Keymap parent = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
            map = JTextComponent.addKeymap(nm, parent);
            String prefix = getPropertyPrefix();
            Object o = DefaultLookup.get(editor, this,
                prefix + ".keyBindings");
            if ((o != null) && (o instanceof JTextComponent.KeyBinding[])) {
                JTextComponent.KeyBinding[] bindings = (JTextComponent.KeyBinding[]) o;
                JTextComponent.loadKeymap(map, bindings, getComponent().getActions());
            }
        }
        return map;
    }
    protected void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("editable") ||
                evt.getPropertyName().equals("enabled")) {
            updateBackground((JTextComponent)evt.getSource());
        }
    }
    private void updateBackground(JTextComponent c) {
        if (this instanceof SynthUI || (c instanceof JTextArea)) {
            return;
        }
        Color background = c.getBackground();
        if (background instanceof UIResource) {
            String prefix = getPropertyPrefix();
            Color disabledBG =
                DefaultLookup.getColor(c, this, prefix + ".disabledBackground", null);
            Color inactiveBG =
                DefaultLookup.getColor(c, this, prefix + ".inactiveBackground", null);
            Color bg =
                DefaultLookup.getColor(c, this, prefix + ".background", null);
            if ((c instanceof JTextArea || c instanceof JEditorPane)
                    && background != disabledBG
                    && background != inactiveBG
                    && background != bg) {
                return;
            }
            Color newColor = null;
            if (!c.isEnabled()) {
                newColor = disabledBG;
            }
            if (newColor == null && !c.isEditable()) {
                newColor = inactiveBG;
            }
            if (newColor == null) {
                newColor = bg;
            }
            if (newColor != null && newColor != background) {
                c.setBackground(newColor);
            }
        }
    }
    protected abstract String getPropertyPrefix();
    protected void installDefaults()
    {
        String prefix = getPropertyPrefix();
        Font f = editor.getFont();
        if ((f == null) || (f instanceof UIResource)) {
            editor.setFont(UIManager.getFont(prefix + ".font"));
        }
        Color bg = editor.getBackground();
        if ((bg == null) || (bg instanceof UIResource)) {
            editor.setBackground(UIManager.getColor(prefix + ".background"));
        }
        Color fg = editor.getForeground();
        if ((fg == null) || (fg instanceof UIResource)) {
            editor.setForeground(UIManager.getColor(prefix + ".foreground"));
        }
        Color color = editor.getCaretColor();
        if ((color == null) || (color instanceof UIResource)) {
            editor.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
        }
        Color s = editor.getSelectionColor();
        if ((s == null) || (s instanceof UIResource)) {
            editor.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
        }
        Color sfg = editor.getSelectedTextColor();
        if ((sfg == null) || (sfg instanceof UIResource)) {
            editor.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
        }
        Color dfg = editor.getDisabledTextColor();
        if ((dfg == null) || (dfg instanceof UIResource)) {
            editor.setDisabledTextColor(UIManager.getColor(prefix + ".inactiveForeground"));
        }
        Border b = editor.getBorder();
        if ((b == null) || (b instanceof UIResource)) {
            editor.setBorder(UIManager.getBorder(prefix + ".border"));
        }
        Insets margin = editor.getMargin();
        if (margin == null || margin instanceof UIResource) {
            editor.setMargin(UIManager.getInsets(prefix + ".margin"));
        }
        updateCursor();
    }
    private void installDefaults2() {
        editor.addMouseListener(dragListener);
        editor.addMouseMotionListener(dragListener);
        String prefix = getPropertyPrefix();
        Caret caret = editor.getCaret();
        if (caret == null || caret instanceof UIResource) {
            caret = createCaret();
            editor.setCaret(caret);
            int rate = DefaultLookup.getInt(getComponent(), this, prefix + ".caretBlinkRate", 500);
            caret.setBlinkRate(rate);
        }
        Highlighter highlighter = editor.getHighlighter();
        if (highlighter == null || highlighter instanceof UIResource) {
            editor.setHighlighter(createHighlighter());
        }
        TransferHandler th = editor.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            editor.setTransferHandler(getTransferHandler());
        }
    }
    protected void uninstallDefaults()
    {
        editor.removeMouseListener(dragListener);
        editor.removeMouseMotionListener(dragListener);
        if (editor.getCaretColor() instanceof UIResource) {
            editor.setCaretColor(null);
        }
        if (editor.getSelectionColor() instanceof UIResource) {
            editor.setSelectionColor(null);
        }
        if (editor.getDisabledTextColor() instanceof UIResource) {
            editor.setDisabledTextColor(null);
        }
        if (editor.getSelectedTextColor() instanceof UIResource) {
            editor.setSelectedTextColor(null);
        }
        if (editor.getBorder() instanceof UIResource) {
            editor.setBorder(null);
        }
        if (editor.getMargin() instanceof UIResource) {
            editor.setMargin(null);
        }
        if (editor.getCaret() instanceof UIResource) {
            editor.setCaret(null);
        }
        if (editor.getHighlighter() instanceof UIResource) {
            editor.setHighlighter(null);
        }
        if (editor.getTransferHandler() instanceof UIResource) {
            editor.setTransferHandler(null);
        }
        if (editor.getCursor() instanceof UIResource) {
            editor.setCursor(null);
        }
    }
    protected void installListeners() {
    }
    protected void uninstallListeners() {
    }
    protected void installKeyboardActions() {
        editor.setKeymap(createKeymap());
        InputMap km = getInputMap();
        if (km != null) {
            SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED,
                                             km);
        }
        ActionMap map = getActionMap();
        if (map != null) {
            SwingUtilities.replaceUIActionMap(editor, map);
        }
        updateFocusAcceleratorBinding(false);
    }
    InputMap getInputMap() {
        InputMap map = new InputMapUIResource();
        InputMap shared =
            (InputMap)DefaultLookup.get(editor, this,
            getPropertyPrefix() + ".focusInputMap");
        if (shared != null) {
            map.setParent(shared);
        }
        return map;
    }
    void updateFocusAcceleratorBinding(boolean changed) {
        char accelerator = editor.getFocusAccelerator();
        if (changed || accelerator != '\0') {
            InputMap km = SwingUtilities.getUIInputMap
                        (editor, JComponent.WHEN_IN_FOCUSED_WINDOW);
            if (km == null && accelerator != '\0') {
                km = new ComponentInputMapUIResource(editor);
                SwingUtilities.replaceUIInputMap(editor, JComponent.
                                                 WHEN_IN_FOCUSED_WINDOW, km);
                ActionMap am = getActionMap();
                SwingUtilities.replaceUIActionMap(editor, am);
            }
            if (km != null) {
                km.clear();
                if (accelerator != '\0') {
                    km.put(KeyStroke.getKeyStroke(accelerator,
                                                  ActionEvent.ALT_MASK),
                           "requestFocus");
                }
            }
        }
    }
    void updateFocusTraversalKeys() {
        EditorKit editorKit = getEditorKit(editor);
        if ( editorKit != null
             && editorKit instanceof DefaultEditorKit) {
            Set<AWTKeyStroke> storedForwardTraversalKeys = editor.
                getFocusTraversalKeys(KeyboardFocusManager.
                                      FORWARD_TRAVERSAL_KEYS);
            Set<AWTKeyStroke> storedBackwardTraversalKeys = editor.
                getFocusTraversalKeys(KeyboardFocusManager.
                                      BACKWARD_TRAVERSAL_KEYS);
            Set<AWTKeyStroke> forwardTraversalKeys =
                new HashSet<AWTKeyStroke>(storedForwardTraversalKeys);
            Set<AWTKeyStroke> backwardTraversalKeys =
                new HashSet<AWTKeyStroke>(storedBackwardTraversalKeys);
            if (editor.isEditable()) {
                forwardTraversalKeys.
                    remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
                backwardTraversalKeys.
                    remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                  InputEvent.SHIFT_MASK));
            } else {
                forwardTraversalKeys.add(KeyStroke.
                                         getKeyStroke(KeyEvent.VK_TAB, 0));
                backwardTraversalKeys.
                    add(KeyStroke.
                        getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
            }
            LookAndFeel.installProperty(editor,
                                        "focusTraversalKeysForward",
                                         forwardTraversalKeys);
            LookAndFeel.installProperty(editor,
                                        "focusTraversalKeysBackward",
                                         backwardTraversalKeys);
        }
    }
    private void updateCursor() {
        if ((! editor.isCursorSet())
               || editor.getCursor() instanceof UIResource) {
            Cursor cursor = (editor.isEditable()) ? textCursor : null;
            editor.setCursor(cursor);
        }
    }
    TransferHandler getTransferHandler() {
        return defaultTransferHandler;
    }
    ActionMap getActionMap() {
        String mapName = getPropertyPrefix() + ".actionMap";
        ActionMap map = (ActionMap)UIManager.get(mapName);
        if (map == null) {
            map = createActionMap();
            if (map != null) {
                UIManager.getLookAndFeelDefaults().put(mapName, map);
            }
        }
        ActionMap componentMap = new ActionMapUIResource();
        componentMap.put("requestFocus", new FocusAction());
        if (getEditorKit(editor) instanceof DefaultEditorKit) {
            if (map != null) {
                Object obj = map.get(DefaultEditorKit.insertBreakAction);
                if (obj != null
                    && obj instanceof DefaultEditorKit.InsertBreakAction) {
                    Action action =  new TextActionWrapper((TextAction)obj);
                    componentMap.put(action.getValue(Action.NAME),action);
                }
            }
        }
        if (map != null) {
            componentMap.setParent(map);
        }
        return componentMap;
    }
    ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        Action[] actions = editor.getActions();
        int n = actions.length;
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.put(a.getValue(Action.NAME), a);
        }
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
        return map;
    }
    protected void uninstallKeyboardActions() {
        editor.setKeymap(null);
        SwingUtilities.replaceUIInputMap(editor, JComponent.
                                         WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIActionMap(editor, null);
    }
    protected void paintBackground(Graphics g) {
        g.setColor(editor.getBackground());
        g.fillRect(0, 0, editor.getWidth(), editor.getHeight());
    }
    protected final JTextComponent getComponent() {
        return editor;
    }
    protected void modelChanged() {
        ViewFactory f = rootView.getViewFactory();
        Document doc = editor.getDocument();
        Element elem = doc.getDefaultRootElement();
        setView(f.create(elem));
    }
    protected final void setView(View v) {
        rootView.setView(v);
        painted = false;
        editor.revalidate();
        editor.repaint();
    }
    protected void paintSafely(Graphics g) {
        painted = true;
        Highlighter highlighter = editor.getHighlighter();
        Caret caret = editor.getCaret();
        if (editor.isOpaque()) {
            paintBackground(g);
        }
        if (highlighter != null) {
            highlighter.paint(g);
        }
        Rectangle alloc = getVisibleEditorRect();
        if (alloc != null) {
            rootView.paint(g, alloc);
        }
        if (caret != null) {
            caret.paint(g);
        }
        if (dropCaret != null) {
            dropCaret.paint(g);
        }
    }
    public void installUI(JComponent c) {
        if (c instanceof JTextComponent) {
            editor = (JTextComponent) c;
            LookAndFeel.installProperty(editor, "opaque", Boolean.TRUE);
            LookAndFeel.installProperty(editor, "autoscrolls", Boolean.TRUE);
            installDefaults();
            installDefaults2();
            editor.addPropertyChangeListener(updateHandler);
            Document doc = editor.getDocument();
            if (doc == null) {
                editor.setDocument(getEditorKit(editor).createDefaultDocument());
            } else {
                doc.addDocumentListener(updateHandler);
                modelChanged();
            }
            installListeners();
            installKeyboardActions();
            LayoutManager oldLayout = editor.getLayout();
            if ((oldLayout == null) || (oldLayout instanceof UIResource)) {
                editor.setLayout(updateHandler);
            }
            updateBackground(editor);
        } else {
            throw new Error("TextUI needs JTextComponent");
        }
    }
    public void uninstallUI(JComponent c) {
        editor.removePropertyChangeListener(updateHandler);
        editor.getDocument().removeDocumentListener(updateHandler);
        painted = false;
        uninstallDefaults();
        rootView.setView(null);
        c.removeAll();
        LayoutManager lm = c.getLayout();
        if (lm instanceof UIResource) {
            c.setLayout(null);
        }
        uninstallKeyboardActions();
        uninstallListeners();
        editor = null;
    }
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }
    public final void paint(Graphics g, JComponent c) {
        if ((rootView.getViewCount() > 0) && (rootView.getView(0) != null)) {
            Document doc = editor.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                paintSafely(g);
            } finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
    }
    public Dimension getPreferredSize(JComponent c) {
        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = c.getSize();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            if ((d.width > (i.left + i.right)) && (d.height > (i.top + i.bottom))) {
                rootView.setSize(d.width - i.left - i.right, d.height - i.top - i.bottom);
            }
            else if (d.width == 0 && d.height == 0) {
                rootView.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
            d.width = (int) Math.min((long) rootView.getPreferredSpan(View.X_AXIS) +
                                     (long) i.left + (long) i.right, Integer.MAX_VALUE);
            d.height = (int) Math.min((long) rootView.getPreferredSpan(View.Y_AXIS) +
                                      (long) i.top + (long) i.bottom, Integer.MAX_VALUE);
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }
    public Dimension getMinimumSize(JComponent c) {
        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            d.width = (int) rootView.getMinimumSpan(View.X_AXIS) + i.left + i.right;
            d.height = (int)  rootView.getMinimumSpan(View.Y_AXIS) + i.top + i.bottom;
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }
    public Dimension getMaximumSize(JComponent c) {
        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            d.width = (int) Math.min((long) rootView.getMaximumSpan(View.X_AXIS) +
                                     (long) i.left + (long) i.right, Integer.MAX_VALUE);
            d.height = (int) Math.min((long) rootView.getMaximumSpan(View.Y_AXIS) +
                                      (long) i.top + (long) i.bottom, Integer.MAX_VALUE);
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return d;
    }
    protected Rectangle getVisibleEditorRect() {
        Rectangle alloc = editor.getBounds();
        if ((alloc.width > 0) && (alloc.height > 0)) {
            alloc.x = alloc.y = 0;
            Insets insets = editor.getInsets();
            alloc.x += insets.left;
            alloc.y += insets.top;
            alloc.width -= insets.left + insets.right;
            alloc.height -= insets.top + insets.bottom;
            return alloc;
        }
        return null;
    }
    public Rectangle modelToView(JTextComponent tc, int pos) throws BadLocationException {
        return modelToView(tc, pos, Position.Bias.Forward);
    }
    public Rectangle modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
        Document doc = editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            Rectangle alloc = getVisibleEditorRect();
            if (alloc != null) {
                rootView.setSize(alloc.width, alloc.height);
                Shape s = rootView.modelToView(pos, alloc, bias);
                if (s != null) {
                  return s.getBounds();
                }
            }
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return null;
    }
    public int viewToModel(JTextComponent tc, Point pt) {
        return viewToModel(tc, pt, discardBias);
    }
    public int viewToModel(JTextComponent tc, Point pt,
                           Position.Bias[] biasReturn) {
        int offs = -1;
        Document doc = editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            Rectangle alloc = getVisibleEditorRect();
            if (alloc != null) {
                rootView.setSize(alloc.width, alloc.height);
                offs = rootView.viewToModel(pt.x, pt.y, alloc, biasReturn);
            }
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return offs;
    }
    public int getNextVisualPositionFrom(JTextComponent t, int pos,
                    Position.Bias b, int direction, Position.Bias[] biasRet)
                    throws BadLocationException{
        Document doc = editor.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            if (painted) {
                Rectangle alloc = getVisibleEditorRect();
                if (alloc != null) {
                    rootView.setSize(alloc.width, alloc.height);
                }
                return rootView.getNextVisualPositionFrom(pos, b, alloc, direction,
                                                          biasRet);
            }
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readUnlock();
            }
        }
        return -1;
    }
    public void damageRange(JTextComponent tc, int p0, int p1) {
        damageRange(tc, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
    }
    public void damageRange(JTextComponent t, int p0, int p1,
                            Position.Bias p0Bias, Position.Bias p1Bias) {
        if (painted) {
            Rectangle alloc = getVisibleEditorRect();
            if (alloc != null) {
                Document doc = t.getDocument();
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readLock();
                }
                try {
                    rootView.setSize(alloc.width, alloc.height);
                    Shape toDamage = rootView.modelToView(p0, p0Bias,
                            p1, p1Bias, alloc);
                    Rectangle rect = (toDamage instanceof Rectangle) ?
                            (Rectangle)toDamage : toDamage.getBounds();
                    editor.repaint(rect.x, rect.y, rect.width, rect.height);
                } catch (BadLocationException e) {
                } finally {
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument)doc).readUnlock();
                    }
                }
            }
        }
    }
    public EditorKit getEditorKit(JTextComponent tc) {
        return defaultKit;
    }
    public View getRootView(JTextComponent tc) {
        return rootView;
    }
    public String getToolTipText(JTextComponent t, Point pt) {
        if (!painted) {
            return null;
        }
        Document doc = editor.getDocument();
        String tt = null;
        Rectangle alloc = getVisibleEditorRect();
        if (alloc != null) {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                tt = rootView.getToolTipText(pt.x, pt.y, alloc);
            } finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
        return tt;
    }
    public View create(Element elem) {
        return null;
    }
    public View create(Element elem, int p0, int p1) {
        return null;
    }
    public static class BasicCaret extends DefaultCaret implements UIResource {}
    public static class BasicHighlighter extends DefaultHighlighter implements UIResource {}
    static class BasicCursor extends Cursor implements UIResource {
        BasicCursor(int type) {
            super(type);
        }
        BasicCursor(String name) {
            super(name);
        }
    }
    private static BasicCursor textCursor = new BasicCursor(Cursor.TEXT_CURSOR);
    private static final EditorKit defaultKit = new DefaultEditorKit();
    transient JTextComponent editor;
    transient boolean painted;
    transient RootView rootView = new RootView();
    transient UpdateHandler updateHandler = new UpdateHandler();
    private static final TransferHandler defaultTransferHandler = new TextTransferHandler();
    private final DragListener dragListener = getDragListener();
    private static final Position.Bias[] discardBias = new Position.Bias[1];
    private DefaultCaret dropCaret;
    class RootView extends View {
        RootView() {
            super(null);
        }
        void setView(View v) {
            View oldView = view;
            view = null;
            if (oldView != null) {
                oldView.setParent(null);
            }
            if (v != null) {
                v.setParent(this);
            }
            view = v;
        }
        public AttributeSet getAttributes() {
            return null;
        }
        public float getPreferredSpan(int axis) {
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
            return 10;
        }
        public float getMinimumSpan(int axis) {
            if (view != null) {
                return view.getMinimumSpan(axis);
            }
            return 10;
        }
        public float getMaximumSpan(int axis) {
            return Integer.MAX_VALUE;
        }
        public void preferenceChanged(View child, boolean width, boolean height) {
            editor.revalidate();
        }
        public float getAlignment(int axis) {
            if (view != null) {
                return view.getAlignment(axis);
            }
            return 0;
        }
        public void paint(Graphics g, Shape allocation) {
            if (view != null) {
                Rectangle alloc = (allocation instanceof Rectangle) ?
                          (Rectangle)allocation : allocation.getBounds();
                setSize(alloc.width, alloc.height);
                view.paint(g, allocation);
            }
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
        public int getViewIndex(int pos, Position.Bias b) {
            return 0;
        }
        public Shape getChildAllocation(int index, Shape a) {
            return a;
        }
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (view != null) {
                return view.modelToView(pos, a, b);
            }
            return null;
        }
        public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
            if (view != null) {
                return view.modelToView(p0, b0, p1, b1, a);
            }
            return null;
        }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (view != null) {
                int retValue = view.viewToModel(x, y, a, bias);
                return retValue;
            }
            return -1;
        }
        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                                             int direction,
                                             Position.Bias[] biasRet)
            throws BadLocationException {
            if( view != null ) {
                int nextPos = view.getNextVisualPositionFrom(pos, b, a,
                                                     direction, biasRet);
                if(nextPos != -1) {
                    pos = nextPos;
                }
                else {
                    biasRet[0] = b;
                }
            }
            return pos;
        }
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.insertUpdate(e, a, f);
            }
        }
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.removeUpdate(e, a, f);
            }
        }
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.changedUpdate(e, a, f);
            }
        }
        public Document getDocument() {
            return editor.getDocument();
        }
        public int getStartOffset() {
            if (view != null) {
                return view.getStartOffset();
            }
            return getElement().getStartOffset();
        }
        public int getEndOffset() {
            if (view != null) {
                return view.getEndOffset();
            }
            return getElement().getEndOffset();
        }
        public Element getElement() {
            if (view != null) {
                return view.getElement();
            }
            return editor.getDocument().getDefaultRootElement();
        }
        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view");
        }
        public int getResizeWeight(int axis) {
            if (view != null) {
                return view.getResizeWeight(axis);
            }
            return 0;
        }
        public void setSize(float width, float height) {
            if (view != null) {
                view.setSize(width, height);
            }
        }
        public Container getContainer() {
            return editor;
        }
        public ViewFactory getViewFactory() {
            EditorKit kit = getEditorKit(editor);
            ViewFactory f = kit.getViewFactory();
            if (f != null) {
                return f;
            }
            return BasicTextUI.this;
        }
        private View view;
    }
    class UpdateHandler implements PropertyChangeListener, DocumentListener, LayoutManager2, UIResource {
        public final void propertyChange(PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            String propertyName = evt.getPropertyName();
            if ((oldValue instanceof Document) || (newValue instanceof Document)) {
                if (oldValue != null) {
                    ((Document)oldValue).removeDocumentListener(this);
                    i18nView = false;
                }
                if (newValue != null) {
                    ((Document)newValue).addDocumentListener(this);
                    if ("document" == propertyName) {
                        setView(null);
                        BasicTextUI.this.propertyChange(evt);
                        modelChanged();
                        return;
                    }
                }
                modelChanged();
            }
            if ("focusAccelerator" == propertyName) {
                updateFocusAcceleratorBinding(true);
            } else if ("componentOrientation" == propertyName) {
                modelChanged();
            } else if ("font" == propertyName) {
                modelChanged();
            } else if ("dropLocation" == propertyName) {
                dropIndexChanged();
            } else if ("editable" == propertyName) {
                updateCursor();
                modelChanged();
            }
            BasicTextUI.this.propertyChange(evt);
        }
        private void dropIndexChanged() {
            if (editor.getDropMode() == DropMode.USE_SELECTION) {
                return;
            }
            JTextComponent.DropLocation dropLocation = editor.getDropLocation();
            if (dropLocation == null) {
                if (dropCaret != null) {
                    dropCaret.deinstall(editor);
                    editor.repaint(dropCaret);
                    dropCaret = null;
                }
            } else {
                if (dropCaret == null) {
                    dropCaret = new BasicCaret();
                    dropCaret.install(editor);
                    dropCaret.setVisible(true);
                }
                dropCaret.setDot(dropLocation.getIndex(),
                                 dropLocation.getBias());
            }
        }
        public final void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            Object o = doc.getProperty("i18n");
            if (o instanceof Boolean) {
                Boolean i18nFlag = (Boolean) o;
                if (i18nFlag.booleanValue() != i18nView) {
                    i18nView = i18nFlag.booleanValue();
                    modelChanged();
                    return;
                }
            }
            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.insertUpdate(e, alloc, rootView.getViewFactory());
        }
        public final void removeUpdate(DocumentEvent e) {
            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.removeUpdate(e, alloc, rootView.getViewFactory());
        }
        public final void changedUpdate(DocumentEvent e) {
            Rectangle alloc = (painted) ? getVisibleEditorRect() : null;
            rootView.changedUpdate(e, alloc, rootView.getViewFactory());
        }
        public void addLayoutComponent(String name, Component comp) {
        }
        public void removeLayoutComponent(Component comp) {
            if (constraints != null) {
                constraints.remove(comp);
            }
        }
        public Dimension preferredLayoutSize(Container parent) {
            return null;
        }
        public Dimension minimumLayoutSize(Container parent) {
            return null;
        }
        public void layoutContainer(Container parent) {
            if ((constraints != null) && (! constraints.isEmpty())) {
                Rectangle alloc = getVisibleEditorRect();
                if (alloc != null) {
                    Document doc = editor.getDocument();
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument)doc).readLock();
                    }
                    try {
                        rootView.setSize(alloc.width, alloc.height);
                        Enumeration<Component> components = constraints.keys();
                        while (components.hasMoreElements()) {
                            Component comp = components.nextElement();
                            View v = (View) constraints.get(comp);
                            Shape ca = calculateViewPosition(alloc, v);
                            if (ca != null) {
                                Rectangle compAlloc = (ca instanceof Rectangle) ?
                                    (Rectangle) ca : ca.getBounds();
                                comp.setBounds(compAlloc);
                            }
                        }
                    } finally {
                        if (doc instanceof AbstractDocument) {
                            ((AbstractDocument)doc).readUnlock();
                        }
                    }
                }
            }
        }
        Shape calculateViewPosition(Shape alloc, View v) {
            int pos = v.getStartOffset();
            View child = null;
            for (View parent = rootView; (parent != null) && (parent != v); parent = child) {
                int index = parent.getViewIndex(pos, Position.Bias.Forward);
                alloc = parent.getChildAllocation(index, alloc);
                child = parent.getView(index);
            }
            return (child != null) ? alloc : null;
        }
        public void addLayoutComponent(Component comp, Object constraint) {
            if (constraint instanceof View) {
                if (constraints == null) {
                    constraints = new Hashtable<Component, Object>(7);
                }
                constraints.put(comp, constraint);
            }
        }
        public Dimension maximumLayoutSize(Container target) {
            return null;
        }
        public float getLayoutAlignmentX(Container target) {
            return 0.5f;
        }
        public float getLayoutAlignmentY(Container target) {
            return 0.5f;
        }
        public void invalidateLayout(Container target) {
        }
        private Hashtable<Component, Object> constraints;
        private boolean i18nView = false;
    }
    class TextActionWrapper extends TextAction {
        public TextActionWrapper(TextAction action) {
            super((String)action.getValue(Action.NAME));
            this.action = action;
        }
        public void actionPerformed(ActionEvent e) {
            action.actionPerformed(e);
        }
        public boolean isEnabled() {
            return (editor == null || editor.isEditable()) ? action.isEnabled() : false;
        }
        TextAction action = null;
    }
    class FocusAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            editor.requestFocus();
        }
        public boolean isEnabled() {
            return editor.isEditable();
        }
    }
    private static DragListener getDragListener() {
        synchronized(DragListener.class) {
            DragListener listener =
                (DragListener)AppContext.getAppContext().
                    get(DragListener.class);
            if (listener == null) {
                listener = new DragListener();
                AppContext.getAppContext().put(DragListener.class, listener);
            }
            return listener;
        }
    }
    static class DragListener extends MouseInputAdapter
                              implements BeforeDrag {
        private boolean dragStarted;
        public void dragStarting(MouseEvent me) {
            dragStarted = true;
        }
        public void mousePressed(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled()) {
                dragStarted = false;
                if (isDragPossible(e) && DragRecognitionSupport.mousePressed(e)) {
                    e.consume();
                }
            }
        }
        public void mouseReleased(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled()) {
                if (dragStarted) {
                    e.consume();
                }
                DragRecognitionSupport.mouseReleased(e);
            }
        }
        public void mouseDragged(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.getDragEnabled()) {
                if (dragStarted || DragRecognitionSupport.mouseDragged(e, this)) {
                    e.consume();
                }
            }
        }
        protected boolean isDragPossible(MouseEvent e) {
            JTextComponent c = (JTextComponent)e.getSource();
            if (c.isEnabled()) {
                Caret caret = c.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                if (dot != mark) {
                    Point p = new Point(e.getX(), e.getY());
                    int pos = c.viewToModel(p);
                    int p0 = Math.min(dot, mark);
                    int p1 = Math.max(dot, mark);
                    if ((pos >= p0) && (pos < p1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    static class TextTransferHandler extends TransferHandler implements UIResource {
        private JTextComponent exportComp;
        private boolean shouldRemove;
        private int p0;
        private int p1;
        private boolean modeBetween = false;
        private boolean isDrop = false;
        private int dropAction = MOVE;
        private Position.Bias dropBias;
        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
            DataFlavor plainFlavor = null;
            DataFlavor refFlavor = null;
            DataFlavor stringFlavor = null;
            if (c instanceof JEditorPane) {
                for (int i = 0; i < flavors.length; i++) {
                    String mime = flavors[i].getMimeType();
                    if (mime.startsWith(((JEditorPane)c).getEditorKit().getContentType())) {
                        return flavors[i];
                    } else if (plainFlavor == null && mime.startsWith("text/plain")) {
                        plainFlavor = flavors[i];
                    } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                                                 && flavors[i].getRepresentationClass() == java.lang.String.class) {
                        refFlavor = flavors[i];
                    } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                        stringFlavor = flavors[i];
                    }
                }
                if (plainFlavor != null) {
                    return plainFlavor;
                } else if (refFlavor != null) {
                    return refFlavor;
                } else if (stringFlavor != null) {
                    return stringFlavor;
                }
                return null;
            }
            for (int i = 0; i < flavors.length; i++) {
                String mime = flavors[i].getMimeType();
                if (mime.startsWith("text/plain")) {
                    return flavors[i];
                } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref")
                                             && flavors[i].getRepresentationClass() == java.lang.String.class) {
                    refFlavor = flavors[i];
                } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                    stringFlavor = flavors[i];
                }
            }
            if (refFlavor != null) {
                return refFlavor;
            } else if (stringFlavor != null) {
                return stringFlavor;
            }
            return null;
        }
        protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead)
                                               throws BadLocationException, IOException {
            if (useRead) {
                int startPosition = c.getSelectionStart();
                int endPosition = c.getSelectionEnd();
                int length = endPosition - startPosition;
                EditorKit kit = c.getUI().getEditorKit(c);
                Document doc = c.getDocument();
                if (length > 0) {
                    doc.remove(startPosition, length);
                }
                kit.read(in, doc, startPosition);
            } else {
                char[] buff = new char[1024];
                int nch;
                boolean lastWasCR = false;
                int last;
                StringBuffer sbuff = null;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    if (sbuff == null) {
                        sbuff = new StringBuffer(nch);
                    }
                    last = 0;
                    for(int counter = 0; counter < nch; counter++) {
                        switch(buff[counter]) {
                        case '\r':
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                            } else {
                                lastWasCR = true;
                            }
                            break;
                        case '\n':
                            if (lastWasCR) {
                                if (counter > (last + 1)) {
                                    sbuff.append(buff, last, counter - last - 1);
                                }
                                lastWasCR = false;
                                last = counter;
                            }
                            break;
                        default:
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                                lastWasCR = false;
                            }
                            break;
                        }
                    }
                    if (last < nch) {
                        if (lastWasCR) {
                            if (last < (nch - 1)) {
                                sbuff.append(buff, last, nch - last - 1);
                            }
                        } else {
                            sbuff.append(buff, last, nch - last);
                        }
                    }
                }
                if (lastWasCR) {
                    sbuff.append('\n');
                }
                c.replaceSelection(sbuff != null ? sbuff.toString() : "");
            }
        }
        public int getSourceActions(JComponent c) {
            if (c instanceof JPasswordField &&
                c.getClientProperty("JPasswordField.cutCopyAllowed") !=
                Boolean.TRUE) {
                return NONE;
            }
            return ((JTextComponent)c).isEditable() ? COPY_OR_MOVE : COPY;
        }
        protected Transferable createTransferable(JComponent comp) {
            exportComp = (JTextComponent)comp;
            shouldRemove = true;
            p0 = exportComp.getSelectionStart();
            p1 = exportComp.getSelectionEnd();
            return (p0 != p1) ? (new TextTransferable(exportComp, p0, p1)) : null;
        }
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (shouldRemove && action == MOVE) {
                TextTransferable t = (TextTransferable)data;
                t.removeText();
            }
            exportComp = null;
        }
        public boolean importData(TransferSupport support) {
            isDrop = support.isDrop();
            if (isDrop) {
                modeBetween =
                    ((JTextComponent)support.getComponent()).getDropMode() == DropMode.INSERT;
                dropBias = ((JTextComponent.DropLocation)support.getDropLocation()).getBias();
                dropAction = support.getDropAction();
            }
            try {
                return super.importData(support);
            } finally {
                isDrop = false;
                modeBetween = false;
                dropBias = null;
                dropAction = MOVE;
            }
        }
        public boolean importData(JComponent comp, Transferable t) {
            JTextComponent c = (JTextComponent)comp;
            int pos = modeBetween
                      ? c.getDropLocation().getIndex() : c.getCaretPosition();
            if (dropAction == MOVE && c == exportComp && pos >= p0 && pos <= p1) {
                shouldRemove = false;
                return true;
            }
            boolean imported = false;
            DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
            if (importFlavor != null) {
                try {
                    boolean useRead = false;
                    if (comp instanceof JEditorPane) {
                        JEditorPane ep = (JEditorPane)comp;
                        if (!ep.getContentType().startsWith("text/plain") &&
                                importFlavor.getMimeType().startsWith(ep.getContentType())) {
                            useRead = true;
                        }
                    }
                    InputContext ic = c.getInputContext();
                    if (ic != null) {
                        ic.endComposition();
                    }
                    Reader r = importFlavor.getReaderForText(t);
                    if (modeBetween) {
                        Caret caret = c.getCaret();
                        if (caret instanceof DefaultCaret) {
                            ((DefaultCaret)caret).setDot(pos, dropBias);
                        } else {
                            c.setCaretPosition(pos);
                        }
                    }
                    handleReaderImport(r, c, useRead);
                    if (isDrop) {
                        c.requestFocus();
                        Caret caret = c.getCaret();
                        if (caret instanceof DefaultCaret) {
                            int newPos = caret.getDot();
                            Position.Bias newBias = ((DefaultCaret)caret).getDotBias();
                            ((DefaultCaret)caret).setDot(pos, dropBias);
                            ((DefaultCaret)caret).moveDot(newPos, newBias);
                        } else {
                            c.select(pos, c.getCaretPosition());
                        }
                    }
                    imported = true;
                } catch (UnsupportedFlavorException ufe) {
                } catch (BadLocationException ble) {
                } catch (IOException ioe) {
                }
            }
            return imported;
        }
        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            JTextComponent c = (JTextComponent)comp;
            if (!(c.isEditable() && c.isEnabled())) {
                return false;
            }
            return (getImportFlavor(flavors, c) != null);
        }
        static class TextTransferable extends BasicTransferable {
            TextTransferable(JTextComponent c, int start, int end) {
                super(null, null);
                this.c = c;
                Document doc = c.getDocument();
                try {
                    p0 = doc.createPosition(start);
                    p1 = doc.createPosition(end);
                    plainData = c.getSelectedText();
                    if (c instanceof JEditorPane) {
                        JEditorPane ep = (JEditorPane)c;
                        mimeType = ep.getContentType();
                        if (mimeType.startsWith("text/plain")) {
                            return;
                        }
                        StringWriter sw = new StringWriter(p1.getOffset() - p0.getOffset());
                        ep.getEditorKit().write(sw, doc, p0.getOffset(), p1.getOffset() - p0.getOffset());
                        if (mimeType.startsWith("text/html")) {
                            htmlData = sw.toString();
                        } else {
                            richText = sw.toString();
                        }
                    }
                } catch (BadLocationException ble) {
                } catch (IOException ioe) {
                }
            }
            void removeText() {
                if ((p0 != null) && (p1 != null) && (p0.getOffset() != p1.getOffset())) {
                    try {
                        Document doc = c.getDocument();
                        doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
                    } catch (BadLocationException e) {
                    }
                }
            }
            protected DataFlavor[] getRicherFlavors() {
                if (richText == null) {
                    return null;
                }
                try {
                    DataFlavor[] flavors = new DataFlavor[3];
                    flavors[0] = new DataFlavor(mimeType + ";class=java.lang.String");
                    flavors[1] = new DataFlavor(mimeType + ";class=java.io.Reader");
                    flavors[2] = new DataFlavor(mimeType + ";class=java.io.InputStream;charset=unicode");
                    return flavors;
                } catch (ClassNotFoundException cle) {
                }
                return null;
            }
            protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (richText == null) {
                    return null;
                }
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return richText;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(richText);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new StringBufferInputStream(richText);
                }
                throw new UnsupportedFlavorException(flavor);
            }
            Position p0;
            Position p1;
            String mimeType;
            String richText;
            JTextComponent c;
        }
    }
}
