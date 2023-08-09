public abstract class XMLKit {
    private XMLKit() {
    }
    static final int NEED_SLOP = 1;
    static final Object[] noPartsFrozen = {};
    static final Object[] noPartsNotFrozen = new Object[NEED_SLOP];
    static final String WHITESPACE_CHARS = " \t\n\r\f";
    static final String ANON_NAME = new String("*");  
    public static final class Element implements Comparable<Element>, Iterable<Object> {
        String name;
        int size;
        Object[] parts;
        Element(String name, int size, int capacity) {
            this.name = name.toString();
            this.size = size;
            assert (size <= capacity);
            this.parts = capacity > 0 ? new Object[capacity] : noPartsFrozen;
        }
        public Element() {
            this(ANON_NAME, 0, NEED_SLOP + 4);
        }
        public Element(int extraCapacity) {
            this(ANON_NAME, 0, NEED_SLOP + Math.max(0, extraCapacity));
        }
        public Element(String name) {
            this(name, 0, NEED_SLOP + 4);
        }
        public Element(String name, int extraCapacity) {
            this(name, 0, NEED_SLOP + Math.max(0, extraCapacity));
        }
        public Element(String name, String... attrs) {
            this(name, attrs, (Element[]) null, 0);
        }
        public Element(String name, String[] attrs, int extraCapacity) {
            this(name, attrs, (Element[]) null, extraCapacity);
        }
        public Element(String name, Element... elems) {
            this(name, (String[]) null, elems, 0);
        }
        public Element(String name, Element[] elems, int extraCapacity) {
            this(name, (String[]) null, elems, extraCapacity);
        }
        public Element(String name, String[] attrs, Object... elems) {
            this(name, attrs, elems, 0);
        }
        public Element(String name, String[] attrs, Object[] elems, int extraCapacity) {
            this(name, 0,
                    ((elems == null) ? 0 : elems.length)
                    + Math.max(0, extraCapacity)
                    + NEED_SLOP
                    + ((attrs == null) ? 0 : attrs.length));
            int ne = ((elems == null) ? 0 : elems.length);
            int na = ((attrs == null) ? 0 : attrs.length);
            int fillp = 0;
            for (int i = 0; i < ne; i++) {
                if (elems[i] != null) {
                    parts[fillp++] = elems[i];
                }
            }
            size = fillp;
            for (int i = 0; i < na; i += 2) {
                setAttr(attrs[i + 0], attrs[i + 1]);
            }
        }
        public Element(Collection c) {
            this(c.size());
            addAll(c);
        }
        public Element(String name, Collection c) {
            this(name, c.size());
            addAll(c);
        }
        public Element(Element old) {
            this(old, 0);
        }
        public Element(Element old, int extraCapacity) {
            this(old.name, old.size,
                    old.size
                    + Math.max(0, extraCapacity) + NEED_SLOP
                    + old.attrLength());
            System.arraycopy(old.parts, 0, parts, 0, size);
            int alen = parts.length
                    - (size + Math.max(0, extraCapacity) + NEED_SLOP);
            System.arraycopy(old.parts, old.parts.length - alen,
                    parts, parts.length - alen,
                    alen);
            assert (!isFrozen());
        }
        public Element shallowCopy() {
            return new Element(this);
        }
        static public final Element EMPTY = new Element(ANON_NAME, 0, 0);
        Element deepFreezeOrCopy(boolean makeFrozen) {
            if (makeFrozen && isFrozen()) {
                return this;  
            }
            int alen = attrLength();
            int plen = size + (makeFrozen ? 0 : NEED_SLOP) + alen;
            Element copy = new Element(name, size, plen);
            System.arraycopy(parts, parts.length - alen, copy.parts, plen - alen, alen);
            for (int i = 0; i < size; i++) {
                Object e = parts[i];
                String str;
                if (e instanceof Element) {  
                    e = ((Element) e).deepFreezeOrCopy(makeFrozen);
                } else if (makeFrozen) {
                    e = fixupString(e);
                }
                copy.setRaw(i, e);
            }
            return copy;
        }
        public Element deepCopy() {
            return deepFreezeOrCopy(false);
        }
        public Element deepFreeze() {
            return deepFreezeOrCopy(true);
        }
        public void shallowFreeze() {
            if (isFrozen()) {
                return;
            }
            int alen = attrLength();
            Object[] nparts = new Object[size + alen];
            System.arraycopy(parts, parts.length - alen, nparts, size, alen);
            for (int i = 0; i < size; i++) {
                Object e = parts[i];
                String str;
                if (e instanceof Element) {  
                    if (!((Element) e).isFrozen()) {
                        throw new IllegalArgumentException("Sub-element must be frozen.");
                    }
                } else {
                    e = fixupString(e);
                }
                nparts[i] = e;
            }
            parts = nparts;
            assert (isFrozen());
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            checkNotFrozen();
            this.name = name.toString();
        }
        public boolean isAnonymous() {
            return name == ANON_NAME;
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean isFrozen() {
            return !hasNulls(NEED_SLOP);
        }
        void checkNotFrozen() {
            if (isFrozen()) {
                throw new UnsupportedOperationException("cannot modify frozen element");
            }
        }
        public void clear() {
            clear(0, size);
        }
        public void clear(int beg) {
            clear(beg, size);
        }
        public void clear(int beg, int end) {
            if (end > size) {
                badIndex(end);
            }
            if (beg < 0 || beg > end) {
                badIndex(beg);
            }
            if (beg == end) {
                return;
            }
            checkNotFrozen();
            if (end == size) {
                if (beg == 0
                        && parts.length > 0 && parts[parts.length - 1] == null) {
                    parts = noPartsNotFrozen;
                    size = 0;
                } else {
                    clearParts(beg, size);
                    size = beg;
                }
            } else {
                close(beg, end - beg);
            }
        }
        void clearParts(int beg, int end) {
            for (int i = beg; i < end; i++) {
                parts[i] = null;
            }
        }
        public boolean equals(Element that) {
            if (!this.name.equals(that.name)) {
                return false;
            }
            if (this.size != that.size) {
                return false;
            }
            Object[] thisParts = this.parts;
            Object[] thatParts = that.parts;
            for (int i = 0; i < size; i++) {
                Object thisPart = thisParts[i];
                Object thatPart = thatParts[i];
                if (thisPart instanceof Element) { 
                    if (!thisPart.equals(thatPart)) {
                        return false;
                    }
                } else {
                    thisPart = fixupString(thisPart);
                    thatPart = fixupString(thatPart);
                    if (!thisPart.equals(thatPart)) {
                        return false;
                    }
                }
            }
            return this.equalAttrs(that);
        }
        public boolean equals(Object o) {
            if (!(o instanceof Element)) {
                return false;
            }
            return equals((Element) o);
        }
        public int hashCode() {
            int hc = 0;
            int alen = attrLength();
            for (int i = parts.length - alen; i < parts.length; i += 2) {
                hc += (parts[i + 0].hashCode() ^ parts[i + 1].hashCode());
            }
            hc ^= hc << 11;
            hc += name.hashCode();
            for (int i = 0; i < size; i++) {
                hc ^= hc << 7;
                Object p = parts[i];
                if (p instanceof Element) {
                    hc += p.hashCode();  
                } else {
                    hc += fixupString(p).hashCode();
                }
            }
            hc ^= hc >>> 19;
            return hc;
        }
        public int compareTo(Element that) {
            int r;
            r = this.name.compareTo(that.name);
            if (r != 0) {
                return r;
            }
            int thisAlen = this.attrLength();
            int thatAlen = that.attrLength();
            if (thisAlen != 0 || thatAlen != 0) {
                r = compareAttrs(thisAlen, that, thatAlen, true);
                assert (assertAttrCompareOK(r, that));
                if (r != 0) {
                    return r;
                }
            }
            Object[] thisParts = this.parts;
            Object[] thatParts = that.parts;
            int minSize = this.size;
            if (minSize > that.size) {
                minSize = that.size;
            }
            Comparator<Object> cc = contentOrder();
            for (int i = 0; i < minSize; i++) {
                r = cc.compare(thisParts[i], thatParts[i]);
                if (r != 0) {
                    return r;
                }
            }
            return this.size - that.size;
        }
        private boolean assertAttrCompareOK(int r, Element that) {
            Element e0 = this.copyAttrsOnly();
            Element e1 = that.copyAttrsOnly();
            e0.sortAttrs();
            e1.sortAttrs();
            int r2;
            for (int k = 0;; k++) {
                boolean con0 = e0.containsAttr(k);
                boolean con1 = e1.containsAttr(k);
                if (con0 != con1) {
                    if (!con0) {
                        r2 = 0 - 1;
                        break;
                    }
                    if (!con1) {
                        r2 = 1 - 0;
                        break;
                    }
                }
                if (!con0) {
                    r2 = 0;
                    break;
                }
                String k0 = e0.getAttrName(k);
                String k1 = e1.getAttrName(k);
                r2 = k0.compareTo(k1);
                if (r2 != 0) {
                    break;
                }
                String v0 = e0.getAttr(k);
                String v1 = e1.getAttr(k);
                r2 = v0.compareTo(v1);
                if (r2 != 0) {
                    break;
                }
            }
            if (r != 0) {
                r = (r > 0) ? 1 : -1;
            }
            if (r2 != 0) {
                r2 = (r2 > 0) ? 1 : -1;
            }
            if (r != r2) {
                System.out.println("*** wrong attr compare, " + r + " != " + r2);
                System.out.println(" this = " + this);
                System.out.println("  attr->" + e0);
                System.out.println(" that = " + that);
                System.out.println("  attr->" + e1);
            }
            return r == r2;
        }
        private void badIndex(int i) {
            Object badRef = (new Object[0])[i];
        }
        public Object get(int i) {
            if (i >= size) {
                badIndex(i);
            }
            return parts[i];
        }
        public Object set(int i, Object e) {
            if (i >= size) {
                badIndex(i);
            }
            e.getClass();  
            checkNotFrozen();
            Object old = parts[i];
            setRaw(i, e);
            return old;
        }
        void setRaw(int i, Object e) {
            parts[i] = e;
        }
        public boolean remove(Object e) {
            int i = indexOf(e);
            if (i < 0) {
                return false;
            }
            close(i, 1);
            return true;
        }
        public Object remove(int i) {
            if (i >= size) {
                badIndex(i);
            }
            Object e = parts[i];
            close(i, 1);
            return e;
        }
        public Object removeLast() {
            if (size == 0) {
                return null;
            }
            return remove(size - 1);
        }
        public Object remove(Filter f) {
            return findOrRemove(f, 0, true);
        }
        public Object remove(Filter f, int fromIndex) {
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            return findOrRemove(f, fromIndex, true);
        }
        public Object removeLast(Filter f) {
            return findOrRemoveLast(f, size - 1, true);
        }
        public Object removeLast(Filter f, int fromIndex) {
            if (fromIndex >= size) {
                fromIndex = size - 1;
            }
            return findOrRemoveLast(f, fromIndex, true);
        }
        public Element removeAll(Filter f) {
            Element result = new Element();
            findOrRemoveAll(f, false, 0, size, result.asList(), true);
            return result;
        }
        public Element removeAll(Filter f, int fromIndex, int toIndex) {
            Element result = new Element();
            findOrRemoveAll(f, true, fromIndex, toIndex, result.asList(), true);
            return result;
        }
        public int removeAll(Filter f, Collection<Object> sink) {
            return findOrRemoveAll(f, false, 0, size, sink, true);
        }
        public int removeAll(Filter f, int fromIndex, int toIndex, Collection<Object> sink) {
            return findOrRemoveAll(f, false, fromIndex, toIndex, sink, true);
        }
        public Element retainAll(Filter f) {
            Element result = new Element();
            findOrRemoveAll(f, true, 0, size, result.asList(), true);
            return result;
        }
        public Element retainAll(Filter f, int fromIndex, int toIndex) {
            Element result = new Element();
            findOrRemoveAll(f, true, fromIndex, toIndex, result.asList(), true);
            return result;
        }
        public int retainAll(Filter f, Collection<Object> sink) {
            return findOrRemoveAll(f, true, 0, size, sink, true);
        }
        public int retainAll(Filter f, int fromIndex, int toIndex, Collection<Object> sink) {
            return findOrRemoveAll(f, true, fromIndex, toIndex, sink, true);
        }
        public void add(int i, Object e) {
            e.getClass();  
            if (hasNulls(1 + NEED_SLOP)) {
                if (i == size) {
                    setRaw(i, e);
                    size++;
                    return;
                }
                if (i > size) {
                    badIndex(i);
                }
                open(i, 1);
                setRaw(i, e);
                return;
            }
            size = expand(i, 1);
            setRaw(i, e);
        }
        public boolean add(Object e) {
            add(size, e);
            return true;
        }
        public Object getLast() {
            return size == 0 ? null : parts[size - 1];
        }
        public CharSequence getText() {
            checkTextOnly();
            if (size == 1) {
                return parts[0].toString();
            } else {
                return new TokenList(parts, 0, size);
            }
        }
        public Iterable<CharSequence> texts() {
            checkTextOnly();
            return (Iterable<CharSequence>) (Iterable) this;
        }
        public String[] toStrings() {
            String[] result = new String[size];
            for (int i = 0; i < size; i++) {
                result[i] = ((CharSequence) parts[i]).toString();
            }
            return result;
        }
        public CharSequence getFlatText() {
            if (size == 1) {
                if (parts[0] instanceof CharSequence) {
                    return parts[0].toString();
                } else {
                    return new TokenList();
                }
            }
            if (isText()) {
                return getText();
            }
            Element result = new Element(size);
            boolean merge = false;
            for (int i = 0; i < size; i++) {
                Object text = parts[i];
                if (!(text instanceof CharSequence)) {
                    if (text instanceof Element) {
                        Element te = (Element) text;
                        if (!te.isEmpty()) {
                            result.addText(te.getFlatText());
                        }
                    }
                    merge = true;
                    continue;
                }
                if (merge) {
                    result.addText((CharSequence) text);
                    merge = false;
                } else {
                    result.add(text);
                }
            }
            if (result.size() == 1) {
                return (CharSequence) result.parts[0];
            } else {
                return result.getText();
            }
        }
        public boolean isText() {
            for (int i = 0; i < size; i++) {
                if (!(parts[i] instanceof CharSequence)) {
                    return false;
                }
            }
            return true;
        }
        public boolean hasText() {
            for (int i = 0; i < size; i++) {
                if (parts[i] instanceof CharSequence) {
                    return true;
                }
            }
            return false;
        }
        public void checkTextOnly() {
            for (int i = 0; i < size; i++) {
                ((CharSequence) parts[i]).getClass();
            }
        }
        public void setText(CharSequence text) {
            checkTextOnly();
            clear();
            if (text instanceof TokenList) {
                addAll(0, (TokenList) text);
            } else {
                add(text);
            }
        }
        public int addText(int i, CharSequence text) {
            if (text instanceof String) {
                return addText(i, (String) text);
            } else if (text instanceof TokenList) {
                TokenList tl = (TokenList) text;
                int tlsize = tl.size();
                if (tlsize == 0) {
                    return 0;
                }
                String token0 = tl.get(0).toString();
                if (tlsize == 1) {
                    return addText(i, token0);
                }
                if (mergeWithPrev(i, token0, false)) {
                    addAll(i, tl.subList(1, tlsize));
                    return tlsize - 1;
                } else {
                    addAll(i, (Collection) tl);
                    return tlsize;
                }
            } else {
                return addText(i, text.toString());
            }
        }
        public int addText(CharSequence text) {
            return addText(size, text);
        }
        private 
                int addText(int i, String text) {
            if (text.length() == 0) {
                return 0;  
            }
            if (mergeWithPrev(i, text, true)) {
                return 0;  
            }
            add(i, text);
            return 1;
        }
        private boolean mergeWithPrev(int i, String token, boolean keepSB) {
            if (i == 0) 
            {
                return (token.length() == 0);
            }
            Object prev = parts[i - 1];
            if (prev instanceof StringBuffer) {
                StringBuffer psb = (StringBuffer) prev;
                psb.append(token);
                if (!keepSB) {
                    parts[i - 1] = psb.toString();
                }
                return true;
            }
            if (token.length() == 0) {
                return true;  
            }
            if (prev instanceof CharSequence) {
                StringBuffer psb = new StringBuffer(prev.toString());
                psb.append(token);
                if (keepSB) {
                    parts[i - 1] = psb;
                } else {
                    parts[i - 1] = psb.toString();
                }
                return true;
            }
            return false;
        }
        public void trimText() {
            checkNotFrozen();
            int fillp = 0;
            int size = this.size;
            Object[] parts = this.parts;
            for (int i = 0; i < size; i++) {
                Object e = parts[i];
                if (e instanceof CharSequence) {
                    String tt = e.toString().trim();
                    if (tt.length() == 0) {
                        continue;
                    }
                    e = tt;
                }
                parts[fillp++] = e;
            }
            while (size > fillp) {
                parts[--size] = null;
            }
            this.size = fillp;
        }
        public int addContent(int i, Object e) {
            if (e == null) {
                return 0;
            } else if (e instanceof TokenList) {
                return addAll(i, (Collection) e);
            } else if (e instanceof Element
                    && ((Element) e).isAnonymous()) {
                return addAll(i, (Element) e);
            } else {
                add(i, e);
                return 1;
            }
        }
        public int addContent(Object e) {
            return addContent(size, e);
        }
        public Object[] toArray() {
            Object[] result = new Object[size];
            System.arraycopy(parts, 0, result, 0, size);
            return result;
        }
        public Element copyContentOnly() {
            Element content = new Element(size);
            System.arraycopy(parts, 0, content.parts, 0, size);
            content.size = size;
            return content;
        }
        public void sort(Comparator<Object> c) {
            Arrays.sort(parts, 0, size, c);
        }
        public void sort() {
            sort(CONTENT_ORDER);
        }
        public void reverse() {
            for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
                Object p = parts[i];
                parts[i] = parts[j];
                parts[j] = p;
            }
        }
        public void shuffle() {
            Collections.shuffle(this.asList());
        }
        public void shuffle(Random rnd) {
            Collections.shuffle(this.asList(), rnd);
        }
        public void rotate(int dist) {
            Collections.rotate(this.asList(), dist);
        }
        public Object min(Comparator<Object> c) {
            return Collections.min(this.asList(), c);
        }
        public Object min() {
            return min(CONTENT_ORDER);
        }
        public Object max(Comparator<Object> c) {
            return Collections.max(this.asList(), c);
        }
        public Object max() {
            return max(CONTENT_ORDER);
        }
        public int addAll(int i, Collection c) {
            if (c instanceof LView) {
                return addAll(i, ((LView) c).asElement());
            } else {
                int csize = c.size();
                if (csize == 0) {
                    return 0;
                }
                openOrExpand(i, csize);
                int fill = i;
                for (Object part : c) {
                    parts[fill++] = part;
                }
                return csize;
            }
        }
        public int addAll(int i, Element e) {
            int esize = e.size;
            if (esize == 0) {
                return 0;
            }
            openOrExpand(i, esize);
            System.arraycopy(e.parts, 0, parts, i, esize);
            return esize;
        }
        public int addAll(Collection c) {
            return addAll(size, c);
        }
        public int addAll(Element e) {
            return addAll(size, e);
        }
        public int addAllAttrsFrom(Element e) {
            int added = 0;
            for (int k = 0; e.containsAttr(k); k++) {
                String old = setAttr(e.getAttrName(k), e.getAttr(k));
                if (old == null) {
                    added += 1;
                }
            }
            return added;
        }
        public boolean matches(Filter f) {
            return f.filter(this) != null;
        }
        public Object find(Filter f) {
            return findOrRemove(f, 0, false);
        }
        public Object find(Filter f, int fromIndex) {
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            return findOrRemove(f, fromIndex, false);
        }
        public Object findLast(Filter f) {
            return findOrRemoveLast(f, size - 1, false);
        }
        public Object findLast(Filter f, int fromIndex) {
            if (fromIndex >= size) {
                fromIndex = size - 1;
            }
            return findOrRemoveLast(f, fromIndex, false);
        }
        public Element findAll(Filter f) {
            Element result = new Element();
            findOrRemoveAll(f, false, 0, size, result.asList(), false);
            return result;
        }
        public Element findAll(Filter f, int fromIndex, int toIndex) {
            Element result = new Element(name);
            findOrRemoveAll(f, false, fromIndex, toIndex, result.asList(), false);
            return result;
        }
        public int findAll(Filter f, Collection<Object> sink) {
            return findOrRemoveAll(f, false, 0, size, sink, false);
        }
        public int findAll(Filter f, int fromIndex, int toIndex, Collection<Object> sink) {
            return findOrRemoveAll(f, false, fromIndex, toIndex, sink, false);
        }
        private Object findOrRemove(Filter f, int fromIndex, boolean remove) {
            for (int i = fromIndex; i < size; i++) {
                Object x = f.filter(parts[i]);
                if (x != null) {
                    if (remove) {
                        close(i, 1);
                    }
                    return x;
                }
            }
            return null;
        }
        private Object findOrRemoveLast(Filter f, int fromIndex, boolean remove) {
            for (int i = fromIndex; i >= 0; i--) {
                Object x = f.filter(parts[i]);
                if (x != null) {
                    if (remove) {
                        close(i, 1);
                    }
                    return x;
                }
            }
            return null;
        }
        private int findOrRemoveAll(Filter f, boolean fInvert,
                int fromIndex, int toIndex,
                Collection<Object> sink, boolean remove) {
            if (fromIndex < 0) {
                badIndex(fromIndex);
            }
            if (toIndex > size) {
                badIndex(toIndex);
            }
            int found = 0;
            for (int i = fromIndex; i < toIndex; i++) {
                Object p = parts[i];
                Object x = f.filter(p);
                if (fInvert ? (x == null) : (x != null)) {
                    if (remove) {
                        close(i--, 1);
                        toIndex--;
                    }
                    found += XMLKit.addContent(fInvert ? p : x, sink);
                }
            }
            return found;
        }
        public void replaceAll(Filter f) {
            XMLKit.replaceAll(f, this.asList());
        }
        public void replaceAll(Filter f, int fromIndex, int toIndex) {
            XMLKit.replaceAll(f, this.asList().subList(fromIndex, toIndex));
        }
        public Element findAllInTree(Filter f) {
            Element result = new Element();
            findAllInTree(f, result.asList());
            return result;
        }
        public int findAllInTree(Filter f, Collection<Object> sink) {
            int found = 0;
            int size = this.size;  
            for (int i = 0; i < size; i++) {
                Object p = parts[i];
                Object x = f.filter(p);
                if (x != null) {
                    found += XMLKit.addContent(x, sink);
                } else if (p instanceof Element) {
                    found += ((Element) p).findAllInTree(f, sink);
                }
            }
            return found;
        }
        public int countAllInTree(Filter f) {
            return findAllInTree(f, null);
        }
        public int removeAllInTree(Filter f, Collection<Object> sink) {
            if (sink == null) {
                sink = newCounterColl();
            }
            replaceAll(replaceInTree(and(content(f, sink), emptyFilter())));
            return sink.size();
        }
        public Element removeAllInTree(Filter f) {
            Element result = new Element();
            removeAllInTree(f, result.asList());
            return result;
        }
        public int retainAllInTree(Filter f, Collection<Object> sink) {
            return removeAllInTree(not(f), sink);
        }
        public Element retainAllInTree(Filter f) {
            Element result = new Element();
            retainAllInTree(f, result.asList());
            return result;
        }
        public void replaceAllInTree(Filter f) {
            replaceAll(replaceInTree(f));
        }
        public void checkPartsOnly(Class<?> elementClass) {
            for (int i = 0; i < size; i++) {
                elementClass.cast(parts[i]).getClass();
            }
        }
        public boolean isPartsOnly(Class<?> elementClass) {
            for (int i = 0; i < size; i++) {
                if (!elementClass.isInstance(parts[i])) {
                    return false;
                }
            }
            return true;
        }
        public <T> Iterable<T> partsOnly(Class<T> elementClass) {
            checkPartsOnly(elementClass);
            return (Iterable<T>) (Iterable) this;
        }
        public Iterable<Element> elements() {
            return partsOnly(Element.class);
        }
        public Element findElement() {
            return (Element) find(elementFilter());
        }
        public Element findAllElements() {
            return findAll(elementFilter());
        }
        public Element removeElement() {
            return (Element) remove(elementFilter());
        }
        public Element removeAllElements() {
            return (Element) removeAll(elementFilter());
        }
        public Element findElement(String name) {
            return (Element) find(elementFilter(name));
        }
        public Element removeElement(String name) {
            return (Element) remove(elementFilter(name));
        }
        public Element findWithAttr(String key) {
            return (Element) find(attrFilter(name));
        }
        public Element findWithAttr(String key, String value) {
            return (Element) find(attrFilter(name, value));
        }
        public Element removeWithAttr(String key) {
            return (Element) remove(attrFilter(name));
        }
        public Element removeWithAttr(String key, String value) {
            return (Element) remove(attrFilter(name, value));
        }
        public Element findAllElements(String name) {
            return findAll(elementFilter(name));
        }
        public Element removeAllElements(String name) {
            return removeAll(elementFilter(name));
        }
        public Element retainAllElements(String name) {
            return retainAll(elementFilter(name));
        }
        public Element findAllWithAttr(String key) {
            return findAll(attrFilter(key));
        }
        public Element removeAllWithAttr(String key) {
            return removeAll(attrFilter(key));
        }
        public Element retainAllWithAttr(String key) {
            return retainAll(attrFilter(key));
        }
        public Element findAllWithAttr(String key, String value) {
            return findAll(attrFilter(key, value));
        }
        public Element removeAllWithAttr(String key, String value) {
            return removeAll(attrFilter(key, value));
        }
        public Element retainAllWithAttr(String key, String value) {
            return retainAll(attrFilter(key, value));
        }
        public int countAll(Filter f) {
            return findAll(f, null);
        }
        public int countAllElements() {
            return countAll(elementFilter());
        }
        public int countAllElements(String name) {
            return countAll(elementFilter(name));
        }
        public int countAllWithAttr(String key) {
            return countAll(attrFilter(name));
        }
        public int countAllWithAttr(String key, String value) {
            return countAll(attrFilter(key, value));
        }
        public int indexOf(Object e) {
            for (int i = 0; i < size; i++) {
                if (e.equals(parts[i])) {
                    return i;
                }
            }
            return -1;
        }
        public int lastIndexOf(Object e) {
            for (int i = size - 1; i >= 0; i--) {
                if (e.equals(parts[i])) {
                    return i;
                }
            }
            return -1;
        }
        public int indexOf(Filter f) {
            return indexOf(f, 0);
        }
        public int indexOf(Filter f, int fromIndex) {
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            for (int i = fromIndex; i < size; i++) {
                Object x = f.filter(parts[i]);
                if (x != null) {
                    return i;
                }
            }
            return -1;
        }
        public int lastIndexOf(Filter f) {
            return lastIndexOf(f, size - 1);
        }
        public int lastIndexOf(Filter f, int fromIndex) {
            if (fromIndex >= size) {
                fromIndex = size - 1;
            }
            for (int i = fromIndex; i >= 0; i--) {
                Object x = f.filter(parts[i]);
                if (x != null) {
                    return i;
                }
            }
            return -1;
        }
        public boolean contains(Object e) {
            return indexOf(e) >= 0;
        }
        private int findOrCreateAttr(String key, boolean create) {
            key.toString();  
            int attrBase = parts.length;
            for (int i = parts.length - 2; i >= size; i -= 2) {
                String akey = (String) parts[i + 0];
                if (akey == null) {
                    if (!create) {
                        return -1;
                    }
                    if (i == size) {
                        break;  
                    }
                    parts[i + 0] = key;
                    return i;
                }
                attrBase = i;
                if (akey.equals(key)) {
                    return i;
                }
            }
            if (!create) {
                return -1;
            }
            assert (!isFrozen());
            int alen = parts.length - attrBase;
            expand(size, 2); 
            assert (parts[size + 0] == null && parts[size + 1] == null);
            alen += 2;
            int i = parts.length - alen;
            parts[i + 0] = key;
            return i;
        }
        public int attrSize() {
            return attrLength() >>> 1;
        }
        public int indexOfAttr(String key) {
            return findOrCreateAttr(key, false);
        }
        public boolean containsAttr(String key) {
            return indexOfAttr(key) >= 0;
        }
        public String getAttr(String key) {
            return getAttr(key, null);
        }
        public String getAttr(String key, String dflt) {
            int i = findOrCreateAttr(key, false);
            return (i < 0) ? dflt : (String) parts[i + 1];
        }
        public TokenList getAttrList(String key) {
            return convertToList(getAttr(key));
        }
        public Number getAttrNumber(String key) {
            return convertToNumber(getAttr(key));
        }
        public long getAttrLong(String key) {
            return getAttrLong(key, 0);
        }
        public double getAttrDouble(String key) {
            return getAttrDouble(key, 0.0);
        }
        public long getAttrLong(String key, long dflt) {
            return convertToLong(getAttr(key), dflt);
        }
        public double getAttrDouble(String key, double dflt) {
            return convertToDouble(getAttr(key), dflt);
        }
        int indexAttr(int k) {
            int i = parts.length - (k * 2) - 2;
            if (i < size || parts[i] == null) {
                return -2;  
            }
            return i;
        }
        public boolean containsAttr(int k) {
            return indexAttr(k) >= 0;
        }
        public String getAttr(int k) {
            return (String) parts[indexAttr(k) + 1];
        }
        public String getAttrName(int k) {
            return (String) parts[indexAttr(k) + 0];
        }
        public Iterable<String> attrNames() {
            return new Iterable<String>() {
                public Iterator<String> iterator() {
                    return new ANItr();
                }
            };
        }
        class ANItr implements Iterator<String> {
            boolean lastRet;
            int cursor = -2;  
            public boolean hasNext() {
                int i = cursor + parts.length;
                return i >= size && parts[i] == null;
            }
            public String next() {
                int i = cursor + parts.length;
                Object x;
                if (i < size || (x = parts[i]) == null) {
                    nsee();
                    return null;
                }
                cursor -= 2;
                lastRet = true;
                return (String) x;
            }
            public void remove() {
                if (!lastRet) {
                    throw new IllegalStateException();
                }
                Element.this.removeAttr((-4 - cursor) / 2);
                cursor += 2;
                lastRet = false;
            }
            Exception nsee() {
                throw new NoSuchElementException("attribute " + (-2 - cursor) / 2);
            }
        }
        public Element copyAttrsOnly() {
            int alen = attrLength();
            Element attrs = new Element(alen);
            Object[] attrParts = attrs.parts;
            assert (attrParts.length == NEED_SLOP + alen);
            System.arraycopy(parts, parts.length - alen,
                    attrParts, NEED_SLOP,
                    alen);
            return attrs;
        }
        public Element getAttrs() {
            int asize = attrSize();
            Element attrs = new Element(ANON_NAME, asize, NEED_SLOP + asize);
            for (int i = 0; i < asize; i++) {
                Element attr = new Element(getAttrName(i), 1, NEED_SLOP + 1);
                attr.setRaw(0, getAttr(i));
                attrs.setRaw(i, attr);
            }
            return attrs;
        }
        public void setAttrs(Element attrs) {
            int alen = attrLength();
            clearParts(parts.length - alen, alen);
            if (!hasNulls(NEED_SLOP + attrs.size * 2)) {
                expand(size, attrs.size * 2);
            }
            addAttrs(attrs);
        }
        public void addAttrs(Element attrs) {
            for (int i = 0; i < attrs.size; i++) {
                Element attr = (Element) attrs.get(i);
                setAttr(attr.name, attr.getText().toString());
            }
        }
        public void removeAttr(int i) {
            checkNotFrozen();
            while ((i -= 2) >= size) {
                Object k = parts[i + 0];
                Object v = parts[i + 1];
                if (k == null) {
                    break;
                }
                parts[i + 2] = k;
                parts[i + 3] = v;
            }
            parts[i + 2] = null;
            parts[i + 3] = null;
        }
        public void clearAttrs() {
            if (parts.length == 0 || parts[parts.length - 1] == null) {
                return;  
            }
            checkNotFrozen();
            if (size == 0) {
                parts = noPartsNotFrozen;
                return;
            }
            for (int i = parts.length - 1; parts[i] != null; i--) {
                assert (i >= size);
                parts[i] = null;
            }
        }
        public String setAttr(String key, String value) {
            String old;
            if (value == null) {
                int i = findOrCreateAttr(key, false);
                if (i >= 0) {
                    old = (String) parts[i + 1];
                    removeAttr(i);
                } else {
                    old = null;
                }
            } else {
                checkNotFrozen();
                int i = findOrCreateAttr(key, true);
                old = (String) parts[i + 1];
                parts[i + 1] = value;
            }
            return old;
        }
        public String setAttrList(String key, List<String> l) {
            if (l == null) {
                return setAttr(key, null);
            }
            if (!(l instanceof TokenList)) {
                l = new TokenList(l);
            }
            return setAttr(key, l.toString());
        }
        public String setAttrNumber(String key, Number n) {
            return setAttr(key, (n == null) ? null : n.toString());
        }
        public String setAttrLong(String key, long n) {
            return setAttr(key, (n == 0) ? null : String.valueOf(n));
        }
        public String setAttrDouble(String key, double n) {
            return setAttr(key, (n == 0) ? null : String.valueOf(n));
        }
        public String setAttr(int k, String value) {
            int i = indexAttr(k);
            String old = (String) parts[i + 1];
            if (value == null) {
                removeAttr(i);
            } else {
                checkNotFrozen();
                parts[i + 1] = value;
            }
            return old;
        }
        int attrLength() {
            return parts.length - attrBase();
        }
        public boolean equalAttrs(Element that) {
            int alen = this.attrLength();
            if (alen != that.attrLength()) {
                return false;
            }
            if (alen == 0) {
                return true;
            }
            return compareAttrs(alen, that, alen, false) == 0;
        }
        private int compareAttrs(int thisAlen,
                Element that, int thatAlen,
                boolean fullCompare) {
            Object[] thisParts = this.parts;
            Object[] thatParts = that.parts;
            int thisBase = thisParts.length - thisAlen;
            int thatBase = thatParts.length - thatAlen;
            int firstI = 0;
            int firstJ = 0;
            int lastJ = thatAlen - 2;
            String firstKey = null;
            int firstKeyValCmp = 0;
            int foundKeys = 0;
            for (int i = 0; i < thisAlen; i += 2) {
                String key = (String) thisParts[thisBase + i + 0];
                String val = (String) thisParts[thisBase + i + 1];
                String otherVal = null;
                for (int j = firstJ; j <= lastJ; j += 2) {
                    if (key.equals(thatParts[thatBase + j + 0])) {
                        foundKeys += 1;
                        otherVal = (String) thatParts[thatBase + j + 1];
                        if (j == lastJ) {
                            lastJ -= 2;
                        } else if (j == firstJ) {
                            firstJ += 2;
                        }
                        if (i == firstI) {
                            firstI += 2;
                        }
                        break;
                    }
                }
                int valCmp;
                if (otherVal != null) {
                    if (!fullCompare) {
                        if (!val.equals(otherVal)) {
                            return 1 - 0; 
                        }
                        continue;
                    }
                    valCmp = val.compareTo(otherVal);
                } else {
                    valCmp = 0 - 1;
                }
                if (valCmp != 0) {
                    if (firstKey == null
                            || firstKey.compareTo(key) > 0) {
                        firstKey = key;
                        firstKeyValCmp = valCmp;
                    }
                }
            }
            if (foundKeys == thatAlen / 2) {
                return firstKeyValCmp;
            }
            findMissingKey:
            for (int j = firstJ; j <= lastJ; j += 2) {
                String otherKey = (String) thatParts[thatBase + j + 0];
                if (firstKey == null
                        || firstKey.compareTo(otherKey) > 0) {
                    for (int i = firstI; i < thisAlen; i += 2) {
                        if (otherKey.equals(thisParts[thisBase + i + 0])) {
                            continue findMissingKey;
                        }
                    }
                    return 1 - 0;
                }
            }
            return firstKeyValCmp;
        }
        int attrBase() {
            int kmin = 0;
            int kmax = (parts.length - size) >>> 1;
            int abase = parts.length - (kmax * 2);
            while (kmin != kmax) {
                int kmid = kmin + ((kmax - kmin) >>> 1);
                if (parts[abase + (kmid * 2)] == null) {
                    kmin = kmid + 1;
                } else {
                    kmax = kmid;
                }
                assert (kmin <= kmax);
            }
            return abase + (kmax * 2);
        }
        public void sortAttrs() {
            checkNotFrozen();
            int abase = attrBase();
            int alen = parts.length - abase;
            String[] buf = new String[alen];
            for (int k = 0; k < alen / 2; k++) {
                String akey = (String) parts[abase + (k * 2) + 0];
                buf[k] = akey;
            }
            Arrays.sort(buf, 0, alen / 2);
            for (int k = 0; k < alen / 2; k++) {
                String akey = buf[k];
                buf[k + alen / 2] = getAttr(akey);
            }
            int fillp = parts.length;
            for (int k = 0; k < alen / 2; k++) {
                String akey = buf[k];
                String aval = buf[k + alen / 2];
                fillp -= 2;
                parts[fillp + 0] = akey;
                parts[fillp + 1] = aval;
            }
            assert (fillp == abase);
        }
        public void tokenize(String delims, boolean returnDelims) {
            checkNotFrozen();
            if (delims == null) {
                delims = WHITESPACE_CHARS;  
            }
            for (int i = 0; i < size; i++) {
                if (!(parts[i] instanceof CharSequence)) {
                    continue;
                }
                int osize = size;
                String str = parts[i].toString();
                StringTokenizer st = new StringTokenizer(str, delims, returnDelims);
                int nstrs = st.countTokens();
                switch (nstrs) {
                    case 0:
                        close(i--, 1);
                        break;
                    case 1:
                        parts[i] = st.nextToken();
                        break;
                    default:
                        openOrExpand(i + 1, nstrs - 1);
                        for (int j = 0; j < nstrs; j++) {
                            parts[i + j] = st.nextToken();
                        }
                        i += nstrs - 1;
                        break;
                }
            }
        }
        public void tokenize(String delims) {
            tokenize(delims, false);
        }
        public void tokenize() {
            tokenize(null, false);
        }
        class LView extends AbstractList<Object> {
            Element asElement() {
                return Element.this;
            }
            public int size() {
                return Element.this.size();
            }
            public Object get(int i) {
                return Element.this.get(i);
            }
            @Override
            public boolean contains(Object e) {
                return Element.this.contains(e);
            }
            @Override
            public Object[] toArray() {
                return Element.this.toArray();
            }
            @Override
            public int indexOf(Object e) {
                return Element.this.indexOf(e);
            }
            @Override
            public int lastIndexOf(Object e) {
                return Element.this.lastIndexOf(e);
            }
            @Override
            public void add(int i, Object e) {
                ++modCount;
                Element.this.add(i, e);
            }
            @Override
            public boolean addAll(int i, Collection<? extends Object> c) {
                ++modCount;
                return Element.this.addAll(i, c) > 0;
            }
            @Override
            public boolean addAll(Collection<? extends Object> c) {
                ++modCount;
                return Element.this.addAll(c) > 0;
            }
            @Override
            public Object remove(int i) {
                ++modCount;
                return Element.this.remove(i);
            }
            @Override
            public Object set(int i, Object e) {
                ++modCount;
                return Element.this.set(i, e);
            }
            @Override
            public void clear() {
                ++modCount;
                Element.this.clear();
            }
        }
        public List<Object> asList() {
            return new LView();
        }
        public ListIterator<Object> iterator() {
            return new Itr();
        }
        class Itr implements ListIterator<Object> {
            int lastRet = -1;
            int cursor = 0;
            public boolean hasNext() {
                return cursor < size;
            }
            public boolean hasPrevious() {
                return cursor > 0 && cursor <= size;
            }
            public Object next() {
                if (!hasNext()) {
                    nsee();
                }
                return parts[lastRet = cursor++];
            }
            public Object previous() {
                if (!hasPrevious()) {
                    nsee();
                }
                return parts[--cursor];
            }
            public int nextIndex() {
                return cursor;
            }
            public int previousIndex() {
                return cursor - 1;
            }
            public void set(Object x) {
                parts[lastRet] = x;
            }
            public void add(Object x) {
                lastRet = -1;
                Element.this.add(cursor++, x);
            }
            public void remove() {
                if (lastRet < 0) {
                    throw new IllegalStateException();
                }
                Element.this.remove(lastRet);
                if (lastRet < cursor) {
                    --cursor;
                }
                lastRet = -1;
            }
            void nsee() {
                throw new NoSuchElementException("element " + cursor);
            }
        }
        public PrintWriter asWriter() {
            return new ElemW();
        }
        class ElemW extends PrintWriter {
            ElemW() {
                super(new StringWriter());
            }
            final StringBuffer buf = ((StringWriter) out).getBuffer();
            {
                lock = buf;
            }  
            @Override
            public void println() {
                synchronized (buf) {
                    ensureCursor();
                    super.println();
                }
            }
            @Override
            public void write(int ch) {
                synchronized (buf) {
                    ensureCursor();
                    super.write(ch);
                }
            }
            @Override
            public void write(char buf[], int off, int len) {
                synchronized (buf) {
                    ensureCursor();
                    super.write(buf, off, len);
                }
            }
            @Override
            public void write(String s, int off, int len) {
                synchronized (buf) {
                    ensureCursor();
                    super.write(s, off, len);
                }
            }
            @Override
            public void write(String s) {
                synchronized (buf) {
                    ensureCursor();
                    super.write(s);
                }
            }
            private void ensureCursor() {
                checkNotFrozen();
                if (getLast() != buf) {
                    int pos = indexOf(buf);
                    if (pos >= 0) {
                        setRaw(pos, buf.toString());
                    }
                    add(buf);
                }
            }
        }
        public Map<String, String> asAttrMap() {
            class Entry implements Map.Entry<String, String> {
                final int k;
                Entry(int k) {
                    this.k = k;
                    assert (((String) getKey()).toString() != null);  
                }
                public String getKey() {
                    return Element.this.getAttrName(k);
                }
                public String getValue() {
                    return Element.this.getAttr(k);
                }
                public String setValue(String v) {
                    return Element.this.setAttr(k, v.toString());
                }
                @Override
                public boolean equals(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry that = (Map.Entry) o;
                    return (this.getKey().equals(that.getKey())
                            && this.getValue().equals(that.getValue()));
                }
                @Override
                public int hashCode() {
                    return getKey().hashCode() ^ getValue().hashCode();
                }
            }
            class EIter implements Iterator<Map.Entry<String, String>> {
                int k = 0;  
                public boolean hasNext() {
                    return Element.this.containsAttr(k);
                }
                public Map.Entry<String, String> next() {
                    return new Entry(k++);
                }
                public void remove() {
                    Element.this.removeAttr(--k);
                }
            }
            class ESet extends AbstractSet<Map.Entry<String, String>> {
                public int size() {
                    return Element.this.attrSize();
                }
                public Iterator<Map.Entry<String, String>> iterator() {
                    return new EIter();
                }
                @Override
                public void clear() {
                    Element.this.clearAttrs();
                }
            }
            class AView extends AbstractMap<String, String> {
                private transient Set<Map.Entry<String, String>> eSet;
                public Set<Map.Entry<String, String>> entrySet() {
                    if (eSet == null) {
                        eSet = new ESet();
                    }
                    return eSet;
                }
                @Override
                public int size() {
                    return Element.this.attrSize();
                }
                public boolean containsKey(String k) {
                    return Element.this.containsAttr(k);
                }
                public String get(String k) {
                    return Element.this.getAttr(k);
                }
                @Override
                public String put(String k, String v) {
                    return Element.this.setAttr(k, v.toString());
                }
                public String remove(String k) {
                    return Element.this.setAttr(k, null);
                }
            }
            return new AView();
        }
        public int getExtraCapacity() {
            int abase = attrBase();
            return Math.max(0, abase - size - NEED_SLOP);
        }
        public void ensureExtraCapacity(int cap) {
            if (cap == 0 || hasNulls(cap + NEED_SLOP)) {
                return;
            }
            setExtraCapacity(cap);
        }
        public void trimToSize() {
            if (isFrozen()) {
                return;
            }
            setExtraCapacity(0);
        }
        public void setExtraCapacity(int cap) {
            checkNotFrozen();
            int abase = attrBase();
            int alen = parts.length - abase;  
            int nlen = size + cap + NEED_SLOP + alen;
            if (nlen != parts.length) {
                Object[] nparts = new Object[nlen];
                System.arraycopy(parts, abase, nparts, nlen - alen, alen);
                System.arraycopy(parts, 0, nparts, 0, size);
                parts = nparts;
            }
            assert (cap == getExtraCapacity());
        }
        boolean hasNulls(int len) {
            if (len == 0) {
                return true;
            }
            int lastNull = size + len - 1;
            if (lastNull >= parts.length) {
                return false;
            }
            return (parts[lastNull] == null);
        }
        void open(int pos, int len) {
            assert (pos < size);
            assert (hasNulls(len + NEED_SLOP));
            checkNotFrozen();
            int nsize = size + len;
            int tlen = size - pos;
            System.arraycopy(parts, pos, parts, pos + len, tlen);
            size = nsize;
        }
        int expand(int pos, int len) {
            assert (pos <= size);
            assert (!hasNulls(NEED_SLOP + len));  
            checkNotFrozen();
            int nsize = size + len;  
            int tlen = size - pos;   
            int abase = attrBase();
            int alen = parts.length - abase;  
            int nlen = nsize + alen + NEED_SLOP;
            nlen += (nlen >>> 1);  
            Object[] nparts = new Object[nlen];
            System.arraycopy(parts, 0, nparts, 0, pos);
            System.arraycopy(parts, pos, nparts, pos + len, tlen);
            System.arraycopy(parts, abase, nparts, nlen - alen, alen);
            parts = nparts;
            return nsize;
        }
        boolean openOrExpand(int pos, int len) {
            if (pos < 0 || pos > size) {
                badIndex(pos);
            }
            if (hasNulls(len + NEED_SLOP)) {
                if (pos == size) {
                    size += len;
                } else {
                    open(pos, len);
                }
                return false;
            } else {
                size = expand(pos, len);
                return true;
            }
        }
        void close(int pos, int len) {
            assert (len > 0);
            assert ((size - pos) >= len);
            checkNotFrozen();
            int tlen = (size - pos) - len;   
            int nsize = size - len;
            System.arraycopy(parts, pos + len, parts, pos, tlen);
            clearParts(nsize, nsize + len);
            size = nsize;
            assert (hasNulls(len));
        }
        public void writeTo(Writer w) throws IOException {
            new Printer(w).print(this);
        }
        public void writePrettyTo(Writer w) throws IOException {
            prettyPrintTo(w, this);
        }
        public String prettyString() {
            StringWriter sw = new StringWriter();
            try {
                writePrettyTo(sw);
            } catch (IOException ee) {
                throw new Error(ee);  
            }
            return sw.toString();
        }
        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            try {
                writeTo(sw);
            } catch (IOException ee) {
                throw new Error(ee);  
            }
            return sw.toString();
        }
        public String dump() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(name).append("[").append(size).append("]");
            for (int i = 0; i < parts.length; i++) {
                Object p = parts[i];
                if (p == null) {
                    buf.append(" null");
                } else {
                    buf.append(" {");
                    String cname = p.getClass().getName();
                    cname = cname.substring(1 + cname.indexOf('/'));
                    cname = cname.substring(1 + cname.indexOf('$'));
                    cname = cname.substring(1 + cname.indexOf('#'));
                    if (!cname.equals("String")) {
                        buf.append(cname).append(":");
                    }
                    buf.append(p);
                    buf.append("}");
                }
            }
            return buf.append(">").toString();
        }
        public static java.lang.reflect.Method method(String name) {
            HashMap allM = allMethods;
            if (allM == null) {
                allM = makeAllMethods();
            }
            java.lang.reflect.Method res = (java.lang.reflect.Method) allMethods.get(name);
            if (res == null) {
                throw new IllegalArgumentException(name);
            }
            return res;
        }
        private static HashMap allMethods;
        private static synchronized HashMap makeAllMethods() {
            if (allMethods != null) {
                return allMethods;
            }
            java.lang.reflect.Method[] methods = Element.class.getMethods();
            HashMap<String, java.lang.reflect.Method> allM = new HashMap<String, java.lang.reflect.Method>(),
                    ambig = new HashMap<String, java.lang.reflect.Method>();
            for (int i = 0; i < methods.length; i++) {
                java.lang.reflect.Method m = methods[i];
                Class[] args = m.getParameterTypes();
                String name = m.getName();
                assert (java.lang.reflect.Modifier.isPublic(m.getModifiers()));
                if (name.startsWith("notify")) {
                    continue;
                }
                if (name.endsWith("Attr")
                        && args.length > 0 && args[0] == int.class) 
                {
                    continue;
                }
                if (name.endsWith("All")
                        && args.length > 1 && args[0] == Filter.class) 
                {
                    continue;
                }
                java.lang.reflect.Method pm = allM.put(name, m);
                if (pm != null) {
                    Class[] pargs = pm.getParameterTypes();
                    if (pargs.length > args.length) {
                        allM.put(name, pm);   
                    } else if (pargs.length == args.length) {
                        ambig.put(name, pm);  
                    }
                }
            }
            for (Map.Entry<String, java.lang.reflect.Method> e : ambig.entrySet()) {
                String name = e.getKey();
                java.lang.reflect.Method pm = e.getValue();
                java.lang.reflect.Method m = allM.get(name);
                Class[] args = m.getParameterTypes();
                Class[] pargs = pm.getParameterTypes();
                if (pargs.length == args.length) {
                    allM.put(name, null);  
                }
            }
            return allMethods = allM;
        }
    }
    static Object fixupString(Object part) {
        if (part instanceof CharSequence && !(part instanceof String)) {
            return part.toString();
        } else {
            return part;
        }
    }
    public static final class Special implements Comparable<Special> {
        String kind;
        Object value;
        public Special(String kind, Object value) {
            this.kind = kind;
            this.value = value;
        }
        public String getKind() {
            return kind;
        }
        public Object getValue() {
            return value;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Special)) {
                return false;
            }
            Special that = (Special) o;
            return this.kind.equals(that.kind) && this.value.equals(that.value);
        }
        @Override
        public int hashCode() {
            return kind.hashCode() * 65 + value.hashCode();
        }
        public int compareTo(Special that) {
            int r = this.kind.compareTo(that.kind);
            if (r != 0) {
                return r;
            }
            return ((Comparable) this.value).compareTo(that.value);
        }
        @Override
        public String toString() {
            int split = kind.indexOf(' ');
            String pref = kind.substring(0, split < 0 ? 0 : split);
            String post = kind.substring(split + 1);
            return pref + value + post;
        }
    }
    public static Comparator<Object> contentOrder() {
        return CONTENT_ORDER;
    }
    private static Comparator<Object> CONTENT_ORDER = new ContentComparator();
    private static class ContentComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            boolean cs1 = (o1 instanceof CharSequence);
            boolean cs2 = (o2 instanceof CharSequence);
            if (cs1 && cs2) {
                String s1 = (String) fixupString(o1);
                String s2 = (String) fixupString(o2);
                return s1.compareTo(s2);
            }
            if (cs1) {
                return 0 - 1;
            }
            if (cs2) {
                return 1 - 0;
            }
            boolean el1 = (o1 instanceof Element);
            boolean el2 = (o2 instanceof Element);
            if (el1 && el2) {
                return ((Element) o1).compareTo((Element) o2);
            }
            if (el1) {
                return 0 - 1;
            }
            if (el2) {
                return 1 - 0;
            }
            return ((Comparable) o1).compareTo(o2);
        }
    }
    public interface Filter {
        Object filter(Object value);
    }
    public static class ElementFilter implements Filter {
        public Element filter(Element elem) {
            return elem;  
        }
        public final Object filter(Object value) {
            if (!(value instanceof Element)) {
                return null;
            }
            return filter((Element) value);
        }
        @Override
        public String toString() {
            return "<ElementFilter name='*'/>";
        }
    }
    private static Filter elementFilter;
    public static Filter elementFilter() {
        return (elementFilter != null) ? elementFilter : (elementFilter = new ElementFilter());
    }
    public static Filter elementFilter(final String name) {
        name.toString();  
        return new ElementFilter() {
            @Override
            public Element filter(Element elem) {
                return name.equals(elem.name) ? elem : null;
            }
            @Override
            public String toString() {
                return "<ElementFilter name='" + name + "'/>";
            }
        };
    }
    public static Filter elementFilter(final Collection nameSet) {
        nameSet.getClass();  
        return new ElementFilter() {
            @Override
            public Element filter(Element elem) {
                return nameSet.contains(elem.name) ? elem : null;
            }
            @Override
            public String toString() {
                return "<ElementFilter name='" + nameSet + "'/>";
            }
        };
    }
    public static Filter elementFilter(String... nameSet) {
        Collection<String> ncoll = Arrays.asList(nameSet);
        if (nameSet.length > 10) {
            ncoll = new HashSet<String>(ncoll);
        }
        return elementFilter(ncoll);
    }
    public static class AttrFilter extends ElementFilter {
        protected final String attrName;
        public AttrFilter(String attrName) {
            this.attrName = attrName.toString();
        }
        public boolean test(String attrVal) {
            return attrVal != null;  
        }
        @Override
        public final Element filter(Element elem) {
            return test(elem.getAttr(attrName)) ? elem : null;
        }
        @Override
        public String toString() {
            return "<AttrFilter name='" + attrName + "' value='*'/>";
        }
    }
    public static Filter attrFilter(String attrName) {
        return new AttrFilter(attrName);
    }
    public static Filter attrFilter(String attrName, final String attrVal) {
        if (attrVal == null) {
            return not(attrFilter(attrName));
        }
        return new AttrFilter(attrName) {
            @Override
            public boolean test(String attrVal2) {
                return attrVal.equals(attrVal2);
            }
            @Override
            public String toString() {
                return "<AttrFilter name='" + attrName + "' value='" + attrVal + "'/>";
            }
        };
    }
    public static Filter attrFilter(Element matchThis, String attrName) {
        return attrFilter(attrName, matchThis.getAttr(attrName));
    }
    public static Filter classFilter(final Class clazz) {
        return new Filter() {
            public Object filter(Object value) {
                return clazz.isInstance(value) ? value : null;
            }
            @Override
            public String toString() {
                return "<ClassFilter class='" + clazz.getName() + "'/>";
            }
        };
    }
    private static Filter textFilter;
    public static Filter textFilter() {
        return (textFilter != null) ? textFilter : (textFilter = classFilter(CharSequence.class));
    }
    private static Filter specialFilter;
    public static Filter specialFilter() {
        return (specialFilter != null) ? specialFilter : (specialFilter = classFilter(Special.class));
    }
    private static Filter selfFilter;
    public static Filter selfFilter() {
        if (selfFilter != null) {
            return selfFilter;
        }
        return selfFilter = new Filter() {
            public Object filter(Object value) {
                return value;
            }
            @Override
            public String toString() {
                return "<Self/>";
            }
        };
    }
    public static Filter constantFilter(final Object value) {
        return new Filter() {
            public Object filter(Object ignore) {
                return value;
            }
            @Override
            public String toString() {
                return "<Constant>" + value + "</Constant>";
            }
        };
    }
    private static Filter nullFilter;
    public static Filter nullFilter() {
        return (nullFilter != null) ? nullFilter : (nullFilter = constantFilter(null));
    }
    private static Filter emptyFilter;
    public static Filter emptyFilter() {
        return (emptyFilter != null) ? emptyFilter : (emptyFilter = constantFilter(Element.EMPTY));
    }
    public static Filter not(final Filter f) {
        return new Filter() {
            public Object filter(Object value) {
                return f.filter(value) == null ? value : null;
            }
            @Override
            public String toString() {
                return "<Not>" + f + "</Not>";
            }
        };
    }
    public static Filter and(final Filter f0, final Filter f1) {
        return and(new Filter[]{f0, f1});
    }
    public static Filter and(final Filter... fs) {
        switch (fs.length) {
            case 0:
                return selfFilter();  
            case 1:
                return fs[0];
        }
        return new Filter() {
            public Object filter(Object value) {
                Object res = fs[0].filter(value);
                if (res != null) {
                    res = fs[1].filter(value);
                    for (int i = 2; res != null && i < fs.length; i++) {
                        res = fs[i].filter(value);
                    }
                }
                return res;
            }
            @Override
            public String toString() {
                return opToString("<And>", fs, "</And>");
            }
        };
    }
    public static Filter or(final Filter f0, final Filter f1) {
        return or(new Filter[]{f0, f1});
    }
    public static Filter or(final Filter... fs) {
        switch (fs.length) {
            case 0:
                return nullFilter();
            case 1:
                return fs[0];
        }
        return new Filter() {
            public Object filter(Object value) {
                Object res = fs[0].filter(value);
                if (res == null) {
                    res = fs[1].filter(value);
                    for (int i = 2; res == null && i < fs.length; i++) {
                        res = fs[i].filter(value);
                    }
                }
                return res;
            }
            @Override
            public String toString() {
                return opToString("<Or>", fs, "</Or>");
            }
        };
    }
    public static Filter stack(final Filter f0, final Filter f1) {
        return stack(new Filter[]{f0, f1});
    }
    public static Filter stack(final Filter... fs) {
        switch (fs.length) {
            case 0:
                return nullFilter();
            case 1:
                return fs[0];
        }
        return new Filter() {
            public Object filter(Object value) {
                Object res = fs[0].filter(value);
                if (res != null) {
                    res = fs[1].filter(res);
                    for (int i = 2; res != null && i < fs.length; i++) {
                        res = fs[i].filter(res);
                    }
                }
                return res;
            }
            @Override
            public String toString() {
                return opToString("<Stack>", fs, "</Stack>");
            }
        };
    }
    public static Filter content(final Filter f, final Collection<Object> sink) {
        return new Filter() {
            public Object filter(Object value) {
                Object res = f.filter(value);
                addContent(res, sink);
                return res;
            }
            @Override
            public String toString() {
                return opToString("<addContent>", new Object[]{f, sink},
                        "</addContent>");
            }
        };
    }
    public static Filter findInTree(Filter f, Collection<Object> sink) {
        if (sink != null) {
            f = content(f, sink);
        }
        return findInTree(f);
    }
    public static Filter findInTree(final Filter f) {
        return new Filter() {
            public Object filter(Object value) {
                Object res = f.filter(value);
                if (res != null) {
                    return res;
                }
                if (value instanceof Element) {
                    return ((Element) value).find(this);
                }
                return null;
            }
            @Override
            public String toString() {
                return opToString("<FindInTree>", new Object[]{f},
                        "</FindInTree>");
            }
        };
    }
    public static Filter replaceInTree(final Filter f, final Filter g) {
        return new Filter() {
            public Object filter(Object value) {
                Object res = (f == null) ? null : f.filter(value);
                if (res != null) {
                    return res;
                }
                if (value instanceof Element) {
                    ((Element) value).replaceAll(this);
                    if (g != null) {
                        res = g.filter(value);
                    }
                }
                return res;  
            }
            @Override
            public String toString() {
                return opToString("<ReplaceInTree>",
                        new Object[]{f, g},
                        "</ReplaceInTree>");
            }
        };
    }
    public static Filter replaceInTree(Filter f) {
        f.getClass(); 
        return replaceInTree(f, null);
    }
    public static Filter methodFilter(java.lang.reflect.Method m, Object[] extraArgs,
            Object falseResult) {
        return methodFilter(m, false, extraArgs, falseResult);
    }
    public static Filter methodFilter(java.lang.reflect.Method m,
            Object[] args) {
        return methodFilter(m, args, null);
    }
    public static Filter methodFilter(java.lang.reflect.Method m) {
        return methodFilter(m, null, null);
    }
    public static Filter testMethodFilter(java.lang.reflect.Method m, Object[] extraArgs,
            Object falseResult) {
        return methodFilter(m, true, extraArgs, falseResult);
    }
    public static Filter testMethodFilter(java.lang.reflect.Method m, Object[] extraArgs) {
        return methodFilter(m, true, extraArgs, zeroArgs.get(m.getReturnType()));
    }
    public static Filter testMethodFilter(java.lang.reflect.Method m) {
        return methodFilter(m, true, null, zeroArgs.get(m.getReturnType()));
    }
    private static Filter methodFilter(final java.lang.reflect.Method m,
            final boolean isTest,
            Object[] extraArgs, final Object falseResult) {
        Class[] params = m.getParameterTypes();
        final boolean isStatic = java.lang.reflect.Modifier.isStatic(m.getModifiers());
        int insertLen = (isStatic ? 1 : 0);
        if (insertLen + (extraArgs == null ? 0 : extraArgs.length) > params.length) {
            throw new IllegalArgumentException("too many arguments");
        }
        final Object[] args = (params.length == insertLen) ? null
                : new Object[params.length];
        final Class valueType = !isStatic ? m.getDeclaringClass() : params[0];
        if (valueType.isPrimitive()) {
            throw new IllegalArgumentException("filtered value must be reference type");
        }
        int fillp = insertLen;
        if (extraArgs != null) {
            for (int i = 0; i < extraArgs.length; i++) {
                args[fillp++] = extraArgs[i];
            }
        }
        if (args != null) {
            while (fillp < args.length) {
                Class param = params[fillp];
                args[fillp++] = param.isPrimitive() ? zeroArgs.get(param) : null;
            }
        }
        final Thread curt = Thread.currentThread();
        class MFilt implements Filter {
            public Object filter(Object value) {
                if (!valueType.isInstance(value)) {
                    return null;  
                }
                Object[] args1 = args;
                if (isStatic) {
                    if (args1 == null) {
                        args1 = new Object[1];
                    } else if (curt != Thread.currentThread()) 
                    {
                        args1 = (Object[]) args1.clone();
                    }
                    args1[0] = value;
                }
                Object res;
                try {
                    res = m.invoke(value, args1);
                } catch (java.lang.reflect.InvocationTargetException te) {
                    Throwable ee = te.getCause();
                    if (ee instanceof RuntimeException) {
                        throw (RuntimeException) ee;
                    }
                    if (ee instanceof Error) {
                        throw (Error) ee;
                    }
                    throw new RuntimeException("throw in filter", ee);
                } catch (IllegalAccessException ee) {
                    throw new RuntimeException("access error in filter", ee);
                }
                if (res == null) {
                    if (!isTest && m.getReturnType() == Void.TYPE) {
                        res = value;
                    }
                } else {
                    if (falseResult != null && falseResult.equals(res)) {
                        res = null;
                    } else if (isTest) {
                        res = value;
                    }
                }
                return res;
            }
            @Override
            public String toString() {
                return "<Method>" + m + "</Method>";
            }
        }
        return new MFilt();
    }
    private static HashMap<Class, Object> zeroArgs = new HashMap<Class, Object>();
    static {
        zeroArgs.put(Boolean.TYPE, Boolean.FALSE);
        zeroArgs.put(Character.TYPE, new Character((char) 0));
        zeroArgs.put(Byte.TYPE, new Byte((byte) 0));
        zeroArgs.put(Short.TYPE, new Short((short) 0));
        zeroArgs.put(Integer.TYPE, new Integer(0));
        zeroArgs.put(Float.TYPE, new Float(0));
        zeroArgs.put(Long.TYPE, new Long(0));
        zeroArgs.put(Double.TYPE, new Double(0));
    }
    private static String opToString(String s1, Object[] s2, String s3) {
        StringBuilder buf = new StringBuilder(s1);
        for (int i = 0; i < s2.length; i++) {
            if (s2[i] != null) {
                buf.append(s2[i]);
            }
        }
        buf.append(s3);
        return buf.toString();
    }
    public static void replaceAll(Filter f, List<Object> target) {
        for (ListIterator<Object> i = target.listIterator(); i.hasNext();) {
            Object x = i.next();
            Object fx = f.filter(x);
            if (fx == null) {
            } else if (fx instanceof TokenList) {
                TokenList tl = (TokenList) fx;
                if (tl.size() == 1) {
                    i.set(tl);
                } else {
                    i.remove();
                    for (String part : tl) {
                        i.add(part);
                    }
                }
            } else if (fx instanceof Element
                    && ((Element) fx).isAnonymous()) {
                Element anon = (Element) fx;
                if (anon.size() == 1) {
                    i.set(anon);
                } else {
                    i.remove();
                    for (Object part : anon) {
                        i.add(part);
                    }
                }
            } else if (x != fx) {
                i.set(fx);
            }
        }
    }
    public static int addContent(Object e, Collection<Object> sink) {
        if (e == null) {
            return 0;
        } else if (e instanceof TokenList) {
            TokenList tl = (TokenList) e;
            if (sink != null) {
                sink.addAll(tl);
            }
            return tl.size();
        } else if (e instanceof Element
                && ((Element) e).isAnonymous()) {
            Element anon = (Element) e;
            if (sink != null) {
                sink.addAll(anon.asList());
            }
            return anon.size();
        } else {
            if (sink != null) {
                sink.add(e);
            }
            return 1;
        }
    }
    static Collection<Object> newCounterColl() {
        return new AbstractCollection<Object>() {
            int size;
            public int size() {
                return size;
            }
            @Override
            public boolean add(Object o) {
                ++size;
                return true;
            }
            public Iterator<Object> iterator() {
                throw new UnsupportedOperationException();
            }
        };
    }
    private static class Builder implements ContentHandler, LexicalHandler {
        Collection<Object> sink;
        boolean makeFrozen;
        boolean tokenizing;
        Builder(Collection<Object> sink, boolean tokenizing, boolean makeFrozen) {
            this.sink = sink;
            this.tokenizing = tokenizing;
            this.makeFrozen = makeFrozen;
        }
        Object[] parts = new Object[30];
        int nparts = 0;
        int[] attrBases = new int[10];  
        int[] elemBases = new int[10];  
        int depth = -1;  
        int mergeableToken = -1;  
        boolean inCData = false;
        void addPart(Object x) {
            if (nparts == parts.length) {
                Object[] newParts = new Object[parts.length * 2];
                System.arraycopy(parts, 0, newParts, 0, parts.length);
                parts = newParts;
            }
            parts[nparts++] = x;
        }
        Object getMergeableToken() {
            if (mergeableToken == nparts - 1) {
                assert (parts[mergeableToken] instanceof CharSequence);
                return parts[nparts - 1];
            } else {
                return null;
            }
        }
        void clearMergeableToken() {
            if (mergeableToken >= 0) {
                assert (parts[mergeableToken] instanceof CharSequence);
                parts[mergeableToken] = parts[mergeableToken].toString();
                mergeableToken = -1;
            }
        }
        void setMergeableToken() {
            if (mergeableToken != nparts - 1) {
                clearMergeableToken();
                mergeableToken = nparts - 1;
                assert (parts[mergeableToken] instanceof CharSequence);
            }
        }
        public void startElement(String ns, String localName, String name, Attributes atts) {
            clearMergeableToken();
            addPart(name.intern());
            ++depth;
            if (depth == attrBases.length) {
                int oldlen = depth;
                int newlen = depth * 2;
                int[] newAB = new int[newlen];
                int[] newEB = new int[newlen];
                System.arraycopy(attrBases, 0, newAB, 0, oldlen);
                System.arraycopy(elemBases, 0, newEB, 0, oldlen);
                attrBases = newAB;
                elemBases = newEB;
            }
            attrBases[depth] = nparts;
            int na = atts.getLength();
            for (int k = 0; k < na; k++) {
                addPart(atts.getQName(k).intern());
                addPart(atts.getValue(k));
            }
            elemBases[depth] = nparts;
        }
        public void endElement(String ns, String localName, String name) {
            assert (depth >= 0);
            clearMergeableToken();
            int ebase = elemBases[depth];
            int elen = nparts - ebase;
            int abase = attrBases[depth];
            int alen = ebase - abase;
            int nbase = abase - 1;
            int cap = alen + (makeFrozen ? 0 : NEED_SLOP) + elen;
            Element e = new Element((String) parts[nbase], elen, cap);
            for (int k = 0; k < alen; k += 2) {
                e.parts[cap - k - 2] = parts[abase + k + 0];
                e.parts[cap - k - 1] = parts[abase + k + 1];
            }
            System.arraycopy(parts, ebase, e.parts, 0, elen);
            --depth;
            nparts = nbase;
            assert (e.isFrozen() == makeFrozen);
            assert (e.size() == elen);
            assert (e.attrSize() * 2 == alen);
            if (depth >= 0) {
                addPart(e);
            } else {
                sink.add(e);
            }
        }
        public void startCDATA() {
            inCData = true;
        }
        public void endCDATA() {
            inCData = false;
        }
        public void characters(char[] buf, int off, int len) {
            boolean headSpace = false;
            boolean tailSpace = false;
            int firstLen;
            if (tokenizing && !inCData) {
                while (len > 0 && isWhitespace(buf[off])) {
                    headSpace = true;
                    ++off;
                    --len;
                }
                if (len == 0) {
                    tailSpace = true;  
                }
                while (len > 0 && isWhitespace(buf[off + len - 1])) {
                    tailSpace = true;
                    --len;
                }
                firstLen = 0;
                while (firstLen < len && !isWhitespace(buf[off + firstLen])) {
                    ++firstLen;
                }
            } else {
                firstLen = len;
            }
            if (headSpace) {
                clearMergeableToken();
            }
            boolean mergeAtEnd = !tailSpace;
            if (len == 0) {
                return;
            }
            Object prev = getMergeableToken();
            if (prev instanceof StringBuffer) {
                ((StringBuffer) prev).append(buf, off, firstLen);
            } else if (prev == null) {
                addPart(new String(buf, off, firstLen));
            } else {
                String prevStr = prev.toString();
                StringBuffer prevBuf = new StringBuffer(prevStr.length() + firstLen);
                prevBuf.append(prevStr);
                prevBuf.append(buf, off, firstLen);
                if (mergeAtEnd && len == firstLen) {
                    parts[nparts - 1] = prevBuf;
                } else {
                    parts[nparts - 1] = prevBuf.toString();
                }
            }
            off += firstLen;
            len -= firstLen;
            if (len > 0) {
                clearMergeableToken();
                while (len > 0) {
                    while (len > 0 && isWhitespace(buf[off])) {
                        ++off;
                        --len;
                    }
                    int nextLen = 0;
                    while (nextLen < len && !isWhitespace(buf[off + nextLen])) {
                        ++nextLen;
                    }
                    assert (nextLen > 0);
                    addPart(new String(buf, off, nextLen));
                    off += nextLen;
                    len -= nextLen;
                }
            }
            if (mergeAtEnd) {
                setMergeableToken();
            }
        }
        public void ignorableWhitespace(char[] buf, int off, int len) {
            clearMergeableToken();
            if (false) {
                characters(buf, off, len);
                clearMergeableToken();
            }
        }
        public void comment(char[] buf, int off, int len) {
            addPart(new Special("<!-- -->", new String(buf, off, len)));
        }
        public void processingInstruction(String name, String instruction) {
            Element pi = new Element(name);
            pi.add(instruction);
            addPart(new Special("<? ?>", pi));
        }
        public void skippedEntity(String name) {
        }
        public void startDTD(String name, String publicId, String systemId) {
        }
        public void endDTD() {
        }
        public void startEntity(String name) {
        }
        public void endEntity(String name) {
        }
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        public void startDocument() {
        }
        public void endDocument() {
        }
        public void startPrefixMapping(String prefix, String uri) {
        }
        public void endPrefixMapping(String prefix) {
        }
    }
    public static ContentHandler makeBuilder(Collection<Object> sink, boolean tokenizing, boolean makeFrozen) {
        return new Builder(sink, tokenizing, makeFrozen);
    }
    public static ContentHandler makeBuilder(Collection<Object> sink, boolean tokenizing) {
        return new Builder(sink, tokenizing, false);
    }
    public static ContentHandler makeBuilder(Collection<Object> sink) {
        return makeBuilder(sink, false, false);
    }
    public static Element readFrom(Reader in, boolean tokenizing, boolean makeFrozen) throws IOException {
        Element sink = new Element();
        ContentHandler b = makeBuilder(sink.asList(), tokenizing, makeFrozen);
        XMLReader parser;
        try {
            parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        } catch (SAXException ee) {
            throw new Error(ee);
        }
        parser.setContentHandler(b);
        try {
            parser.setProperty("http:
                    (LexicalHandler) b);
        } catch (SAXException ee) {
        }
        try {
            parser.parse(new InputSource(in));
        } catch (SAXParseException ee) {
            throw new RuntimeException("line " + ee.getLineNumber() + " col " + ee.getColumnNumber() + ": ", ee);
        } catch (SAXException ee) {
            throw new RuntimeException(ee);
        }
        switch (sink.size()) {
            case 0:
                return null;
            case 1:
                if (sink.get(0) instanceof Element) {
                    return (Element) sink.get(0);
                }
            default:
                if (makeFrozen) {
                    sink.shallowFreeze();
                }
                return sink;
        }
    }
    public static Element readFrom(Reader in, boolean tokenizing) throws IOException {
        return readFrom(in, tokenizing, false);
    }
    public static Element readFrom(Reader in) throws IOException {
        return readFrom(in, false, false);
    }
    public static void prettyPrintTo(OutputStream out, Element e) throws IOException {
        prettyPrintTo(new OutputStreamWriter(out), e);
    }
    public static void prettyPrintTo(Writer out, Element e) throws IOException {
        Printer pr = new Printer(out);
        pr.pretty = true;
        pr.print(e);
    }
    static class Outputter {
        ContentHandler ch;
        LexicalHandler lh;
        Outputter(ContentHandler ch, LexicalHandler lh) {
            this.ch = ch;
            this.lh = lh;
        }
        AttributesImpl atts = new AttributesImpl();  
        void output(Object x) throws SAXException {
            if (x instanceof Element) {
                Element e = (Element) x;
                atts.clear();
                for (int asize = e.attrSize(), k = 0; k < asize; k++) {
                    String key = e.getAttrName(k);
                    String val = e.getAttr(k);
                    atts.addAttribute("", "", key, "CDATA", val);
                }
                ch.startElement("", "", e.getName(), atts);
                for (int i = 0; i < e.size(); i++) {
                    output(e.get(i));
                }
                ch.endElement("", "", e.getName());
            } else if (x instanceof Special) {
                Special sp = (Special) x;
                if (sp.kind.startsWith("<!--")) {
                    char[] chars = sp.value.toString().toCharArray();
                    lh.comment(chars, 0, chars.length);
                } else if (sp.kind.startsWith("<?")) {
                    Element nameInstr = (Element) sp.value;
                    ch.processingInstruction(nameInstr.name,
                            nameInstr.get(0).toString());
                } else {
                }
            } else {
                char[] chars = x.toString().toCharArray();
                ch.characters(chars, 0, chars.length);
            }
        }
    }
    public static class Printer {
        public Writer w;
        public boolean tokenizing;
        public boolean pretty;
        public boolean abbreviated;  
        int depth = 0;
        boolean prevStr;
        int tabStop = 2;
        public Printer(Writer w) {
            this.w = w;
        }
        public Printer() {
            StringWriter sw = new StringWriter();
            this.w = sw;
        }
        public String nextString() {
            StringBuffer sb = ((StringWriter) w).getBuffer();
            String next = sb.toString();
            sb.setLength(0);  
            return next;
        }
        void indent(int depth) throws IOException {
            if (depth > 0) {
                w.write("\n");
            }
            int nsp = tabStop * depth;
            while (nsp > 0) {
                String s = "                ";
                String t = s.substring(0, nsp < s.length() ? nsp : s.length());
                w.write(t);
                nsp -= t.length();
            }
        }
        public void print(Element e) throws IOException {
            if (e.isAnonymous()) {
                printParts(e);
                return;
            }
            printRecursive(e);
        }
        public void println(Element e) throws IOException {
            print(e);
            w.write("\n");
            w.flush();
        }
        public void printRecursive(Element e) throws IOException {
            boolean indented = false;
            if (pretty && !prevStr && e.size() + e.attrSize() > 0) {
                indent(depth);
                indented = true;
            }
            w.write("<");
            w.write(e.name);
            for (int asize = e.attrSize(), k = 0; k < asize; k++) {
                String key = e.getAttrName(k);
                String val = e.getAttr(k);
                w.write(" ");
                w.write(key);
                w.write("=");
                if (val == null) {
                    w.write("null");  
                } else if (val.indexOf("\"") < 0) {
                    w.write("\"");
                    writeToken(val, '"', w);
                    w.write("\"");
                } else {
                    w.write("'");
                    writeToken(val, '\'', w);
                    w.write("'");
                }
            }
            if (e.size() == 0) {
                w.write("/>");
            } else {
                ++depth;
                if (abbreviated) {
                    w.write("/");
                } else {
                    w.write(">");
                }
                prevStr = false;
                printParts(e);
                if (abbreviated) {
                    w.write(">");
                } else {
                    if (indented && !prevStr) {
                        indent(depth - 1);
                    }
                    w.write("</");
                    w.write(e.name);
                    w.write(">");
                }
                prevStr = false;
                --depth;
            }
        }
        private void printParts(Element e) throws IOException {
            for (int i = 0; i < e.size(); i++) {
                Object x = e.get(i);
                if (x instanceof Element) {
                    printRecursive((Element) x);
                    prevStr = false;
                } else if (x instanceof Special) {
                    w.write(((Special) x).toString());
                    prevStr = false;
                } else {
                    String s = String.valueOf(x);
                    if (pretty) {
                        s = s.trim();
                        if (s.length() == 0) {
                            continue;
                        }
                    }
                    if (prevStr) {
                        w.write(' ');
                    }
                    writeToken(s, tokenizing ? ' ' : (char) -1, w);
                    prevStr = true;
                }
                if (pretty && depth == 0) {
                    w.write("\n");
                    prevStr = false;
                }
            }
        }
    }
    public static void output(Object e, ContentHandler ch, LexicalHandler lh) throws SAXException {
        new Outputter(ch, lh).output(e);
    }
    public static void output(Object e, ContentHandler ch) throws SAXException {
        if (ch instanceof LexicalHandler) {
            output(e, ch, (LexicalHandler) ch);
        } else {
            output(e, ch, null);
        }
    }
    public static void writeToken(String val, char quote, Writer w) throws IOException {
        int len = val.length();
        boolean canUseCData = (quote != '"' && quote != '\'');
        int vpos = 0;
        for (int i = 0; i < len; i++) {
            char ch = val.charAt(i);
            if ((ch == '<' || ch == '&' || ch == '>' || ch == quote)
                    || (quote == ' ' && isWhitespace(ch))) {
                if (canUseCData) {
                    assert (vpos == 0);
                    writeCData(val, w);
                    return;
                } else {
                    if (vpos < i) {
                        w.write(val, vpos, i - vpos);
                    }
                    String esc;
                    switch (ch) {
                        case '&':
                            esc = "&amp;";
                            break;
                        case '<':
                            esc = "&lt;";
                            break;
                        case '\'':
                            esc = "&apos;";
                            break;
                        case '"':
                            esc = "&quot;";
                            break;
                        case '>':
                            esc = "&gt;";
                            break;
                        default:
                            esc = "&#" + (int) ch + ";";
                            break;
                    }
                    w.write(esc);
                    vpos = i + 1;  
                }
            }
        }
        w.write(val, vpos, val.length() - vpos);
    }
    public static void writeCData(String val, Writer w) throws IOException {
        String begCData = "<![CDATA[";
        String endCData = "]]>";
        w.write(begCData);
        for (int vpos = 0, split;; vpos = split) {
            split = val.indexOf(endCData, vpos);
            if (split < 0) {
                w.write(val, vpos, val.length() - vpos);
                w.write(endCData);
                return;
            }
            split += 2; 
            w.write(val, vpos, split - vpos);
            w.write(endCData);
            w.write(begCData);
        }
    }
    public static TokenList convertToList(String str) {
        if (str == null) {
            return null;
        }
        return new TokenList(str);
    }
    public static Number convertToNumber(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        if (str.length() == 0) {
            return null;
        }
        if (str.indexOf('.') >= 0
                || str.indexOf('e') >= 0
                || str.indexOf('E') >= 0) {
            return Double.valueOf(str);
        }
        try {
            long lval = Long.parseLong(str);
            if (lval == (int) lval) {
                return new Integer((int) lval);
            }
            return new Long(lval);
        } catch (NumberFormatException ee) {
            return new java.math.BigInteger(str, 10);
        }
    }
    public static Number convertToNumber(String str, Number dflt) {
        Number n = convertToNumber(str);
        return (n == null) ? dflt : n;
    }
    public static long convertToLong(String str) {
        return convertToLong(str, 0);
    }
    public static long convertToLong(String str, long dflt) {
        Number n = convertToNumber(str);
        return (n == null) ? dflt : n.longValue();
    }
    public static double convertToDouble(String str) {
        return convertToDouble(str, 0);
    }
    public static double convertToDouble(String str, double dflt) {
        Number n = convertToNumber(str);
        return (n == null) ? dflt : n.doubleValue();
    }
    public static void main(String... av) throws Exception {
        Element.method("getAttr");
        int reps = 0;
        boolean tokenizing = false;
        boolean makeFrozen = false;
        if (av.length > 0) {
            tokenizing = true;
            try {
                reps = Integer.parseInt(av[0]);
            } catch (NumberFormatException ee) {
            }
        }
        Reader inR = new BufferedReader(new InputStreamReader(System.in));
        String inS = null;
        if (reps > 1) {
            StringWriter inBufR = new StringWriter(1 << 14);
            char[] cbuf = new char[1024];
            for (int nr; (nr = inR.read(cbuf)) >= 0;) {
                inBufR.write(cbuf, 0, nr);
            }
            inS = inBufR.toString();
            inR = new StringReader(inS);
        }
        Element e = XMLKit.readFrom(inR, tokenizing, makeFrozen);
        System.out.println("transform = " + e.findAll(methodFilter(Element.method("prettyString"))));
        System.out.println("transform = " + e.findAll(testMethodFilter(Element.method("hasText"))));
        long tm0 = 0;
        int warmup = 10;
        for (int i = 1; i < reps; i++) {
            inR = new StringReader(inS);
            readFrom(inR, tokenizing, makeFrozen);
            if (i == warmup) {
                System.out.println("Start timing...");
                tm0 = System.currentTimeMillis();
            }
        }
        if (tm0 != 0) {
            long tm1 = System.currentTimeMillis();
            System.out.println((reps - warmup) + " in " + (tm1 - tm0) + " ms");
        }
        System.out.println("hashCode = " + e.hashCode());
        String eStr = e.toString();
        System.out.println(eStr);
        Element e2 = readFrom(new StringReader(eStr), tokenizing, !makeFrozen);
        System.out.println("hashCode = " + e2.hashCode());
        if (!e.equals(e2)) {
            System.out.println("**** NOT EQUAL 1\n" + e2);
        }
        e = e.deepCopy();
        System.out.println("hashCode = " + e.hashCode());
        if (!e.equals(e2)) {
            System.out.println("**** NOT EQUAL 2");
        }
        e2.shallowFreeze();
        System.out.println("hashCode = " + e2.hashCode());
        if (!e.equals(e2)) {
            System.out.println("**** NOT EQUAL 3");
        }
        if (false) {
            System.out.println(e);
        } else {
            prettyPrintTo(new OutputStreamWriter(System.out), e);
        }
        System.out.println("Flat text:|" + e.getFlatText() + "|");
        {
            System.out.println("<!--- Sorted: --->");
            Element ce = e.copyContentOnly();
            ce.sort();
            prettyPrintTo(new OutputStreamWriter(System.out), ce);
        }
        {
            System.out.println("<!--- Trimmed: --->");
            Element tr = e.deepCopy();
            findInTree(testMethodFilter(Element.method("trimText"))).filter(tr);
            System.out.println(tr);
        }
        {
            System.out.println("<!--- Unstrung: --->");
            Element us = e.deepCopy();
            int nr = us.retainAllInTree(elementFilter(), null);
            System.out.println("nr=" + nr);
            System.out.println(us);
        }
        {
            System.out.println("<!--- Rollup: --->");
            Element ru = e.deepCopy();
            Filter makeAnonF =
                    methodFilter(Element.method("setName"),
                    new Object[]{ANON_NAME});
            Filter testSizeF =
                    testMethodFilter(Element.method("size"));
            Filter walk =
                    replaceInTree(and(not(elementFilter()), emptyFilter()),
                    and(testSizeF, makeAnonF));
            ru = (Element) walk.filter(ru);
            prettyPrintTo(new OutputStreamWriter(System.out), ru);
        }
    }
    static boolean isWhitespace(char c) {
        switch (c) {
            case 0x20:
            case 0x09:
            case 0x0D:
            case 0x0A:
                return true;
        }
        return false;
    }
}
