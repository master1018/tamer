public class JViewport extends JComponent implements Accessible
{
    private static final String uiClassID = "ViewportUI";
    static final Object EnableWindowBlit = "EnableWindowBlit";
    protected boolean isViewSizeSet = false;
    protected Point lastPaintPosition = null;
    @Deprecated
    protected boolean backingStore = false;
    transient protected Image backingStoreImage = null;
    protected boolean scrollUnderway = false;
    private ComponentListener viewListener = null;
    private transient ChangeEvent changeEvent = null;
    public static final int BLIT_SCROLL_MODE = 1;
    public static final int BACKINGSTORE_SCROLL_MODE = 2;
    public static final int SIMPLE_SCROLL_MODE = 0;
    private int scrollMode = BLIT_SCROLL_MODE;
    private transient boolean repaintAll;
    private transient boolean waitingForRepaint;
    private transient Timer repaintTimer;
    private transient boolean inBlitPaint;
    private boolean hasHadValidView;
    public JViewport() {
        super();
        setLayout(createLayoutManager());
        setOpaque(true);
        updateUI();
        setInheritsPopupMenu(true);
    }
    public ViewportUI getUI() {
        return (ViewportUI)ui;
    }
    public void setUI(ViewportUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((ViewportUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    protected void addImpl(Component child, Object constraints, int index) {
      setView(child);
    }
    public void remove(Component child) {
        child.removeComponentListener(viewListener);
        super.remove(child);
    }
    public void scrollRectToVisible(Rectangle contentRect) {
        Component view = getView();
        if (view == null) {
            return;
        } else {
            if (!view.isValid()) {
                validateView();
            }
            int dx, dy;
            dx = positionAdjustment(getWidth(), contentRect.width, contentRect.x);
            dy = positionAdjustment(getHeight(), contentRect.height, contentRect.y);
            if (dx != 0 || dy != 0) {
                Point viewPosition = getViewPosition();
                Dimension viewSize = view.getSize();
                int startX = viewPosition.x;
                int startY = viewPosition.y;
                Dimension extent = getExtentSize();
                viewPosition.x -= dx;
                viewPosition.y -= dy;
                if (view.isValid()) {
                    if (getParent().getComponentOrientation().isLeftToRight()) {
                        if (viewPosition.x + extent.width > viewSize.width) {
                            viewPosition.x = Math.max(0, viewSize.width - extent.width);
                        } else if (viewPosition.x < 0) {
                            viewPosition.x = 0;
                        }
                    } else {
                        if (extent.width > viewSize.width) {
                            viewPosition.x = viewSize.width - extent.width;
                        } else {
                            viewPosition.x = Math.max(0, Math.min(viewSize.width - extent.width, viewPosition.x));
                        }
                    }
                    if (viewPosition.y + extent.height > viewSize.height) {
                        viewPosition.y = Math.max(0, viewSize.height -
                                                  extent.height);
                    }
                    else if (viewPosition.y < 0) {
                        viewPosition.y = 0;
                    }
                }
                if (viewPosition.x != startX || viewPosition.y != startY) {
                    setViewPosition(viewPosition);
                    scrollUnderway = false;
                }
            }
        }
    }
    private void validateView() {
        Component validateRoot = SwingUtilities.getValidateRoot(this, false);
        if (validateRoot == null) {
            return;
        }
        validateRoot.validate();
        RepaintManager rm = RepaintManager.currentManager(this);
        if (rm != null) {
            rm.removeInvalidComponent((JComponent)validateRoot);
        }
    }
    private int positionAdjustment(int parentWidth, int childWidth, int childAt)    {
        if (childAt >= 0 && childWidth + childAt <= parentWidth)    {
            return 0;
        }
        if (childAt <= 0 && childWidth + childAt >= parentWidth) {
            return 0;
        }
        if (childAt > 0 && childWidth <= parentWidth)    {
            return -childAt + parentWidth - childWidth;
        }
        if (childAt >= 0 && childWidth >= parentWidth)   {
            return -childAt;
        }
        if (childAt <= 0 && childWidth <= parentWidth)   {
            return -childAt;
        }
        if (childAt < 0 && childWidth >= parentWidth)    {
            return -childAt + parentWidth - childWidth;
        }
        return 0;
    }
    public final void setBorder(Border border) {
        if (border != null) {
            throw new IllegalArgumentException("JViewport.setBorder() not supported");
        }
    }
    public final Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    public final Insets getInsets(Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 0;
        return insets;
    }
    private Graphics getBackingStoreGraphics(Graphics g) {
        Graphics bsg = backingStoreImage.getGraphics();
        bsg.setColor(g.getColor());
        bsg.setFont(g.getFont());
        bsg.setClip(g.getClipBounds());
        return bsg;
    }
    private void paintViaBackingStore(Graphics g) {
        Graphics bsg = getBackingStoreGraphics(g);
        try {
            super.paint(bsg);
            g.drawImage(backingStoreImage, 0, 0, this);
        } finally {
            bsg.dispose();
        }
    }
    private void paintViaBackingStore(Graphics g, Rectangle oClip) {
        Graphics bsg = getBackingStoreGraphics(g);
        try {
            super.paint(bsg);
            g.setClip(oClip);
            g.drawImage(backingStoreImage, 0, 0, this);
        } finally {
            bsg.dispose();
        }
    }
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }
    protected boolean isPaintingOrigin() {
        return scrollMode == BACKINGSTORE_SCROLL_MODE;
    }
    private Point getViewLocation() {
        Component view = getView();
        if (view != null) {
            return view.getLocation();
        }
        else {
            return new Point(0,0);
        }
    }
    public void paint(Graphics g)
    {
        int width = getWidth();
        int height = getHeight();
        if ((width <= 0) || (height <= 0)) {
            return;
        }
        if (inBlitPaint) {
            super.paint(g);
            return;
        }
        if (repaintAll) {
            repaintAll = false;
            Rectangle clipB = g.getClipBounds();
            if (clipB.width < getWidth() ||
                clipB.height < getHeight()) {
                waitingForRepaint = true;
                if (repaintTimer == null) {
                    repaintTimer = createRepaintTimer();
                }
                repaintTimer.stop();
                repaintTimer.start();
            }
            else {
                if (repaintTimer != null) {
                    repaintTimer.stop();
                }
                waitingForRepaint = false;
            }
        }
        else if (waitingForRepaint) {
            Rectangle clipB = g.getClipBounds();
            if (clipB.width >= getWidth() &&
                clipB.height >= getHeight()) {
                waitingForRepaint = false;
                repaintTimer.stop();
            }
        }
        if (!backingStore || isBlitting() || getView() == null) {
            super.paint(g);
            lastPaintPosition = getViewLocation();
            return;
        }
        Rectangle viewBounds = getView().getBounds();
        if (!isOpaque()) {
            g.clipRect(0, 0, viewBounds.width, viewBounds.height);
        }
        if (backingStoreImage == null) {
            backingStoreImage = createImage(width, height);
            Rectangle clip = g.getClipBounds();
            if (clip.width != width || clip.height != height) {
                if (!isOpaque()) {
                    g.setClip(0, 0, Math.min(viewBounds.width, width),
                              Math.min(viewBounds.height, height));
                }
                else {
                    g.setClip(0, 0, width, height);
                }
                paintViaBackingStore(g, clip);
            }
            else {
                paintViaBackingStore(g);
            }
        }
        else {
            if (!scrollUnderway || lastPaintPosition.equals(getViewLocation())) {
                paintViaBackingStore(g);
            } else {
                Point blitFrom = new Point();
                Point blitTo = new Point();
                Dimension blitSize = new Dimension();
                Rectangle blitPaint = new Rectangle();
                Point newLocation = getViewLocation();
                int dx = newLocation.x - lastPaintPosition.x;
                int dy = newLocation.y - lastPaintPosition.y;
                boolean canBlit = computeBlit(dx, dy, blitFrom, blitTo, blitSize, blitPaint);
                if (!canBlit) {
                    paintViaBackingStore(g);
                } else {
                    int bdx = blitTo.x - blitFrom.x;
                    int bdy = blitTo.y - blitFrom.y;
                    Rectangle clip = g.getClipBounds();
                    g.setClip(0, 0, width, height);
                    Graphics bsg = getBackingStoreGraphics(g);
                    try {
                        bsg.copyArea(blitFrom.x, blitFrom.y, blitSize.width, blitSize.height, bdx, bdy);
                        g.setClip(clip.x, clip.y, clip.width, clip.height);
                        Rectangle r = viewBounds.intersection(blitPaint);
                        bsg.setClip(r);
                        super.paint(bsg);
                        g.drawImage(backingStoreImage, 0, 0, this);
                    } finally {
                        bsg.dispose();
                    }
                }
            }
        }
        lastPaintPosition = getViewLocation();
        scrollUnderway = false;
    }
    public void reshape(int x, int y, int w, int h) {
        boolean sizeChanged = (getWidth() != w) || (getHeight() != h);
        if (sizeChanged) {
            backingStoreImage = null;
        }
        super.reshape(x, y, w, h);
        if (sizeChanged) {
            fireStateChanged();
        }
    }
    public void setScrollMode(int mode) {
        scrollMode = mode;
        backingStore = mode == BACKINGSTORE_SCROLL_MODE;
    }
    public int getScrollMode() {
        return scrollMode;
    }
    @Deprecated
    public boolean isBackingStoreEnabled() {
        return scrollMode == BACKINGSTORE_SCROLL_MODE;
    }
    @Deprecated
    public void setBackingStoreEnabled(boolean enabled) {
        if (enabled) {
            setScrollMode(BACKINGSTORE_SCROLL_MODE);
        } else {
            setScrollMode(BLIT_SCROLL_MODE);
        }
    }
    private boolean isBlitting() {
        Component view = getView();
        return (scrollMode == BLIT_SCROLL_MODE) &&
               (view instanceof JComponent) && view.isOpaque();
    }
    public Component getView() {
        return (getComponentCount() > 0) ? getComponent(0) : null;
    }
    public void setView(Component view) {
        int n = getComponentCount();
        for(int i = n - 1; i >= 0; i--) {
            remove(getComponent(i));
        }
        isViewSizeSet = false;
        if (view != null) {
            super.addImpl(view, null, -1);
            viewListener = createViewListener();
            view.addComponentListener(viewListener);
        }
        if (hasHadValidView) {
            fireStateChanged();
        }
        else if (view != null) {
            hasHadValidView = true;
        }
        revalidate();
        repaint();
    }
    public Dimension getViewSize() {
        Component view = getView();
        if (view == null) {
            return new Dimension(0,0);
        }
        else if (isViewSizeSet) {
            return view.getSize();
        }
        else {
            return view.getPreferredSize();
        }
    }
    public void setViewSize(Dimension newSize) {
        Component view = getView();
        if (view != null) {
            Dimension oldSize = view.getSize();
            if (!newSize.equals(oldSize)) {
                scrollUnderway = false;
                view.setSize(newSize);
                isViewSizeSet = true;
                fireStateChanged();
            }
        }
    }
    public Point getViewPosition() {
        Component view = getView();
        if (view != null) {
            Point p = view.getLocation();
            p.x = -p.x;
            p.y = -p.y;
            return p;
        }
        else {
            return new Point(0,0);
        }
    }
    public void setViewPosition(Point p)
    {
        Component view = getView();
        if (view == null) {
            return;
        }
        int oldX, oldY, x = p.x, y = p.y;
        if (view instanceof JComponent) {
            JComponent c = (JComponent)view;
            oldX = c.getX();
            oldY = c.getY();
        }
        else {
            Rectangle r = view.getBounds();
            oldX = r.x;
            oldY = r.y;
        }
        int newX = -x;
        int newY = -y;
        if ((oldX != newX) || (oldY != newY)) {
            if (!waitingForRepaint && isBlitting() && canUseWindowBlitter()) {
                RepaintManager rm = RepaintManager.currentManager(this);
                JComponent jview = (JComponent)view;
                Rectangle dirty = rm.getDirtyRegion(jview);
                if (dirty == null || !dirty.contains(jview.getVisibleRect())) {
                    rm.beginPaint();
                    try {
                        Graphics g = JComponent.safelyGetGraphics(this);
                        flushViewDirtyRegion(g, dirty);
                        view.setLocation(newX, newY);
                        g.setClip(0,0,getWidth(), Math.min(getHeight(),
                                                           jview.getHeight()));
                        repaintAll = (windowBlitPaint(g) &&
                                      needsRepaintAfterBlit());
                        g.dispose();
                        rm.markCompletelyClean((JComponent)getParent());
                        rm.markCompletelyClean(this);
                        rm.markCompletelyClean(jview);
                    } finally {
                        rm.endPaint();
                    }
                }
                else {
                    view.setLocation(newX, newY);
                    repaintAll = false;
                }
            }
            else {
                scrollUnderway = true;
                view.setLocation(newX, newY);
                repaintAll = false;
            }
            revalidate();
            fireStateChanged();
        }
    }
    public Rectangle getViewRect() {
        return new Rectangle(getViewPosition(), getExtentSize());
    }
    protected boolean computeBlit(
        int dx,
        int dy,
        Point blitFrom,
        Point blitTo,
        Dimension blitSize,
        Rectangle blitPaint)
    {
        int dxAbs = Math.abs(dx);
        int dyAbs = Math.abs(dy);
        Dimension extentSize = getExtentSize();
        if ((dx == 0) && (dy != 0) && (dyAbs < extentSize.height)) {
            if (dy < 0) {
                blitFrom.y = -dy;
                blitTo.y = 0;
                blitPaint.y = extentSize.height + dy;
            }
            else {
                blitFrom.y = 0;
                blitTo.y = dy;
                blitPaint.y = 0;
            }
            blitPaint.x = blitFrom.x = blitTo.x = 0;
            blitSize.width = extentSize.width;
            blitSize.height = extentSize.height - dyAbs;
            blitPaint.width = extentSize.width;
            blitPaint.height = dyAbs;
            return true;
        }
        else if ((dy == 0) && (dx != 0) && (dxAbs < extentSize.width)) {
            if (dx < 0) {
                blitFrom.x = -dx;
                blitTo.x = 0;
                blitPaint.x = extentSize.width + dx;
            }
            else {
                blitFrom.x = 0;
                blitTo.x = dx;
                blitPaint.x = 0;
            }
            blitPaint.y = blitFrom.y = blitTo.y = 0;
            blitSize.width = extentSize.width - dxAbs;
            blitSize.height = extentSize.height;
            blitPaint.width = dxAbs;
            blitPaint.height = extentSize.height;
            return true;
        }
        else {
            return false;
        }
    }
    @Transient
    public Dimension getExtentSize() {
        return getSize();
    }
    public Dimension toViewCoordinates(Dimension size) {
        return new Dimension(size);
    }
    public Point toViewCoordinates(Point p) {
        return new Point(p);
    }
    public void setExtentSize(Dimension newExtent) {
        Dimension oldExtent = getExtentSize();
        if (!newExtent.equals(oldExtent)) {
            setSize(newExtent);
            fireStateChanged();
        }
    }
    protected class ViewListener extends ComponentAdapter implements Serializable
    {
        public void componentResized(ComponentEvent e) {
            fireStateChanged();
            revalidate();
        }
    }
    protected ViewListener createViewListener() {
        return new ViewListener();
    }
    protected LayoutManager createLayoutManager() {
        return ViewportLayout.SHARED_INSTANCE;
    }
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    public ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }
    protected void fireStateChanged()
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
    public void repaint(long tm, int x, int y, int w, int h) {
        Container parent = getParent();
        if(parent != null)
            parent.repaint(tm,x+getX(),y+getY(),w,h);
        else
            super.repaint(tm,x,y,w,h);
    }
    protected String paramString() {
        String isViewSizeSetString = (isViewSizeSet ?
                                      "true" : "false");
        String lastPaintPositionString = (lastPaintPosition != null ?
                                          lastPaintPosition.toString() : "");
        String scrollUnderwayString = (scrollUnderway ?
                                       "true" : "false");
        return super.paramString() +
        ",isViewSizeSet=" + isViewSizeSetString +
        ",lastPaintPosition=" + lastPaintPositionString +
        ",scrollUnderway=" + scrollUnderwayString;
    }
    protected void firePropertyChange(String propertyName, Object oldValue,
                                      Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
        if (propertyName.equals(EnableWindowBlit)) {
            if (newValue != null) {
                setScrollMode(BLIT_SCROLL_MODE);
            } else {
                setScrollMode(SIMPLE_SCROLL_MODE);
            }
        }
    }
    private boolean needsRepaintAfterBlit() {
        Component heavyParent = getParent();
        while (heavyParent != null && heavyParent.isLightweight()) {
            heavyParent = heavyParent.getParent();
        }
        if (heavyParent != null) {
            ComponentPeer peer = heavyParent.getPeer();
            if (peer != null && peer.canDetermineObscurity() &&
                                !peer.isObscured()) {
                return false;
            }
        }
        return true;
    }
    private Timer createRepaintTimer() {
        Timer timer = new Timer(300, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (waitingForRepaint) {
                    repaint();
                }
            }
        });
        timer.setRepeats(false);
        return timer;
    }
    private void flushViewDirtyRegion(Graphics g, Rectangle dirty) {
        JComponent view = (JComponent) getView();
        if(dirty != null && dirty.width > 0 && dirty.height > 0) {
            dirty.x += view.getX();
            dirty.y += view.getY();
            Rectangle clip = g.getClipBounds();
            if (clip == null) {
                g.setClip(0, 0, getWidth(), getHeight());
            }
            g.clipRect(dirty.x, dirty.y, dirty.width, dirty.height);
            clip = g.getClipBounds();
            if (clip.width > 0 && clip.height > 0) {
                paintView(g);
            }
        }
    }
    private boolean windowBlitPaint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        if ((width == 0) || (height == 0)) {
            return false;
        }
        boolean retValue;
        RepaintManager rm = RepaintManager.currentManager(this);
        JComponent view = (JComponent) getView();
        if (lastPaintPosition == null ||
            lastPaintPosition.equals(getViewLocation())) {
            paintView(g);
            retValue = false;
        } else {
            Point blitFrom = new Point();
            Point blitTo = new Point();
            Dimension blitSize = new Dimension();
            Rectangle blitPaint = new Rectangle();
            Point newLocation = getViewLocation();
            int dx = newLocation.x - lastPaintPosition.x;
            int dy = newLocation.y - lastPaintPosition.y;
            boolean canBlit = computeBlit(dx, dy, blitFrom, blitTo, blitSize,
                                          blitPaint);
            if (!canBlit) {
                paintView(g);
                retValue = false;
            } else {
                Rectangle r = view.getBounds().intersection(blitPaint);
                r.x -= view.getX();
                r.y -= view.getY();
                blitDoubleBuffered(view, g, r.x, r.y, r.width, r.height,
                                   blitFrom.x, blitFrom.y, blitTo.x, blitTo.y,
                                   blitSize.width, blitSize.height);
                retValue = true;
            }
        }
        lastPaintPosition = getViewLocation();
        return retValue;
    }
    private void blitDoubleBuffered(JComponent view, Graphics g,
                                    int clipX, int clipY, int clipW, int clipH,
                                    int blitFromX, int blitFromY, int blitToX, int blitToY,
                                    int blitW, int blitH) {
        RepaintManager rm = RepaintManager.currentManager(this);
        int bdx = blitToX - blitFromX;
        int bdy = blitToY - blitFromY;
        rm.copyArea(this, g, blitFromX, blitFromY, blitW, blitH, bdx, bdy,
                    false);
        int x = view.getX();
        int y = view.getY();
        g.translate(x, y);
        g.setClip(clipX, clipY, clipW, clipH);
        view.paintForceDoubleBuffered(g);
        g.translate(-x, -y);
    }
    private void paintView(Graphics g) {
        Rectangle clip = g.getClipBounds();
        JComponent view = (JComponent)getView();
        if (view.getWidth() >= getWidth()) {
            int x = view.getX();
            int y = view.getY();
            g.translate(x, y);
            g.setClip(clip.x - x, clip.y - y, clip.width, clip.height);
            view.paintForceDoubleBuffered(g);
            g.translate(-x, -y);
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }
        else {
            try {
                inBlitPaint = true;
                paintForceDoubleBuffered(g);
            } finally {
                inBlitPaint = false;
            }
        }
    }
    private boolean canUseWindowBlitter() {
        if (!isShowing() || (!(getParent() instanceof JComponent) &&
                             !(getView() instanceof JComponent))) {
            return false;
        }
        if (isPainting()) {
            return false;
        }
        Rectangle dirtyRegion = RepaintManager.currentManager(this).
                                getDirtyRegion((JComponent)getParent());
        if (dirtyRegion != null && dirtyRegion.width > 0 &&
            dirtyRegion.height > 0) {
            return false;
        }
        Rectangle clip = new Rectangle(0,0,getWidth(),getHeight());
        Rectangle oldClip = new Rectangle();
        Rectangle tmp2 = null;
        Container parent;
        Component lastParent = null;
        int x, y, w, h;
        for(parent = this; parent != null && isLightweightComponent(parent); parent = parent.getParent()) {
            x = parent.getX();
            y = parent.getY();
            w = parent.getWidth();
            h = parent.getHeight();
            oldClip.setBounds(clip);
            SwingUtilities.computeIntersection(0, 0, w, h, clip);
            if(!clip.equals(oldClip))
                return false;
            if(lastParent != null && parent instanceof JComponent &&
               !((JComponent)parent).isOptimizedDrawingEnabled()) {
                Component comps[] = parent.getComponents();
                int index = 0;
                for(int i = comps.length - 1 ;i >= 0; i--) {
                    if(comps[i] == lastParent) {
                        index = i - 1;
                        break;
                    }
                }
                while(index >= 0) {
                    tmp2 = comps[index].getBounds(tmp2);
                    if(tmp2.intersects(clip))
                        return false;
                    index--;
                }
            }
            clip.x += x;
            clip.y += y;
            lastParent = parent;
        }
        if (parent == null) {
            return false;
        }
        return true;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJViewport();
        }
        return accessibleContext;
    }
    protected class AccessibleJViewport extends AccessibleJComponent {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.VIEWPORT;
        }
    } 
}
