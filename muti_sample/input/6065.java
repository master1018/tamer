class WChoicePeer extends WComponentPeer implements ChoicePeer {
    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(((Choice)target).getFont());
        Choice c = (Choice)target;
        int w = 0;
        for (int i = c.getItemCount() ; i-- > 0 ;) {
            w = Math.max(fm.stringWidth(c.getItem(i)), w);
        }
        return new Dimension(28 + w, Math.max(fm.getHeight() + 6, 15));
    }
    public boolean isFocusable() {
        return true;
    }
    public native void select(int index);
    public void add(String item, int index) {
        addItem(item, index);
    }
    public boolean shouldClearRectBeforePaint() {
        return false;
    }
    public native void removeAll();
    public native void remove(int index);
    public void addItem(String item, int index) {
        addItems(new String[] {item}, index);
    }
    public native void addItems(String[] items, int index);
    public synchronized native void reshape(int x, int y, int width, int height);
    private WindowListener windowListener;
    WChoicePeer(Choice target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    void initialize() {
        Choice opt = (Choice)target;
        int itemCount = opt.getItemCount();
        if (itemCount > 0) {
            String[] items = new String[itemCount];
            for (int i=0; i < itemCount; i++) {
                items[i] = opt.getItem(i);
            }
            addItems(items, 0);
            if (opt.getSelectedIndex() >= 0) {
                select(opt.getSelectedIndex());
            }
        }
        Window parentWindow = SunToolkit.getContainingWindow((Component)target);
        if (parentWindow != null) {
            WWindowPeer wpeer = (WWindowPeer)parentWindow.getPeer();
            if (wpeer != null) {
                windowListener = new WindowAdapter() {
                        public void windowIconified(WindowEvent e) {
                            closeList();
                        }
                        public void windowClosing(WindowEvent e) {
                            closeList();
                        }
                    };
                wpeer.addWindowListener(windowListener);
            }
        }
        super.initialize();
    }
    protected void disposeImpl() {
        Window parentWindow = SunToolkit.getContainingWindow((Component)target);
        if (parentWindow != null) {
            WWindowPeer wpeer = (WWindowPeer)parentWindow.getPeer();
            if (wpeer != null) {
                wpeer.removeWindowListener(windowListener);
            }
        }
        super.disposeImpl();
    }
    void handleAction(final int index) {
        final Choice c = (Choice)target;
        WToolkit.executeOnEventHandlerThread(c, new Runnable() {
            public void run() {
                c.select(index);
                postEvent(new ItemEvent(c, ItemEvent.ITEM_STATE_CHANGED,
                                c.getItem(index), ItemEvent.SELECTED));
            }
        });
    }
    int getDropDownHeight() {
        Choice c = (Choice)target;
        FontMetrics fm = getFontMetrics(c.getFont());
        int maxItems = Math.min(c.getItemCount(), 8);
        return fm.getHeight() * maxItems;
    }
    public Dimension minimumSize() {
            return getMinimumSize();
    }
    native void closeList();
}
