public class ZoneView extends BoxView {
    int maxZoneSize = 8 * 1024;
    int maxZonesLoaded = 3;
    Vector<View> loadedZones;
    public ZoneView(Element elem, int axis) {
        super(elem, axis);
        loadedZones = new Vector<View>();
    }
    public int getMaximumZoneSize() {
        return maxZoneSize;
    }
    public void setMaximumZoneSize(int size) {
        maxZoneSize = size;
    }
    public int getMaxZonesLoaded() {
        return maxZonesLoaded;
    }
    public void setMaxZonesLoaded(int mzl) {
        if (mzl < 1) {
            throw new IllegalArgumentException("ZoneView.setMaxZonesLoaded must be greater than 0.");
        }
        maxZonesLoaded = mzl;
        unloadOldZones();
    }
    protected void zoneWasLoaded(View zone) {
        loadedZones.addElement(zone);
        unloadOldZones();
    }
    void unloadOldZones() {
        while (loadedZones.size() > getMaxZonesLoaded()) {
            View zone = loadedZones.elementAt(0);
            loadedZones.removeElementAt(0);
            unloadZone(zone);
        }
    }
    protected void unloadZone(View zone) {
        zone.removeAll();
    }
    protected boolean isZoneLoaded(View zone) {
        return (zone.getViewCount() > 0);
    }
    protected View createZone(int p0, int p1) {
        Document doc = getDocument();
        View zone;
        try {
            zone = new Zone(getElement(),
                            doc.createPosition(p0),
                            doc.createPosition(p1));
        } catch (BadLocationException ble) {
            throw new StateInvariantError(ble.getMessage());
        }
        return zone;
    }
    protected void loadChildren(ViewFactory f) {
        Document doc = getDocument();
        int offs0 = getStartOffset();
        int offs1 = getEndOffset();
        append(createZone(offs0, offs1));
        handleInsert(offs0, offs1 - offs0);
    }
    protected int getViewIndexAtPosition(int pos) {
        int n = getViewCount();
        if (pos == getEndOffset()) {
            return n - 1;
        }
        for(int i = 0; i < n; i++) {
            View v = getView(i);
            if(pos >= v.getStartOffset() &&
               pos < v.getEndOffset()) {
                return i;
            }
        }
        return -1;
    }
    void handleInsert(int pos, int length) {
        int index = getViewIndex(pos, Position.Bias.Forward);
        View v = getView(index);
        int offs0 = v.getStartOffset();
        int offs1 = v.getEndOffset();
        if ((offs1 - offs0) > maxZoneSize) {
            splitZone(index, offs0, offs1);
        }
    }
    void handleRemove(int pos, int length) {
    }
    void splitZone(int index, int offs0, int offs1) {
        Element elem = getElement();
        Document doc = elem.getDocument();
        Vector<View> zones = new Vector<View>();
        int offs = offs0;
        do {
            offs0 = offs;
            offs = Math.min(getDesiredZoneEnd(offs0), offs1);
            zones.addElement(createZone(offs0, offs));
        } while (offs < offs1);
        View oldZone = getView(index);
        View[] newZones = new View[zones.size()];
        zones.copyInto(newZones);
        replace(index, 1, newZones);
    }
    int getDesiredZoneEnd(int pos) {
        Element elem = getElement();
        int index = elem.getElementIndex(pos + (maxZoneSize / 2));
        Element child = elem.getElement(index);
        int offs0 = child.getStartOffset();
        int offs1 = child.getEndOffset();
        if ((offs1 - pos) > maxZoneSize) {
            if (offs0 > pos) {
                return offs0;
            }
        }
        return offs1;
    }
    protected boolean updateChildren(DocumentEvent.ElementChange ec,
                                     DocumentEvent e, ViewFactory f) {
        return false;
    }
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        handleInsert(changes.getOffset(), changes.getLength());
        super.insertUpdate(changes, a, f);
    }
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        handleRemove(changes.getOffset(), changes.getLength());
        super.removeUpdate(changes, a, f);
    }
    class Zone extends AsyncBoxView {
        private Position start;
        private Position end;
        public Zone(Element elem, Position start, Position end) {
            super(elem, ZoneView.this.getAxis());
            this.start = start;
            this.end = end;
        }
        public void load() {
            if (! isLoaded()) {
                setEstimatedMajorSpan(true);
                Element e = getElement();
                ViewFactory f = getViewFactory();
                int index0 = e.getElementIndex(getStartOffset());
                int index1 = e.getElementIndex(getEndOffset());
                View[] added = new View[index1 - index0 + 1];
                for (int i = index0; i <= index1; i++) {
                    added[i - index0] = f.create(e.getElement(i));
                }
                replace(0, 0, added);
                zoneWasLoaded(this);
            }
        }
        public void unload() {
            setEstimatedMajorSpan(true);
            removeAll();
        }
        public boolean isLoaded() {
            return (getViewCount() != 0);
        }
        protected void loadChildren(ViewFactory f) {
            setEstimatedMajorSpan(true);
            Element elem = getElement();
            int index0 = elem.getElementIndex(getStartOffset());
            int index1 = elem.getElementIndex(getEndOffset());
            int nChildren = index1 - index0;
            View first = f.create(elem.getElement(index0));
            first.setParent(this);
            float w = first.getPreferredSpan(X_AXIS);
            float h = first.getPreferredSpan(Y_AXIS);
            if (getMajorAxis() == X_AXIS) {
                w *= nChildren;
            } else {
                h += nChildren;
            }
            setSize(w, h);
        }
        protected void flushRequirementChanges() {
            if (isLoaded()) {
                super.flushRequirementChanges();
            }
        }
        public int getViewIndex(int pos, Position.Bias b) {
            boolean isBackward = (b == Position.Bias.Backward);
            pos = (isBackward) ? Math.max(0, pos - 1) : pos;
            Element elem = getElement();
            int index1 = elem.getElementIndex(pos);
            int index0 = elem.getElementIndex(getStartOffset());
            return index1 - index0;
        }
        protected boolean updateChildren(DocumentEvent.ElementChange ec,
                                         DocumentEvent e, ViewFactory f) {
            Element[] removedElems = ec.getChildrenRemoved();
            Element[] addedElems = ec.getChildrenAdded();
            Element elem = getElement();
            int index0 = elem.getElementIndex(getStartOffset());
            int index1 = elem.getElementIndex(getEndOffset()-1);
            int index = ec.getIndex();
            if ((index >= index0) && (index <= index1)) {
                int replaceIndex = index - index0;
                int nadd = Math.min(index1 - index0 + 1, addedElems.length);
                int nremove = Math.min(index1 - index0 + 1, removedElems.length);
                View[] added = new View[nadd];
                for (int i = 0; i < nadd; i++) {
                    added[i] = f.create(addedElems[i]);
                }
                replace(replaceIndex, nremove, added);
            }
            return true;
        }
        public AttributeSet getAttributes() {
            return ZoneView.this.getAttributes();
        }
        public void paint(Graphics g, Shape a) {
            load();
            super.paint(g, a);
        }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            load();
            return super.viewToModel(x, y, a, bias);
        }
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            load();
            return super.modelToView(pos, a, b);
        }
        public int getStartOffset() {
            return start.getOffset();
        }
        public int getEndOffset() {
            return end.getOffset();
        }
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (isLoaded()) {
                super.insertUpdate(e, a, f);
            }
        }
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (isLoaded()) {
                super.removeUpdate(e, a, f);
            }
        }
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (isLoaded()) {
                super.changedUpdate(e, a, f);
            }
        }
    }
}
