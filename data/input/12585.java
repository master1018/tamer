public class StyleContext implements Serializable, AbstractDocument.AttributeContext {
    public static final StyleContext getDefaultStyleContext() {
        if (defaultContext == null) {
            defaultContext = new StyleContext();
        }
        return defaultContext;
    }
    private static StyleContext defaultContext;
    public StyleContext() {
        styles = new NamedStyle(null);
        addStyle(DEFAULT_STYLE, null);
    }
    public Style addStyle(String nm, Style parent) {
        Style style = new NamedStyle(nm, parent);
        if (nm != null) {
            styles.addAttribute(nm, style);
        }
        return style;
    }
    public void removeStyle(String nm) {
        styles.removeAttribute(nm);
    }
    public Style getStyle(String nm) {
        return (Style) styles.getAttribute(nm);
    }
    public Enumeration<?> getStyleNames() {
        return styles.getAttributeNames();
    }
    public void addChangeListener(ChangeListener l) {
        styles.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        styles.removeChangeListener(l);
    }
    public ChangeListener[] getChangeListeners() {
        return ((NamedStyle)styles).getChangeListeners();
    }
    public Font getFont(AttributeSet attr) {
        int style = Font.PLAIN;
        if (StyleConstants.isBold(attr)) {
            style |= Font.BOLD;
        }
        if (StyleConstants.isItalic(attr)) {
            style |= Font.ITALIC;
        }
        String family = StyleConstants.getFontFamily(attr);
        int size = StyleConstants.getFontSize(attr);
        if (StyleConstants.isSuperscript(attr) ||
            StyleConstants.isSubscript(attr)) {
            size -= 2;
        }
        return getFont(family, style, size);
    }
    public Color getForeground(AttributeSet attr) {
        return StyleConstants.getForeground(attr);
    }
    public Color getBackground(AttributeSet attr) {
        return StyleConstants.getBackground(attr);
    }
    public Font getFont(String family, int style, int size) {
        fontSearch.setValue(family, style, size);
        Font f = fontTable.get(fontSearch);
        if (f == null) {
            Style defaultStyle =
                getStyle(StyleContext.DEFAULT_STYLE);
            if (defaultStyle != null) {
                final String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";
                Font defaultFont =
                    (Font) defaultStyle.getAttribute(FONT_ATTRIBUTE_KEY);
                if (defaultFont != null
                      && defaultFont.getFamily().equalsIgnoreCase(family)) {
                    f = defaultFont.deriveFont(style, size);
                }
            }
            if (f == null) {
                f = new Font(family, style, size);
            }
            if (! FontUtilities.fontSupportsDefaultEncoding(f)) {
                f = FontUtilities.getCompositeFontUIResource(f);
            }
            FontKey key = new FontKey(family, style, size);
            fontTable.put(key, f);
        }
        return f;
    }
    public FontMetrics getFontMetrics(Font f) {
        return Toolkit.getDefaultToolkit().getFontMetrics(f);
    }
    public synchronized AttributeSet addAttribute(AttributeSet old, Object name, Object value) {
        if ((old.getAttributeCount() + 1) <= getCompressionThreshold()) {
            search.removeAttributes(search);
            search.addAttributes(old);
            search.addAttribute(name, value);
            reclaim(old);
            return getImmutableUniqueSet();
        }
        MutableAttributeSet ma = getMutableAttributeSet(old);
        ma.addAttribute(name, value);
        return ma;
    }
    public synchronized AttributeSet addAttributes(AttributeSet old, AttributeSet attr) {
        if ((old.getAttributeCount() + attr.getAttributeCount()) <= getCompressionThreshold()) {
            search.removeAttributes(search);
            search.addAttributes(old);
            search.addAttributes(attr);
            reclaim(old);
            return getImmutableUniqueSet();
        }
        MutableAttributeSet ma = getMutableAttributeSet(old);
        ma.addAttributes(attr);
        return ma;
    }
    public synchronized AttributeSet removeAttribute(AttributeSet old, Object name) {
        if ((old.getAttributeCount() - 1) <= getCompressionThreshold()) {
            search.removeAttributes(search);
            search.addAttributes(old);
            search.removeAttribute(name);
            reclaim(old);
            return getImmutableUniqueSet();
        }
        MutableAttributeSet ma = getMutableAttributeSet(old);
        ma.removeAttribute(name);
        return ma;
    }
    public synchronized AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names) {
        if (old.getAttributeCount() <= getCompressionThreshold()) {
            search.removeAttributes(search);
            search.addAttributes(old);
            search.removeAttributes(names);
            reclaim(old);
            return getImmutableUniqueSet();
        }
        MutableAttributeSet ma = getMutableAttributeSet(old);
        ma.removeAttributes(names);
        return ma;
    }
    public synchronized AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs) {
        if (old.getAttributeCount() <= getCompressionThreshold()) {
            search.removeAttributes(search);
            search.addAttributes(old);
            search.removeAttributes(attrs);
            reclaim(old);
            return getImmutableUniqueSet();
        }
        MutableAttributeSet ma = getMutableAttributeSet(old);
        ma.removeAttributes(attrs);
        return ma;
    }
    public AttributeSet getEmptySet() {
        return SimpleAttributeSet.EMPTY;
    }
    public void reclaim(AttributeSet a) {
        if (SwingUtilities.isEventDispatchThread()) {
            attributesPool.size(); 
        }
    }
    protected int getCompressionThreshold() {
        return THRESHOLD;
    }
    protected SmallAttributeSet createSmallAttributeSet(AttributeSet a) {
        return new SmallAttributeSet(a);
    }
    protected MutableAttributeSet createLargeAttributeSet(AttributeSet a) {
        return new SimpleAttributeSet(a);
    }
    synchronized void removeUnusedSets() {
        attributesPool.size(); 
    }
    AttributeSet getImmutableUniqueSet() {
        SmallAttributeSet key = createSmallAttributeSet(search);
        WeakReference<SmallAttributeSet> reference = attributesPool.get(key);
        SmallAttributeSet a;
        if (reference == null || (a = reference.get()) == null) {
            a = key;
            attributesPool.put(a, new WeakReference<SmallAttributeSet>(a));
        }
        return a;
    }
    MutableAttributeSet getMutableAttributeSet(AttributeSet a) {
        if (a instanceof MutableAttributeSet &&
            a != SimpleAttributeSet.EMPTY) {
            return (MutableAttributeSet) a;
        }
        return createLargeAttributeSet(a);
    }
    public String toString() {
        removeUnusedSets();
        String s = "";
        for (SmallAttributeSet set : attributesPool.keySet()) {
            s = s + set + "\n";
        }
        return s;
    }
    public void writeAttributes(ObjectOutputStream out,
                                  AttributeSet a) throws IOException {
        writeAttributeSet(out, a);
    }
    public void readAttributes(ObjectInputStream in,
                               MutableAttributeSet a) throws ClassNotFoundException, IOException {
        readAttributeSet(in, a);
    }
    public static void writeAttributeSet(ObjectOutputStream out,
                                         AttributeSet a) throws IOException {
        int n = a.getAttributeCount();
        out.writeInt(n);
        Enumeration keys = a.getAttributeNames();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof Serializable) {
                out.writeObject(key);
            } else {
                Object ioFmt = freezeKeyMap.get(key);
                if (ioFmt == null) {
                    throw new NotSerializableException(key.getClass().
                                 getName() + " is not serializable as a key in an AttributeSet");
                }
                out.writeObject(ioFmt);
            }
            Object value = a.getAttribute(key);
            Object ioFmt = freezeKeyMap.get(value);
            if (value instanceof Serializable) {
                out.writeObject((ioFmt != null) ? ioFmt : value);
            } else {
                if (ioFmt == null) {
                    throw new NotSerializableException(value.getClass().
                                 getName() + " is not serializable as a value in an AttributeSet");
                }
                out.writeObject(ioFmt);
            }
        }
    }
    public static void readAttributeSet(ObjectInputStream in,
        MutableAttributeSet a) throws ClassNotFoundException, IOException {
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            Object key = in.readObject();
            Object value = in.readObject();
            if (thawKeyMap != null) {
                Object staticKey = thawKeyMap.get(key);
                if (staticKey != null) {
                    key = staticKey;
                }
                Object staticValue = thawKeyMap.get(value);
                if (staticValue != null) {
                    value = staticValue;
                }
            }
            a.addAttribute(key, value);
        }
    }
    public static void registerStaticAttributeKey(Object key) {
        String ioFmt = key.getClass().getName() + "." + key.toString();
        if (freezeKeyMap == null) {
            freezeKeyMap = new Hashtable<Object, String>();
            thawKeyMap = new Hashtable<String, Object>();
        }
        freezeKeyMap.put(key, ioFmt);
        thawKeyMap.put(ioFmt, key);
    }
    public static Object getStaticAttribute(Object key) {
        if (thawKeyMap == null || key == null) {
            return null;
        }
        return thawKeyMap.get(key);
    }
    public static Object getStaticAttributeKey(Object key) {
        return key.getClass().getName() + "." + key.toString();
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        removeUnusedSets();
        s.defaultWriteObject();
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        fontSearch = new FontKey(null, 0, 0);
        fontTable = new Hashtable<FontKey, Font>();
        search = new SimpleAttributeSet();
        attributesPool = Collections.
                synchronizedMap(new WeakHashMap<SmallAttributeSet, WeakReference<SmallAttributeSet>>());
        s.defaultReadObject();
    }
    public static final String DEFAULT_STYLE = "default";
    private static Hashtable<Object, String> freezeKeyMap;
    private static Hashtable<String, Object> thawKeyMap;
    private Style styles;
    private transient FontKey fontSearch = new FontKey(null, 0, 0);
    private transient Hashtable<FontKey, Font> fontTable = new Hashtable<FontKey, Font>();
    private transient Map<SmallAttributeSet, WeakReference<SmallAttributeSet>> attributesPool = Collections.
            synchronizedMap(new WeakHashMap<SmallAttributeSet, WeakReference<SmallAttributeSet>>());
    private transient MutableAttributeSet search = new SimpleAttributeSet();
    private int unusedSets;
    static final int THRESHOLD = 9;
    public class SmallAttributeSet implements AttributeSet {
        public SmallAttributeSet(Object[] attributes) {
            this.attributes = attributes;
            updateResolveParent();
        }
        public SmallAttributeSet(AttributeSet attrs) {
            int n = attrs.getAttributeCount();
            Object[] tbl = new Object[2 * n];
            Enumeration names = attrs.getAttributeNames();
            int i = 0;
            while (names.hasMoreElements()) {
                tbl[i] = names.nextElement();
                tbl[i+1] = attrs.getAttribute(tbl[i]);
                i += 2;
            }
            attributes = tbl;
            updateResolveParent();
        }
        private void updateResolveParent() {
            resolveParent = null;
            Object[] tbl = attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                if (tbl[i] == StyleConstants.ResolveAttribute) {
                    resolveParent = (AttributeSet)tbl[i + 1];
                    break;
                }
            }
        }
        Object getLocalAttribute(Object nm) {
            if (nm == StyleConstants.ResolveAttribute) {
                return resolveParent;
            }
            Object[] tbl = attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                if (nm.equals(tbl[i])) {
                    return tbl[i+1];
                }
            }
            return null;
        }
        public String toString() {
            String s = "{";
            Object[] tbl = attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                if (tbl[i+1] instanceof AttributeSet) {
                    s = s + tbl[i] + "=" + "AttributeSet" + ",";
                } else {
                    s = s + tbl[i] + "=" + tbl[i+1] + ",";
                }
            }
            s = s + "}";
            return s;
        }
        public int hashCode() {
            int code = 0;
            Object[] tbl = attributes;
            for (int i = 1; i < tbl.length; i += 2) {
                code ^= tbl[i].hashCode();
            }
            return code;
        }
        public boolean equals(Object obj) {
            if (obj instanceof AttributeSet) {
                AttributeSet attrs = (AttributeSet) obj;
                return ((getAttributeCount() == attrs.getAttributeCount()) &&
                        containsAttributes(attrs));
            }
            return false;
        }
        public Object clone() {
            return this;
        }
        public int getAttributeCount() {
            return attributes.length / 2;
        }
        public boolean isDefined(Object key) {
            Object[] a = attributes;
            int n = a.length;
            for (int i = 0; i < n; i += 2) {
                if (key.equals(a[i])) {
                    return true;
                }
            }
            return false;
        }
        public boolean isEqual(AttributeSet attr) {
            if (attr instanceof SmallAttributeSet) {
                return attr == this;
            }
            return ((getAttributeCount() == attr.getAttributeCount()) &&
                    containsAttributes(attr));
        }
        public AttributeSet copyAttributes() {
            return this;
        }
        public Object getAttribute(Object key) {
            Object value = getLocalAttribute(key);
            if (value == null) {
                AttributeSet parent = getResolveParent();
                if (parent != null)
                    value = parent.getAttribute(key);
            }
            return value;
        }
        public Enumeration<?> getAttributeNames() {
            return new KeyEnumeration(attributes);
        }
        public boolean containsAttribute(Object name, Object value) {
            return value.equals(getAttribute(name));
        }
        public boolean containsAttributes(AttributeSet attrs) {
            boolean result = true;
            Enumeration names = attrs.getAttributeNames();
            while (result && names.hasMoreElements()) {
                Object name = names.nextElement();
                result = attrs.getAttribute(name).equals(getAttribute(name));
            }
            return result;
        }
        public AttributeSet getResolveParent() {
            return resolveParent;
        }
        Object[] attributes;
        AttributeSet resolveParent;
    }
    class KeyEnumeration implements Enumeration<Object> {
        KeyEnumeration(Object[] attr) {
            this.attr = attr;
            i = 0;
        }
        public boolean hasMoreElements() {
            return i < attr.length;
        }
        public Object nextElement() {
            if (i < attr.length) {
                Object o = attr[i];
                i += 2;
                return o;
            }
            throw new NoSuchElementException();
        }
        Object[] attr;
        int i;
    }
    class KeyBuilder {
        public void initialize(AttributeSet a) {
            if (a instanceof SmallAttributeSet) {
                initialize(((SmallAttributeSet)a).attributes);
            } else {
                keys.removeAllElements();
                data.removeAllElements();
                Enumeration names = a.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    addAttribute(name, a.getAttribute(name));
                }
            }
        }
        private void initialize(Object[] sorted) {
            keys.removeAllElements();
            data.removeAllElements();
            int n = sorted.length;
            for (int i = 0; i < n; i += 2) {
                keys.addElement(sorted[i]);
                data.addElement(sorted[i+1]);
            }
        }
        public Object[] createTable() {
            int n = keys.size();
            Object[] tbl = new Object[2 * n];
            for (int i = 0; i < n; i ++) {
                int offs = 2 * i;
                tbl[offs] = keys.elementAt(i);
                tbl[offs + 1] = data.elementAt(i);
            }
            return tbl;
        }
        int getCount() {
            return keys.size();
        }
        public void addAttribute(Object key, Object value) {
            keys.addElement(key);
            data.addElement(value);
        }
        public void addAttributes(AttributeSet attr) {
            if (attr instanceof SmallAttributeSet) {
                Object[] tbl = ((SmallAttributeSet)attr).attributes;
                int n = tbl.length;
                for (int i = 0; i < n; i += 2) {
                    addAttribute(tbl[i], tbl[i+1]);
                }
            } else {
                Enumeration names = attr.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    addAttribute(name, attr.getAttribute(name));
                }
            }
        }
        public void removeAttribute(Object key) {
            int n = keys.size();
            for (int i = 0; i < n; i++) {
                if (keys.elementAt(i).equals(key)) {
                    keys.removeElementAt(i);
                    data.removeElementAt(i);
                    return;
                }
            }
        }
        public void removeAttributes(Enumeration names) {
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                removeAttribute(name);
            }
        }
        public void removeAttributes(AttributeSet attr) {
            Enumeration names = attr.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = attr.getAttribute(name);
                removeSearchAttribute(name, value);
            }
        }
        private void removeSearchAttribute(Object ikey, Object value) {
            int n = keys.size();
            for (int i = 0; i < n; i++) {
                if (keys.elementAt(i).equals(ikey)) {
                    if (data.elementAt(i).equals(value)) {
                        keys.removeElementAt(i);
                        data.removeElementAt(i);
                    }
                    return;
                }
            }
        }
        private Vector<Object> keys = new Vector<Object>();
        private Vector<Object> data = new Vector<Object>();
    }
    static class FontKey {
        private String family;
        private int style;
        private int size;
        public FontKey(String family, int style, int size) {
            setValue(family, style, size);
        }
        public void setValue(String family, int style, int size) {
            this.family = (family != null) ? family.intern() : null;
            this.style = style;
            this.size = size;
        }
        public int hashCode() {
            int fhash = (family != null) ? family.hashCode() : 0;
            return fhash ^ style ^ size;
        }
        public boolean equals(Object obj) {
            if (obj instanceof FontKey) {
                FontKey font = (FontKey)obj;
                return (size == font.size) && (style == font.style) && (family == font.family);
            }
            return false;
        }
    }
    public class NamedStyle implements Style, Serializable {
        public NamedStyle(String name, Style parent) {
            attributes = getEmptySet();
            if (name != null) {
                setName(name);
            }
            if (parent != null) {
                setResolveParent(parent);
            }
        }
        public NamedStyle(Style parent) {
            this(null, parent);
        }
        public NamedStyle() {
            attributes = getEmptySet();
        }
        public String toString() {
            return "NamedStyle:" + getName() + " " + attributes;
        }
        public String getName() {
            if (isDefined(StyleConstants.NameAttribute)) {
                return getAttribute(StyleConstants.NameAttribute).toString();
            }
            return null;
        }
        public void setName(String name) {
            if (name != null) {
                this.addAttribute(StyleConstants.NameAttribute, name);
            }
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
        protected void fireStateChanged() {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==ChangeListener.class) {
                    if (changeEvent == null)
                        changeEvent = new ChangeEvent(this);
                    ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
                }
            }
        }
        public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
            return listenerList.getListeners(listenerType);
        }
        public int getAttributeCount() {
            return attributes.getAttributeCount();
        }
        public boolean isDefined(Object attrName) {
            return attributes.isDefined(attrName);
        }
        public boolean isEqual(AttributeSet attr) {
            return attributes.isEqual(attr);
        }
        public AttributeSet copyAttributes() {
            NamedStyle a = new NamedStyle();
            a.attributes = attributes.copyAttributes();
            return a;
        }
        public Object getAttribute(Object attrName) {
            return attributes.getAttribute(attrName);
        }
        public Enumeration<?> getAttributeNames() {
            return attributes.getAttributeNames();
        }
        public boolean containsAttribute(Object name, Object value) {
            return attributes.containsAttribute(name, value);
        }
        public boolean containsAttributes(AttributeSet attrs) {
            return attributes.containsAttributes(attrs);
        }
        public AttributeSet getResolveParent() {
            return attributes.getResolveParent();
        }
        public void addAttribute(Object name, Object value) {
            StyleContext context = StyleContext.this;
            attributes = context.addAttribute(attributes, name, value);
            fireStateChanged();
        }
        public void addAttributes(AttributeSet attr) {
            StyleContext context = StyleContext.this;
            attributes = context.addAttributes(attributes, attr);
            fireStateChanged();
        }
        public void removeAttribute(Object name) {
            StyleContext context = StyleContext.this;
            attributes = context.removeAttribute(attributes, name);
            fireStateChanged();
        }
        public void removeAttributes(Enumeration<?> names) {
            StyleContext context = StyleContext.this;
            attributes = context.removeAttributes(attributes, names);
            fireStateChanged();
        }
        public void removeAttributes(AttributeSet attrs) {
            StyleContext context = StyleContext.this;
            if (attrs == this) {
                attributes = context.getEmptySet();
            } else {
                attributes = context.removeAttributes(attributes, attrs);
            }
            fireStateChanged();
        }
        public void setResolveParent(AttributeSet parent) {
            if (parent != null) {
                addAttribute(StyleConstants.ResolveAttribute, parent);
            } else {
                removeAttribute(StyleConstants.ResolveAttribute);
            }
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            writeAttributeSet(s, attributes);
        }
        private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException
        {
            s.defaultReadObject();
            attributes = SimpleAttributeSet.EMPTY;
            readAttributeSet(s, this);
        }
        protected EventListenerList listenerList = new EventListenerList();
        protected transient ChangeEvent changeEvent = null;
        private transient AttributeSet attributes;
    }
    static {
        try {
            int n = StyleConstants.keys.length;
            for (int i = 0; i < n; i++) {
                StyleContext.registerStaticAttributeKey(StyleConstants.keys[i]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
