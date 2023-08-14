class WListPeer extends WComponentPeer implements ListPeer {
    public Dimension minimumSize() {
        return minimumSize(4);
    }
    public boolean isFocusable() {
        return true;
    }
    public int[] getSelectedIndexes() {
        List l = (List)target;
        int len = l.countItems();
        int sel[] = new int[len];
        int nsel = 0;
        for (int i = 0 ; i < len ; i++) {
            if (isSelected(i)) {
                sel[nsel++] = i;
            }
        }
        int selected[] = new int[nsel];
        System.arraycopy(sel, 0, selected, 0, nsel);
        return selected;
    }
    public void add(String item, int index) {
        addItem(item, index);
    }
    public void removeAll() {
        clear();
    }
    public void setMultipleMode (boolean b) {
        setMultipleSelections(b);
    }
    public Dimension getPreferredSize(int rows) {
        return preferredSize(rows);
    }
    public Dimension getMinimumSize(int rows) {
        return minimumSize(rows);
    }
    private FontMetrics   fm;
    public void addItem(String item, int index) {
        addItems(new String[] {item}, index, fm.stringWidth(item));
    }
    native void addItems(String[] items, int index, int width);
    public native void delItems(int start, int end);
    public void clear() {
        List l = (List)target;
        delItems(0, l.countItems());
    }
    public native void select(int index);
    public native void deselect(int index);
    public native void makeVisible(int index);
    public native void setMultipleSelections(boolean v);
    public native int  getMaxWidth();
    public Dimension preferredSize(int v) {
        if ( fm == null ) {
            List li = (List)target;
            fm = getFontMetrics( li.getFont() );
        }
        Dimension d = minimumSize(v);
        d.width = Math.max(d.width, getMaxWidth() + 20);
        return d;
    }
    public Dimension minimumSize(int v) {
        return new Dimension(20 + fm.stringWidth("0123456789abcde"),
                             (fm.getHeight() * v) + 4); 
    }
    WListPeer(List target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    void initialize() {
        List li = (List)target;
        fm = getFontMetrics( li.getFont() );
        Font  f = li.getFont();
        if (f != null) {
            setFont(f);
        }
        int  nitems = li.countItems();
        if (nitems > 0) {
            String[] items = new String[nitems];
            int maxWidth = 0;
            int width = 0;
            for (int i = 0; i < nitems; i++) {
                items[i] = li.getItem(i);
                width = fm.stringWidth(items[i]);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
            addItems(items, 0, maxWidth);
        }
        setMultipleSelections(li.allowsMultipleSelections());
        int sel[] = li.getSelectedIndexes();
        for (int i = 0 ; i < sel.length ; i++) {
            select(sel[i]);
        }
        int index = li.getVisibleIndex();
        if (index < 0 && sel.length > 0) {
            index = sel[0];
        }
        if (index >= 0) {
            makeVisible(index);
        }
        super.initialize();
    }
    public boolean shouldClearRectBeforePaint() {
        return false;
    }
    private native void updateMaxItemWidth();
     native boolean isSelected(int index);
    public synchronized void _setFont(Font f)
    {
        super._setFont( f );
            fm = getFontMetrics( ((List)target).getFont() );
        updateMaxItemWidth();
    }
    void handleAction(final int index, final long when, final int modifiers) {
        final List l = (List)target;
        WToolkit.executeOnEventHandlerThread(l, new Runnable() {
            public void run() {
                l.select(index);
                postEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                          l.getItem(index), when, modifiers));
            }
        });
    }
    void handleListChanged(final int index) {
        final List l = (List)target;
        WToolkit.executeOnEventHandlerThread(l, new Runnable() {
            public void run() {
                postEvent(new ItemEvent(l, ItemEvent.ITEM_STATE_CHANGED,
                                Integer.valueOf(index),
                                isSelected(index)? ItemEvent.SELECTED :
                                                   ItemEvent.DESELECTED));
            }
        });
    }
}
