public abstract class JTextComponent extends JComponent implements Scrollable, Accessible
{
    public JTextComponent() {
        super();
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
        caretEvent = new MutableCaretEvent(this);
        addMouseListener(caretEvent);
        addFocusListener(caretEvent);
        setEditable(true);
        setDragEnabled(false);
        setLayout(null); 
        updateUI();
    }
    public TextUI getUI() { return (TextUI)ui; }
    public void setUI(TextUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((TextUI)UIManager.getUI(this));
        invalidate();
    }
    public void addCaretListener(CaretListener listener) {
        listenerList.add(CaretListener.class, listener);
    }
    public void removeCaretListener(CaretListener listener) {
        listenerList.remove(CaretListener.class, listener);
    }
    public CaretListener[] getCaretListeners() {
        return listenerList.getListeners(CaretListener.class);
    }
    protected void fireCaretUpdate(CaretEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==CaretListener.class) {
                ((CaretListener)listeners[i+1]).caretUpdate(e);
            }
        }
    }
    public void setDocument(Document doc) {
        Document old = model;
        try {
            if (old instanceof AbstractDocument) {
                ((AbstractDocument)old).readLock();
            }
            if (accessibleContext != null) {
                model.removeDocumentListener(
                    ((AccessibleJTextComponent)accessibleContext));
            }
            if (inputMethodRequestsHandler != null) {
                model.removeDocumentListener((DocumentListener)inputMethodRequestsHandler);
            }
            model = doc;
            Boolean runDir = getComponentOrientation().isLeftToRight()
                             ? TextAttribute.RUN_DIRECTION_LTR
                             : TextAttribute.RUN_DIRECTION_RTL;
            if (runDir != doc.getProperty(TextAttribute.RUN_DIRECTION)) {
                doc.putProperty(TextAttribute.RUN_DIRECTION, runDir );
            }
            firePropertyChange("document", old, doc);
        } finally {
            if (old instanceof AbstractDocument) {
                ((AbstractDocument)old).readUnlock();
            }
        }
        revalidate();
        repaint();
        if (accessibleContext != null) {
            model.addDocumentListener(
                ((AccessibleJTextComponent)accessibleContext));
        }
        if (inputMethodRequestsHandler != null) {
            model.addDocumentListener((DocumentListener)inputMethodRequestsHandler);
        }
    }
    public Document getDocument() {
        return model;
    }
    public void setComponentOrientation( ComponentOrientation o ) {
        Document doc = getDocument();
        if( doc !=  null ) {
            Boolean runDir = o.isLeftToRight()
                             ? TextAttribute.RUN_DIRECTION_LTR
                             : TextAttribute.RUN_DIRECTION_RTL;
            doc.putProperty( TextAttribute.RUN_DIRECTION, runDir );
        }
        super.setComponentOrientation( o );
    }
    public Action[] getActions() {
        return getUI().getEditorKit(this).getActions();
    }
    public void setMargin(Insets m) {
        Insets old = margin;
        margin = m;
        firePropertyChange("margin", old, m);
        invalidate();
    }
    public Insets getMargin() {
        return margin;
    }
    public void setNavigationFilter(NavigationFilter filter) {
        navigationFilter = filter;
    }
    public NavigationFilter getNavigationFilter() {
        return navigationFilter;
    }
    @Transient
    public Caret getCaret() {
        return caret;
    }
    public void setCaret(Caret c) {
        if (caret != null) {
            caret.removeChangeListener(caretEvent);
            caret.deinstall(this);
        }
        Caret old = caret;
        caret = c;
        if (caret != null) {
            caret.install(this);
            caret.addChangeListener(caretEvent);
        }
        firePropertyChange("caret", old, caret);
    }
    public Highlighter getHighlighter() {
        return highlighter;
    }
    public void setHighlighter(Highlighter h) {
        if (highlighter != null) {
            highlighter.deinstall(this);
        }
        Highlighter old = highlighter;
        highlighter = h;
        if (highlighter != null) {
            highlighter.install(this);
        }
        firePropertyChange("highlighter", old, h);
    }
    public void setKeymap(Keymap map) {
        Keymap old = keymap;
        keymap = map;
        firePropertyChange("keymap", old, keymap);
        updateInputMap(old, map);
    }
    public void setDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        dragEnabled = b;
    }
    public boolean getDragEnabled() {
        return dragEnabled;
    }
    public final void setDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case USE_SELECTION:
                case INSERT:
                    this.dropMode = dropMode;
                    return;
            }
        }
        throw new IllegalArgumentException(dropMode + ": Unsupported drop mode for text");
    }
    public final DropMode getDropMode() {
        return dropMode;
    }
    static {
        SwingAccessor.setJTextComponentAccessor(
            new SwingAccessor.JTextComponentAccessor() {
                public TransferHandler.DropLocation dropLocationForPoint(JTextComponent textComp,
                                                                         Point p)
                {
                    return textComp.dropLocationForPoint(p);
                }
                public Object setDropLocation(JTextComponent textComp,
                                              TransferHandler.DropLocation location,
                                              Object state, boolean forDrop)
                {
                    return textComp.setDropLocation(location, state, forDrop);
                }
            });
    }
    DropLocation dropLocationForPoint(Point p) {
        Position.Bias[] bias = new Position.Bias[1];
        int index = getUI().viewToModel(this, p, bias);
        if (bias[0] == null) {
            bias[0] = Position.Bias.Forward;
        }
        return new DropLocation(p, index, bias[0]);
    }
    Object setDropLocation(TransferHandler.DropLocation location,
                           Object state,
                           boolean forDrop) {
        Object retVal = null;
        DropLocation textLocation = (DropLocation)location;
        if (dropMode == DropMode.USE_SELECTION) {
            if (textLocation == null) {
                if (state != null) {
                    Object[] vals = (Object[])state;
                    if (!forDrop) {
                        if (caret instanceof DefaultCaret) {
                            ((DefaultCaret)caret).setDot(((Integer)vals[0]).intValue(),
                                                         (Position.Bias)vals[3]);
                            ((DefaultCaret)caret).moveDot(((Integer)vals[1]).intValue(),
                                                         (Position.Bias)vals[4]);
                        } else {
                            caret.setDot(((Integer)vals[0]).intValue());
                            caret.moveDot(((Integer)vals[1]).intValue());
                        }
                    }
                    caret.setVisible(((Boolean)vals[2]).booleanValue());
                }
            } else {
                if (dropLocation == null) {
                    boolean visible;
                    if (caret instanceof DefaultCaret) {
                        DefaultCaret dc = (DefaultCaret)caret;
                        visible = dc.isActive();
                        retVal = new Object[] {Integer.valueOf(dc.getMark()),
                                               Integer.valueOf(dc.getDot()),
                                               Boolean.valueOf(visible),
                                               dc.getMarkBias(),
                                               dc.getDotBias()};
                    } else {
                        visible = caret.isVisible();
                        retVal = new Object[] {Integer.valueOf(caret.getMark()),
                                               Integer.valueOf(caret.getDot()),
                                               Boolean.valueOf(visible)};
                    }
                    caret.setVisible(true);
                } else {
                    retVal = state;
                }
                if (caret instanceof DefaultCaret) {
                    ((DefaultCaret)caret).setDot(textLocation.getIndex(), textLocation.getBias());
                } else {
                    caret.setDot(textLocation.getIndex());
                }
            }
        } else {
            if (textLocation == null) {
                if (state != null) {
                    caret.setVisible(((Boolean)state).booleanValue());
                }
            } else {
                if (dropLocation == null) {
                    boolean visible = caret instanceof DefaultCaret
                                      ? ((DefaultCaret)caret).isActive()
                                      : caret.isVisible();
                    retVal = Boolean.valueOf(visible);
                    caret.setVisible(false);
                } else {
                    retVal = state;
                }
            }
        }
        DropLocation old = dropLocation;
        dropLocation = textLocation;
        firePropertyChange("dropLocation", old, dropLocation);
        return retVal;
    }
    public final DropLocation getDropLocation() {
        return dropLocation;
    }
    void updateInputMap(Keymap oldKm, Keymap newKm) {
        InputMap km = getInputMap(JComponent.WHEN_FOCUSED);
        InputMap last = km;
        while (km != null && !(km instanceof KeymapWrapper)) {
            last = km;
            km = km.getParent();
        }
        if (km != null) {
            if (newKm == null) {
                if (last != km) {
                    last.setParent(km.getParent());
                }
                else {
                    last.setParent(null);
                }
            }
            else {
                InputMap newKM = new KeymapWrapper(newKm);
                last.setParent(newKM);
                if (last != km) {
                    newKM.setParent(km.getParent());
                }
            }
        }
        else if (newKm != null) {
            km = getInputMap(JComponent.WHEN_FOCUSED);
            if (km != null) {
                InputMap newKM = new KeymapWrapper(newKm);
                newKM.setParent(km.getParent());
                km.setParent(newKM);
            }
        }
        ActionMap am = getActionMap();
        ActionMap lastAM = am;
        while (am != null && !(am instanceof KeymapActionMap)) {
            lastAM = am;
            am = am.getParent();
        }
        if (am != null) {
            if (newKm == null) {
                if (lastAM != am) {
                    lastAM.setParent(am.getParent());
                }
                else {
                    lastAM.setParent(null);
                }
            }
            else {
                ActionMap newAM = new KeymapActionMap(newKm);
                lastAM.setParent(newAM);
                if (lastAM != am) {
                    newAM.setParent(am.getParent());
                }
            }
        }
        else if (newKm != null) {
            am = getActionMap();
            if (am != null) {
                ActionMap newAM = new KeymapActionMap(newKm);
                newAM.setParent(am.getParent());
                am.setParent(newAM);
            }
        }
    }
    public Keymap getKeymap() {
        return keymap;
    }
    public static Keymap addKeymap(String nm, Keymap parent) {
        Keymap map = new DefaultKeymap(nm, parent);
        if (nm != null) {
            getKeymapTable().put(nm, map);
        }
        return map;
    }
    public static Keymap removeKeymap(String nm) {
        return getKeymapTable().remove(nm);
    }
    public static Keymap getKeymap(String nm) {
        return getKeymapTable().get(nm);
    }
    private static HashMap<String,Keymap> getKeymapTable() {
        synchronized (KEYMAP_TABLE) {
            AppContext appContext = AppContext.getAppContext();
            HashMap<String,Keymap> keymapTable =
                (HashMap<String,Keymap>)appContext.get(KEYMAP_TABLE);
            if (keymapTable == null) {
                keymapTable = new HashMap<String,Keymap>(17);
                appContext.put(KEYMAP_TABLE, keymapTable);
                Keymap binding = addKeymap(DEFAULT_KEYMAP, null);
                binding.setDefaultAction(new
                                         DefaultEditorKit.DefaultKeyTypedAction());
            }
            return keymapTable;
        }
    }
    public static class KeyBinding {
        public KeyStroke key;
        public String actionName;
        public KeyBinding(KeyStroke key, String actionName) {
            this.key = key;
            this.actionName = actionName;
        }
    }
    public static void loadKeymap(Keymap map, KeyBinding[] bindings, Action[] actions) {
        Hashtable<String, Action> h = new Hashtable<String, Action>();
        for (Action a : actions) {
            String value = (String)a.getValue(Action.NAME);
            h.put((value!=null ? value:""), a);
        }
        for (KeyBinding binding : bindings) {
            Action a = h.get(binding.actionName);
            if (a != null) {
                map.addActionForKeyStroke(binding.key, a);
            }
        }
    }
    private static Boolean isProcessInputMethodEventOverridden(Class<?> klass) {
        if (klass == JTextComponent.class) {
            return Boolean.FALSE;
        }
        Boolean retValue = overrideMap.get(klass.getName());
        if (retValue != null) {
            return retValue;
        }
        Boolean sOverriden = isProcessInputMethodEventOverridden(
                                       klass.getSuperclass());
        if (sOverriden.booleanValue()) {
            overrideMap.put(klass.getName(), sOverriden);
            return sOverriden;
        }
        try {
            Class[] classes = new Class[1];
            classes[0] = InputMethodEvent.class;
            Method m = klass.getDeclaredMethod("processInputMethodEvent",
                                               classes);
            retValue = Boolean.TRUE;
        } catch (NoSuchMethodException nsme) {
            retValue = Boolean.FALSE;
        }
        overrideMap.put(klass.getName(), retValue);
        return retValue;
    }
    public Color getCaretColor() {
        return caretColor;
    }
    public void setCaretColor(Color c) {
        Color old = caretColor;
        caretColor = c;
        firePropertyChange("caretColor", old, caretColor);
    }
    public Color getSelectionColor() {
        return selectionColor;
    }
    public void setSelectionColor(Color c) {
        Color old = selectionColor;
        selectionColor = c;
        firePropertyChange("selectionColor", old, selectionColor);
    }
    public Color getSelectedTextColor() {
        return selectedTextColor;
    }
    public void setSelectedTextColor(Color c) {
        Color old = selectedTextColor;
        selectedTextColor = c;
        firePropertyChange("selectedTextColor", old, selectedTextColor);
    }
    public Color getDisabledTextColor() {
        return disabledTextColor;
    }
    public void setDisabledTextColor(Color c) {
        Color old = disabledTextColor;
        disabledTextColor = c;
        firePropertyChange("disabledTextColor", old, disabledTextColor);
    }
    public void replaceSelection(String content) {
        Document doc = getDocument();
        if (doc != null) {
            try {
                boolean composedTextSaved = saveComposedText(caret.getDot());
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).replace(p0, p1 - p0, content,null);
                }
                else {
                    if (p0 != p1) {
                        doc.remove(p0, p1 - p0);
                    }
                    if (content != null && content.length() > 0) {
                        doc.insertString(p0, content, null);
                    }
                }
                if (composedTextSaved) {
                    restoreComposedText();
                }
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
            }
        }
    }
    public String getText(int offs, int len) throws BadLocationException {
        return getDocument().getText(offs, len);
    }
    public Rectangle modelToView(int pos) throws BadLocationException {
        return getUI().modelToView(this, pos);
    }
    public int viewToModel(Point pt) {
        return getUI().viewToModel(this, pt);
    }
    public void cut() {
        if (isEditable() && isEnabled()) {
            invokeAction("cut", TransferHandler.getCutAction());
        }
    }
    public void copy() {
        invokeAction("copy", TransferHandler.getCopyAction());
    }
    public void paste() {
        if (isEditable() && isEnabled()) {
            invokeAction("paste", TransferHandler.getPasteAction());
        }
    }
    private void invokeAction(String name, Action altAction) {
        ActionMap map = getActionMap();
        Action action = null;
        if (map != null) {
            action = map.get(name);
        }
        if (action == null) {
            installDefaultTransferHandlerIfNecessary();
            action = altAction;
        }
        action.actionPerformed(new ActionEvent(this,
                               ActionEvent.ACTION_PERFORMED, (String)action.
                               getValue(Action.NAME),
                               EventQueue.getMostRecentEventTime(),
                               getCurrentEventModifiers()));
    }
    private void installDefaultTransferHandlerIfNecessary() {
        if (getTransferHandler() == null) {
            if (defaultTransferHandler == null) {
                defaultTransferHandler = new DefaultTransferHandler();
            }
            setTransferHandler(defaultTransferHandler);
        }
    }
    public void moveCaretPosition(int pos) {
        Document doc = getDocument();
        if (doc != null) {
            if (pos > doc.getLength() || pos < 0) {
                throw new IllegalArgumentException("bad position: " + pos);
            }
            caret.moveDot(pos);
        }
    }
    public static final String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey";
    public void setFocusAccelerator(char aKey) {
        aKey = Character.toUpperCase(aKey);
        char old = focusAccelerator;
        focusAccelerator = aKey;
        firePropertyChange(FOCUS_ACCELERATOR_KEY, old, focusAccelerator);
        firePropertyChange("focusAccelerator", old, focusAccelerator);
    }
    public char getFocusAccelerator() {
        return focusAccelerator;
    }
    public void read(Reader in, Object desc) throws IOException {
        EditorKit kit = getUI().getEditorKit(this);
        Document doc = kit.createDefaultDocument();
        if (desc != null) {
            doc.putProperty(Document.StreamDescriptionProperty, desc);
        }
        try {
            kit.read(in, doc, 0);
            setDocument(doc);
        } catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }
    public void write(Writer out) throws IOException {
        Document doc = getDocument();
        try {
            getUI().getEditorKit(this).write(out, doc, 0, doc.getLength());
        } catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }
    public void removeNotify() {
        super.removeNotify();
        if (getFocusedComponent() == this) {
            AppContext.getAppContext().remove(FOCUSED_COMPONENT);
        }
    }
    public void setCaretPosition(int position) {
        Document doc = getDocument();
        if (doc != null) {
            if (position > doc.getLength() || position < 0) {
                throw new IllegalArgumentException("bad position: " + position);
            }
            caret.setDot(position);
        }
    }
    @Transient
    public int getCaretPosition() {
        return caret.getDot();
    }
    public void setText(String t) {
        try {
            Document doc = getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).replace(0, doc.getLength(), t,null);
            }
            else {
                doc.remove(0, doc.getLength());
                doc.insertString(0, t, null);
            }
        } catch (BadLocationException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
        }
    }
    public String getText() {
        Document doc = getDocument();
        String txt;
        try {
            txt = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            txt = null;
        }
        return txt;
    }
    public String getSelectedText() {
        String txt = null;
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        if (p0 != p1) {
            try {
                Document doc = getDocument();
                txt = doc.getText(p0, p1 - p0);
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return txt;
    }
    public boolean isEditable() {
        return editable;
    }
    public void setEditable(boolean b) {
        if (b != editable) {
            boolean oldVal = editable;
            editable = b;
            enableInputMethods(editable);
            firePropertyChange("editable", Boolean.valueOf(oldVal), Boolean.valueOf(editable));
            repaint();
        }
    }
    @Transient
    public int getSelectionStart() {
        int start = Math.min(caret.getDot(), caret.getMark());
        return start;
    }
    public void setSelectionStart(int selectionStart) {
        select(selectionStart, getSelectionEnd());
    }
    @Transient
    public int getSelectionEnd() {
        int end = Math.max(caret.getDot(), caret.getMark());
        return end;
    }
    public void setSelectionEnd(int selectionEnd) {
        select(getSelectionStart(), selectionEnd);
    }
    public void select(int selectionStart, int selectionEnd) {
        int docLength = getDocument().getLength();
        if (selectionStart < 0) {
            selectionStart = 0;
        }
        if (selectionStart > docLength) {
            selectionStart = docLength;
        }
        if (selectionEnd > docLength) {
            selectionEnd = docLength;
        }
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
        }
        setCaretPosition(selectionStart);
        moveCaretPosition(selectionEnd);
    }
    public void selectAll() {
        Document doc = getDocument();
        if (doc != null) {
            setCaretPosition(0);
            moveCaretPosition(doc.getLength());
        }
    }
    public String getToolTipText(MouseEvent event) {
        String retValue = super.getToolTipText(event);
        if (retValue == null) {
            TextUI ui = getUI();
            if (ui != null) {
                retValue = ui.getToolTipText(this, new Point(event.getX(),
                                                             event.getY()));
            }
        }
        return retValue;
    }
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch(orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height / 10;
        case SwingConstants.HORIZONTAL:
            return visibleRect.width / 10;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch(orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height;
        case SwingConstants.HORIZONTAL:
            return visibleRect.width;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }
    public boolean getScrollableTracksViewportWidth() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getWidth() > getPreferredSize().width;
        }
        return false;
    }
    public boolean getScrollableTracksViewportHeight() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getHeight() > getPreferredSize().height;
        }
        return false;
    }
    public boolean print() throws PrinterException {
        return print(null, null, true, null, null, true);
    }
    public boolean print(final MessageFormat headerFormat,
            final MessageFormat footerFormat) throws PrinterException {
        return print(headerFormat, footerFormat, true, null, null, true);
    }
    public boolean print(final MessageFormat headerFormat,
            final MessageFormat footerFormat,
            final boolean showPrintDialog,
            final PrintService service,
            final PrintRequestAttributeSet attributes,
            final boolean interactive)
            throws PrinterException {
        final PrinterJob job = PrinterJob.getPrinterJob();
        final Printable printable;
        final PrintingStatus printingStatus;
        final boolean isHeadless = GraphicsEnvironment.isHeadless();
        final boolean isEventDispatchThread =
            SwingUtilities.isEventDispatchThread();
        final Printable textPrintable = getPrintable(headerFormat, footerFormat);
        if (interactive && ! isHeadless) {
            printingStatus =
                PrintingStatus.createPrintingStatus(this, job);
            printable =
                printingStatus.createNotificationPrintable(textPrintable);
        } else {
            printingStatus = null;
            printable = textPrintable;
        }
        if (service != null) {
            job.setPrintService(service);
        }
        job.setPrintable(printable);
        final PrintRequestAttributeSet attr = (attributes == null)
            ? new HashPrintRequestAttributeSet()
            : attributes;
        if (showPrintDialog && ! isHeadless && ! job.printDialog(attr)) {
            return false;
        }
        final Callable<Object> doPrint =
            new Callable<Object>() {
                public Object call() throws Exception {
                    try {
                        job.print(attr);
                    } finally {
                        if (printingStatus != null) {
                            printingStatus.dispose();
                        }
                    }
                    return null;
                }
            };
        final FutureTask<Object> futurePrinting =
            new FutureTask<Object>(doPrint);
        final Runnable runnablePrinting =
            new Runnable() {
                public void run() {
                    boolean wasEnabled = false;
                    if (isEventDispatchThread) {
                        if (isEnabled()) {
                            wasEnabled = true;
                            setEnabled(false);
                        }
                    } else {
                        try {
                            wasEnabled = SwingUtilities2.submit(
                                new Callable<Boolean>() {
                                    public Boolean call() throws Exception {
                                        boolean rv = isEnabled();
                                        if (rv) {
                                            setEnabled(false);
                                        }
                                        return rv;
                                    }
                                }).get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof Error) {
                                throw (Error) cause;
                            }
                            if (cause instanceof RuntimeException) {
                                throw (RuntimeException) cause;
                            }
                            throw new AssertionError(cause);
                        }
                    }
                    getDocument().render(futurePrinting);
                    if (wasEnabled) {
                        if (isEventDispatchThread) {
                            setEnabled(true);
                        } else {
                            try {
                                SwingUtilities2.submit(
                                    new Runnable() {
                                        public void run() {
                                            setEnabled(true);
                                        }
                                    }, null).get();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } catch (ExecutionException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof Error) {
                                    throw (Error) cause;
                                }
                                if (cause instanceof RuntimeException) {
                                    throw (RuntimeException) cause;
                                }
                                throw new AssertionError(cause);
                            }
                        }
                    }
                }
            };
        if (! interactive || isHeadless) {
            runnablePrinting.run();
        } else {
            if (isEventDispatchThread) {
                (new Thread(runnablePrinting)).start();
                printingStatus.showModal(true);
            } else {
                printingStatus.showModal(false);
                runnablePrinting.run();
            }
        }
        try {
            futurePrinting.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof PrinterAbortException) {
                if (printingStatus != null
                    && printingStatus.isAborted()) {
                    return false;
                } else {
                    throw (PrinterAbortException) cause;
                }
            } else if (cause instanceof PrinterException) {
                throw (PrinterException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
        return true;
    }
    public Printable getPrintable(final MessageFormat headerFormat,
                                  final MessageFormat footerFormat) {
        return TextComponentPrintable.getPrintable(
                   this, headerFormat, footerFormat);
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTextComponent();
        }
        return accessibleContext;
    }
    public class AccessibleJTextComponent extends AccessibleJComponent
    implements AccessibleText, CaretListener, DocumentListener,
               AccessibleAction, AccessibleEditableText,
               AccessibleExtendedText {
        int caretPos;
        Point oldLocationOnScreen;
        public AccessibleJTextComponent() {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null) {
                doc.addDocumentListener(this);
            }
            JTextComponent.this.addCaretListener(this);
            caretPos = getCaretPosition();
            try {
                oldLocationOnScreen = getLocationOnScreen();
            } catch (IllegalComponentStateException iae) {
            }
            JTextComponent.this.addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    try {
                        Point newLocationOnScreen = getLocationOnScreen();
                        firePropertyChange(ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                                           oldLocationOnScreen,
                                           newLocationOnScreen);
                        oldLocationOnScreen = newLocationOnScreen;
                    } catch (IllegalComponentStateException iae) {
                    }
                }
            });
        }
        public void caretUpdate(CaretEvent e) {
            int dot = e.getDot();
            int mark = e.getMark();
            if (caretPos != dot) {
                firePropertyChange(ACCESSIBLE_CARET_PROPERTY,
                    new Integer(caretPos), new Integer(dot));
                caretPos = dot;
                try {
                    oldLocationOnScreen = getLocationOnScreen();
                } catch (IllegalComponentStateException iae) {
                }
            }
            if (mark != dot) {
                firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, null,
                    getSelectedText());
            }
        }
        public void insertUpdate(DocumentEvent e) {
            final Integer pos = new Integer (e.getOffset());
            if (SwingUtilities.isEventDispatchThread()) {
                firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
            } else {
                Runnable doFire = new Runnable() {
                    public void run() {
                        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY,
                                           null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }
        public void removeUpdate(DocumentEvent e) {
            final Integer pos = new Integer (e.getOffset());
            if (SwingUtilities.isEventDispatchThread()) {
                firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
            } else {
                Runnable doFire = new Runnable() {
                    public void run() {
                        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY,
                                           null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }
        public void changedUpdate(DocumentEvent e) {
            final Integer pos = new Integer (e.getOffset());
            if (SwingUtilities.isEventDispatchThread()) {
                firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
            } else {
                Runnable doFire = new Runnable() {
                    public void run() {
                        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY,
                                           null, pos);
                    }
                };
                SwingUtilities.invokeLater(doFire);
            }
        }
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (JTextComponent.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }
        public AccessibleText getAccessibleText() {
            return this;
        }
        public int getIndexAtPoint(Point p) {
            if (p == null) {
                return -1;
            }
            return JTextComponent.this.viewToModel(p);
        }
            Rectangle getRootEditorRect() {
                Rectangle alloc = JTextComponent.this.getBounds();
                if ((alloc.width > 0) && (alloc.height > 0)) {
                        alloc.x = alloc.y = 0;
                        Insets insets = JTextComponent.this.getInsets();
                        alloc.x += insets.left;
                        alloc.y += insets.top;
                        alloc.width -= insets.left + insets.right;
                        alloc.height -= insets.top + insets.bottom;
                        return alloc;
                }
                return null;
            }
        public Rectangle getCharacterBounds(int i) {
            if (i < 0 || i > model.getLength()-1) {
                return null;
            }
            TextUI ui = getUI();
            if (ui == null) {
                return null;
            }
            Rectangle rect = null;
            Rectangle alloc = getRootEditorRect();
            if (alloc == null) {
                return null;
            }
            if (model instanceof AbstractDocument) {
                ((AbstractDocument)model).readLock();
            }
            try {
                View rootView = ui.getRootView(JTextComponent.this);
                if (rootView != null) {
                    rootView.setSize(alloc.width, alloc.height);
                    Shape bounds = rootView.modelToView(i,
                                    Position.Bias.Forward, i+1,
                                    Position.Bias.Backward, alloc);
                    rect = (bounds instanceof Rectangle) ?
                     (Rectangle)bounds : bounds.getBounds();
                }
            } catch (BadLocationException e) {
            } finally {
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readUnlock();
                }
            }
            return rect;
        }
        public int getCharCount() {
            return model.getLength();
        }
        public int getCaretPosition() {
            return JTextComponent.this.getCaretPosition();
        }
        public AttributeSet getCharacterAttribute(int i) {
            Element e = null;
            if (model instanceof AbstractDocument) {
                ((AbstractDocument)model).readLock();
            }
            try {
                for (e = model.getDefaultRootElement(); ! e.isLeaf(); ) {
                    int index = e.getElementIndex(i);
                    e = e.getElement(index);
                }
            } finally {
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readUnlock();
                }
            }
            return e.getAttributes();
        }
        public int getSelectionStart() {
            return JTextComponent.this.getSelectionStart();
        }
        public int getSelectionEnd() {
            return JTextComponent.this.getSelectionEnd();
        }
        public String getSelectedText() {
            return JTextComponent.this.getSelectedText();
        }
        private class IndexedSegment extends Segment {
            public int modelOffset;
        }
        public String getAtIndex(int part, int index) {
            return getAtIndex(part, index, 0);
        }
        public String getAfterIndex(int part, int index) {
            return getAtIndex(part, index, 1);
        }
        public String getBeforeIndex(int part, int index) {
            return getAtIndex(part, index, -1);
        }
        private String getAtIndex(int part, int index, int direction) {
            if (model instanceof AbstractDocument) {
                ((AbstractDocument)model).readLock();
            }
            try {
                if (index < 0 || index >= model.getLength()) {
                    return null;
                }
                switch (part) {
                case AccessibleText.CHARACTER:
                    if (index + direction < model.getLength() &&
                        index + direction >= 0) {
                        return model.getText(index + direction, 1);
                    }
                    break;
                case AccessibleText.WORD:
                case AccessibleText.SENTENCE:
                    IndexedSegment seg = getSegmentAt(part, index);
                    if (seg != null) {
                        if (direction != 0) {
                            int next;
                            if (direction < 0) {
                                next = seg.modelOffset - 1;
                            }
                            else {
                                next = seg.modelOffset + direction * seg.count;
                            }
                            if (next >= 0 && next <= model.getLength()) {
                                seg = getSegmentAt(part, next);
                            }
                            else {
                                seg = null;
                            }
                        }
                        if (seg != null) {
                            return new String(seg.array, seg.offset,
                                                  seg.count);
                        }
                    }
                    break;
                default:
                    break;
                }
            } catch (BadLocationException e) {
            } finally {
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readUnlock();
                }
            }
            return null;
        }
        private Element getParagraphElement(int index) {
            if (model instanceof PlainDocument ) {
                PlainDocument sdoc = (PlainDocument)model;
                return sdoc.getParagraphElement(index);
            } else if (model instanceof StyledDocument) {
                StyledDocument sdoc = (StyledDocument)model;
                return sdoc.getParagraphElement(index);
            } else {
                Element para;
                for (para = model.getDefaultRootElement(); ! para.isLeaf(); ) {
                    int pos = para.getElementIndex(index);
                    para = para.getElement(pos);
                }
                if (para == null) {
                    return null;
                }
                return para.getParentElement();
            }
        }
        private IndexedSegment getParagraphElementText(int index)
                                  throws BadLocationException {
            Element para = getParagraphElement(index);
            if (para != null) {
                IndexedSegment segment = new IndexedSegment();
                try {
                    int length = para.getEndOffset() - para.getStartOffset();
                    model.getText(para.getStartOffset(), length, segment);
                } catch (BadLocationException e) {
                    return null;
                }
                segment.modelOffset = para.getStartOffset();
                return segment;
            }
            return null;
        }
        private IndexedSegment getSegmentAt(int part, int index) throws
                                  BadLocationException {
            IndexedSegment seg = getParagraphElementText(index);
            if (seg == null) {
                return null;
            }
            BreakIterator iterator;
            switch (part) {
            case AccessibleText.WORD:
                iterator = BreakIterator.getWordInstance(getLocale());
                break;
            case AccessibleText.SENTENCE:
                iterator = BreakIterator.getSentenceInstance(getLocale());
                break;
            default:
                return null;
            }
            seg.first();
            iterator.setText(seg);
            int end = iterator.following(index - seg.modelOffset + seg.offset);
            if (end == BreakIterator.DONE) {
                return null;
            }
            if (end > seg.offset + seg.count) {
                return null;
            }
            int begin = iterator.previous();
            if (begin == BreakIterator.DONE ||
                         begin >= seg.offset + seg.count) {
                return null;
            }
            seg.modelOffset = seg.modelOffset + begin - seg.offset;
            seg.offset = begin;
            seg.count = end - begin;
            return seg;
        }
        public AccessibleEditableText getAccessibleEditableText() {
            return this;
        }
        public void setTextContents(String s) {
            JTextComponent.this.setText(s);
        }
        public void insertTextAtIndex(int index, String s) {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null) {
                try {
                    if (s != null && s.length() > 0) {
                        boolean composedTextSaved = saveComposedText(index);
                        doc.insertString(index, s, null);
                        if (composedTextSaved) {
                            restoreComposedText();
                        }
                    }
                } catch (BadLocationException e) {
                    UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
                }
            }
        }
        public String getTextRange(int startIndex, int endIndex) {
            String txt = null;
            int p0 = Math.min(startIndex, endIndex);
            int p1 = Math.max(startIndex, endIndex);
            if (p0 != p1) {
                try {
                    Document doc = JTextComponent.this.getDocument();
                    txt = doc.getText(p0, p1 - p0);
                } catch (BadLocationException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return txt;
        }
        public void delete(int startIndex, int endIndex) {
            if (isEditable() && isEnabled()) {
                try {
                    int p0 = Math.min(startIndex, endIndex);
                    int p1 = Math.max(startIndex, endIndex);
                    if (p0 != p1) {
                        Document doc = getDocument();
                        doc.remove(p0, p1 - p0);
                    }
                } catch (BadLocationException e) {
                }
            } else {
                UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
            }
        }
        public void cut(int startIndex, int endIndex) {
            selectText(startIndex, endIndex);
            JTextComponent.this.cut();
        }
        public void paste(int startIndex) {
            setCaretPosition(startIndex);
            JTextComponent.this.paste();
        }
        public void replaceText(int startIndex, int endIndex, String s) {
            selectText(startIndex, endIndex);
            JTextComponent.this.replaceSelection(s);
        }
        public void selectText(int startIndex, int endIndex) {
            JTextComponent.this.select(startIndex, endIndex);
        }
        public void setAttributes(int startIndex, int endIndex,
            AttributeSet as) {
            Document doc = JTextComponent.this.getDocument();
            if (doc != null && doc instanceof StyledDocument) {
                StyledDocument sDoc = (StyledDocument)doc;
                int offset = startIndex;
                int length = endIndex - startIndex;
                sDoc.setCharacterAttributes(offset, length, as, true);
            }
        }
        private AccessibleTextSequence getSequenceAtIndex(int part,
            int index, int direction) {
            if (index < 0 || index >= model.getLength()) {
                return null;
            }
            if (direction < -1 || direction > 1) {
                return null;    
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readLock();
                }
                AccessibleTextSequence charSequence = null;
                try {
                    if (index + direction < model.getLength() &&
                        index + direction >= 0) {
                        charSequence =
                            new AccessibleTextSequence(index + direction,
                            index + direction + 1,
                            model.getText(index + direction, 1));
                    }
                } catch (BadLocationException e) {
                } finally {
                    if (model instanceof AbstractDocument) {
                        ((AbstractDocument)model).readUnlock();
                    }
                }
                return charSequence;
            case AccessibleText.WORD:
            case AccessibleText.SENTENCE:
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readLock();
                }
                AccessibleTextSequence rangeSequence = null;
                try {
                    IndexedSegment seg = getSegmentAt(part, index);
                    if (seg != null) {
                        if (direction != 0) {
                            int next;
                            if (direction < 0) {
                                next = seg.modelOffset - 1;
                            }
                            else {
                                next = seg.modelOffset + seg.count;
                            }
                            if (next >= 0 && next <= model.getLength()) {
                                seg = getSegmentAt(part, next);
                            }
                            else {
                                seg = null;
                            }
                        }
                        if (seg != null &&
                            (seg.offset + seg.count) <= model.getLength()) {
                            rangeSequence =
                                new AccessibleTextSequence (seg.offset,
                                seg.offset + seg.count,
                                new String(seg.array, seg.offset, seg.count));
                        } 
                    }
                } catch(BadLocationException e) {
                } finally {
                    if (model instanceof AbstractDocument) {
                        ((AbstractDocument)model).readUnlock();
                    }
                }
                return rangeSequence;
            case AccessibleExtendedText.LINE:
                AccessibleTextSequence lineSequence = null;
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readLock();
                }
                try {
                    int startIndex =
                        Utilities.getRowStart(JTextComponent.this, index);
                    int endIndex =
                        Utilities.getRowEnd(JTextComponent.this, index);
                    if (startIndex >= 0 && endIndex >= startIndex) {
                        if (direction == 0) {
                            lineSequence =
                                new AccessibleTextSequence(startIndex, endIndex,
                                    model.getText(startIndex,
                                        endIndex - startIndex + 1));
                        } else if (direction == -1 && startIndex > 0) {
                            endIndex =
                                Utilities.getRowEnd(JTextComponent.this,
                                    startIndex - 1);
                            startIndex =
                                Utilities.getRowStart(JTextComponent.this,
                                    startIndex - 1);
                            if (startIndex >= 0 && endIndex >= startIndex) {
                                lineSequence =
                                    new AccessibleTextSequence(startIndex,
                                        endIndex,
                                        model.getText(startIndex,
                                            endIndex - startIndex + 1));
                            }
                        } else if (direction == 1 &&
                         endIndex < model.getLength()) {
                            startIndex =
                                Utilities.getRowStart(JTextComponent.this,
                                    endIndex + 1);
                            endIndex =
                                Utilities.getRowEnd(JTextComponent.this,
                                    endIndex + 1);
                            if (startIndex >= 0 && endIndex >= startIndex) {
                                lineSequence =
                                    new AccessibleTextSequence(startIndex,
                                        endIndex, model.getText(startIndex,
                                            endIndex - startIndex + 1));
                            }
                        }
                    }
                } catch(BadLocationException e) {
                } finally {
                    if (model instanceof AbstractDocument) {
                        ((AbstractDocument)model).readUnlock();
                    }
                }
                return lineSequence;
            case AccessibleExtendedText.ATTRIBUTE_RUN:
                int attributeRunStartIndex, attributeRunEndIndex;
                String runText = null;
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readLock();
                }
                try {
                    attributeRunStartIndex = attributeRunEndIndex =
                     Integer.MIN_VALUE;
                    int tempIndex = index;
                    switch (direction) {
                    case -1:
                        attributeRunEndIndex = getRunEdge(index, direction);
                        tempIndex = attributeRunEndIndex - 1;
                        break;
                    case 1:
                        attributeRunStartIndex = getRunEdge(index, direction);
                        tempIndex = attributeRunStartIndex;
                        break;
                    case 0:
                        break;
                    default:
                        throw new AssertionError(direction);
                    }
                    attributeRunStartIndex =
                        (attributeRunStartIndex != Integer.MIN_VALUE) ?
                        attributeRunStartIndex : getRunEdge(tempIndex, -1);
                    attributeRunEndIndex =
                        (attributeRunEndIndex != Integer.MIN_VALUE) ?
                        attributeRunEndIndex : getRunEdge(tempIndex, 1);
                    runText = model.getText(attributeRunStartIndex,
                                            attributeRunEndIndex -
                                            attributeRunStartIndex);
                } catch (BadLocationException e) {
                    return null;
                } finally {
                    if (model instanceof AbstractDocument) {
                        ((AbstractDocument)model).readUnlock();
                    }
                }
                return new AccessibleTextSequence(attributeRunStartIndex,
                                                  attributeRunEndIndex,
                                                  runText);
            default:
                break;
            }
            return null;
        }
        private int getRunEdge(int index, int direction) throws
         BadLocationException {
            if (index < 0 || index >= model.getLength()) {
                throw new BadLocationException("Location out of bounds", index);
            }
            Element indexElement;
            int elementIndex = -1;        
            for (indexElement = model.getDefaultRootElement();
                 ! indexElement.isLeaf(); ) {
                elementIndex = indexElement.getElementIndex(index);
                indexElement = indexElement.getElement(elementIndex);
            }
            if (elementIndex == -1) {
                throw new AssertionError(index);
            }
            AttributeSet indexAS = indexElement.getAttributes();
            Element parent = indexElement.getParentElement();
            Element edgeElement;
            switch (direction) {
            case -1:
            case 1:
                int edgeElementIndex = elementIndex;
                int elementCount = parent.getElementCount();
                while ((edgeElementIndex + direction) > 0 &&
                       ((edgeElementIndex + direction) < elementCount) &&
                       parent.getElement(edgeElementIndex
                       + direction).getAttributes().isEqual(indexAS)) {
                    edgeElementIndex += direction;
                }
                edgeElement = parent.getElement(edgeElementIndex);
                break;
            default:
                throw new AssertionError(direction);
            }
            switch (direction) {
            case -1:
                return edgeElement.getStartOffset();
            case 1:
                return edgeElement.getEndOffset();
            default:
                return Integer.MIN_VALUE;
            }
        }
        public AccessibleTextSequence getTextSequenceAt(int part, int index) {
            return getSequenceAtIndex(part, index, 0);
        }
        public AccessibleTextSequence getTextSequenceAfter(int part, int index) {
            return getSequenceAtIndex(part, index, 1);
        }
        public AccessibleTextSequence getTextSequenceBefore(int part, int index) {
            return getSequenceAtIndex(part, index, -1);
        }
        public Rectangle getTextBounds(int startIndex, int endIndex) {
            if (startIndex < 0 || startIndex > model.getLength()-1 ||
                endIndex < 0 || endIndex > model.getLength()-1 ||
                startIndex > endIndex) {
                return null;
            }
            TextUI ui = getUI();
            if (ui == null) {
                return null;
            }
            Rectangle rect = null;
            Rectangle alloc = getRootEditorRect();
            if (alloc == null) {
                return null;
            }
            if (model instanceof AbstractDocument) {
                ((AbstractDocument)model).readLock();
            }
            try {
                View rootView = ui.getRootView(JTextComponent.this);
                if (rootView != null) {
                    Shape bounds = rootView.modelToView(startIndex,
                                    Position.Bias.Forward, endIndex,
                                    Position.Bias.Backward, alloc);
                    rect = (bounds instanceof Rectangle) ?
                     (Rectangle)bounds : bounds.getBounds();
                }
            } catch (BadLocationException e) {
            } finally {
                if (model instanceof AbstractDocument) {
                    ((AbstractDocument)model).readUnlock();
                }
            }
            return rect;
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public int getAccessibleActionCount() {
            Action [] actions = JTextComponent.this.getActions();
            return actions.length;
        }
        public String getAccessibleActionDescription(int i) {
            Action [] actions = JTextComponent.this.getActions();
            if (i < 0 || i >= actions.length) {
                return null;
            }
            return (String)actions[i].getValue(Action.NAME);
        }
        public boolean doAccessibleAction(int i) {
            Action [] actions = JTextComponent.this.getActions();
            if (i < 0 || i >= actions.length) {
                return false;
            }
            ActionEvent ae =
                new ActionEvent(JTextComponent.this,
                                ActionEvent.ACTION_PERFORMED, null,
                                EventQueue.getMostRecentEventTime(),
                                getCurrentEventModifiers());
            actions[i].actionPerformed(ae);
            return true;
        }
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        caretEvent = new MutableCaretEvent(this);
        addMouseListener(caretEvent);
        addFocusListener(caretEvent);
    }
    private Document model;
    private transient Caret caret;
    private NavigationFilter navigationFilter;
    private transient Highlighter highlighter;
    private transient Keymap keymap;
    private transient MutableCaretEvent caretEvent;
    private Color caretColor;
    private Color selectionColor;
    private Color selectedTextColor;
    private Color disabledTextColor;
    private boolean editable;
    private Insets margin;
    private char focusAccelerator;
    private boolean dragEnabled;
    private DropMode dropMode = DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    public static final class DropLocation extends TransferHandler.DropLocation {
        private final int index;
        private final Position.Bias bias;
        private DropLocation(Point p, int index, Position.Bias bias) {
            super(p);
            this.index = index;
            this.bias = bias;
        }
        public int getIndex() {
            return index;
        }
        public Position.Bias getBias() {
            return bias;
        }
        public String toString() {
            return getClass().getName()
                   + "[dropPoint=" + getDropPoint() + ","
                   + "index=" + index + ","
                   + "bias=" + bias + "]";
        }
    }
    private static DefaultTransferHandler defaultTransferHandler;
    private static Map<String, Boolean> overrideMap;
    protected String paramString() {
        String editableString = (editable ?
                                 "true" : "false");
        String caretColorString = (caretColor != null ?
                                   caretColor.toString() : "");
        String selectionColorString = (selectionColor != null ?
                                       selectionColor.toString() : "");
        String selectedTextColorString = (selectedTextColor != null ?
                                          selectedTextColor.toString() : "");
        String disabledTextColorString = (disabledTextColor != null ?
                                          disabledTextColor.toString() : "");
        String marginString = (margin != null ?
                               margin.toString() : "");
        return super.paramString() +
        ",caretColor=" + caretColorString +
        ",disabledTextColor=" + disabledTextColorString +
        ",editable=" + editableString +
        ",margin=" + marginString +
        ",selectedTextColor=" + selectedTextColorString +
        ",selectionColor=" + selectionColorString;
    }
    static class DefaultTransferHandler extends TransferHandler implements
                                        UIResource {
        public void exportToClipboard(JComponent comp, Clipboard clipboard,
                                      int action) throws IllegalStateException {
            if (comp instanceof JTextComponent) {
                JTextComponent text = (JTextComponent)comp;
                int p0 = text.getSelectionStart();
                int p1 = text.getSelectionEnd();
                if (p0 != p1) {
                    try {
                        Document doc = text.getDocument();
                        String srcData = doc.getText(p0, p1 - p0);
                        StringSelection contents =new StringSelection(srcData);
                        clipboard.setContents(contents, null);
                        if (action == TransferHandler.MOVE) {
                            doc.remove(p0, p1 - p0);
                        }
                    } catch (BadLocationException ble) {}
                }
            }
        }
        public boolean importData(JComponent comp, Transferable t) {
            if (comp instanceof JTextComponent) {
                DataFlavor flavor = getFlavor(t.getTransferDataFlavors());
                if (flavor != null) {
                    InputContext ic = comp.getInputContext();
                    if (ic != null) {
                        ic.endComposition();
                    }
                    try {
                        String data = (String)t.getTransferData(flavor);
                        ((JTextComponent)comp).replaceSelection(data);
                        return true;
                    } catch (UnsupportedFlavorException ufe) {
                    } catch (IOException ioe) {
                    }
                }
            }
            return false;
        }
        public boolean canImport(JComponent comp,
                                 DataFlavor[] transferFlavors) {
            JTextComponent c = (JTextComponent)comp;
            if (!(c.isEditable() && c.isEnabled())) {
                return false;
            }
            return (getFlavor(transferFlavors) != null);
        }
        public int getSourceActions(JComponent c) {
            return NONE;
        }
        private DataFlavor getFlavor(DataFlavor[] flavors) {
            if (flavors != null) {
                for (DataFlavor flavor : flavors) {
                    if (flavor.equals(DataFlavor.stringFlavor)) {
                        return flavor;
                    }
                }
            }
            return null;
        }
    }
    static final JTextComponent getFocusedComponent() {
        return (JTextComponent)AppContext.getAppContext().
            get(FOCUSED_COMPONENT);
    }
    private int getCurrentEventModifiers() {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent)currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent)currentEvent).getModifiers();
        }
        return modifiers;
    }
    private static final Object KEYMAP_TABLE =
        new StringBuilder("JTextComponent_KeymapTable");
    private transient InputMethodRequests inputMethodRequestsHandler;
    private SimpleAttributeSet composedTextAttribute;
    private String composedTextContent;
    private Position composedTextStart;
    private Position composedTextEnd;
    private Position latestCommittedTextStart;
    private Position latestCommittedTextEnd;
    private ComposedTextCaret composedTextCaret;
    private transient Caret originalCaret;
    private boolean checkedInputOverride;
    private boolean needToSendKeyTypedEvent;
    static class DefaultKeymap implements Keymap {
        DefaultKeymap(String nm, Keymap parent) {
            this.nm = nm;
            this.parent = parent;
            bindings = new Hashtable<KeyStroke, Action>();
        }
        public Action getDefaultAction() {
            if (defaultAction != null) {
                return defaultAction;
            }
            return (parent != null) ? parent.getDefaultAction() : null;
        }
        public void setDefaultAction(Action a) {
            defaultAction = a;
        }
        public String getName() {
            return nm;
        }
        public Action getAction(KeyStroke key) {
            Action a = bindings.get(key);
            if ((a == null) && (parent != null)) {
                a = parent.getAction(key);
            }
            return a;
        }
        public KeyStroke[] getBoundKeyStrokes() {
            KeyStroke[] keys = new KeyStroke[bindings.size()];
            int i = 0;
            for (Enumeration<KeyStroke> e = bindings.keys() ; e.hasMoreElements() ;) {
                keys[i++] = e.nextElement();
            }
            return keys;
        }
        public Action[] getBoundActions() {
            Action[] actions = new Action[bindings.size()];
            int i = 0;
            for (Enumeration<Action> e = bindings.elements() ; e.hasMoreElements() ;) {
                actions[i++] = e.nextElement();
            }
            return actions;
        }
        public KeyStroke[] getKeyStrokesForAction(Action a) {
            if (a == null) {
                return null;
            }
            KeyStroke[] retValue = null;
            Vector<KeyStroke> keyStrokes = null;
            for (Enumeration<KeyStroke> keys = bindings.keys(); keys.hasMoreElements();) {
                KeyStroke key = keys.nextElement();
                if (bindings.get(key) == a) {
                    if (keyStrokes == null) {
                        keyStrokes = new Vector<KeyStroke>();
                    }
                    keyStrokes.addElement(key);
                }
            }
            if (parent != null) {
                KeyStroke[] pStrokes = parent.getKeyStrokesForAction(a);
                if (pStrokes != null) {
                    int rCount = 0;
                    for (int counter = pStrokes.length - 1; counter >= 0;
                         counter--) {
                        if (isLocallyDefined(pStrokes[counter])) {
                            pStrokes[counter] = null;
                            rCount++;
                        }
                    }
                    if (rCount > 0 && rCount < pStrokes.length) {
                        if (keyStrokes == null) {
                            keyStrokes = new Vector<KeyStroke>();
                        }
                        for (int counter = pStrokes.length - 1; counter >= 0;
                             counter--) {
                            if (pStrokes[counter] != null) {
                                keyStrokes.addElement(pStrokes[counter]);
                            }
                        }
                    }
                    else if (rCount == 0) {
                        if (keyStrokes == null) {
                            retValue = pStrokes;
                        }
                        else {
                            retValue = new KeyStroke[keyStrokes.size() +
                                                    pStrokes.length];
                            keyStrokes.copyInto(retValue);
                            System.arraycopy(pStrokes, 0, retValue,
                                        keyStrokes.size(), pStrokes.length);
                            keyStrokes = null;
                        }
                    }
                }
            }
            if (keyStrokes != null) {
                retValue = new KeyStroke[keyStrokes.size()];
                keyStrokes.copyInto(retValue);
            }
            return retValue;
        }
        public boolean isLocallyDefined(KeyStroke key) {
            return bindings.containsKey(key);
        }
        public void addActionForKeyStroke(KeyStroke key, Action a) {
            bindings.put(key, a);
        }
        public void removeKeyStrokeBinding(KeyStroke key) {
            bindings.remove(key);
        }
        public void removeBindings() {
            bindings.clear();
        }
        public Keymap getResolveParent() {
            return parent;
        }
        public void setResolveParent(Keymap parent) {
            this.parent = parent;
        }
        public String toString() {
            return "Keymap[" + nm + "]" + bindings;
        }
        String nm;
        Keymap parent;
        Hashtable<KeyStroke, Action> bindings;
        Action defaultAction;
    }
    static class KeymapWrapper extends InputMap {
        static final Object DefaultActionKey = new Object();
        private Keymap keymap;
        KeymapWrapper(Keymap keymap) {
            this.keymap = keymap;
        }
        public KeyStroke[] keys() {
            KeyStroke[] sKeys = super.keys();
            KeyStroke[] keymapKeys = keymap.getBoundKeyStrokes();
            int sCount = (sKeys == null) ? 0 : sKeys.length;
            int keymapCount = (keymapKeys == null) ? 0 : keymapKeys.length;
            if (sCount == 0) {
                return keymapKeys;
            }
            if (keymapCount == 0) {
                return sKeys;
            }
            KeyStroke[] retValue = new KeyStroke[sCount + keymapCount];
            System.arraycopy(sKeys, 0, retValue, 0, sCount);
            System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);
            return retValue;
        }
        public int size() {
            KeyStroke[] keymapStrokes = keymap.getBoundKeyStrokes();
            int keymapCount = (keymapStrokes == null) ? 0:
                               keymapStrokes.length;
            return super.size() + keymapCount;
        }
        public Object get(KeyStroke keyStroke) {
            Object retValue = keymap.getAction(keyStroke);
            if (retValue == null) {
                retValue = super.get(keyStroke);
                if (retValue == null &&
                    keyStroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
                    keymap.getDefaultAction() != null) {
                    retValue = DefaultActionKey;
                }
            }
            return retValue;
        }
    }
    static class KeymapActionMap extends ActionMap {
        private Keymap keymap;
        KeymapActionMap(Keymap keymap) {
            this.keymap = keymap;
        }
        public Object[] keys() {
            Object[] sKeys = super.keys();
            Object[] keymapKeys = keymap.getBoundActions();
            int sCount = (sKeys == null) ? 0 : sKeys.length;
            int keymapCount = (keymapKeys == null) ? 0 : keymapKeys.length;
            boolean hasDefault = (keymap.getDefaultAction() != null);
            if (hasDefault) {
                keymapCount++;
            }
            if (sCount == 0) {
                if (hasDefault) {
                    Object[] retValue = new Object[keymapCount];
                    if (keymapCount > 1) {
                        System.arraycopy(keymapKeys, 0, retValue, 0,
                                         keymapCount - 1);
                    }
                    retValue[keymapCount - 1] = KeymapWrapper.DefaultActionKey;
                    return retValue;
                }
                return keymapKeys;
            }
            if (keymapCount == 0) {
                return sKeys;
            }
            Object[] retValue = new Object[sCount + keymapCount];
            System.arraycopy(sKeys, 0, retValue, 0, sCount);
            if (hasDefault) {
                if (keymapCount > 1) {
                    System.arraycopy(keymapKeys, 0, retValue, sCount,
                                     keymapCount - 1);
                }
                retValue[sCount + keymapCount - 1] = KeymapWrapper.
                                                 DefaultActionKey;
            }
            else {
                System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);
            }
            return retValue;
        }
        public int size() {
            Object[] actions = keymap.getBoundActions();
            int keymapCount = (actions == null) ? 0 : actions.length;
            if (keymap.getDefaultAction() != null) {
                keymapCount++;
            }
            return super.size() + keymapCount;
        }
        public Action get(Object key) {
            Action retValue = super.get(key);
            if (retValue == null) {
                if (key == KeymapWrapper.DefaultActionKey) {
                    retValue = keymap.getDefaultAction();
                }
                else if (key instanceof Action) {
                    retValue = (Action)key;
                }
            }
            return retValue;
        }
    }
    private static final Object FOCUSED_COMPONENT =
        new StringBuilder("JTextComponent_FocusedComponent");
    public static final String DEFAULT_KEYMAP = "default";
    static class MutableCaretEvent extends CaretEvent implements ChangeListener, FocusListener, MouseListener {
        MutableCaretEvent(JTextComponent c) {
            super(c);
        }
        final void fire() {
            JTextComponent c = (JTextComponent) getSource();
            if (c != null) {
                Caret caret = c.getCaret();
                dot = caret.getDot();
                mark = caret.getMark();
                c.fireCaretUpdate(this);
            }
        }
        public final String toString() {
            return "dot=" + dot + "," + "mark=" + mark;
        }
        public final int getDot() {
            return dot;
        }
        public final int getMark() {
            return mark;
        }
        public final void stateChanged(ChangeEvent e) {
            if (! dragActive) {
                fire();
            }
        }
        public void focusGained(FocusEvent fe) {
            AppContext.getAppContext().put(FOCUSED_COMPONENT,
                                           fe.getSource());
        }
        public void focusLost(FocusEvent fe) {
        }
        public final void mousePressed(MouseEvent e) {
            dragActive = true;
        }
        public final void mouseReleased(MouseEvent e) {
            dragActive = false;
            fire();
        }
        public final void mouseClicked(MouseEvent e) {
        }
        public final void mouseEntered(MouseEvent e) {
        }
        public final void mouseExited(MouseEvent e) {
        }
        private boolean dragActive;
        private int dot;
        private int mark;
    }
    protected void processInputMethodEvent(InputMethodEvent e) {
        super.processInputMethodEvent(e);
        if (!e.isConsumed()) {
            if (! isEditable()) {
                return;
            } else {
                switch (e.getID()) {
                case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
                    replaceInputMethodText(e);
                case InputMethodEvent.CARET_POSITION_CHANGED:
                    setInputMethodCaretPosition(e);
                    break;
                }
            }
            e.consume();
        }
    }
    public InputMethodRequests getInputMethodRequests() {
        if (inputMethodRequestsHandler == null) {
            inputMethodRequestsHandler = new InputMethodRequestsHandler();
            Document doc = getDocument();
            if (doc != null) {
                doc.addDocumentListener((DocumentListener)inputMethodRequestsHandler);
            }
        }
        return inputMethodRequestsHandler;
    }
    public void addInputMethodListener(InputMethodListener l) {
        super.addInputMethodListener(l);
        if (l != null) {
            needToSendKeyTypedEvent = false;
            checkedInputOverride = true;
        }
    }
    class InputMethodRequestsHandler implements InputMethodRequests, DocumentListener {
        public AttributedCharacterIterator cancelLatestCommittedText(
                                                Attribute[] attributes) {
            Document doc = getDocument();
            if ((doc != null) && (latestCommittedTextStart != null)
                && (!latestCommittedTextStart.equals(latestCommittedTextEnd))) {
                try {
                    int startIndex = latestCommittedTextStart.getOffset();
                    int endIndex = latestCommittedTextEnd.getOffset();
                    String latestCommittedText =
                        doc.getText(startIndex, endIndex - startIndex);
                    doc.remove(startIndex, endIndex - startIndex);
                    return new AttributedString(latestCommittedText).getIterator();
                } catch (BadLocationException ble) {}
            }
            return null;
        }
        public AttributedCharacterIterator getCommittedText(int beginIndex,
                                        int endIndex, Attribute[] attributes) {
            int composedStartIndex = 0;
            int composedEndIndex = 0;
            if (composedTextExists()) {
                composedStartIndex = composedTextStart.getOffset();
                composedEndIndex = composedTextEnd.getOffset();
            }
            String committed;
            try {
                if (beginIndex < composedStartIndex) {
                    if (endIndex <= composedStartIndex) {
                        committed = getText(beginIndex, endIndex - beginIndex);
                    } else {
                        int firstPartLength = composedStartIndex - beginIndex;
                        committed = getText(beginIndex, firstPartLength) +
                            getText(composedEndIndex, endIndex - beginIndex - firstPartLength);
                    }
                } else {
                    committed = getText(beginIndex + (composedEndIndex - composedStartIndex),
                                        endIndex - beginIndex);
                }
            } catch (BadLocationException ble) {
                throw new IllegalArgumentException("Invalid range");
            }
            return new AttributedString(committed).getIterator();
        }
        public int getCommittedTextLength() {
            Document doc = getDocument();
            int length = 0;
            if (doc != null) {
                length = doc.getLength();
                if (composedTextContent != null) {
                    if (composedTextEnd == null
                          || composedTextStart == null) {
                        length -= composedTextContent.length();
                    } else {
                        length -= composedTextEnd.getOffset() -
                            composedTextStart.getOffset();
                    }
                }
            }
            return length;
        }
        public int getInsertPositionOffset() {
            int composedStartIndex = 0;
            int composedEndIndex = 0;
            if (composedTextExists()) {
                composedStartIndex = composedTextStart.getOffset();
                composedEndIndex = composedTextEnd.getOffset();
            }
            int caretIndex = getCaretPosition();
            if (caretIndex < composedStartIndex) {
                return caretIndex;
            } else if (caretIndex < composedEndIndex) {
                return composedStartIndex;
            } else {
                return caretIndex - (composedEndIndex - composedStartIndex);
            }
        }
        public TextHitInfo getLocationOffset(int x, int y) {
            if (composedTextAttribute == null) {
                return null;
            } else {
                Point p = getLocationOnScreen();
                p.x = x - p.x;
                p.y = y - p.y;
                int pos = viewToModel(p);
                if ((pos >= composedTextStart.getOffset()) &&
                    (pos <= composedTextEnd.getOffset())) {
                    return TextHitInfo.leading(pos - composedTextStart.getOffset());
                } else {
                    return null;
                }
            }
        }
        public Rectangle getTextLocation(TextHitInfo offset) {
            Rectangle r;
            try {
                r = modelToView(getCaretPosition());
                if (r != null) {
                    Point p = getLocationOnScreen();
                    r.translate(p.x, p.y);
                }
            } catch (BadLocationException ble) {
                r = null;
            }
            if (r == null)
                r = new Rectangle();
            return r;
        }
        public AttributedCharacterIterator getSelectedText(
                                                Attribute[] attributes) {
            String selection = JTextComponent.this.getSelectedText();
            if (selection != null) {
                return new AttributedString(selection).getIterator();
            } else {
                return null;
            }
        }
        public void changedUpdate(DocumentEvent e) {
            latestCommittedTextStart = latestCommittedTextEnd = null;
        }
        public void insertUpdate(DocumentEvent e) {
            latestCommittedTextStart = latestCommittedTextEnd = null;
        }
        public void removeUpdate(DocumentEvent e) {
            latestCommittedTextStart = latestCommittedTextEnd = null;
        }
    }
    private void replaceInputMethodText(InputMethodEvent e) {
        int commitCount = e.getCommittedCharacterCount();
        AttributedCharacterIterator text = e.getText();
        int composedTextIndex;
        Document doc = getDocument();
        if (composedTextExists()) {
            try {
                doc.remove(composedTextStart.getOffset(),
                           composedTextEnd.getOffset() -
                           composedTextStart.getOffset());
            } catch (BadLocationException ble) {}
            composedTextStart = composedTextEnd = null;
            composedTextAttribute = null;
            composedTextContent = null;
        }
        if (text != null) {
            text.first();
            int committedTextStartIndex = 0;
            int committedTextEndIndex = 0;
            if (commitCount > 0) {
                committedTextStartIndex = caret.getDot();
                if (shouldSynthensizeKeyEvents()) {
                    for (char c = text.current(); commitCount > 0;
                         c = text.next(), commitCount--) {
                        KeyEvent ke = new KeyEvent(this, KeyEvent.KEY_TYPED,
                                                   EventQueue.getMostRecentEventTime(),
                                                   0, KeyEvent.VK_UNDEFINED, c);
                        processKeyEvent(ke);
                    }
                } else {
                    StringBuilder strBuf = new StringBuilder();
                    for (char c = text.current(); commitCount > 0;
                         c = text.next(), commitCount--) {
                        strBuf.append(c);
                    }
                    mapCommittedTextToAction(strBuf.toString());
                }
                committedTextEndIndex = caret.getDot();
            }
            composedTextIndex = text.getIndex();
            if (composedTextIndex < text.getEndIndex()) {
                createComposedTextAttribute(composedTextIndex, text);
                try {
                    replaceSelection(null);
                    doc.insertString(caret.getDot(), composedTextContent,
                                        composedTextAttribute);
                    composedTextStart = doc.createPosition(caret.getDot() -
                                                composedTextContent.length());
                    composedTextEnd = doc.createPosition(caret.getDot());
                } catch (BadLocationException ble) {
                    composedTextStart = composedTextEnd = null;
                    composedTextAttribute = null;
                    composedTextContent = null;
                }
            }
            if (committedTextStartIndex != committedTextEndIndex) {
                try {
                    latestCommittedTextStart = doc.
                        createPosition(committedTextStartIndex);
                    latestCommittedTextEnd = doc.
                        createPosition(committedTextEndIndex);
                } catch (BadLocationException ble) {
                    latestCommittedTextStart =
                        latestCommittedTextEnd = null;
                }
            } else {
                latestCommittedTextStart =
                    latestCommittedTextEnd = null;
            }
        }
    }
    private void createComposedTextAttribute(int composedIndex,
                                        AttributedCharacterIterator text) {
        Document doc = getDocument();
        StringBuilder strBuf = new StringBuilder();
        for (char c = text.setIndex(composedIndex);
             c != CharacterIterator.DONE; c = text.next()) {
            strBuf.append(c);
        }
        composedTextContent = strBuf.toString();
        composedTextAttribute = new SimpleAttributeSet();
        composedTextAttribute.addAttribute(StyleConstants.ComposedTextAttribute,
                new AttributedString(text, composedIndex, text.getEndIndex()));
    }
    protected boolean saveComposedText(int pos) {
        if (composedTextExists()) {
            int start = composedTextStart.getOffset();
            int len = composedTextEnd.getOffset() -
                composedTextStart.getOffset();
            if (pos >= start && pos <= start + len) {
                try {
                    getDocument().remove(start, len);
                    return true;
                } catch (BadLocationException ble) {}
            }
        }
        return false;
    }
    protected void restoreComposedText() {
        Document doc = getDocument();
        try {
            doc.insertString(caret.getDot(),
                             composedTextContent,
                             composedTextAttribute);
            composedTextStart = doc.createPosition(caret.getDot() -
                                composedTextContent.length());
            composedTextEnd = doc.createPosition(caret.getDot());
        } catch (BadLocationException ble) {}
    }
    private void mapCommittedTextToAction(String committedText) {
        Keymap binding = getKeymap();
        if (binding != null) {
            Action a = null;
            if (committedText.length() == 1) {
                KeyStroke k = KeyStroke.getKeyStroke(committedText.charAt(0));
                a = binding.getAction(k);
            }
            if (a == null) {
                a = binding.getDefaultAction();
            }
            if (a != null) {
                ActionEvent ae =
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                    committedText,
                                    EventQueue.getMostRecentEventTime(),
                                    getCurrentEventModifiers());
                a.actionPerformed(ae);
            }
        }
    }
    private void setInputMethodCaretPosition(InputMethodEvent e) {
        int dot;
        if (composedTextExists()) {
            dot = composedTextStart.getOffset();
            if (!(caret instanceof ComposedTextCaret)) {
                if (composedTextCaret == null) {
                    composedTextCaret = new ComposedTextCaret();
                }
                originalCaret = caret;
                exchangeCaret(originalCaret, composedTextCaret);
            }
            TextHitInfo caretPos = e.getCaret();
            if (caretPos != null) {
                int index = caretPos.getInsertionIndex();
                dot += index;
                if (index == 0) {
                    try {
                        Rectangle d = modelToView(dot);
                        Rectangle end = modelToView(composedTextEnd.getOffset());
                        Rectangle b = getBounds();
                        d.x += Math.min(end.x - d.x, b.width);
                        scrollRectToVisible(d);
                    } catch (BadLocationException ble) {}
                }
            }
            caret.setDot(dot);
        } else if (caret instanceof ComposedTextCaret) {
            dot = caret.getDot();
            exchangeCaret(caret, originalCaret);
            caret.setDot(dot);
        }
    }
    private void exchangeCaret(Caret oldCaret, Caret newCaret) {
        int blinkRate = oldCaret.getBlinkRate();
        setCaret(newCaret);
        caret.setBlinkRate(blinkRate);
        caret.setVisible(hasFocus());
    }
    private boolean shouldSynthensizeKeyEvents() {
        if (!checkedInputOverride) {
            checkedInputOverride = true;
            needToSendKeyTypedEvent =
                             !isProcessInputMethodEventOverridden();
        }
        return needToSendKeyTypedEvent;
    }
    private boolean isProcessInputMethodEventOverridden() {
        if (overrideMap == null) {
            overrideMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
        }
        Boolean retValue = overrideMap.get(getClass().getName());
        if (retValue != null) {
            return retValue.booleanValue();
        }
        Boolean ret = AccessController.doPrivileged(new
                       PrivilegedAction<Boolean>() {
            public Boolean run() {
                return isProcessInputMethodEventOverridden(
                                JTextComponent.this.getClass());
            }
        });
        return ret.booleanValue();
    }
    boolean composedTextExists() {
        return (composedTextStart != null);
    }
    class ComposedTextCaret extends DefaultCaret implements Serializable {
        Color bg;
        public void install(JTextComponent c) {
            super.install(c);
            Document doc = c.getDocument();
            if (doc instanceof StyledDocument) {
                StyledDocument sDoc = (StyledDocument)doc;
                Element elem = sDoc.getCharacterElement(c.composedTextStart.getOffset());
                AttributeSet attr = elem.getAttributes();
                bg = sDoc.getBackground(attr);
            }
            if (bg == null) {
                bg = c.getBackground();
            }
        }
        public void paint(Graphics g) {
            if(isVisible()) {
                try {
                    Rectangle r = component.modelToView(getDot());
                    g.setXORMode(bg);
                    g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                    g.setPaintMode();
                } catch (BadLocationException e) {
                }
            }
        }
        protected void positionCaret(MouseEvent me) {
            JTextComponent host = component;
            Point pt = new Point(me.getX(), me.getY());
            int offset = host.viewToModel(pt);
            int composedStartIndex = host.composedTextStart.getOffset();
            if ((offset < composedStartIndex) ||
                (offset > composedTextEnd.getOffset())) {
                try {
                    Position newPos = host.getDocument().createPosition(offset);
                    host.getInputContext().endComposition();
                    EventQueue.invokeLater(new DoSetCaretPosition(host, newPos));
                } catch (BadLocationException ble) {
                    System.err.println(ble);
                }
            } else {
                super.positionCaret(me);
            }
        }
    }
    private class DoSetCaretPosition implements Runnable {
        JTextComponent host;
        Position newPos;
        DoSetCaretPosition(JTextComponent host, Position newPos) {
            this.host = host;
            this.newPos = newPos;
        }
        public void run() {
            host.setCaretPosition(newPos.getOffset());
        }
    }
}
