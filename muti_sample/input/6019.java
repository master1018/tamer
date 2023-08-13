public class TextArea extends TextComponent {
    int rows;
    int columns;
    private static final String base = "text";
    private static int nameCounter = 0;
    public static final int SCROLLBARS_BOTH = 0;
    public static final int SCROLLBARS_VERTICAL_ONLY = 1;
    public static final int SCROLLBARS_HORIZONTAL_ONLY = 2;
    public static final int SCROLLBARS_NONE = 3;
    private int scrollbarVisibility;
    private static Set forwardTraversalKeys, backwardTraversalKeys;
     private static final long serialVersionUID = 3692302836626095722L;
    private static native void initIDs();
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        forwardTraversalKeys = KeyboardFocusManager.initFocusTraversalKeysSet(
            "ctrl TAB",
            new HashSet());
        backwardTraversalKeys = KeyboardFocusManager.initFocusTraversalKeysSet(
            "ctrl shift TAB",
            new HashSet());
    }
    public TextArea() throws HeadlessException {
        this("", 0, 0, SCROLLBARS_BOTH);
    }
    public TextArea(String text) throws HeadlessException {
        this(text, 0, 0, SCROLLBARS_BOTH);
    }
    public TextArea(int rows, int columns) throws HeadlessException {
        this("", rows, columns, SCROLLBARS_BOTH);
    }
    public TextArea(String text, int rows, int columns)
        throws HeadlessException {
        this(text, rows, columns, SCROLLBARS_BOTH);
    }
    public TextArea(String text, int rows, int columns, int scrollbars)
        throws HeadlessException {
        super(text);
        this.rows = (rows >= 0) ? rows : 0;
        this.columns = (columns >= 0) ? columns : 0;
        if (scrollbars >= SCROLLBARS_BOTH && scrollbars <= SCROLLBARS_NONE) {
            this.scrollbarVisibility = scrollbars;
        } else {
            this.scrollbarVisibility = SCROLLBARS_BOTH;
        }
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                              forwardTraversalKeys);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                              backwardTraversalKeys);
    }
    String constructComponentName() {
        synchronized (TextArea.class) {
            return base + nameCounter++;
        }
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createTextArea(this);
            super.addNotify();
        }
    }
    public void insert(String str, int pos) {
        insertText(str, pos);
    }
    @Deprecated
    public synchronized void insertText(String str, int pos) {
        TextAreaPeer peer = (TextAreaPeer)this.peer;
        if (peer != null) {
            peer.insert(str, pos);
        } else {
            text = text.substring(0, pos) + str + text.substring(pos);
        }
    }
    public void append(String str) {
        appendText(str);
    }
    @Deprecated
    public synchronized void appendText(String str) {
        if (peer != null) {
            insertText(str, getText().length());
        } else {
            text = text + str;
        }
    }
    public void replaceRange(String str, int start, int end) {
        replaceText(str, start, end);
    }
    @Deprecated
    public synchronized void replaceText(String str, int start, int end) {
        TextAreaPeer peer = (TextAreaPeer)this.peer;
        if (peer != null) {
            peer.replaceRange(str, start, end);
        } else {
            text = text.substring(0, start) + str + text.substring(end);
        }
    }
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        int oldVal = this.rows;
        if (rows < 0) {
            throw new IllegalArgumentException("rows less than zero.");
        }
        if (rows != oldVal) {
            this.rows = rows;
            invalidate();
        }
    }
    public int getColumns() {
        return columns;
    }
    public void setColumns(int columns) {
        int oldVal = this.columns;
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        if (columns != oldVal) {
            this.columns = columns;
            invalidate();
        }
    }
    public int getScrollbarVisibility() {
        return scrollbarVisibility;
    }
    public Dimension getPreferredSize(int rows, int columns) {
        return preferredSize(rows, columns);
    }
    @Deprecated
    public Dimension preferredSize(int rows, int columns) {
        synchronized (getTreeLock()) {
            TextAreaPeer peer = (TextAreaPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(rows, columns) :
                       super.preferredSize();
        }
    }
    public Dimension getPreferredSize() {
        return preferredSize();
    }
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return ((rows > 0) && (columns > 0)) ?
                        preferredSize(rows, columns) :
                        super.preferredSize();
        }
    }
    public Dimension getMinimumSize(int rows, int columns) {
        return minimumSize(rows, columns);
    }
    @Deprecated
    public Dimension minimumSize(int rows, int columns) {
        synchronized (getTreeLock()) {
            TextAreaPeer peer = (TextAreaPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(rows, columns) :
                       super.minimumSize();
        }
    }
    public Dimension getMinimumSize() {
        return minimumSize();
    }
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return ((rows > 0) && (columns > 0)) ?
                        minimumSize(rows, columns) :
                        super.minimumSize();
        }
    }
    protected String paramString() {
        String sbVisStr;
        switch (scrollbarVisibility) {
            case SCROLLBARS_BOTH:
                sbVisStr = "both";
                break;
            case SCROLLBARS_VERTICAL_ONLY:
                sbVisStr = "vertical-only";
                break;
            case SCROLLBARS_HORIZONTAL_ONLY:
                sbVisStr = "horizontal-only";
                break;
            case SCROLLBARS_NONE:
                sbVisStr = "none";
                break;
            default:
                sbVisStr = "invalid display policy";
        }
        return super.paramString() + ",rows=" + rows +
            ",columns=" + columns +
          ",scrollbarVisibility=" + sbVisStr;
    }
    private int textAreaSerializedDataVersion = 2;
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
        s.defaultReadObject();
        if (columns < 0) {
            columns = 0;
        }
        if (rows < 0) {
            rows = 0;
        }
        if ((scrollbarVisibility < SCROLLBARS_BOTH) ||
            (scrollbarVisibility > SCROLLBARS_NONE)) {
            this.scrollbarVisibility = SCROLLBARS_BOTH;
        }
        if (textAreaSerializedDataVersion < 2) {
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                  forwardTraversalKeys);
            setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                  backwardTraversalKeys);
        }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextArea();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTTextArea extends AccessibleAWTTextComponent
    {
        private static final long serialVersionUID = 3472827823632144419L;
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }
}
