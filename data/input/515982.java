public class PaletteComposite extends Composite {
    private Composite mRoot;
    private ScrollBar mVBar;
    private ControlListener mControlListener;
    private Listener mScrollbarListener;
    public PaletteComposite(Composite parent) {
        super(parent, SWT.BORDER | SWT.V_SCROLL);
        mVBar = getVerticalBar();
        mScrollbarListener = new Listener() {
            public void handleEvent(Event event) {
                scrollScrollbar();
            }
        };
        mVBar.addListener(SWT.Selection, mScrollbarListener);
        mControlListener = new ControlListener() {
            public void controlMoved(ControlEvent e) {
            }
            public void controlResized(ControlEvent e) {
                if (recomputeScrollbar()) {
                    redraw();
                }
            }
        };
        addControlListener(mControlListener);
    }
    @Override
    protected void checkSubclass() {
    }
    @Override
    public void dispose() {
        if (mControlListener != null) {
            removeControlListener(mControlListener);
            mControlListener = null;
        }
        if (mVBar != null && !mVBar.isDisposed()) {
            if (mScrollbarListener != null) {
                mVBar.removeListener(SWT.Selection, mScrollbarListener);
            }
            mVBar = null;
        }
        super.dispose();
    }
    public void reloadPalette(AndroidTargetData targetData) {
        for (Control c : getChildren()) {
            c.dispose();
        }
        setGridLayout(this, 2);
        mRoot = new Composite(this, SWT.NONE);
        setGridLayout(mRoot, 0);
        if (targetData != null) {
            addGroup(mRoot, "Views", targetData.getLayoutDescriptors().getViewDescriptors());
            addGroup(mRoot, "Layouts", targetData.getLayoutDescriptors().getLayoutDescriptors());
        }
        layout(true);
        recomputeScrollbar();
    }
    private  boolean recomputeScrollbar() {
        if (mVBar != null && mRoot != null) {
            int sel = mVBar.getSelection();
            int max = mVBar.getMaximum();
            float current = max > 0 ? (float)sel / max : 0;
            int ry = mRoot.getSize().y;
            Control[] children = mRoot.getChildren();
            findVisibleItem: for (int i = children.length - 1; i >= 0; i--) {
                Control ci = children[i];
                if (ci.isVisible() && ci instanceof Composite) {
                    Control[] children2 = ((Composite) ci).getChildren();
                    for (int j = children2.length - 1; j >= 0; j--) {
                        Control cj = children2[j];
                        if (cj.isVisible()) {
                            ry = ci.getLocation().y + cj.getLocation().y + cj.getSize().y;
                            break findVisibleItem;
                        }
                    }
                }
            }
            int vy = getSize().y;
            int y = ry > vy ? ry - vy + 2 : 0;
            float ft = ry > 0 ? (float)vy / ry : 1;
            int thumb = (int) Math.ceil(y * ft);
            y += thumb;
            if (y != max) {
                mVBar.setEnabled(y > 0);
                mVBar.setMaximum(y < 0 ? 1 : y);
                mVBar.setSelection((int) (y * current));
                mVBar.setThumb(thumb);
                scrollScrollbar();
                return true;
            }
        }
        return false;
    }
    private void scrollScrollbar() {
        if (mVBar != null && mRoot != null) {
            Point p = mRoot.getLocation();
            p.y = - mVBar.getSelection();
            mRoot.setLocation(p);
        }
    }
    private void setGridLayout(Composite parent, int spacing) {
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = spacing;
        gl.marginBottom = spacing;
        gl.marginLeft = spacing;
        gl.marginRight = spacing;
        gl.marginTop = spacing;
        gl.marginBottom = spacing;
        parent.setLayout(gl);
    }
    private void addGroup(Composite parent,
            String uiName,
            List<ElementDescriptor> descriptors) {
        Composite group = new Composite(parent, SWT.NONE);
        setGridLayout(group, 0);
        Toggle toggle = new Toggle(group, uiName);
        for (ElementDescriptor desc : descriptors) {
            Item item = new Item(group, desc);
            toggle.addItem(item);
            GridData gd = new GridData();
            item.setLayoutData(gd);
        }
    }
    private class Toggle extends CLabel implements MouseTrackListener, MouseListener {
        private boolean mMouseIn;
        private DragSource mSource;
        private ArrayList<Item> mItems = new ArrayList<Item>();
        public Toggle(Composite parent, String groupName) {
            super(parent, SWT.NONE);
            mMouseIn = false;
            setData(null);
            String s = String.format("-= %s =-", groupName);
            setText(s);
            setToolTipText(s);
            addMouseTrackListener(this);
            addMouseListener(this);
        }
        public void addItem(Item item) {
            mItems.add(item);
        }
        @Override
        public void dispose() {
            if (mSource != null) {
                mSource.dispose();
                mSource = null;
            }
            super.dispose();
        }
        @Override
        public int getStyle() {
            int style = super.getStyle();
            if (mMouseIn) {
                style |= SWT.SHADOW_IN;
            }
            return style;
        }
        public void mouseEnter(MouseEvent e) {
            if (!mMouseIn) {
                mMouseIn = true;
                redraw();
            }
        }
        public void mouseExit(MouseEvent e) {
            if (mMouseIn) {
                mMouseIn = false;
                redraw();
            }
        }
        public void mouseHover(MouseEvent e) {
        }
        public void mouseDoubleClick(MouseEvent arg0) {
        }
        public void mouseDown(MouseEvent arg0) {
        }
        public void mouseUp(MouseEvent arg0) {
            for (Item i : mItems) {
                if (i.isVisible()) {
                    Object ld = i.getLayoutData();
                    if (ld instanceof GridData) {
                        GridData gd = (GridData) ld;
                        i.setData(gd.heightHint != SWT.DEFAULT ?
                                    Integer.valueOf(gd.heightHint) :
                                        null);
                        gd.heightHint = 0;
                    }
                } else {
                    Object ld = i.getLayoutData();
                    if (ld instanceof GridData) {
                        GridData gd = (GridData) ld;
                        Object d = i.getData();
                        if (d instanceof Integer) {
                            gd.heightHint = ((Integer) d).intValue();
                        } else {
                            gd.heightHint = SWT.DEFAULT;
                        }
                    }
                }
                i.setVisible(!i.isVisible());
            }
            mRoot.layout(true );
            mControlListener.controlResized(null );
        }
    }
    private static class Item extends CLabel implements MouseTrackListener {
        private boolean mMouseIn;
        private DragSource mSource;
        public Item(Composite parent, ElementDescriptor desc) {
            super(parent, SWT.NONE);
            mMouseIn = false;
            setText(desc.getUiName());
            setImage(desc.getIcon());
            setToolTipText(desc.getTooltip());
            addMouseTrackListener(this);
            mSource = new DragSource(this, DND.DROP_COPY);
            mSource.setTransfer(new Transfer[] { ElementDescTransfer.getInstance() });
            mSource.addDragListener(new DescDragSourceListener(desc));
        }
        @Override
        public void dispose() {
            if (mSource != null) {
                mSource.dispose();
                mSource = null;
            }
            super.dispose();
        }
        @Override
        public int getStyle() {
            int style = super.getStyle();
            if (mMouseIn) {
                style |= SWT.SHADOW_IN;
            }
            return style;
        }
        public void mouseEnter(MouseEvent e) {
            if (!mMouseIn) {
                mMouseIn = true;
                redraw();
            }
        }
        public void mouseExit(MouseEvent e) {
            if (mMouseIn) {
                mMouseIn = false;
                redraw();
            }
        }
        public void mouseHover(MouseEvent e) {
        }
    }
    private static class DescDragSourceListener implements DragSourceListener {
        private final ElementDescriptor mDesc;
        public DescDragSourceListener(ElementDescriptor desc) {
            mDesc = desc;
        }
        public void dragStart(DragSourceEvent e) {
            if (mDesc == null) {
                e.doit = false;
            } else {
                GlobalCanvasDragInfo.getInstance().startDrag(ElementDescTransfer.getFqcn(mDesc));
            }
        }
        public void dragSetData(DragSourceEvent e) {
            if (ElementDescTransfer.getInstance().isSupportedType(e.dataType)) {
                e.data = mDesc;
            }
        }
        public void dragFinished(DragSourceEvent e) {
            GlobalCanvasDragInfo.getInstance().stopDrag();
        }
    }
}
