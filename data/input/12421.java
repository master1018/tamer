public class AsyncBoxView extends View {
    public AsyncBoxView(Element elem, int axis) {
        super(elem);
        stats = new ArrayList<ChildState>();
        this.axis = axis;
        locator = new ChildLocator();
        flushTask = new FlushTask();
        minorSpan = Short.MAX_VALUE;
        estimatedMajorSpan = false;
    }
    public int getMajorAxis() {
        return axis;
    }
    public int getMinorAxis() {
        return (axis == X_AXIS) ? Y_AXIS : X_AXIS;
    }
    public float getTopInset() {
        return topInset;
    }
    public void setTopInset(float i) {
        topInset = i;
    }
    public float getBottomInset() {
        return bottomInset;
    }
    public void setBottomInset(float i) {
        bottomInset = i;
    }
    public float getLeftInset() {
        return leftInset;
    }
    public void setLeftInset(float i) {
        leftInset = i;
    }
    public float getRightInset() {
        return rightInset;
    }
    public void setRightInset(float i) {
        rightInset = i;
    }
    protected float getInsetSpan(int axis) {
        float margin = (axis == X_AXIS) ?
            getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
        return margin;
    }
    protected void setEstimatedMajorSpan(boolean isEstimated) {
        estimatedMajorSpan = isEstimated;
    }
    protected boolean getEstimatedMajorSpan() {
        return estimatedMajorSpan;
    }
    protected ChildState getChildState(int index) {
        synchronized(stats) {
            if ((index >= 0) && (index < stats.size())) {
                return stats.get(index);
            }
            return null;
        }
    }
    protected LayoutQueue getLayoutQueue() {
        return LayoutQueue.getDefaultQueue();
    }
    protected ChildState createChildState(View v) {
        return new ChildState(v);
    }
    protected synchronized void majorRequirementChange(ChildState cs, float delta) {
        if (estimatedMajorSpan == false) {
            majorSpan += delta;
        }
        majorChanged = true;
    }
    protected synchronized void minorRequirementChange(ChildState cs) {
        minorChanged = true;
    }
    protected void flushRequirementChanges() {
        AbstractDocument doc = (AbstractDocument) getDocument();
        try {
            doc.readLock();
            View parent = null;
            boolean horizontal = false;
            boolean vertical = false;
            synchronized(this) {
                synchronized(stats) {
                    int n = getViewCount();
                    if ((n > 0) && (minorChanged || estimatedMajorSpan)) {
                        LayoutQueue q = getLayoutQueue();
                        ChildState min = getChildState(0);
                        ChildState pref = getChildState(0);
                        float span = 0f;
                        for (int i = 1; i < n; i++) {
                            ChildState cs = getChildState(i);
                            if (minorChanged) {
                                if (cs.min > min.min) {
                                    min = cs;
                                }
                                if (cs.pref > pref.pref) {
                                    pref = cs;
                                }
                            }
                            if (estimatedMajorSpan) {
                                span += cs.getMajorSpan();
                            }
                        }
                        if (minorChanged) {
                            minRequest = min;
                            prefRequest = pref;
                        }
                        if (estimatedMajorSpan) {
                            majorSpan = span;
                            estimatedMajorSpan = false;
                            majorChanged = true;
                        }
                    }
                }
                if (majorChanged || minorChanged) {
                    parent = getParent();
                    if (parent != null) {
                        if (axis == X_AXIS) {
                            horizontal = majorChanged;
                            vertical = minorChanged;
                        } else {
                            vertical = majorChanged;
                            horizontal = minorChanged;
                        }
                    }
                    majorChanged = false;
                    minorChanged = false;
                }
            }
            if (parent != null) {
                parent.preferenceChanged(this, horizontal, vertical);
                Component c = getContainer();
                if (c != null) {
                    c.repaint();
                }
            }
        } finally {
            doc.readUnlock();
        }
    }
    public void replace(int offset, int length, View[] views) {
        synchronized(stats) {
            for (int i = 0; i < length; i++) {
                ChildState cs = stats.remove(offset);
                float csSpan = cs.getMajorSpan();
                cs.getChildView().setParent(null);
                if (csSpan != 0) {
                    majorRequirementChange(cs, -csSpan);
                }
            }
            LayoutQueue q = getLayoutQueue();
            if (views != null) {
                for (int i = 0; i < views.length; i++) {
                    ChildState s = createChildState(views[i]);
                    stats.add(offset + i, s);
                    q.addTask(s);
                }
            }
            q.addTask(flushTask);
        }
    }
    protected void loadChildren(ViewFactory f) {
        Element e = getElement();
        int n = e.getElementCount();
        if (n > 0) {
            View[] added = new View[n];
            for (int i = 0; i < n; i++) {
                added[i] = f.create(e.getElement(i));
            }
            replace(0, 0, added);
        }
    }
    protected synchronized int getViewIndexAtPosition(int pos, Position.Bias b) {
        boolean isBackward = (b == Position.Bias.Backward);
        pos = (isBackward) ? Math.max(0, pos - 1) : pos;
        Element elem = getElement();
        return elem.getElementIndex(pos);
    }
    protected void updateLayout(DocumentEvent.ElementChange ec,
                                    DocumentEvent e, Shape a) {
        if (ec != null) {
            int index = Math.max(ec.getIndex() - 1, 0);
            ChildState cs = getChildState(index);
            locator.childChanged(cs);
        }
    }
    public void setParent(View parent) {
        super.setParent(parent);
        if ((parent != null) && (getViewCount() == 0)) {
            ViewFactory f = getViewFactory();
            loadChildren(f);
        }
    }
    public synchronized void preferenceChanged(View child, boolean width, boolean height) {
        if (child == null) {
            getParent().preferenceChanged(this, width, height);
        } else {
            if (changing != null) {
                View cv = changing.getChildView();
                if (cv == child) {
                    changing.preferenceChanged(width, height);
                    return;
                }
            }
            int index = getViewIndex(child.getStartOffset(),
                                     Position.Bias.Forward);
            ChildState cs = getChildState(index);
            cs.preferenceChanged(width, height);
            LayoutQueue q = getLayoutQueue();
            q.addTask(cs);
            q.addTask(flushTask);
        }
    }
    public void setSize(float width, float height) {
        setSpanOnAxis(X_AXIS, width);
        setSpanOnAxis(Y_AXIS, height);
    }
    float getSpanOnAxis(int axis) {
        if (axis == getMajorAxis()) {
            return majorSpan;
        }
        return minorSpan;
    }
    void setSpanOnAxis(int axis, float span) {
        float margin = getInsetSpan(axis);
        if (axis == getMinorAxis()) {
            float targetSpan = span - margin;
            if (targetSpan != minorSpan) {
                minorSpan = targetSpan;
                int n = getViewCount();
                if (n != 0) {
                    LayoutQueue q = getLayoutQueue();
                    for (int i = 0; i < n; i++) {
                        ChildState cs = getChildState(i);
                        cs.childSizeValid = false;
                        q.addTask(cs);
                    }
                    q.addTask(flushTask);
                }
            }
        } else {
            if (estimatedMajorSpan) {
                majorSpan = span - margin;
            }
        }
    }
    public void paint(Graphics g, Shape alloc) {
        synchronized (locator) {
            locator.setAllocation(alloc);
            locator.paintChildren(g);
        }
    }
    public float getPreferredSpan(int axis) {
        float margin = getInsetSpan(axis);
        if (axis == this.axis) {
            return majorSpan + margin;
        }
        if (prefRequest != null) {
            View child = prefRequest.getChildView();
            return child.getPreferredSpan(axis) + margin;
        }
        return margin + 30;
    }
    public float getMinimumSpan(int axis) {
        if (axis == this.axis) {
            return getPreferredSpan(axis);
        }
        if (minRequest != null) {
            View child = minRequest.getChildView();
            return child.getMinimumSpan(axis);
        }
        if (axis == X_AXIS) {
            return getLeftInset() + getRightInset() + 5;
        } else {
            return getTopInset() + getBottomInset() + 5;
        }
    }
    public float getMaximumSpan(int axis) {
        if (axis == this.axis) {
            return getPreferredSpan(axis);
        }
        return Integer.MAX_VALUE;
    }
    public int getViewCount() {
        synchronized(stats) {
            return stats.size();
        }
    }
    public View getView(int n) {
        ChildState cs = getChildState(n);
        if (cs != null) {
            return cs.getChildView();
        }
        return null;
    }
    public Shape getChildAllocation(int index, Shape a) {
        Shape ca = locator.getChildAllocation(index, a);
        return ca;
    }
    public int getViewIndex(int pos, Position.Bias b) {
        return getViewIndexAtPosition(pos, b);
    }
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int index = getViewIndex(pos, b);
        Shape ca = locator.getChildAllocation(index, a);
        ChildState cs = getChildState(index);
        synchronized (cs) {
            View cv = cs.getChildView();
            Shape v = cv.modelToView(pos, ca, b);
            return v;
        }
    }
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        int pos;    
        int index;  
        Shape ca;   
        synchronized (locator) {
            index = locator.getViewIndexAtPoint(x, y, a);
            ca = locator.getChildAllocation(index, a);
        }
        ChildState cs = getChildState(index);
        synchronized (cs) {
            View v = cs.getChildView();
            pos = v.viewToModel(x, y, ca, biasReturn);
        }
        return pos;
    }
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
                                         int direction,
                                         Position.Bias[] biasRet)
                                                  throws BadLocationException {
        return Utilities.getNextVisualPositionFrom(
                            this, pos, b, a, direction, biasRet);
    }
    int axis;
    List<ChildState> stats;
    float majorSpan;
    boolean estimatedMajorSpan;
    float minorSpan;
    protected ChildLocator locator;
    float topInset;
    float bottomInset;
    float leftInset;
    float rightInset;
    ChildState minRequest;
    ChildState prefRequest;
    boolean majorChanged;
    boolean minorChanged;
    Runnable flushTask;
    ChildState changing;
    public class ChildLocator {
        public ChildLocator() {
            lastAlloc = new Rectangle();
            childAlloc = new Rectangle();
        }
        public synchronized void childChanged(ChildState cs) {
            if (lastValidOffset == null) {
                lastValidOffset = cs;
            } else if (cs.getChildView().getStartOffset() <
                       lastValidOffset.getChildView().getStartOffset()) {
                lastValidOffset = cs;
            }
        }
        public synchronized void paintChildren(Graphics g) {
            Rectangle clip = g.getClipBounds();
            float targetOffset = (axis == X_AXIS) ?
                clip.x - lastAlloc.x : clip.y - lastAlloc.y;
            int index = getViewIndexAtVisualOffset(targetOffset);
            int n = getViewCount();
            float offs = getChildState(index).getMajorOffset();
            for (int i = index; i < n; i++) {
                ChildState cs = getChildState(i);
                cs.setMajorOffset(offs);
                Shape ca = getChildAllocation(i);
                if (intersectsClip(ca, clip)) {
                    synchronized (cs) {
                        View v = cs.getChildView();
                        v.paint(g, ca);
                    }
                } else {
                    break;
                }
                offs += cs.getMajorSpan();
            }
        }
        public synchronized Shape getChildAllocation(int index, Shape a) {
            if (a == null) {
                return null;
            }
            setAllocation(a);
            ChildState cs = getChildState(index);
            if (lastValidOffset == null) {
                lastValidOffset = getChildState(0);
            }
            if (cs.getChildView().getStartOffset() >
                lastValidOffset.getChildView().getStartOffset()) {
                updateChildOffsetsToIndex(index);
            }
            Shape ca = getChildAllocation(index);
            return ca;
        }
        public int getViewIndexAtPoint(float x, float y, Shape a) {
            setAllocation(a);
            float targetOffset = (axis == X_AXIS) ? x - lastAlloc.x : y - lastAlloc.y;
            int index = getViewIndexAtVisualOffset(targetOffset);
            return index;
        }
        protected Shape getChildAllocation(int index) {
            ChildState cs = getChildState(index);
            if (! cs.isLayoutValid()) {
                cs.run();
            }
            if (axis == X_AXIS) {
                childAlloc.x = lastAlloc.x + (int) cs.getMajorOffset();
                childAlloc.y = lastAlloc.y + (int) cs.getMinorOffset();
                childAlloc.width = (int) cs.getMajorSpan();
                childAlloc.height = (int) cs.getMinorSpan();
            } else {
                childAlloc.y = lastAlloc.y + (int) cs.getMajorOffset();
                childAlloc.x = lastAlloc.x + (int) cs.getMinorOffset();
                childAlloc.height = (int) cs.getMajorSpan();
                childAlloc.width = (int) cs.getMinorSpan();
            }
            childAlloc.x += (int)getLeftInset();
            childAlloc.y += (int)getRightInset();
            return childAlloc;
        }
        protected void setAllocation(Shape a) {
            if (a instanceof Rectangle) {
                lastAlloc.setBounds((Rectangle) a);
            } else {
                lastAlloc.setBounds(a.getBounds());
            }
            setSize(lastAlloc.width, lastAlloc.height);
        }
        protected int getViewIndexAtVisualOffset(float targetOffset) {
            int n = getViewCount();
            if (n > 0) {
                boolean lastValid = (lastValidOffset != null);
                if (lastValidOffset == null) {
                    lastValidOffset = getChildState(0);
                }
                if (targetOffset > majorSpan) {
                    if (!lastValid) {
                        return 0;
                    }
                    int pos = lastValidOffset.getChildView().getStartOffset();
                    int index = getViewIndex(pos, Position.Bias.Forward);
                    return index;
                } else if (targetOffset > lastValidOffset.getMajorOffset()) {
                    return updateChildOffsets(targetOffset);
                } else {
                    float offs = 0f;
                    for (int i = 0; i < n; i++) {
                        ChildState cs = getChildState(i);
                        float nextOffs = offs + cs.getMajorSpan();
                        if (targetOffset < nextOffs) {
                            return i;
                        }
                        offs = nextOffs;
                    }
                }
            }
            return n - 1;
        }
        int updateChildOffsets(float targetOffset) {
            int n = getViewCount();
            int targetIndex = n - 1;
            int pos = lastValidOffset.getChildView().getStartOffset();
            int startIndex = getViewIndex(pos, Position.Bias.Forward);
            float start = lastValidOffset.getMajorOffset();
            float lastOffset = start;
            for (int i = startIndex; i < n; i++) {
                ChildState cs = getChildState(i);
                cs.setMajorOffset(lastOffset);
                lastOffset += cs.getMajorSpan();
                if (targetOffset < lastOffset) {
                    targetIndex = i;
                    lastValidOffset = cs;
                    break;
                }
            }
            return targetIndex;
        }
        void updateChildOffsetsToIndex(int index) {
            int pos = lastValidOffset.getChildView().getStartOffset();
            int startIndex = getViewIndex(pos, Position.Bias.Forward);
            float lastOffset = lastValidOffset.getMajorOffset();
            for (int i = startIndex; i <= index; i++) {
                ChildState cs = getChildState(i);
                cs.setMajorOffset(lastOffset);
                lastOffset += cs.getMajorSpan();
            }
        }
        boolean intersectsClip(Shape childAlloc, Rectangle clip) {
            Rectangle cs = (childAlloc instanceof Rectangle) ?
                (Rectangle) childAlloc : childAlloc.getBounds();
            if (cs.intersects(clip)) {
                return lastAlloc.intersects(cs);
            }
            return false;
        }
        protected ChildState lastValidOffset;
        protected Rectangle lastAlloc;
        protected Rectangle childAlloc;
    }
    public class ChildState implements Runnable {
        public ChildState(View v) {
            child = v;
            minorValid = false;
            majorValid = false;
            childSizeValid = false;
            child.setParent(AsyncBoxView.this);
        }
        public View getChildView() {
            return child;
        }
        public void run () {
            AbstractDocument doc = (AbstractDocument) getDocument();
            try {
                doc.readLock();
                if (minorValid && majorValid && childSizeValid) {
                    return;
                }
                if (child.getParent() == AsyncBoxView.this) {
                    synchronized(AsyncBoxView.this) {
                        changing = this;
                    }
                    updateChild();
                    synchronized(AsyncBoxView.this) {
                        changing = null;
                    }
                    updateChild();
                }
            } finally {
                doc.readUnlock();
            }
        }
        void updateChild() {
            boolean minorUpdated = false;
            synchronized(this) {
                if (! minorValid) {
                    int minorAxis = getMinorAxis();
                    min = child.getMinimumSpan(minorAxis);
                    pref = child.getPreferredSpan(minorAxis);
                    max = child.getMaximumSpan(minorAxis);
                    minorValid = true;
                    minorUpdated = true;
                }
            }
            if (minorUpdated) {
                minorRequirementChange(this);
            }
            boolean majorUpdated = false;
            float delta = 0.0f;
            synchronized(this) {
                if (! majorValid) {
                    float old = span;
                    span = child.getPreferredSpan(axis);
                    delta = span - old;
                    majorValid = true;
                    majorUpdated = true;
                }
            }
            if (majorUpdated) {
                majorRequirementChange(this, delta);
                locator.childChanged(this);
            }
            synchronized(this) {
                if (! childSizeValid) {
                    float w;
                    float h;
                    if (axis == X_AXIS) {
                        w = span;
                        h = getMinorSpan();
                    } else {
                        w = getMinorSpan();
                        h = span;
                    }
                    childSizeValid = true;
                    child.setSize(w, h);
                }
            }
        }
        public float getMinorSpan() {
            if (max < minorSpan) {
                return max;
            }
            return Math.max(min, minorSpan);
        }
        public float getMinorOffset() {
            if (max < minorSpan) {
                float align = child.getAlignment(getMinorAxis());
                return ((minorSpan - max) * align);
            }
            return 0f;
        }
        public float getMajorSpan() {
            return span;
        }
        public float getMajorOffset() {
            return offset;
        }
        public void setMajorOffset(float offs) {
            offset = offs;
        }
        public void preferenceChanged(boolean width, boolean height) {
            if (axis == X_AXIS) {
                if (width) {
                    majorValid = false;
                }
                if (height) {
                    minorValid = false;
                }
            } else {
                if (width) {
                    minorValid = false;
                }
                if (height) {
                    majorValid = false;
                }
            }
            childSizeValid = false;
        }
        public boolean isLayoutValid() {
            return (minorValid && majorValid && childSizeValid);
        }
        private float min;
        private float pref;
        private float max;
        private boolean minorValid;
        private float span;
        private float offset;
        private boolean majorValid;
        private View child;
        private boolean childSizeValid;
    }
    class FlushTask implements Runnable {
        public void run() {
            flushRequirementChanges();
        }
    }
}
