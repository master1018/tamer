public abstract class FlowView extends BoxView {
    public FlowView(Element elem, int axis) {
        super(elem, axis);
        layoutSpan = Integer.MAX_VALUE;
        strategy = new FlowStrategy();
    }
    public int getFlowAxis() {
        if (getAxis() == Y_AXIS) {
            return X_AXIS;
        }
        return Y_AXIS;
    }
    public int getFlowSpan(int index) {
        return layoutSpan;
    }
    public int getFlowStart(int index) {
        return 0;
    }
    protected abstract View createRow();
    protected void loadChildren(ViewFactory f) {
        if (layoutPool == null) {
            layoutPool = new LogicalView(getElement());
        }
        layoutPool.setParent(this);
        strategy.insertUpdate(this, null, null);
    }
    protected int getViewIndexAtPosition(int pos) {
        if (pos >= getStartOffset() && (pos < getEndOffset())) {
            for (int counter = 0; counter < getViewCount(); counter++) {
                View v = getView(counter);
                if(pos >= v.getStartOffset() &&
                   pos < v.getEndOffset()) {
                    return counter;
                }
            }
        }
        return -1;
    }
    protected void layout(int width, int height) {
        final int faxis = getFlowAxis();
        int newSpan;
        if (faxis == X_AXIS) {
            newSpan = width;
        } else {
            newSpan = height;
        }
        if (layoutSpan != newSpan) {
            layoutChanged(faxis);
            layoutChanged(getAxis());
            layoutSpan = newSpan;
        }
        if (! isLayoutValid(faxis)) {
            final int heightAxis = getAxis();
            int oldFlowHeight = (heightAxis == X_AXIS)? getWidth() : getHeight();
            strategy.layout(this);
            int newFlowHeight = (int) getPreferredSpan(heightAxis);
            if (oldFlowHeight != newFlowHeight) {
                View p = getParent();
                if (p != null) {
                    p.preferenceChanged(this, (heightAxis == X_AXIS), (heightAxis == Y_AXIS));
                }
                Component host = getContainer();
                if (host != null) {
                    host.repaint();
                }
            }
        }
        super.layout(width, height);
    }
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        if (r == null) {
            r = new SizeRequirements();
        }
        float pref = layoutPool.getPreferredSpan(axis);
        float min = layoutPool.getMinimumSpan(axis);
        r.minimum = (int)min;
        r.preferred = Math.max(r.minimum, (int) pref);
        r.maximum = Integer.MAX_VALUE;
        r.alignment = 0.5f;
        return r;
    }
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        layoutPool.insertUpdate(changes, a, f);
        strategy.insertUpdate(this, changes, getInsideAllocation(a));
    }
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        layoutPool.removeUpdate(changes, a, f);
        strategy.removeUpdate(this, changes, getInsideAllocation(a));
    }
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        layoutPool.changedUpdate(changes, a, f);
        strategy.changedUpdate(this, changes, getInsideAllocation(a));
    }
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent == null
                && layoutPool != null ) {
            layoutPool.setParent(null);
        }
    }
    protected int layoutSpan;
    protected View layoutPool;
    protected FlowStrategy strategy;
    public static class FlowStrategy {
        Position damageStart = null;
        Vector<View> viewBuffer;
        void addDamage(FlowView fv, int offset) {
            if (offset >= fv.getStartOffset() && offset < fv.getEndOffset()) {
                if (damageStart == null || offset < damageStart.getOffset()) {
                    try {
                        damageStart = fv.getDocument().createPosition(offset);
                    } catch (BadLocationException e) {
                        assert(false);
                    }
                }
            }
        }
        void unsetDamage() {
            damageStart = null;
        }
        public void insertUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            if (e != null) {
                addDamage(fv, e.getOffset());
            }
            if (alloc != null) {
                Component host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }
        public void removeUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            addDamage(fv, e.getOffset());
            if (alloc != null) {
                Component host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }
        public void changedUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            addDamage(fv, e.getOffset());
            if (alloc != null) {
                Component host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }
        protected View getLogicalView(FlowView fv) {
            return fv.layoutPool;
        }
        public void layout(FlowView fv) {
            View pool = getLogicalView(fv);
            int rowIndex, p0;
            int p1 = fv.getEndOffset();
            if (fv.majorAllocValid) {
                if (damageStart == null) {
                    return;
                }
                int offset = damageStart.getOffset();
                while ((rowIndex = fv.getViewIndexAtPosition(offset)) < 0) {
                    offset--;
                }
                if (rowIndex > 0) {
                    rowIndex--;
                }
                p0 = fv.getView(rowIndex).getStartOffset();
            } else {
                rowIndex = 0;
                p0 = fv.getStartOffset();
            }
            reparentViews(pool, p0);
            viewBuffer = new Vector<View>(10, 10);
            int rowCount = fv.getViewCount();
            while (p0 < p1) {
                View row;
                if (rowIndex >= rowCount) {
                    row = fv.createRow();
                    fv.append(row);
                } else {
                    row = fv.getView(rowIndex);
                }
                p0 = layoutRow(fv, rowIndex, p0);
                rowIndex++;
            }
            viewBuffer = null;
            if (rowIndex < rowCount) {
                fv.replace(rowIndex, rowCount - rowIndex, null);
            }
            unsetDamage();
        }
        protected int layoutRow(FlowView fv, int rowIndex, int pos) {
            View row = fv.getView(rowIndex);
            float x = fv.getFlowStart(rowIndex);
            float spanLeft = fv.getFlowSpan(rowIndex);
            int end = fv.getEndOffset();
            TabExpander te = (fv instanceof TabExpander) ? (TabExpander)fv : null;
            final int flowAxis = fv.getFlowAxis();
            int breakWeight = BadBreakWeight;
            float breakX = 0f;
            float breakSpan = 0f;
            int breakIndex = -1;
            int n = 0;
            viewBuffer.clear();
            while (pos < end && spanLeft >= 0) {
                View v = createView(fv, pos, (int)spanLeft, rowIndex);
                if (v == null) {
                    break;
                }
                int bw = v.getBreakWeight(flowAxis, x, spanLeft);
                if (bw >= ForcedBreakWeight) {
                    View w = v.breakView(flowAxis, pos, x, spanLeft);
                    if (w != null) {
                        viewBuffer.add(w);
                    } else if (n == 0) {
                        viewBuffer.add(v);
                    }
                    break;
                } else if (bw >= breakWeight && bw > BadBreakWeight) {
                    breakWeight = bw;
                    breakX = x;
                    breakSpan = spanLeft;
                    breakIndex = n;
                }
                float chunkSpan;
                if (flowAxis == X_AXIS && v instanceof TabableView) {
                    chunkSpan = ((TabableView)v).getTabbedSpan(x, te);
                } else {
                    chunkSpan = v.getPreferredSpan(flowAxis);
                }
                if (chunkSpan > spanLeft && breakIndex >= 0) {
                    if (breakIndex < n) {
                        v = viewBuffer.get(breakIndex);
                    }
                    for (int i = n - 1; i >= breakIndex; i--) {
                        viewBuffer.remove(i);
                    }
                    v = v.breakView(flowAxis, v.getStartOffset(), breakX, breakSpan);
                }
                spanLeft -= chunkSpan;
                x += chunkSpan;
                viewBuffer.add(v);
                pos = v.getEndOffset();
                n++;
            }
            View[] views = new View[viewBuffer.size()];
            viewBuffer.toArray(views);
            row.replace(0, row.getViewCount(), views);
            return (views.length > 0 ? row.getEndOffset() : pos);
        }
        protected void adjustRow(FlowView fv, int rowIndex, int desiredSpan, int x) {
            final int flowAxis = fv.getFlowAxis();
            View r = fv.getView(rowIndex);
            int n = r.getViewCount();
            int span = 0;
            int bestWeight = BadBreakWeight;
            int bestSpan = 0;
            int bestIndex = -1;
            View v;
            for (int i = 0; i < n; i++) {
                v = r.getView(i);
                int spanLeft = desiredSpan - span;
                int w = v.getBreakWeight(flowAxis, x + span, spanLeft);
                if ((w >= bestWeight) && (w > BadBreakWeight)) {
                    bestWeight = w;
                    bestIndex = i;
                    bestSpan = span;
                    if (w >= ForcedBreakWeight) {
                        break;
                    }
                }
                span += v.getPreferredSpan(flowAxis);
            }
            if (bestIndex < 0) {
                return;
            }
            int spanLeft = desiredSpan - bestSpan;
            v = r.getView(bestIndex);
            v = v.breakView(flowAxis, v.getStartOffset(), x + bestSpan, spanLeft);
            View[] va = new View[1];
            va[0] = v;
            View lv = getLogicalView(fv);
            int p0 = r.getView(bestIndex).getStartOffset();
            int p1 = r.getEndOffset();
            for (int i = 0; i < lv.getViewCount(); i++) {
                View tmpView = lv.getView(i);
                if (tmpView.getEndOffset() > p1) {
                    break;
                }
                if (tmpView.getStartOffset() >= p0) {
                    tmpView.setParent(lv);
                }
            }
            r.replace(bestIndex, n - bestIndex, va);
        }
        void reparentViews(View pool, int startPos) {
            int n = pool.getViewIndex(startPos, Position.Bias.Forward);
            if (n >= 0) {
                for (int i = n; i < pool.getViewCount(); i++) {
                    pool.getView(i).setParent(pool);
                }
            }
        }
        protected View createView(FlowView fv, int startOffset, int spanLeft, int rowIndex) {
            View lv = getLogicalView(fv);
            int childIndex = lv.getViewIndex(startOffset, Position.Bias.Forward);
            View v = lv.getView(childIndex);
            if (startOffset==v.getStartOffset()) {
                return v;
            }
            v = v.createFragment(startOffset, v.getEndOffset());
            return v;
        }
    }
    static class LogicalView extends CompositeView {
        LogicalView(Element elem) {
            super(elem);
        }
        protected int getViewIndexAtPosition(int pos) {
            Element elem = getElement();
            if (elem.isLeaf()) {
                return 0;
            }
            return super.getViewIndexAtPosition(pos);
        }
        protected void loadChildren(ViewFactory f) {
            Element elem = getElement();
            if (elem.isLeaf()) {
                View v = new LabelView(elem);
                append(v);
            } else {
                super.loadChildren(f);
            }
        }
        public AttributeSet getAttributes() {
            View p = getParent();
            return (p != null) ? p.getAttributes() : null;
        }
        public float getPreferredSpan(int axis) {
            float maxpref = 0;
            float pref = 0;
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                pref += v.getPreferredSpan(axis);
                if (v.getBreakWeight(axis, 0, Integer.MAX_VALUE) >= ForcedBreakWeight) {
                    maxpref = Math.max(maxpref, pref);
                    pref = 0;
                }
            }
            maxpref = Math.max(maxpref, pref);
            return maxpref;
        }
        public float getMinimumSpan(int axis) {
            float maxmin = 0;
            float min = 0;
            boolean nowrap = false;
            int n = getViewCount();
            for (int i = 0; i < n; i++) {
                View v = getView(i);
                if (v.getBreakWeight(axis, 0, Integer.MAX_VALUE) == BadBreakWeight) {
                    min += v.getPreferredSpan(axis);
                    nowrap = true;
                } else if (nowrap) {
                    maxmin = Math.max(min, maxmin);
                    nowrap = false;
                    min = 0;
                }
                if (v instanceof ComponentView) {
                    maxmin = Math.max(maxmin, v.getMinimumSpan(axis));
                }
            }
            maxmin = Math.max(maxmin, min);
            return maxmin;
        }
        protected void forwardUpdateToView(View v, DocumentEvent e,
                                           Shape a, ViewFactory f) {
            View parent = v.getParent();
            v.setParent(this);
            super.forwardUpdateToView(v, e, a, f);
            v.setParent(parent);
        }
        public void paint(Graphics g, Shape allocation) {
        }
        protected boolean isBefore(int x, int y, Rectangle alloc) {
            return false;
        }
        protected boolean isAfter(int x, int y, Rectangle alloc) {
            return false;
        }
        protected View getViewAtPoint(int x, int y, Rectangle alloc) {
            return null;
        }
        protected void childAllocation(int index, Rectangle a) {
        }
    }
}
