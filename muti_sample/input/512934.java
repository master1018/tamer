  class LayoutCanvas extends Canvas {
    private RulesEngine mRulesEngine;
    private ILayoutResult mLastValidResult;
    private CanvasViewInfo mLastValidViewInfoRoot;
    private boolean mIsResultValid;
    private Image mImage;
    private final LinkedList<CanvasSelection> mSelections = new LinkedList<CanvasSelection>();
    private Color mSelectionFgColor;
    private final GCWrapper mGCWrapper;
    private Font mFont;
    private CanvasViewInfo mHoverViewInfo;
    private Rectangle mHoverRect;
    private Color mHoverFgColor;
    private Color mOutlineColor;
    private CanvasAlternateSelection mAltSelection;
    private boolean mShowOutline;
    private DropTarget mDropTarget;
    private CanvasDropListener mDropListener;
    private final NodeFactory mNodeFactory = new NodeFactory();
    private ScaleInfo mVScale;
    private ScaleInfo mHScale;
    public LayoutCanvas(RulesEngine rulesEngine, Composite parent, int style) {
        super(parent, style | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL);
        mRulesEngine = rulesEngine;
        mHScale = new ScaleInfo(getHorizontalBar());
        mVScale = new ScaleInfo(getVerticalBar());
        mGCWrapper = new GCWrapper(mHScale, mVScale);
        Display d = getDisplay();
        mSelectionFgColor = d.getSystemColor(SWT.COLOR_RED);
        mHoverFgColor     = new Color(d, 0xFF, 0x99, 0x00); 
        mOutlineColor     = d.getSystemColor(SWT.COLOR_GREEN);
        mFont = d.getSystemFont();
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                onPaint(e);
            }
        });
        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                super.controlResized(e);
                mHScale.setClientSize(getClientArea().width);
                mVScale.setClientSize(getClientArea().height);
            }
        });
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                onMouseMove(e);
            }
        });
        addMouseListener(new MouseListener() {
            public void mouseUp(MouseEvent e) {
                onMouseUp(e);
            }
            public void mouseDown(MouseEvent e) {
                onMouseDown(e);
            }
            public void mouseDoubleClick(MouseEvent e) {
                onDoubleClick(e);
            }
        });
        mDropTarget = new DropTarget(this, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT);
        mDropTarget.setTransfer(new Transfer[] { ElementDescTransfer.getInstance() });
        mDropListener = new CanvasDropListener(this);
        mDropTarget.addDropListener(mDropListener);
    }
    @Override
    public void dispose() {
        super.dispose();
        if (mHoverFgColor != null) {
            mHoverFgColor.dispose();
            mHoverFgColor = null;
        }
        if (mDropTarget != null) {
            mDropTarget.dispose();
            mDropTarget = null;
        }
        if (mRulesEngine != null) {
            mRulesEngine.dispose();
            mRulesEngine = null;
        }
    }
     boolean isResultValid() {
        return mIsResultValid;
    }
     RulesEngine getRulesEngine() {
        return mRulesEngine;
    }
     void setRulesEngine(RulesEngine rulesEngine) {
        mRulesEngine = rulesEngine;
    }
    public NodeFactory getNodeFactory() {
        return mNodeFactory;
    }
    public void setResult(ILayoutResult result) {
        mHoverRect = null;
        mIsResultValid = (result != null && result.getSuccess() == ILayoutResult.SUCCESS);
        if (mIsResultValid && result != null) {
            mLastValidResult = result;
            mLastValidViewInfoRoot = new CanvasViewInfo(result.getRootView());
            setImage(result.getImage());
            updateNodeProxies(mLastValidViewInfoRoot);
            for (ListIterator<CanvasSelection> it = mSelections.listIterator(); it.hasNext(); ) {
                CanvasSelection s = it.next();
                Object key = s.getViewInfo().getUiViewKey();
                CanvasViewInfo vi = findViewInfoKey(key, mLastValidViewInfoRoot);
                it.remove();
                if (vi != null) {
                    it.add(new CanvasSelection(vi, mRulesEngine, mNodeFactory));
                }
            }
            mAltSelection = null;
            mHScale.setSize(mImage.getImageData().width, getClientArea().width);
            mVScale.setSize(mImage.getImageData().height, getClientArea().height);
        }
        redraw();
    }
    public void setShowOutline(boolean newState) {
        mShowOutline = newState;
        redraw();
    }
    public double getScale() {
        return mHScale.getScale();
    }
    public void setScale(double scale) {
        mHScale.setScale(scale);
        mVScale.setScale(scale);
        redraw();
    }
    public void onCopy(Clipboard clipboard) {
    }
    public void onCut(Clipboard clipboard) {
    }
    public void onPaste(Clipboard clipboard) {
    }
    public void onSelectAll() {
        mSelections.clear();
        mAltSelection = null;
        if (mIsResultValid && mLastValidResult != null) {
            selectAllViewInfos(mLastValidViewInfoRoot);
            redraw();
        }
    }
    public void onDelete() {
    }
    public Point displayToCanvasPoint(int displayX, int displayY) {
        org.eclipse.swt.graphics.Point p = this.toControl(displayX, displayY);
        int x = mHScale.inverseTranslate(p.x);
        int y = mVScale.inverseTranslate(p.y);
        return new Point(x, y);
    }
    public interface ScaleTransform {
        public int translate(int canvasX);
        public int scale(int canwasW);
        public int inverseTranslate(int screenX);
    }
    private class ScaleInfo implements ScaleTransform {
        private static final int IMAGE_MARGIN = 25;
        private int mImgSize;
        private int mClientSize;
        private int mTranslate;
        private double mScale;
        ScrollBar mScrollbar;
        public ScaleInfo(ScrollBar scrollbar) {
            mScrollbar = scrollbar;
            mScale = 1.0;
            mTranslate = 0;
            mScrollbar.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    mTranslate = mScrollbar.getSelection();
                    redraw();
                }
            });
        }
        public void setScale(double scale) {
            if (mScale != scale) {
                mScale = scale;
                resizeScrollbar();
            }
        }
        public double getScale() {
            return mScale;
        }
        public int getImgSize() {
            return mImgSize;
        }
        public int getScalledImgSize() {
            return (int) (mImgSize * mScale);
        }
        public void setSize(int imgSize, int clientSize) {
            mImgSize = imgSize;
            setClientSize(clientSize);
        }
        public void setClientSize(int clientSize) {
            mClientSize = clientSize;
            resizeScrollbar();
        }
        private void resizeScrollbar() {
            int sx = (int) (mImgSize * mScale);
            int cx = mClientSize - 2 * IMAGE_MARGIN;
            if (sx < cx) {
                mScrollbar.setEnabled(false);
            } else {
                mScrollbar.setEnabled(true);
                mScrollbar.setMaximum(sx);
                mScrollbar.setThumb(cx);
            }
        }
        public int translate(int canvasX) {
            return IMAGE_MARGIN - mTranslate + (int)(mScale * canvasX);
        }
        public int scale(int canwasW) {
            return (int)(mScale * canwasW);
        }
        public int inverseTranslate(int screenX) {
            return (int) ((screenX - IMAGE_MARGIN + mTranslate) / mScale);
        }
    }
    private void updateNodeProxies(CanvasViewInfo vi) {
        if (vi == null) {
            return;
        }
        UiViewElementNode key = vi.getUiViewKey();
        if (key != null) {
            mNodeFactory.create(vi);
        }
        for (CanvasViewInfo child : vi.getChildren()) {
            updateNodeProxies(child);
        }
    }
    private void setImage(BufferedImage awtImage) {
        int width = awtImage.getWidth();
        int height = awtImage.getHeight();
        Raster raster = awtImage.getData(new java.awt.Rectangle(width, height));
        int[] imageDataBuffer = ((DataBufferInt)raster.getDataBuffer()).getData();
        ImageData imageData = new ImageData(width, height, 32,
                new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF));
        imageData.setPixels(0, 0, imageDataBuffer.length, imageDataBuffer, 0);
        mImage = new Image(getDisplay(), imageData);
    }
    private boolean gc_setAlpha(GC gc, int alpha) {
        try {
            gc.setAlpha(alpha);
            return true;
        } catch (SWTException e) {
            return false;
        }
    }
    private int gc_setAntialias(GC gc, int alias) {
        try {
            int old = gc.getAntialias();
            gc.setAntialias(alias);
            return old;
        } catch (SWTException e) {
            return -2;
        }
    }
    private void onPaint(PaintEvent e) {
        GC gc = e.gc;
        gc.setFont(mFont);
        mGCWrapper.setGC(gc);
        try {
            if (mImage != null) {
                if (!mIsResultValid) {
                    gc_setAlpha(gc, 128);  
                }
                ScaleInfo hi = mHScale;
                ScaleInfo vi = mVScale;
                int oldAlias = -2;
                if (hi.getScale() < 1.0) {
                    oldAlias = gc_setAntialias(gc, SWT.ON);
                }
                gc.drawImage(mImage,
                        0,                          
                        0,                          
                        hi.getImgSize(),            
                        vi.getImgSize(),            
                        hi.translate(0),            
                        vi.translate(0),            
                        hi.getScalledImgSize(),     
                        vi.getScalledImgSize()      
                        );
                if (oldAlias != -2) {
                    gc_setAntialias(gc, oldAlias);
                }
                if (!mIsResultValid) {
                    gc_setAlpha(gc, 255);  
                }
            }
            if (mShowOutline) {
                gc.setForeground(mOutlineColor);
                gc.setLineStyle(SWT.LINE_DOT);
                drawOutline(gc, mLastValidViewInfoRoot);
            }
            if (mHoverRect != null) {
                gc.setForeground(mHoverFgColor);
                gc.setLineStyle(SWT.LINE_DOT);
                int x = mHScale.translate(mHoverRect.x);
                int y = mVScale.translate(mHoverRect.y);
                int w = mHScale.scale(mHoverRect.width);
                int h = mVScale.scale(mHoverRect.height);
                gc.drawRectangle(x, y, w, h);
            }
            int n = mSelections.size();
            if (n > 0) {
                boolean isMultipleSelection = n > 1;
                if (n == 1) {
                    gc.setForeground(mSelectionFgColor);
                    mSelections.get(0).paintParentSelection(mRulesEngine, mGCWrapper);
                }
                for (CanvasSelection s : mSelections) {
                    gc.setForeground(mSelectionFgColor);
                    s.paintSelection(mRulesEngine, mGCWrapper, isMultipleSelection);
                }
            }
            if (mDropListener != null) {
                mDropListener.paintFeedback(mGCWrapper);
            }
        } finally {
            mGCWrapper.setGC(null);
        }
    }
    private void drawOutline(GC gc, CanvasViewInfo info) {
        Rectangle r = info.getAbsRect();
        int x = mHScale.translate(r.x);
        int y = mVScale.translate(r.y);
        int w = mHScale.scale(r.width);
        int h = mVScale.scale(r.height);
        gc.drawRectangle(x, y, w, h);
        for (CanvasViewInfo vi : info.getChildren()) {
            drawOutline(gc, vi);
        }
    }
    private void onMouseMove(MouseEvent e) {
        if (mLastValidResult != null) {
            CanvasViewInfo root = mLastValidViewInfoRoot;
            int x = mHScale.inverseTranslate(e.x);
            int y = mVScale.inverseTranslate(e.y);
            CanvasViewInfo vi = findViewInfoAt(x, y);
            if (vi == root) {
                vi = null;
            }
            boolean needsUpdate = vi != mHoverViewInfo;
            mHoverViewInfo = vi;
            if (vi == null) {
                mHoverRect = null;
            } else {
                Rectangle r = vi.getSelectionRect();
                mHoverRect = new Rectangle(r.x, r.y, r.width, r.height);
            }
            if (needsUpdate) {
                redraw();
            }
        }
    }
    private void onMouseDown(MouseEvent e) {
    }
    private void onMouseUp(MouseEvent e) {
        if (mLastValidResult != null) {
            boolean isShift = (e.stateMask & SWT.SHIFT) != 0;
            boolean isAlt   = (e.stateMask & SWT.ALT)   != 0;
            int x = mHScale.inverseTranslate(e.x);
            int y = mVScale.inverseTranslate(e.y);
            CanvasViewInfo vi = findViewInfoAt(x, y);
            if (isShift && !isAlt) {
                mAltSelection = null;
                if (vi != null) {
                    if (deselect(vi)) {
                        redraw();
                        return;
                    }
                    mSelections.add(new CanvasSelection(vi, mRulesEngine, mNodeFactory));
                    redraw();
                }
            } else if (isAlt) {
                if (mAltSelection == null || mAltSelection.getOriginatingView() != vi) {
                    mAltSelection = new CanvasAlternateSelection(vi, findAltViewInfoAt(
                                                    x, y, mLastValidViewInfoRoot, null));
                    deselectAll(mAltSelection.getAltViews());
                    CanvasViewInfo vi2 = mAltSelection.getCurrent();
                    if (vi2 != null) {
                        mSelections.addFirst(new CanvasSelection(vi2, mRulesEngine, mNodeFactory));
                    }
                } else {
                    CanvasViewInfo vi2 = mAltSelection.getCurrent();
                    deselect(vi2);
                    vi2 = mAltSelection.getNext();
                    if (vi2 != null) {
                        mSelections.addFirst(new CanvasSelection(vi2, mRulesEngine, mNodeFactory));
                    }
                }
                redraw();
            } else {
                mAltSelection = null;
                if (mSelections.size() > 0) {
                    if (mSelections.size() == 1 && mSelections.getFirst().getViewInfo() == vi) {
                        return;
                    }
                    mSelections.clear();
                }
                if (vi != null) {
                    mSelections.add(new CanvasSelection(vi, mRulesEngine, mNodeFactory));
                }
                redraw();
            }
        }
    }
    private boolean deselect(CanvasViewInfo canvasViewInfo) {
        if (canvasViewInfo == null) {
            return false;
        }
        for (ListIterator<CanvasSelection> it = mSelections.listIterator(); it.hasNext(); ) {
            CanvasSelection s = it.next();
            if (canvasViewInfo == s.getViewInfo()) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    private void deselectAll(List<CanvasViewInfo> canvasViewInfos) {
        for (ListIterator<CanvasSelection> it = mSelections.listIterator(); it.hasNext(); ) {
            CanvasSelection s = it.next();
            if (canvasViewInfos.contains(s.getViewInfo())) {
                it.remove();
            }
        }
    }
    private void onDoubleClick(MouseEvent e) {
    }
    private CanvasViewInfo findViewInfoKey(Object viewKey, CanvasViewInfo canvasViewInfo) {
        if (canvasViewInfo.getUiViewKey() == viewKey) {
            return canvasViewInfo;
        }
        for (CanvasViewInfo child : canvasViewInfo.getChildren()) {
            CanvasViewInfo v = findViewInfoKey(viewKey, child);
            if (v != null) {
                return v;
            }
        }
        return null;
    }
     CanvasViewInfo findViewInfoAt(int x, int y) {
        if (mLastValidViewInfoRoot == null) {
            return null;
        } else {
            return findViewInfoAt(x, y, mLastValidViewInfoRoot);
        }
    }
    private CanvasViewInfo findViewInfoAt(int x, int y, CanvasViewInfo canvasViewInfo) {
        Rectangle r = canvasViewInfo.getSelectionRect();
        if (r.contains(x, y)) {
            for (CanvasViewInfo child : canvasViewInfo.getChildren()) {
                CanvasViewInfo v = findViewInfoAt(x, y, child);
                if (v != null) {
                    return v;
                }
            }
            return canvasViewInfo;
        }
        return null;
    }
    private ArrayList<CanvasViewInfo> findAltViewInfoAt(
            int x, int y, CanvasViewInfo parent, ArrayList<CanvasViewInfo> outList) {
        Rectangle r;
        if (outList == null) {
            outList = new ArrayList<CanvasViewInfo>();
            r = parent.getSelectionRect();
            if (r.contains(x, y)) {
                outList.add(parent);
            }
        }
        if (parent.getChildren().size() > 0) {
            for (CanvasViewInfo child : parent.getChildren()) {
                r = child.getSelectionRect();
                if (r.contains(x, y)) {
                    outList.add(child);
                }
            }
            for (CanvasViewInfo child : parent.getChildren()) {
                r = child.getSelectionRect();
                if (r.contains(x, y)) {
                    findAltViewInfoAt(x, y, child, outList);
                }
            }
        }
        return outList;
    }
    private void selectAllViewInfos(CanvasViewInfo canvasViewInfo) {
        mSelections.add(new CanvasSelection(canvasViewInfo, mRulesEngine, mNodeFactory));
        for (CanvasViewInfo vi : canvasViewInfo.getChildren()) {
            selectAllViewInfos(vi);
        }
    }
}
