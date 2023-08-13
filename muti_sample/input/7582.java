class XTextAreaPeer extends XComponentPeer implements TextAreaPeer {
    boolean editable;
    AWTTextPane textPane;
    AWTTextArea jtext;
    boolean firstChangeSkipped;
    private final JavaMouseEventHandler javaMouseEventHandler
        = new JavaMouseEventHandler( this );
    public long filterEvents(long mask) {
        Thread.dumpStack();
        return 0;
    }
    public Rectangle getCharacterBounds(int i) {
        Thread.dumpStack();
        return null;
    }
    public int getIndexAtPoint(int x, int y) {
        Thread.dumpStack();
        return 0;
    }
    XTextAreaPeer(TextArea target) {
        super( target  );
        this.target = target;
        target.enableInputMethods(true);
        firstChangeSkipped = false;
        String text = ((TextArea)target).getText();
        jtext = new AWTTextArea(text, this);
        jtext.setWrapStyleWord(true);
        jtext.getDocument().addDocumentListener(jtext);
        XToolkit.specialPeerMap.put(jtext,this);
        jtext.enableInputMethods(true);
        textPane = new AWTTextPane(jtext,this, target.getParent());
        setBounds(x, y, width, height, SET_BOUNDS);
        textPane.setVisible(true);
        textPane.validate();
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        foreground = compAccessor.getForeground(target);
        if (foreground == null)  {
            foreground = SystemColor.textText;
        }
        setForeground(foreground);
        background = compAccessor.getBackground(target);
        if (background == null) {
            if (target.isEditable()) background = SystemColor.text;
            else background = SystemColor.control;
        }
        setBackground(background);
        if (!target.isBackgroundSet()) {
            compAccessor.setBackground(target, background);
        }
        if (!target.isForegroundSet()) {
            target.setForeground(SystemColor.textText);
        }
        setFont(font);
        int start = target.getSelectionStart();
        int end = target.getSelectionEnd();
        if (end > start) {
            select(start, end);
        }
        int caretPosition = Math.min(end, text.length());
        setCaretPosition(caretPosition);
        setEditable(target.isEditable());
        setScrollBarVisibility();
        setTextImpl(target.getText());  
        firstChangeSkipped = true;
    }
    public void dispose() {
        XToolkit.specialPeerMap.remove(jtext);
        jtext.removeNotify();
        textPane.removeNotify();
        super.dispose();
    }
    @Override
    public void pSetCursor(Cursor cursor, boolean ignoreSubComponents) {
        Point onScreen = getLocationOnScreen();
        if (ignoreSubComponents ||
            javaMouseEventHandler == null ||
            onScreen == null)
        {
            super.pSetCursor(cursor, true);
            return;
        }
        Point cursorPos = new Point();
        ((XGlobalCursorManager)XGlobalCursorManager.getCursorManager()).getCursorPos(cursorPos);
        Point localPoint = new Point(cursorPos.x - onScreen.x, cursorPos.y - onScreen.y );
        javaMouseEventHandler.setPointerToUnderPoint(localPoint);
        javaMouseEventHandler.setCursor();
    }
    void setScrollBarVisibility() {
        int visibility = ((TextArea)target).getScrollbarVisibility();
        jtext.setLineWrap(false);
        if (visibility == TextArea.SCROLLBARS_NONE) {
            textPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            jtext.setLineWrap(true);
        }
        else if (visibility == TextArea.SCROLLBARS_BOTH) {
            textPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }
        else if (visibility == TextArea.SCROLLBARS_VERTICAL_ONLY) {
            textPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            jtext.setLineWrap(true);
        }
        else if (visibility == TextArea.SCROLLBARS_HORIZONTAL_ONLY) {
            textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            textPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        }
    }
    public Dimension getMinimumSize() {
        return getMinimumSize(10, 60);
    }
    public Dimension getPreferredSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }
    public Dimension getMinimumSize(int rows, int cols) {
        int vsbwidth=0;
        int hsbheight=0;
        JScrollBar vsb = textPane.getVerticalScrollBar();
        if (vsb != null) {
            vsbwidth = vsb.getMinimumSize().width;
        }
        JScrollBar hsb = textPane.getHorizontalScrollBar();
        if (hsb != null) {
            hsbheight = hsb.getMinimumSize().height;
        }
        Font f = jtext.getFont();
        FontMetrics fm = jtext.getFontMetrics(f);
        return new Dimension(fm.charWidth('0') * cols +  vsbwidth,
                             fm.getHeight() * rows +  hsbheight);
    }
    public boolean isFocusable() {
        return true;
    }
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (textPane != null)
            textPane.setVisible(b);
    }
    void repaintText() {
        jtext.repaintNow();
    }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        jtext.forwardFocusGained(e);
    }
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        jtext.forwardFocusLost(e);
    }
    public void repaint() {
        if (textPane  != null)  {
            textPane.repaint();
        }
    }
    public void paint(Graphics g) {
        if (textPane  != null)  {
            textPane.paint(g);
        }
    }
    public void setBounds(int x, int y, int width, int height, int op) {
        super.setBounds(x, y, width, height, op);
        if (textPane != null) {
            int childX = x;
            int childY = y;
            Component parent = target.getParent();
            while (parent.isLightweight()){
                childX -= parent.getX();
                childY -= parent.getY();
                parent = parent.getParent();
            }
            textPane.setBounds(childX,childY,width,height);
            textPane.validate();
        }
    }
    void handleJavaKeyEvent(KeyEvent e) {
        AWTAccessor.getComponentAccessor().processEvent(jtext,e);
    }
    public boolean handlesWheelScrolling() { return true; }
    void handleJavaMouseWheelEvent(MouseWheelEvent e) {
        AWTAccessor.getComponentAccessor().processEvent(textPane,e);
    }
    public void handleJavaMouseEvent( MouseEvent e ) {
        super.handleJavaMouseEvent( e );
        javaMouseEventHandler.handle( e );
    }
    void handleJavaInputMethodEvent(InputMethodEvent e) {
        if (jtext != null)
            jtext.processInputMethodEventPublic((InputMethodEvent)e);
    }
    public void select(int s, int e) {
        jtext.select(s,e);
        jtext.repaint();
    }
    public void setBackground(Color c) {
        super.setBackground(c);
        if (jtext != null) {
            jtext.setBackground(c);
            jtext.setSelectedTextColor(c);
        }
    }
    public void setForeground(Color c) {
        super.setForeground(c);
        if (jtext != null) {
            jtext.setForeground(foreground);
            jtext.setSelectionColor(foreground);
            jtext.setCaretColor(foreground);
        }
    }
    public void setFont(Font f) {
        super.setFont(f);
        if (jtext != null) {
            jtext.setFont(font);
        }
        textPane.validate();
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (jtext != null) jtext.setEditable(editable);
        repaintText();
    }
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (jtext != null) {
            jtext.setEnabled(enabled);
            jtext.repaint();
        }
    }
    public InputMethodRequests getInputMethodRequests() {
        if (jtext != null) return jtext.getInputMethodRequests();
        else  return null;
    }
    public int getSelectionStart() {
        return jtext.getSelectionStart();
    }
    public int getSelectionEnd() {
        return jtext.getSelectionEnd();
    }
    public String getText() {
        return jtext.getText();
    }
    public void setText(String txt) {
        setTextImpl(txt);
        repaintText();
    }
    protected boolean setTextImpl(String txt) {
        if (jtext != null) {
            if (jtext.getDocument().getLength() == 0 && txt.length() == 0) {
                return true;
            }
            jtext.getDocument().removeDocumentListener(jtext);
            jtext.setText(txt);
            if (firstChangeSkipped) {
                postEvent(new TextEvent(target, TextEvent.TEXT_VALUE_CHANGED));
            }
            jtext.getDocument().addDocumentListener(jtext);
        }
        return true;
    }
    public void insert(String txt, int p) {
        if (jtext != null) {
            boolean doScroll = (p >= jtext.getDocument().getLength() && jtext.getDocument().getLength() != 0);
            jtext.insert(txt,p);
            textPane.validate();
            if (doScroll) {
                JScrollBar bar = textPane.getVerticalScrollBar();
                if (bar != null) {
                    bar.setValue(bar.getMaximum()-bar.getVisibleAmount());
                }
            }
        }
    }
    public void replaceRange(String txt, int s, int e) {
        if (jtext != null) {
            jtext.getDocument().removeDocumentListener(jtext);
            jtext.replaceRange(txt, s, e);
            postEvent(new TextEvent(target, TextEvent.TEXT_VALUE_CHANGED));
            jtext.getDocument().addDocumentListener(jtext);
        }
    }
    public void setCaretPosition(int position) {
        jtext.setCaretPosition(position);
    }
    public int getCaretPosition() {
        return jtext.getCaretPosition();
    }
    public void insertText(String txt, int pos) {
        insert(txt, pos);
    }
    public void replaceText(String txt, int start, int end) {
        replaceRange(txt, start, end);
    }
    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }
    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }
    class  AWTTextAreaUI extends MotifTextAreaUI {
        JTextArea jta;
        protected String getPropertyPrefix() { return "TextArea"; }
        public void installUI(JComponent c) {
            super.installUI(c);
            jta = (JTextArea) c;
            JTextArea editor = jta;
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            String prefix = getPropertyPrefix();
            Font f = editor.getFont();
            if ((f == null) || (f instanceof UIResource)) {
                editor.setFont(uidefaults.getFont(prefix + ".font"));
            }
            Color bg = editor.getBackground();
            if ((bg == null) || (bg instanceof UIResource)) {
                editor.setBackground(uidefaults.getColor(prefix + ".background"));
            }
            Color fg = editor.getForeground();
            if ((fg == null) || (fg instanceof UIResource)) {
                editor.setForeground(uidefaults.getColor(prefix + ".foreground"));
            }
            Color color = editor.getCaretColor();
            if ((color == null) || (color instanceof UIResource)) {
                editor.setCaretColor(uidefaults.getColor(prefix + ".caretForeground"));
            }
            Color s = editor.getSelectionColor();
            if ((s == null) || (s instanceof UIResource)) {
                editor.setSelectionColor(uidefaults.getColor(prefix + ".selectionBackground"));
            }
            Color sfg = editor.getSelectedTextColor();
            if ((sfg == null) || (sfg instanceof UIResource)) {
                editor.setSelectedTextColor(uidefaults.getColor(prefix + ".selectionForeground"));
            }
            Color dfg = editor.getDisabledTextColor();
            if ((dfg == null) || (dfg instanceof UIResource)) {
                editor.setDisabledTextColor(uidefaults.getColor(prefix + ".inactiveForeground"));
            }
            Border b = new BevelBorder(false,SystemColor.controlDkShadow,SystemColor.controlLtHighlight);
            editor.setBorder(new BorderUIResource.CompoundBorderUIResource(
                b,new EmptyBorder(2, 2, 2, 2)));
            Insets margin = editor.getMargin();
            if (margin == null || margin instanceof UIResource) {
                editor.setMargin(uidefaults.getInsets(prefix + ".margin"));
            }
        }
        protected void installKeyboardActions() {
            super.installKeyboardActions();
            JTextComponent comp = getComponent();
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            String prefix = getPropertyPrefix();
            InputMap map = (InputMap)uidefaults.get(prefix + ".focusInputMap");
            if (map != null) {
                SwingUtilities.replaceUIInputMap(comp, JComponent.WHEN_FOCUSED,
                                                 map);
            }
        }
        protected Caret createCaret() {
            return new XAWTCaret();
        }
    }
    class XAWTCaret extends DefaultCaret {
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            getComponent().repaint();
        }
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            getComponent().repaint();
        }
        public void setSelectionVisible(boolean vis) {
            if (vis){
                super.setSelectionVisible(vis);
            }else{
                setDot(getDot());
            }
        }
    }
    class XAWTScrollBarButton extends BasicArrowButton
    {
        UIDefaults uidefaults = XToolkit.getUIDefaults();
        private Color darkShadow = SystemColor.controlShadow;
        private Color lightShadow = SystemColor.controlLtHighlight;
        private Color buttonBack = uidefaults.getColor("ScrollBar.track");
        public XAWTScrollBarButton(int direction)
        {
            super(direction);
            switch (direction) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                this.direction = direction;
                break;
            default:
                throw new IllegalArgumentException("invalid direction");
            }
            setRequestFocusEnabled(false);
            setOpaque(true);
            setBackground(uidefaults.getColor("ScrollBar.thumb"));
            setForeground(uidefaults.getColor("ScrollBar.foreground"));
        }
        public Dimension getPreferredSize() {
            switch (direction) {
            case NORTH:
            case SOUTH:
                return new Dimension(11, 12);
            case EAST:
            case WEST:
            default:
                return new Dimension(12, 11);
            }
        }
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
        public boolean isFocusTraversable() {
            return false;
        }
        public void paint(Graphics g)
        {
            int w = getWidth();
            int h = getHeight();
            if (isOpaque()) {
                g.setColor(buttonBack);
                g.fillRect(0, 0, w, h);
            }
            boolean isPressed = getModel().isPressed();
            Color lead = (isPressed) ? darkShadow : lightShadow;
            Color trail = (isPressed) ? lightShadow : darkShadow;
            Color fill = getBackground();
            int cx = w / 2;
            int cy = h / 2;
            int s = Math.min(w, h);
            switch (direction) {
            case NORTH:
                g.setColor(lead);
                g.drawLine(cx, 0, cx, 0);
                for (int x = cx - 1, y = 1, dx = 1; y <= s - 2; y += 2) {
                    g.setColor(lead);
                    g.drawLine(x, y, x, y);
                    if (y >= (s - 2)) {
                        g.drawLine(x, y + 1, x, y + 1);
                    }
                    g.setColor(fill);
                    g.drawLine(x + 1, y, x + dx, y);
                    if (y < (s - 2)) {
                        g.drawLine(x, y + 1, x + dx + 1, y + 1);
                    }
                    g.setColor(trail);
                    g.drawLine(x + dx + 1, y, x + dx + 1, y);
                    if (y >= (s - 2)) {
                        g.drawLine(x + 1, y + 1, x + dx + 1, y + 1);
                    }
                    dx += 2;
                    x -= 1;
                }
                break;
            case SOUTH:
                g.setColor(trail);
                g.drawLine(cx, s, cx, s);
                for (int x = cx - 1, y = s - 1, dx = 1; y >= 1; y -= 2) {
                    g.setColor(lead);
                    g.drawLine(x, y, x, y);
                    if (y <= 2) {
                        g.drawLine(x, y - 1, x + dx + 1, y - 1);
                    }
                    g.setColor(fill);
                    g.drawLine(x + 1, y, x + dx, y);
                    if (y > 2) {
                        g.drawLine(x, y - 1, x + dx + 1, y - 1);
                    }
                    g.setColor(trail);
                    g.drawLine(x + dx + 1, y, x + dx + 1, y);
                    dx += 2;
                    x -= 1;
                }
                break;
            case EAST:
                g.setColor(lead);
                g.drawLine(s, cy, s, cy);
                for (int y = cy - 1, x = s - 1, dy = 1; x >= 1; x -= 2) {
                    g.setColor(lead);
                    g.drawLine(x, y, x, y);
                    if (x <= 2) {
                        g.drawLine(x - 1, y, x - 1, y + dy + 1);
                    }
                    g.setColor(fill);
                    g.drawLine(x, y + 1, x, y + dy);
                    if (x > 2) {
                        g.drawLine(x - 1, y, x - 1, y + dy + 1);
                    }
                    g.setColor(trail);
                    g.drawLine(x, y + dy + 1, x, y + dy + 1);
                    dy += 2;
                    y -= 1;
                }
                break;
            case WEST:
                g.setColor(trail);
                g.drawLine(0, cy, 0, cy);
                for (int y = cy - 1, x = 1, dy = 1; x <= s - 2; x += 2) {
                    g.setColor(lead);
                    g.drawLine(x, y, x, y);
                    if (x >= (s - 2)) {
                        g.drawLine(x + 1, y, x + 1, y);
                    }
                    g.setColor(fill);
                    g.drawLine(x, y + 1, x, y + dy);
                    if (x < (s - 2)) {
                        g.drawLine(x + 1, y, x + 1, y + dy + 1);
                    }
                    g.setColor(trail);
                    g.drawLine(x, y + dy + 1, x, y + dy + 1);
                    if (x >= (s - 2)) {
                        g.drawLine(x + 1, y + 1, x + 1, y + dy + 1);
                    }
                    dy += 2;
                    y -= 1;
                }
                break;
            }
        }
    }
    class XAWTScrollBarUI extends BasicScrollBarUI
    {
        public XAWTScrollBarUI() {
            super();
        }
        protected void installDefaults()
        {
            super.installDefaults();
            scrollbar.setBorder(new BevelBorder(false,SystemColor.controlDkShadow,SystemColor.controlLtHighlight) );
        }
        protected void configureScrollBarColors() {
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            Color bg = scrollbar.getBackground();
            if (bg == null || bg instanceof UIResource) {
                scrollbar.setBackground(uidefaults.getColor("ScrollBar.background"));
            }
            Color fg = scrollbar.getForeground();
            if (fg == null || fg instanceof UIResource) {
                scrollbar.setForeground(uidefaults.getColor("ScrollBar.foreground"));
            }
            thumbHighlightColor = uidefaults.getColor("ScrollBar.thumbHighlight");
            thumbLightShadowColor = uidefaults.getColor("ScrollBar.thumbShadow");
            thumbDarkShadowColor = uidefaults.getColor("ScrollBar.thumbDarkShadow");
            thumbColor = uidefaults.getColor("ScrollBar.thumb");
            trackColor = uidefaults.getColor("ScrollBar.track");
            trackHighlightColor = uidefaults.getColor("ScrollBar.trackHighlight");
        }
        protected JButton createDecreaseButton(int orientation) {
            JButton b = new XAWTScrollBarButton(orientation);
            return b;
        }
        protected JButton createIncreaseButton(int orientation) {
            JButton b = new XAWTScrollBarButton(orientation);
            return b;
        }
        public JButton getDecreaseButton(){
            return decrButton;
        }
        public JButton getIncreaseButton(){
            return incrButton;
        }
        public void paint(Graphics g, JComponent c) {
            paintTrack(g, c, getTrackBounds());
            Rectangle thumbBounds = getThumbBounds();
            paintThumb(g, c, thumbBounds);
        }
        public void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
        {
            if(!scrollbar.isEnabled()) {
                return;
            }
            if (thumbBounds.isEmpty())
                thumbBounds = getTrackBounds();
            int w = thumbBounds.width;
            int h = thumbBounds.height;
            g.translate(thumbBounds.x, thumbBounds.y);
            g.setColor(thumbColor);
            g.fillRect(0, 0, w-1, h-1);
            g.setColor(thumbHighlightColor);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(1, 0, w-1, 0);
            g.setColor(thumbLightShadowColor);
            g.drawLine(1, h-1, w-1, h-1);
            g.drawLine(w-1, 1, w-1, h-2);
            g.translate(-thumbBounds.x, -thumbBounds.y);
        }
    }
    class AWTTextArea extends JTextArea implements DocumentListener {
        boolean isFocused = false;
        XTextAreaPeer peer;
        public AWTTextArea(String text, XTextAreaPeer peer) {
            super(text);
            setFocusable(false);
            this.peer = peer;
        }
        public void insertUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        public void removeUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        public void changedUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        void forwardFocusGained( FocusEvent e) {
            isFocused = true;
            FocusEvent fe = CausedFocusEvent.retarget(e, this);
            super.processFocusEvent(fe);
        }
        void forwardFocusLost( FocusEvent e) {
            isFocused = false;
            FocusEvent fe = CausedFocusEvent.retarget(e, this);
            super.processFocusEvent(fe);
        }
        public boolean hasFocus() {
            return isFocused;
        }
        public void repaintNow() {
            paintImmediately(getBounds());
        }
        public void processMouseEventPublic(MouseEvent e) {
            processMouseEvent(e);
        }
        public void processMouseMotionEventPublic(MouseEvent e) {
            processMouseMotionEvent(e);
        }
        public void processInputMethodEventPublic(InputMethodEvent e) {
            processInputMethodEvent(e);
        }
        public void updateUI() {
            ComponentUI ui = new AWTTextAreaUI();
            setUI(ui);
        }
        public void setTransferHandler(TransferHandler newHandler) {
            TransferHandler oldHandler = (TransferHandler)
                getClientProperty(XTextTransferHelper.getTransferHandlerKey());
            putClientProperty(XTextTransferHelper.getTransferHandlerKey(),
                              newHandler);
            firePropertyChange("transferHandler", oldHandler, newHandler);
        }
    }
    class XAWTScrollPaneUI extends BasicScrollPaneUI
    {
        private final Border vsbMarginBorderR = new EmptyBorder(0, 2, 0, 0);
        private final Border vsbMarginBorderL = new EmptyBorder(0, 0, 0, 2);
        private final Border hsbMarginBorder = new EmptyBorder(2, 0, 0, 0);
        private Border vsbBorder;
        private Border hsbBorder;
        private PropertyChangeListener propertyChangeHandler;
        protected void installListeners(JScrollPane scrollPane) {
            super.installListeners(scrollPane);
            propertyChangeHandler = createPropertyChangeHandler();
            scrollPane.addPropertyChangeListener(propertyChangeHandler);
        }
        public void paint(Graphics g, JComponent c) {
            Border vpBorder = scrollpane.getViewportBorder();
            if (vpBorder != null) {
                Rectangle r = scrollpane.getViewportBorderBounds();
                vpBorder.paintBorder(scrollpane, g, r.x, r.y, r.width, r.height);
            }
        }
        protected void uninstallListeners(JScrollPane scrollPane) {
            super.uninstallListeners(scrollPane);
            scrollPane.removePropertyChangeListener(propertyChangeHandler);
        }
        private PropertyChangeListener createPropertyChangeHandler() {
            return new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String propertyName = e.getPropertyName();
                        if (propertyName.equals("componentOrientation")) {
                            JScrollPane pane = (JScrollPane)e.getSource();
                            JScrollBar vsb = pane.getVerticalScrollBar();
                            if (vsb != null) {
                                if (isLeftToRight(pane)) {
                                    vsbBorder = new CompoundBorder(new EmptyBorder(0, 4, 0, -4),
                                                                   vsb.getBorder());
                                } else {
                                    vsbBorder = new CompoundBorder(new EmptyBorder(0, -4, 0, 4),
                                                                   vsb.getBorder());
                                }
                                vsb.setBorder(vsbBorder);
                            }
                        }
                    }};
        }
        boolean isLeftToRight( Component c ) {
            return c.getComponentOrientation().isLeftToRight();
        }
        protected void installDefaults(JScrollPane scrollpane) {
            Border b = scrollpane.getBorder();
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            scrollpane.setBorder(uidefaults.getBorder("ScrollPane.border"));
            scrollpane.setBackground(uidefaults.getColor("ScrollPane.background"));
            scrollpane.setViewportBorder(uidefaults.getBorder("TextField.border"));
            JScrollBar vsb = scrollpane.getVerticalScrollBar();
            if (vsb != null) {
                if (isLeftToRight(scrollpane)) {
                    vsbBorder = new CompoundBorder(vsbMarginBorderR,
                                                   vsb.getBorder());
                }
                else {
                    vsbBorder = new CompoundBorder(vsbMarginBorderL,
                                                   vsb.getBorder());
                }
                vsb.setBorder(vsbBorder);
            }
            JScrollBar hsb = scrollpane.getHorizontalScrollBar();
            if (hsb != null) {
                hsbBorder = new CompoundBorder(hsbMarginBorder, hsb.getBorder());
                hsb.setBorder(hsbBorder);
            }
        }
        protected void uninstallDefaults(JScrollPane c) {
            super.uninstallDefaults(c);
            JScrollBar vsb = scrollpane.getVerticalScrollBar();
            if (vsb != null) {
                if (vsb.getBorder() == vsbBorder) {
                    vsb.setBorder(null);
                }
                vsbBorder = null;
            }
            JScrollBar hsb = scrollpane.getHorizontalScrollBar();
            if (hsb != null) {
                if (hsb.getBorder() == hsbBorder) {
                    hsb.setBorder(null);
                }
                hsbBorder = null;
            }
        }
    }
    private class AWTTextPane extends JScrollPane implements FocusListener {
        JTextArea jtext;
        XWindow xwin;
        Color control = SystemColor.control;
        Color focus = SystemColor.activeCaptionBorder;
        public AWTTextPane(JTextArea jt, XWindow xwin, Container parent) {
            super(jt);
            this.xwin = xwin;
            setDoubleBuffered(true);
            jt.addFocusListener(this);
            AWTAccessor.getComponentAccessor().setParent(this,parent);
            setViewportBorder(new BevelBorder(false,SystemColor.controlDkShadow,SystemColor.controlLtHighlight) );
            this.jtext = jt;
            setFocusable(false);
            addNotify();
        }
        public void focusGained(FocusEvent e) {
            Graphics g = getGraphics();
            Rectangle r = getViewportBorderBounds();
            g.setColor(focus);
            g.drawRect(r.x,r.y,r.width,r.height);
            g.dispose();
        }
        public void focusLost(FocusEvent e) {
            Graphics g = getGraphics();
            Rectangle r = getViewportBorderBounds();
            g.setColor(control);
            g.drawRect(r.x,r.y,r.width,r.height);
            g.dispose();
        }
        public Window getRealParent() {
            return (Window) xwin.target;
        }
        public ComponentPeer getPeer() {
            return (ComponentPeer) (xwin);
        }
        public void updateUI() {
            ComponentUI ui = new XAWTScrollPaneUI();
            setUI(ui);
        }
        public JScrollBar createVerticalScrollBar() {
            return new XAWTScrollBar(JScrollBar.VERTICAL);
        }
        public JScrollBar createHorizontalScrollBar() {
            return new XAWTScrollBar(JScrollBar.HORIZONTAL);
        }
        public JTextArea getTextArea () {
            return this.jtext;
        }
        public Graphics getGraphics() {
            return xwin.getGraphics();
        }
        class XAWTScrollBar extends ScrollBar {
            public XAWTScrollBar(int i) {
                super(i);
                setFocusable(false);
            }
            public void updateUI() {
                ComponentUI ui = new XAWTScrollBarUI();
                setUI(ui);
            }
        }
    }
    static class BevelBorder extends AbstractBorder implements UIResource {
        private Color darkShadow = SystemColor.controlDkShadow;
        private Color lightShadow = SystemColor.controlLtHighlight;
        private Color control = SystemColor.controlShadow;
        private boolean isRaised;
        public BevelBorder(boolean isRaised, Color darkShadow, Color lightShadow) {
            this.isRaised = isRaised;
            this.darkShadow = darkShadow;
            this.lightShadow = lightShadow;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor((isRaised) ? lightShadow : darkShadow);
            g.drawLine(x, y, x+w-1, y);           
            g.drawLine(x, y+h-1, x, y+1);         
            g.setColor(control);
            g.drawLine(x+1, y+1, x+w-2, y+1);           
            g.drawLine(x+1, y+h-1, x+1, y+1);         
            g.setColor((isRaised) ? darkShadow : lightShadow);
            g.drawLine(x+1, y+h-1, x+w-1, y+h-1); 
            g.drawLine(x+w-1, y+h-1, x+w-1, y+1); 
            g.setColor(control);
            g.drawLine(x+1, y+h-2, x+w-2, y+h-2); 
            g.drawLine(x+w-2, y+h-2, x+w-2, y+1); 
        }
        public Insets getBorderInsets(Component c) {
            return getBorderInsets(c, new Insets(0,0,0,0));
        }
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = insets.left = insets.bottom = insets.right = 2;
            return insets;
        }
        public boolean isOpaque(Component c) {
            return true;
        }
    }
    private static final class JavaMouseEventHandler {
        private final XTextAreaPeer outer;
        private final Pointer current = new Pointer();
        private boolean grabbed = false;
        JavaMouseEventHandler( XTextAreaPeer outer ) {
            this.outer = outer;
        }
        void handle( MouseEvent event ) {
            if ( ! grabbed ) {
                setPointerToUnderPoint( event.getPoint() );
            }
            dispatch( event );
            boolean wasGrabbed = grabbed;
            grabbed_update( event );
            if ( wasGrabbed && ! grabbed ) {
                setPointerToUnderPoint( event.getPoint() );
            }
            setCursor();
        }
        private void dispatch( MouseEvent event ) {
            switch( current.getType() )
            {
                case TEXT:
                    Point point = toViewportChildLocalSpace(
                        outer.textPane.getViewport(), event.getPoint() );
                    XTextAreaPeer.AWTTextArea jtext = outer.jtext;
                    MouseEvent newEvent = newMouseEvent( jtext, point, event );
                    int id = newEvent.getID();
                    if ( id==MouseEvent.MOUSE_MOVED || id==MouseEvent.MOUSE_DRAGGED ) {
                        jtext.processMouseMotionEventPublic( newEvent );
                    } else {
                        jtext.processMouseEventPublic( newEvent );
                    }
                    break;
                case BAR:
                case BUTTON:
                    Component c = current.getBar();
                    Point p = toLocalSpace( c, event.getPoint() );
                    if ( current.getType()==Pointer.Type.BUTTON ) {
                        c = current.getButton();
                        p = toLocalSpace( c, p );
                    }
                    AWTAccessor.getComponentAccessor().processEvent( c, newMouseEvent( c, p, event ) );
                    break;
            }
        }
        private static MouseEvent newMouseEvent(
            Component source, Point point, MouseEvent template )
        {
            MouseEvent e = template;
            MouseEvent nme = new MouseEvent(
                source,
                e.getID(), e.getWhen(),
                e.getModifiersEx() | e.getModifiers(),
                point.x, point.y,
                e.getXOnScreen(), e.getYOnScreen(),
                e.getClickCount(), e.isPopupTrigger(), e.getButton() );
            SunToolkit.setSystemGenerated(nme);
            return nme;
        }
        private void setCursor() {
            if ( current.getType()==Pointer.Type.TEXT ) {
                outer.pSetCursor( outer.target.getCursor(), true );
            }
            else {
                outer.pSetCursor( outer.textPane.getCursor(), true );
            }
        }
        private void grabbed_update( MouseEvent event ) {
            final int allButtonsMask
                = MouseEvent.BUTTON1_DOWN_MASK
                | MouseEvent.BUTTON2_DOWN_MASK
                | MouseEvent.BUTTON3_DOWN_MASK;
            grabbed = ( (event.getModifiersEx() & allButtonsMask) != 0 );
        }
        private static Point toLocalSpace( Component local, Point inParentSpace )
        {
            Point p = inParentSpace;
            Point l = local.getLocation();
            return new Point( p.x - l.x, p.y - l.y );
        }
        private static Point toViewportChildLocalSpace( JViewport v, Point inViewportParentSpace )
        {
            Point l = toLocalSpace(v, inViewportParentSpace);
            Point p = v.getViewPosition();
            l.x += p.x;
            l.y += p.y;
            return l;
        }
        private void setPointerToUnderPoint( Point point ) {
            if ( outer.textPane.getViewport().getBounds().contains( point ) ) {
                current.setText();
            }
            else if ( ! setPointerIfPointOverScrollbar(
                outer.textPane.getVerticalScrollBar(), point ) )
            {
                if ( ! setPointerIfPointOverScrollbar(
                    outer.textPane.getHorizontalScrollBar(), point ) )
                {
                    current.setNone();
                }
            }
        }
        private boolean setPointerIfPointOverScrollbar( JScrollBar bar, Point point ) {
            if ( ! bar.getBounds().contains( point ) ) {
                return false;
            }
            current.setBar( bar );
            Point local = toLocalSpace( bar, point );
            XTextAreaPeer.XAWTScrollBarUI ui =
                (XTextAreaPeer.XAWTScrollBarUI) bar.getUI();
            if ( ! setPointerIfPointOverButton( ui.getIncreaseButton(), local ) ) {
                setPointerIfPointOverButton( ui.getDecreaseButton(), local );
            }
            return true;
        }
        private boolean setPointerIfPointOverButton( JButton button, Point point ) {
            if ( ! button.getBounds().contains( point ) ) {
                return false;
            }
            current.setButton( button );
            return true;
        }
        private static final class Pointer {
            static enum Type {
                NONE, TEXT, BAR, BUTTON  
            }
            Type getType() {
                return type;
            }
            boolean isNone() {
                return type==Type.NONE;
            }
            JScrollBar getBar() {
                boolean ok = type==Type.BAR || type==Type.BUTTON;
                assert ok;
                return ok ? bar : null;
            }
            JButton getButton() {
                boolean ok = type==Type.BUTTON;
                assert ok;
                return ok ? button : null;
            }
            void setNone() {
                type = Type.NONE;
            }
            void setText() {
                type = Type.TEXT;
            }
            void setBar( JScrollBar bar ) {
                this.bar=bar;
                type=Type.BAR;
            }
            void setButton( JButton button ) {
                this.button=button;
                type=Type.BUTTON;
            }
            private Type type;
            private JScrollBar bar;
            private JButton button;
        }
    }
}
