public class StyleSheet extends StyleContext {
    public StyleSheet() {
        super();
        selectorMapping = new SelectorMapping(0);
        resolvedStyles = new Hashtable<String, ResolvedStyle>();
        if (css == null) {
            css = new CSS();
        }
    }
    public Style getRule(HTML.Tag t, Element e) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        try {
            Vector<Element> searchContext = sb.getVector();
            for (Element p = e; p != null; p = p.getParentElement()) {
                searchContext.addElement(p);
            }
            int              n = searchContext.size();
            StringBuffer     cacheLookup = sb.getStringBuffer();
            AttributeSet     attr;
            String           eName;
            Object           name;
            for (int counter = n - 1; counter >= 1; counter--) {
                e = searchContext.elementAt(counter);
                attr = e.getAttributes();
                name = attr.getAttribute(StyleConstants.NameAttribute);
                eName = name.toString();
                cacheLookup.append(eName);
                if (attr != null) {
                    if (attr.isDefined(HTML.Attribute.ID)) {
                        cacheLookup.append('#');
                        cacheLookup.append(attr.getAttribute
                                           (HTML.Attribute.ID));
                    }
                    else if (attr.isDefined(HTML.Attribute.CLASS)) {
                        cacheLookup.append('.');
                        cacheLookup.append(attr.getAttribute
                                           (HTML.Attribute.CLASS));
                    }
                }
                cacheLookup.append(' ');
            }
            cacheLookup.append(t.toString());
            e = searchContext.elementAt(0);
            attr = e.getAttributes();
            if (e.isLeaf()) {
                Object testAttr = attr.getAttribute(t);
                if (testAttr instanceof AttributeSet) {
                    attr = (AttributeSet)testAttr;
                }
                else {
                    attr = null;
                }
            }
            if (attr != null) {
                if (attr.isDefined(HTML.Attribute.ID)) {
                    cacheLookup.append('#');
                    cacheLookup.append(attr.getAttribute(HTML.Attribute.ID));
                }
                else if (attr.isDefined(HTML.Attribute.CLASS)) {
                    cacheLookup.append('.');
                    cacheLookup.append(attr.getAttribute
                                       (HTML.Attribute.CLASS));
                }
            }
            Style style = getResolvedStyle(cacheLookup.toString(),
                                           searchContext, t);
            return style;
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }
    public Style getRule(String selector) {
        selector = cleanSelectorString(selector);
        if (selector != null) {
            Style style = getResolvedStyle(selector);
            return style;
        }
        return null;
    }
    public void addRule(String rule) {
        if (rule != null) {
            final String baseUnitsDisable = "BASE_SIZE_DISABLE";
            final String baseUnits = "BASE_SIZE ";
            final String w3cLengthUnitsEnable = "W3C_LENGTH_UNITS_ENABLE";
            final String w3cLengthUnitsDisable = "W3C_LENGTH_UNITS_DISABLE";
            if (rule == baseUnitsDisable) {
                sizeMap = sizeMapDefault;
            } else if (rule.startsWith(baseUnits)) {
                rebaseSizeMap(Integer.
                              parseInt(rule.substring(baseUnits.length())));
            } else if (rule == w3cLengthUnitsEnable) {
                w3cLengthUnits = true;
            } else if (rule == w3cLengthUnitsDisable) {
                w3cLengthUnits = false;
            } else {
                CssParser parser = new CssParser();
                try {
                    parser.parse(getBase(), new StringReader(rule), false, false);
                } catch (IOException ioe) { }
            }
        }
    }
    public AttributeSet getDeclaration(String decl) {
        if (decl == null) {
            return SimpleAttributeSet.EMPTY;
        }
        CssParser parser = new CssParser();
        return parser.parseDeclaration(decl);
    }
    public void loadRules(Reader in, URL ref) throws IOException {
        CssParser parser = new CssParser();
        parser.parse(ref, in, false, false);
    }
    public AttributeSet getViewAttributes(View v) {
        return new ViewAttributeSet(v);
    }
    public void removeStyle(String nm) {
        Style       aStyle = getStyle(nm);
        if (aStyle != null) {
            String selector = cleanSelectorString(nm);
            String[] selectors = getSimpleSelectors(selector);
            synchronized(this) {
                SelectorMapping mapping = getRootSelectorMapping();
                for (int i = selectors.length - 1; i >= 0; i--) {
                    mapping = mapping.getChildSelectorMapping(selectors[i],
                                                              true);
                }
                Style rule = mapping.getStyle();
                if (rule != null) {
                    mapping.setStyle(null);
                    if (resolvedStyles.size() > 0) {
                        Enumeration<ResolvedStyle> values = resolvedStyles.elements();
                        while (values.hasMoreElements()) {
                            ResolvedStyle style = values.nextElement();
                            style.removeStyle(rule);
                        }
                    }
                }
            }
        }
        super.removeStyle(nm);
    }
    public void addStyleSheet(StyleSheet ss) {
        synchronized(this) {
            if (linkedStyleSheets == null) {
                linkedStyleSheets = new Vector<StyleSheet>();
            }
            if (!linkedStyleSheets.contains(ss)) {
                int index = 0;
                if (ss instanceof javax.swing.plaf.UIResource
                    && linkedStyleSheets.size() > 1) {
                    index = linkedStyleSheets.size() - 1;
                }
                linkedStyleSheets.insertElementAt(ss, index);
                linkStyleSheetAt(ss, index);
            }
        }
    }
    public void removeStyleSheet(StyleSheet ss) {
        synchronized(this) {
            if (linkedStyleSheets != null) {
                int index = linkedStyleSheets.indexOf(ss);
                if (index != -1) {
                    linkedStyleSheets.removeElementAt(index);
                    unlinkStyleSheet(ss, index);
                    if (index == 0 && linkedStyleSheets.size() == 0) {
                        linkedStyleSheets = null;
                    }
                }
            }
        }
    }
    public StyleSheet[] getStyleSheets() {
        StyleSheet[] retValue;
        synchronized(this) {
            if (linkedStyleSheets != null) {
                retValue = new StyleSheet[linkedStyleSheets.size()];
                linkedStyleSheets.copyInto(retValue);
            }
            else {
                retValue = null;
            }
        }
        return retValue;
    }
    public void importStyleSheet(URL url) {
        try {
            InputStream is;
            is = url.openStream();
            Reader r = new BufferedReader(new InputStreamReader(is));
            CssParser parser = new CssParser();
            parser.parse(url, r, false, true);
            r.close();
            is.close();
        } catch (Throwable e) {
        }
    }
    public void setBase(URL base) {
        this.base = base;
    }
    public URL getBase() {
        return base;
    }
    public void addCSSAttribute(MutableAttributeSet attr, CSS.Attribute key,
                                String value) {
        css.addInternalCSSValue(attr, key, value);
    }
    public boolean addCSSAttributeFromHTML(MutableAttributeSet attr,
                                           CSS.Attribute key, String value) {
        Object iValue = css.getCssValue(key, value);
        if (iValue != null) {
            attr.addAttribute(key, iValue);
            return true;
        }
        return false;
    }
    public AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
        AttributeSet cssAttrSet = css.translateHTMLToCSS(htmlAttrSet);
        MutableAttributeSet cssStyleSet = addStyle(null, null);
        cssStyleSet.addAttributes(cssAttrSet);
        return cssStyleSet;
    }
    public AttributeSet addAttribute(AttributeSet old, Object key,
                                     Object value) {
        if (css == null) {
            css = new CSS();
        }
        if (key instanceof StyleConstants) {
            HTML.Tag tag = HTML.getTagForStyleConstantsKey(
                                (StyleConstants)key);
            if (tag != null && old.isDefined(tag)) {
                old = removeAttribute(old, tag);
            }
            Object cssValue = css.styleConstantsValueToCSSValue
                              ((StyleConstants)key, value);
            if (cssValue != null) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    return super.addAttribute(old, cssKey, cssValue);
                }
            }
        }
        return super.addAttribute(old, key, value);
    }
    public AttributeSet addAttributes(AttributeSet old, AttributeSet attr) {
        if (!(attr instanceof HTMLDocument.TaggedAttributeSet)) {
            old = removeHTMLTags(old, attr);
        }
        return super.addAttributes(old, convertAttributeSet(attr));
    }
    public AttributeSet removeAttribute(AttributeSet old, Object key) {
        if (key instanceof StyleConstants) {
            HTML.Tag tag = HTML.getTagForStyleConstantsKey(
                                   (StyleConstants)key);
            if (tag != null) {
                old = super.removeAttribute(old, tag);
            }
            Object cssKey = css.styleConstantsKeyToCSSKey((StyleConstants)key);
            if (cssKey != null) {
                return super.removeAttribute(old, cssKey);
            }
        }
        return super.removeAttribute(old, key);
    }
    public AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names) {
        return super.removeAttributes(old, names);
    }
    public AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs) {
        if (old != attrs) {
            old = removeHTMLTags(old, attrs);
        }
        return super.removeAttributes(old, convertAttributeSet(attrs));
    }
    protected SmallAttributeSet createSmallAttributeSet(AttributeSet a) {
        return new SmallConversionSet(a);
    }
    protected MutableAttributeSet createLargeAttributeSet(AttributeSet a) {
        return new LargeConversionSet(a);
    }
    private AttributeSet removeHTMLTags(AttributeSet old, AttributeSet attr) {
        if (!(attr instanceof LargeConversionSet) &&
            !(attr instanceof SmallConversionSet)) {
            Enumeration names = attr.getAttributeNames();
            while (names.hasMoreElements()) {
                Object key = names.nextElement();
                if (key instanceof StyleConstants) {
                    HTML.Tag tag = HTML.getTagForStyleConstantsKey(
                        (StyleConstants)key);
                    if (tag != null && old.isDefined(tag)) {
                        old = super.removeAttribute(old, tag);
                    }
                }
            }
        }
        return old;
    }
    AttributeSet convertAttributeSet(AttributeSet a) {
        if ((a instanceof LargeConversionSet) ||
            (a instanceof SmallConversionSet)) {
            return a;
        }
        Enumeration names = a.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (name instanceof StyleConstants) {
                MutableAttributeSet converted = new LargeConversionSet();
                Enumeration keys = a.getAttributeNames();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object cssValue = null;
                    if (key instanceof StyleConstants) {
                        Object cssKey = css.styleConstantsKeyToCSSKey
                                            ((StyleConstants)key);
                        if (cssKey != null) {
                            Object value = a.getAttribute(key);
                            cssValue = css.styleConstantsValueToCSSValue
                                           ((StyleConstants)key, value);
                            if (cssValue != null) {
                                converted.addAttribute(cssKey, cssValue);
                            }
                        }
                    }
                    if (cssValue == null) {
                        converted.addAttribute(key, a.getAttribute(key));
                    }
                }
                return converted;
            }
        }
        return a;
    }
    class LargeConversionSet extends SimpleAttributeSet {
        public LargeConversionSet(AttributeSet source) {
            super(source);
        }
        public LargeConversionSet() {
            super();
        }
        public boolean isDefined(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    return super.isDefined(cssKey);
                }
            }
            return super.isDefined(key);
        }
        public Object getAttribute(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    Object value = super.getAttribute(cssKey);
                    if (value != null) {
                        return css.cssValueToStyleConstantsValue
                                           ((StyleConstants)key, value);
                    }
                }
            }
            return super.getAttribute(key);
        }
    }
    class SmallConversionSet extends SmallAttributeSet {
        public SmallConversionSet(AttributeSet attrs) {
            super(attrs);
        }
        public boolean isDefined(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    return super.isDefined(cssKey);
                }
            }
            return super.isDefined(key);
        }
        public Object getAttribute(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    Object value = super.getAttribute(cssKey);
                    if (value != null) {
                        return css.cssValueToStyleConstantsValue
                                           ((StyleConstants)key, value);
                    }
                }
            }
            return super.getAttribute(key);
        }
    }
    public Font getFont(AttributeSet a) {
        return css.getFont(this, a, 12, this);
    }
    public Color getForeground(AttributeSet a) {
        Color c = css.getColor(a, CSS.Attribute.COLOR);
        if (c == null) {
            return Color.black;
        }
        return c;
    }
    public Color getBackground(AttributeSet a) {
        return css.getColor(a, CSS.Attribute.BACKGROUND_COLOR);
    }
    public BoxPainter getBoxPainter(AttributeSet a) {
        return new BoxPainter(a, css, this);
    }
    public ListPainter getListPainter(AttributeSet a) {
        return new ListPainter(a, this);
    }
    public void setBaseFontSize(int sz) {
        css.setBaseFontSize(sz);
    }
    public void setBaseFontSize(String size) {
        css.setBaseFontSize(size);
    }
    public static int getIndexOfSize(float pt) {
        return CSS.getIndexOfSize(pt, sizeMapDefault);
    }
    public float getPointSize(int index) {
        return css.getPointSize(index, this);
    }
    public float getPointSize(String size) {
        return css.getPointSize(size, this);
    }
    public Color stringToColor(String string) {
        return CSS.stringToColor(string);
    }
    ImageIcon getBackgroundImage(AttributeSet attr) {
        Object value = attr.getAttribute(CSS.Attribute.BACKGROUND_IMAGE);
        if (value != null) {
            return ((CSS.BackgroundImage)value).getImage(getBase());
        }
        return null;
    }
    void addRule(String[] selector, AttributeSet declaration,
                 boolean isLinked) {
        int n = selector.length;
        StringBuilder sb = new StringBuilder();
        sb.append(selector[0]);
        for (int counter = 1; counter < n; counter++) {
            sb.append(' ');
            sb.append(selector[counter]);
        }
        String selectorName = sb.toString();
        Style rule = getStyle(selectorName);
        if (rule == null) {
            Style altRule = addStyle(selectorName, null);
            synchronized(this) {
                SelectorMapping mapping = getRootSelectorMapping();
                for (int i = n - 1; i >= 0; i--) {
                    mapping = mapping.getChildSelectorMapping
                                      (selector[i], true);
                }
                rule = mapping.getStyle();
                if (rule == null) {
                    rule = altRule;
                    mapping.setStyle(rule);
                    refreshResolvedRules(selectorName, selector, rule,
                                         mapping.getSpecificity());
                }
            }
        }
        if (isLinked) {
            rule = getLinkedStyle(rule);
        }
        rule.addAttributes(declaration);
    }
    private synchronized void linkStyleSheetAt(StyleSheet ss, int index) {
        if (resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle rule = values.nextElement();
                rule.insertExtendedStyleAt(ss.getRule(rule.getName()),
                                           index);
            }
        }
    }
    private synchronized void unlinkStyleSheet(StyleSheet ss, int index) {
        if (resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle rule = values.nextElement();
                rule.removeExtendedStyleAt(index);
            }
        }
    }
    String[] getSimpleSelectors(String selector) {
        selector = cleanSelectorString(selector);
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector<String> selectors = sb.getVector();
        int lastIndex = 0;
        int length = selector.length();
        while (lastIndex != -1) {
            int newIndex = selector.indexOf(' ', lastIndex);
            if (newIndex != -1) {
                selectors.addElement(selector.substring(lastIndex, newIndex));
                if (++newIndex == length) {
                    lastIndex = -1;
                }
                else {
                    lastIndex = newIndex;
                }
            }
            else {
                selectors.addElement(selector.substring(lastIndex));
                lastIndex = -1;
            }
        }
        String[] retValue = new String[selectors.size()];
        selectors.copyInto(retValue);
        SearchBuffer.releaseSearchBuffer(sb);
        return retValue;
    }
     String cleanSelectorString(String selector) {
        boolean lastWasSpace = true;
        for (int counter = 0, maxCounter = selector.length();
             counter < maxCounter; counter++) {
            switch(selector.charAt(counter)) {
            case ' ':
                if (lastWasSpace) {
                    return _cleanSelectorString(selector);
                }
                lastWasSpace = true;
                break;
            case '\n':
            case '\r':
            case '\t':
                return _cleanSelectorString(selector);
            default:
                lastWasSpace = false;
            }
        }
        if (lastWasSpace) {
            return _cleanSelectorString(selector);
        }
        return selector;
    }
    private String _cleanSelectorString(String selector) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        StringBuffer buff = sb.getStringBuffer();
        boolean lastWasSpace = true;
        int lastIndex = 0;
        char[] chars = selector.toCharArray();
        int numChars = chars.length;
        String retValue = null;
        try {
            for (int counter = 0; counter < numChars; counter++) {
                switch(chars[counter]) {
                case ' ':
                    if (!lastWasSpace) {
                        lastWasSpace = true;
                        if (lastIndex < counter) {
                            buff.append(chars, lastIndex,
                                        1 + counter - lastIndex);
                        }
                    }
                    lastIndex = counter + 1;
                    break;
                case '\n':
                case '\r':
                case '\t':
                    if (!lastWasSpace) {
                        lastWasSpace = true;
                        if (lastIndex < counter) {
                            buff.append(chars, lastIndex,
                                        counter - lastIndex);
                            buff.append(' ');
                        }
                    }
                    lastIndex = counter + 1;
                    break;
                default:
                    lastWasSpace = false;
                    break;
                }
            }
            if (lastWasSpace && buff.length() > 0) {
                buff.setLength(buff.length() - 1);
            }
            else if (lastIndex < numChars) {
                buff.append(chars, lastIndex, numChars - lastIndex);
            }
            retValue = buff.toString();
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
        return retValue;
    }
    private SelectorMapping getRootSelectorMapping() {
        return selectorMapping;
    }
     static int getSpecificity(String selector) {
        int specificity = 0;
        boolean lastWasSpace = true;
        for (int counter = 0, maxCounter = selector.length();
             counter < maxCounter; counter++) {
            switch(selector.charAt(counter)) {
            case '.':
                specificity += 100;
                break;
            case '#':
                specificity += 10000;
                break;
            case ' ':
                lastWasSpace = true;
                break;
            default:
                if (lastWasSpace) {
                    lastWasSpace = false;
                    specificity += 1;
                }
            }
        }
        return specificity;
    }
    private Style getLinkedStyle(Style localStyle) {
        Style retStyle = (Style)localStyle.getResolveParent();
        if (retStyle == null) {
            retStyle = addStyle(null, null);
            localStyle.setResolveParent(retStyle);
        }
        return retStyle;
    }
    private synchronized Style getResolvedStyle(String selector,
                                                Vector elements,
                                                HTML.Tag t) {
        Style retStyle = resolvedStyles.get(selector);
        if (retStyle == null) {
            retStyle = createResolvedStyle(selector, elements, t);
        }
        return retStyle;
    }
    private synchronized Style getResolvedStyle(String selector) {
        Style retStyle = resolvedStyles.get(selector);
        if (retStyle == null) {
            retStyle = createResolvedStyle(selector);
        }
        return retStyle;
    }
    private void addSortedStyle(SelectorMapping mapping, Vector<SelectorMapping> elements) {
        int       size = elements.size();
        if (size > 0) {
            int     specificity = mapping.getSpecificity();
            for (int counter = 0; counter < size; counter++) {
                if (specificity >= elements.elementAt(counter).getSpecificity()) {
                    elements.insertElementAt(mapping, counter);
                    return;
                }
            }
        }
        elements.addElement(mapping);
    }
    private synchronized void getStyles(SelectorMapping parentMapping,
                           Vector<SelectorMapping> styles,
                           String[] tags, String[] ids, String[] classes,
                           int index, int numElements,
                           Hashtable<SelectorMapping, SelectorMapping> alreadyChecked) {
        if (alreadyChecked.contains(parentMapping)) {
            return;
        }
        alreadyChecked.put(parentMapping, parentMapping);
        Style style = parentMapping.getStyle();
        if (style != null) {
            addSortedStyle(parentMapping, styles);
        }
        for (int counter = index; counter < numElements; counter++) {
            String tagString = tags[counter];
            if (tagString != null) {
                SelectorMapping childMapping = parentMapping.
                                getChildSelectorMapping(tagString, false);
                if (childMapping != null) {
                    getStyles(childMapping, styles, tags, ids, classes,
                              counter + 1, numElements, alreadyChecked);
                }
                if (classes[counter] != null) {
                    String className = classes[counter];
                    childMapping = parentMapping.getChildSelectorMapping(
                                         tagString + "." + className, false);
                    if (childMapping != null) {
                        getStyles(childMapping, styles, tags, ids, classes,
                                  counter + 1, numElements, alreadyChecked);
                    }
                    childMapping = parentMapping.getChildSelectorMapping(
                                         "." + className, false);
                    if (childMapping != null) {
                        getStyles(childMapping, styles, tags, ids, classes,
                                  counter + 1, numElements, alreadyChecked);
                    }
                }
                if (ids[counter] != null) {
                    String idName = ids[counter];
                    childMapping = parentMapping.getChildSelectorMapping(
                                         tagString + "#" + idName, false);
                    if (childMapping != null) {
                        getStyles(childMapping, styles, tags, ids, classes,
                                  counter + 1, numElements, alreadyChecked);
                    }
                    childMapping = parentMapping.getChildSelectorMapping(
                                   "#" + idName, false);
                    if (childMapping != null) {
                        getStyles(childMapping, styles, tags, ids, classes,
                                  counter + 1, numElements, alreadyChecked);
                    }
                }
            }
        }
    }
    private synchronized Style createResolvedStyle(String selector,
                                      String[] tags,
                                      String[] ids, String[] classes) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector<SelectorMapping> tempVector = sb.getVector();
        Hashtable<SelectorMapping, SelectorMapping> tempHashtable = sb.getHashtable();
        try {
            SelectorMapping mapping = getRootSelectorMapping();
            int numElements = tags.length;
            String tagString = tags[0];
            SelectorMapping childMapping = mapping.getChildSelectorMapping(
                                                   tagString, false);
            if (childMapping != null) {
                getStyles(childMapping, tempVector, tags, ids, classes, 1,
                          numElements, tempHashtable);
            }
            if (classes[0] != null) {
                String className = classes[0];
                childMapping = mapping.getChildSelectorMapping(
                                       tagString + "." + className, false);
                if (childMapping != null) {
                    getStyles(childMapping, tempVector, tags, ids, classes, 1,
                              numElements, tempHashtable);
                }
                childMapping = mapping.getChildSelectorMapping(
                                       "." + className, false);
                if (childMapping != null) {
                    getStyles(childMapping, tempVector, tags, ids, classes,
                              1, numElements, tempHashtable);
                }
            }
            if (ids[0] != null) {
                String idName = ids[0];
                childMapping = mapping.getChildSelectorMapping(
                                       tagString + "#" + idName, false);
                if (childMapping != null) {
                    getStyles(childMapping, tempVector, tags, ids, classes,
                              1, numElements, tempHashtable);
                }
                childMapping = mapping.getChildSelectorMapping(
                                       "#" + idName, false);
                if (childMapping != null) {
                    getStyles(childMapping, tempVector, tags, ids, classes,
                              1, numElements, tempHashtable);
                }
            }
            int numLinkedSS = (linkedStyleSheets != null) ?
                              linkedStyleSheets.size() : 0;
            int numStyles = tempVector.size();
            AttributeSet[] attrs = new AttributeSet[numStyles + numLinkedSS];
            for (int counter = 0; counter < numStyles; counter++) {
                attrs[counter] = tempVector.elementAt(counter).getStyle();
            }
            for (int counter = 0; counter < numLinkedSS; counter++) {
                AttributeSet attr = linkedStyleSheets.elementAt(counter).getRule(selector);
                if (attr == null) {
                    attrs[counter + numStyles] = SimpleAttributeSet.EMPTY;
                }
                else {
                    attrs[counter + numStyles] = attr;
                }
            }
            ResolvedStyle retStyle = new ResolvedStyle(selector, attrs,
                                                       numStyles);
            resolvedStyles.put(selector, retStyle);
            return retStyle;
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }
    private Style createResolvedStyle(String selector, Vector elements,
                                      HTML.Tag t) {
        int numElements = elements.size();
        String tags[] = new String[numElements];
        String ids[] = new String[numElements];
        String classes[] = new String[numElements];
        for (int counter = 0; counter < numElements; counter++) {
            Element e = (Element)elements.elementAt(counter);
            AttributeSet attr = e.getAttributes();
            if (counter == 0 && e.isLeaf()) {
                Object testAttr = attr.getAttribute(t);
                if (testAttr instanceof AttributeSet) {
                    attr = (AttributeSet)testAttr;
                }
                else {
                    attr = null;
                }
            }
            if (attr != null) {
                HTML.Tag tag = (HTML.Tag)attr.getAttribute(StyleConstants.
                                                           NameAttribute);
                if (tag != null) {
                    tags[counter] = tag.toString();
                }
                else {
                    tags[counter] = null;
                }
                if (attr.isDefined(HTML.Attribute.CLASS)) {
                    classes[counter] = attr.getAttribute
                                      (HTML.Attribute.CLASS).toString();
                }
                else {
                    classes[counter] = null;
                }
                if (attr.isDefined(HTML.Attribute.ID)) {
                    ids[counter] = attr.getAttribute(HTML.Attribute.ID).
                                        toString();
                }
                else {
                    ids[counter] = null;
                }
            }
            else {
                tags[counter] = ids[counter] = classes[counter] = null;
            }
        }
        tags[0] = t.toString();
        return createResolvedStyle(selector, tags, ids, classes);
    }
    private Style createResolvedStyle(String selector) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector<String> elements = sb.getVector();
        try {
            boolean done;
            int dotIndex = 0;
            int spaceIndex;
            int poundIndex = 0;
            int lastIndex = 0;
            int length = selector.length();
            while (lastIndex < length) {
                if (dotIndex == lastIndex) {
                    dotIndex = selector.indexOf('.', lastIndex);
                }
                if (poundIndex == lastIndex) {
                    poundIndex = selector.indexOf('#', lastIndex);
                }
                spaceIndex = selector.indexOf(' ', lastIndex);
                if (spaceIndex == -1) {
                    spaceIndex = length;
                }
                if (dotIndex != -1 && poundIndex != -1 &&
                    dotIndex < spaceIndex && poundIndex < spaceIndex) {
                    if (poundIndex < dotIndex) {
                        if (lastIndex == poundIndex) {
                            elements.addElement("");
                        }
                        else {
                            elements.addElement(selector.substring(lastIndex,
                                                                  poundIndex));
                        }
                        if ((dotIndex + 1) < spaceIndex) {
                            elements.addElement(selector.substring
                                                (dotIndex + 1, spaceIndex));
                        }
                        else {
                            elements.addElement(null);
                        }
                        if ((poundIndex + 1) == dotIndex) {
                            elements.addElement(null);
                        }
                        else {
                            elements.addElement(selector.substring
                                                (poundIndex + 1, dotIndex));
                        }
                    }
                    else if(poundIndex < spaceIndex) {
                        if (lastIndex == dotIndex) {
                            elements.addElement("");
                        }
                        else {
                            elements.addElement(selector.substring(lastIndex,
                                                                  dotIndex));
                        }
                        if ((dotIndex + 1) < poundIndex) {
                            elements.addElement(selector.substring
                                                (dotIndex + 1, poundIndex));
                        }
                        else {
                            elements.addElement(null);
                        }
                        if ((poundIndex + 1) == spaceIndex) {
                            elements.addElement(null);
                        }
                        else {
                            elements.addElement(selector.substring
                                                (poundIndex + 1, spaceIndex));
                        }
                    }
                    dotIndex = poundIndex = spaceIndex + 1;
                }
                else if (dotIndex != -1 && dotIndex < spaceIndex) {
                    if (dotIndex == lastIndex) {
                        elements.addElement("");
                    }
                    else {
                        elements.addElement(selector.substring(lastIndex,
                                                               dotIndex));
                    }
                    if ((dotIndex + 1) == spaceIndex) {
                        elements.addElement(null);
                    }
                    else {
                        elements.addElement(selector.substring(dotIndex + 1,
                                                               spaceIndex));
                    }
                    elements.addElement(null);
                    dotIndex = spaceIndex + 1;
                }
                else if (poundIndex != -1 && poundIndex < spaceIndex) {
                    if (poundIndex == lastIndex) {
                        elements.addElement("");
                    }
                    else {
                        elements.addElement(selector.substring(lastIndex,
                                                               poundIndex));
                    }
                    elements.addElement(null);
                    if ((poundIndex + 1) == spaceIndex) {
                        elements.addElement(null);
                    }
                    else {
                        elements.addElement(selector.substring(poundIndex + 1,
                                                               spaceIndex));
                    }
                    poundIndex = spaceIndex + 1;
                }
                else {
                    elements.addElement(selector.substring(lastIndex,
                                                           spaceIndex));
                    elements.addElement(null);
                    elements.addElement(null);
                }
                lastIndex = spaceIndex + 1;
            }
            int total = elements.size();
            int numTags = total / 3;
            String[] tags = new String[numTags];
            String[] ids = new String[numTags];
            String[] classes = new String[numTags];
            for (int index = 0, eIndex = total - 3; index < numTags;
                 index++, eIndex -= 3) {
                tags[index] = elements.elementAt(eIndex);
                classes[index] = elements.elementAt(eIndex + 1);
                ids[index] = elements.elementAt(eIndex + 2);
            }
            return createResolvedStyle(selector, tags, ids, classes);
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }
    private synchronized void refreshResolvedRules(String selectorName,
                                                   String[] selector,
                                                   Style newStyle,
                                                   int specificity) {
        if (resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle style = values.nextElement();
                if (style.matches(selectorName)) {
                    style.insertStyle(newStyle, specificity);
                }
            }
        }
    }
    private static class SearchBuffer {
        static Stack<SearchBuffer> searchBuffers = new Stack<SearchBuffer>();
        Vector vector = null;
        StringBuffer stringBuffer = null;
        Hashtable hashtable = null;
        static SearchBuffer obtainSearchBuffer() {
            SearchBuffer sb;
            try {
                if(!searchBuffers.empty()) {
                   sb = searchBuffers.pop();
                } else {
                   sb = new SearchBuffer();
                }
            } catch (EmptyStackException ese) {
                sb = new SearchBuffer();
            }
            return sb;
        }
        static void releaseSearchBuffer(SearchBuffer sb) {
            sb.empty();
            searchBuffers.push(sb);
        }
        StringBuffer getStringBuffer() {
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
            }
            return stringBuffer;
        }
        Vector getVector() {
            if (vector == null) {
                vector = new Vector();
            }
            return vector;
        }
        Hashtable getHashtable() {
            if (hashtable == null) {
                hashtable = new Hashtable();
            }
            return hashtable;
        }
        void empty() {
            if (stringBuffer != null) {
                stringBuffer.setLength(0);
            }
            if (vector != null) {
                vector.removeAllElements();
            }
            if (hashtable != null) {
                hashtable.clear();
            }
        }
    }
    static final Border noBorder = new EmptyBorder(0,0,0,0);
    public static class BoxPainter implements Serializable {
        BoxPainter(AttributeSet a, CSS css, StyleSheet ss) {
            this.ss = ss;
            this.css = css;
            border = getBorder(a);
            binsets = border.getBorderInsets(null);
            topMargin = getLength(CSS.Attribute.MARGIN_TOP, a);
            bottomMargin = getLength(CSS.Attribute.MARGIN_BOTTOM, a);
            leftMargin = getLength(CSS.Attribute.MARGIN_LEFT, a);
            rightMargin = getLength(CSS.Attribute.MARGIN_RIGHT, a);
            bg = ss.getBackground(a);
            if (ss.getBackgroundImage(a) != null) {
                bgPainter = new BackgroundImagePainter(a, css, ss);
            }
        }
        Border getBorder(AttributeSet a) {
            return new CSSBorder(a);
        }
        Color getBorderColor(AttributeSet a) {
            Color color = css.getColor(a, CSS.Attribute.BORDER_COLOR);
            if (color == null) {
                color = css.getColor(a, CSS.Attribute.COLOR);
                if (color == null) {
                    return Color.black;
                }
            }
            return color;
        }
        public float getInset(int side, View v) {
            AttributeSet a = v.getAttributes();
            float inset = 0;
            switch(side) {
            case View.LEFT:
                inset += getOrientationMargin(HorizontalMargin.LEFT,
                                              leftMargin, a, isLeftToRight(v));
                inset += binsets.left;
                inset += getLength(CSS.Attribute.PADDING_LEFT, a);
                break;
            case View.RIGHT:
                inset += getOrientationMargin(HorizontalMargin.RIGHT,
                                              rightMargin, a, isLeftToRight(v));
                inset += binsets.right;
                inset += getLength(CSS.Attribute.PADDING_RIGHT, a);
                break;
            case View.TOP:
                inset += topMargin;
                inset += binsets.top;
                inset += getLength(CSS.Attribute.PADDING_TOP, a);
                break;
            case View.BOTTOM:
                inset += bottomMargin;
                inset += binsets.bottom;
                inset += getLength(CSS.Attribute.PADDING_BOTTOM, a);
                break;
            default:
                throw new IllegalArgumentException("Invalid side: " + side);
            }
            return inset;
        }
        public void paint(Graphics g, float x, float y, float w, float h, View v) {
            float dx = 0;
            float dy = 0;
            float dw = 0;
            float dh = 0;
            AttributeSet a = v.getAttributes();
            boolean isLeftToRight = isLeftToRight(v);
            float localLeftMargin = getOrientationMargin(HorizontalMargin.LEFT,
                                                         leftMargin,
                                                         a, isLeftToRight);
            float localRightMargin = getOrientationMargin(HorizontalMargin.RIGHT,
                                                          rightMargin,
                                                          a, isLeftToRight);
            if (!(v instanceof HTMLEditorKit.HTMLFactory.BodyBlockView)) {
                dx = localLeftMargin;
                dy = topMargin;
                dw = -(localLeftMargin + localRightMargin);
                dh = -(topMargin + bottomMargin);
            }
            if (bg != null) {
                g.setColor(bg);
                g.fillRect((int) (x + dx),
                           (int) (y + dy),
                           (int) (w + dw),
                           (int) (h + dh));
            }
            if (bgPainter != null) {
                bgPainter.paint(g, x + dx, y + dy, w + dw, h + dh, v);
            }
            x += localLeftMargin;
            y += topMargin;
            w -= localLeftMargin + localRightMargin;
            h -= topMargin + bottomMargin;
            if (border instanceof BevelBorder) {
                int bw = (int) getLength(CSS.Attribute.BORDER_TOP_WIDTH, a);
                for (int i = bw - 1; i >= 0; i--) {
                    border.paintBorder(null, g, (int) x + i, (int) y + i,
                                       (int) w - 2 * i, (int) h - 2 * i);
                }
            } else {
                border.paintBorder(null, g, (int) x, (int) y, (int) w, (int) h);
            }
        }
        float getLength(CSS.Attribute key, AttributeSet a) {
            return css.getLength(a, key, ss);
        }
        static boolean isLeftToRight(View v) {
            boolean ret = true;
            if (isOrientationAware(v)) {
                Container container;
                if (v != null && (container = v.getContainer()) != null) {
                    ret = container.getComponentOrientation().isLeftToRight();
                }
            }
            return ret;
        }
        static boolean isOrientationAware(View v) {
            boolean ret = false;
            AttributeSet attr;
            Object obj;
            if (v != null
                && (attr = v.getElement().getAttributes()) != null
                && (obj = attr.getAttribute(StyleConstants.NameAttribute)) instanceof HTML.Tag
                && (obj == HTML.Tag.DIR
                    || obj == HTML.Tag.MENU
                    || obj == HTML.Tag.UL
                    || obj == HTML.Tag.OL)) {
                ret = true;
            }
            return ret;
        }
        static enum HorizontalMargin { LEFT, RIGHT }
        float getOrientationMargin(HorizontalMargin side, float cssMargin,
                                   AttributeSet a, boolean isLeftToRight) {
            float margin = cssMargin;
            float orientationMargin = cssMargin;
            Object cssMarginValue = null;
            switch (side) {
            case RIGHT:
                {
                    orientationMargin = (isLeftToRight) ?
                        getLength(CSS.Attribute.MARGIN_RIGHT_LTR, a) :
                        getLength(CSS.Attribute.MARGIN_RIGHT_RTL, a);
                    cssMarginValue = a.getAttribute(CSS.Attribute.MARGIN_RIGHT);
                }
                break;
            case LEFT :
                {
                    orientationMargin = (isLeftToRight) ?
                        getLength(CSS.Attribute.MARGIN_LEFT_LTR, a) :
                        getLength(CSS.Attribute.MARGIN_LEFT_RTL, a);
                    cssMarginValue = a.getAttribute(CSS.Attribute.MARGIN_LEFT);
                }
                break;
            }
            if (cssMarginValue == null
                && orientationMargin != Integer.MIN_VALUE) {
                margin = orientationMargin;
            }
            return margin;
        }
        float topMargin;
        float bottomMargin;
        float leftMargin;
        float rightMargin;
        short marginFlags;
        Border border;
        Insets binsets;
        CSS css;
        StyleSheet ss;
        Color bg;
        BackgroundImagePainter bgPainter;
    }
    public static class ListPainter implements Serializable {
        ListPainter(AttributeSet attr, StyleSheet ss) {
            this.ss = ss;
            String imgstr = (String)attr.getAttribute(CSS.Attribute.
                                                      LIST_STYLE_IMAGE);
            type = null;
            if (imgstr != null && !imgstr.equals("none")) {
                String tmpstr = null;
                try {
                    StringTokenizer st = new StringTokenizer(imgstr, "()");
                    if (st.hasMoreTokens())
                        tmpstr = st.nextToken();
                    if (st.hasMoreTokens())
                        tmpstr = st.nextToken();
                    URL u = new URL(tmpstr);
                    img = new ImageIcon(u);
                } catch (MalformedURLException e) {
                    if (tmpstr != null && ss != null && ss.getBase() != null) {
                        try {
                            URL u = new URL(ss.getBase(), tmpstr);
                            img = new ImageIcon(u);
                        } catch (MalformedURLException murle) {
                            img = null;
                        }
                    }
                    else {
                        img = null;
                    }
                }
            }
            if (img == null) {
                type = (CSS.Value)attr.getAttribute(CSS.Attribute.
                                                    LIST_STYLE_TYPE);
            }
            start = 1;
            paintRect = new Rectangle();
        }
        private CSS.Value getChildType(View childView) {
            CSS.Value childtype = (CSS.Value)childView.getAttributes().
                                  getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
            if (childtype == null) {
                if (type == null) {
                    View v = childView.getParent();
                    HTMLDocument doc = (HTMLDocument)v.getDocument();
                    if (doc.matchNameAttribute(v.getElement().getAttributes(),
                                               HTML.Tag.OL)) {
                        childtype = CSS.Value.DECIMAL;
                    } else {
                        childtype = CSS.Value.DISC;
                    }
                } else {
                    childtype = type;
                }
            }
            return childtype;
        }
        private void getStart(View parent) {
            checkedForStart = true;
            Element element = parent.getElement();
            if (element != null) {
                AttributeSet attr = element.getAttributes();
                Object startValue;
                if (attr != null && attr.isDefined(HTML.Attribute.START) &&
                    (startValue = attr.getAttribute
                     (HTML.Attribute.START)) != null &&
                    (startValue instanceof String)) {
                    try {
                        start = Integer.parseInt((String)startValue);
                    }
                    catch (NumberFormatException nfe) {}
                }
            }
        }
        private int getRenderIndex(View parentView, int childIndex) {
            if (!checkedForStart) {
                getStart(parentView);
            }
            int retIndex = childIndex;
            for (int counter = childIndex; counter >= 0; counter--) {
                AttributeSet as = parentView.getElement().getElement(counter).
                                  getAttributes();
                if (as.getAttribute(StyleConstants.NameAttribute) !=
                    HTML.Tag.LI) {
                    retIndex--;
                } else if (as.isDefined(HTML.Attribute.VALUE)) {
                    Object value = as.getAttribute(HTML.Attribute.VALUE);
                    if (value != null &&
                        (value instanceof String)) {
                        try {
                            int iValue = Integer.parseInt((String)value);
                            return retIndex - counter + iValue;
                        }
                        catch (NumberFormatException nfe) {}
                    }
                }
            }
            return retIndex + start;
        }
        public void paint(Graphics g, float x, float y, float w, float h, View v, int item) {
            View cv = v.getView(item);
            Container host = v.getContainer();
            Object name = cv.getElement().getAttributes().getAttribute
                         (StyleConstants.NameAttribute);
            if (!(name instanceof HTML.Tag) ||
                name != HTML.Tag.LI) {
                return;
            }
            isLeftToRight =
                host.getComponentOrientation().isLeftToRight();
            float align = 0;
            if (cv.getViewCount() > 0) {
                View pView = cv.getView(0);
                Object cName = pView.getElement().getAttributes().
                               getAttribute(StyleConstants.NameAttribute);
                if ((cName == HTML.Tag.P || cName == HTML.Tag.IMPLIED) &&
                              pView.getViewCount() > 0) {
                    paintRect.setBounds((int)x, (int)y, (int)w, (int)h);
                    Shape shape = cv.getChildAllocation(0, paintRect);
                    if (shape != null && (shape = pView.getView(0).
                                 getChildAllocation(0, shape)) != null) {
                        Rectangle rect = (shape instanceof Rectangle) ?
                                         (Rectangle)shape : shape.getBounds();
                        align = pView.getView(0).getAlignment(View.Y_AXIS);
                        y = rect.y;
                        h = rect.height;
                    }
                }
            }
            Color c = (host.isEnabled()
                ? (ss != null
                    ? ss.getForeground(cv.getAttributes())
                    : host.getForeground())
                : UIManager.getColor("textInactiveText"));
            g.setColor(c);
            if (img != null) {
                drawIcon(g, (int) x, (int) y, (int) w, (int) h, align, host);
                return;
            }
            CSS.Value childtype = getChildType(cv);
            Font font = ((StyledDocument)cv.getDocument()).
                                         getFont(cv.getAttributes());
            if (font != null) {
                g.setFont(font);
            }
            if (childtype == CSS.Value.SQUARE || childtype == CSS.Value.CIRCLE
                || childtype == CSS.Value.DISC) {
                drawShape(g, childtype, (int) x, (int) y,
                          (int) w, (int) h, align);
            } else if (childtype == CSS.Value.DECIMAL) {
                drawLetter(g, '1', (int) x, (int) y, (int) w, (int) h, align,
                           getRenderIndex(v, item));
            } else if (childtype == CSS.Value.LOWER_ALPHA) {
                drawLetter(g, 'a', (int) x, (int) y, (int) w, (int) h, align,
                           getRenderIndex(v, item));
            } else if (childtype == CSS.Value.UPPER_ALPHA) {
                drawLetter(g, 'A', (int) x, (int) y, (int) w, (int) h, align,
                           getRenderIndex(v, item));
            } else if (childtype == CSS.Value.LOWER_ROMAN) {
                drawLetter(g, 'i', (int) x, (int) y, (int) w, (int) h, align,
                           getRenderIndex(v, item));
            } else if (childtype == CSS.Value.UPPER_ROMAN) {
                drawLetter(g, 'I', (int) x, (int) y, (int) w, (int) h, align,
                           getRenderIndex(v, item));
            }
        }
        void drawIcon(Graphics g, int ax, int ay, int aw, int ah,
                      float align, Component c) {
            int gap = isLeftToRight ? - (img.getIconWidth() + bulletgap) :
                                        (aw + bulletgap);
            int x = ax + gap;
            int y = Math.max(ay, ay + (int)(align * ah) -img.getIconHeight());
            img.paintIcon(c, g, x, y);
        }
        void drawShape(Graphics g, CSS.Value type, int ax, int ay, int aw,
                       int ah, float align) {
            int gap = isLeftToRight ? - (bulletgap + 8) : (aw + bulletgap);
            int x = ax + gap;
            int y = Math.max(ay, ay + (int)(align * ah) - 8);
            if (type == CSS.Value.SQUARE) {
                g.drawRect(x, y, 8, 8);
            } else if (type == CSS.Value.CIRCLE) {
                g.drawOval(x, y, 8, 8);
            } else {
                g.fillOval(x, y, 8, 8);
            }
        }
        void drawLetter(Graphics g, char letter, int ax, int ay, int aw,
                        int ah, float align, int index) {
            String str = formatItemNum(index, letter);
            str = isLeftToRight ? str + "." : "." + str;
            FontMetrics fm = SwingUtilities2.getFontMetrics(null, g);
            int stringwidth = SwingUtilities2.stringWidth(null, fm, str);
            int gap = isLeftToRight ? - (stringwidth + bulletgap) :
                                        (aw + bulletgap);
            int x = ax + gap;
            int y = Math.max(ay + fm.getAscent(), ay + (int)(ah * align));
            SwingUtilities2.drawString(null, g, str, x, y);
        }
        String formatItemNum(int itemNum, char type) {
            String numStyle = "1";
            boolean uppercase = false;
            String formattedNum;
            switch (type) {
            case '1':
            default:
                formattedNum = String.valueOf(itemNum);
                break;
            case 'A':
                uppercase = true;
            case 'a':
                formattedNum = formatAlphaNumerals(itemNum);
                break;
            case 'I':
                uppercase = true;
            case 'i':
                formattedNum = formatRomanNumerals(itemNum);
            }
            if (uppercase) {
                formattedNum = formattedNum.toUpperCase();
            }
            return formattedNum;
        }
        String formatAlphaNumerals(int itemNum) {
            String result;
            if (itemNum > 26) {
                result = formatAlphaNumerals(itemNum / 26) +
                    formatAlphaNumerals(itemNum % 26);
            } else {
                result = String.valueOf((char)('a' + itemNum - 1));
            }
            return result;
        }
        static final char romanChars[][] = {
            {'i', 'v'},
            {'x', 'l' },
            {'c', 'd' },
            {'m', '?' },
        };
        String formatRomanNumerals(int num) {
            return formatRomanNumerals(0, num);
        }
        String formatRomanNumerals(int level, int num) {
            if (num < 10) {
                return formatRomanDigit(level, num);
            } else {
                return formatRomanNumerals(level + 1, num / 10) +
                    formatRomanDigit(level, num % 10);
            }
        }
        String formatRomanDigit(int level, int digit) {
            String result = "";
            if (digit == 9) {
                result = result + romanChars[level][0];
                result = result + romanChars[level + 1][0];
                return result;
            } else if (digit == 4) {
                result = result + romanChars[level][0];
                result = result + romanChars[level][1];
                return result;
            } else if (digit >= 5) {
                result = result + romanChars[level][1];
                digit -= 5;
            }
            for (int i = 0; i < digit; i++) {
                result = result + romanChars[level][0];
            }
            return result;
        }
        private Rectangle paintRect;
        private boolean checkedForStart;
        private int start;
        private CSS.Value type;
        URL imageurl;
        private StyleSheet ss = null;
        Icon img = null;
        private int bulletgap = 5;
        private boolean isLeftToRight;
    }
    static class BackgroundImagePainter implements Serializable {
        ImageIcon   backgroundImage;
        float       hPosition;
        float       vPosition;
        short       flags;
        private int paintX;
        private int paintY;
        private int paintMaxX;
        private int paintMaxY;
        BackgroundImagePainter(AttributeSet a, CSS css, StyleSheet ss) {
            backgroundImage = ss.getBackgroundImage(a);
            CSS.BackgroundPosition pos = (CSS.BackgroundPosition)a.getAttribute
                                           (CSS.Attribute.BACKGROUND_POSITION);
            if (pos != null) {
                hPosition = pos.getHorizontalPosition();
                vPosition = pos.getVerticalPosition();
                if (pos.isHorizontalPositionRelativeToSize()) {
                    flags |= 4;
                }
                else if (pos.isHorizontalPositionRelativeToSize()) {
                    hPosition *= css.getFontSize(a, 12, ss);
                }
                if (pos.isVerticalPositionRelativeToSize()) {
                    flags |= 8;
                }
                else if (pos.isVerticalPositionRelativeToFontSize()) {
                    vPosition *= css.getFontSize(a, 12, ss);
                }
            }
            CSS.Value repeats = (CSS.Value)a.getAttribute(CSS.Attribute.
                                                          BACKGROUND_REPEAT);
            if (repeats == null || repeats == CSS.Value.BACKGROUND_REPEAT) {
                flags |= 3;
            }
            else if (repeats == CSS.Value.BACKGROUND_REPEAT_X) {
                flags |= 1;
            }
            else if (repeats == CSS.Value.BACKGROUND_REPEAT_Y) {
                flags |= 2;
            }
        }
        void paint(Graphics g, float x, float y, float w, float h, View v) {
            Rectangle clip = g.getClipRect();
            if (clip != null) {
                g.clipRect((int)x, (int)y, (int)w, (int)h);
            }
            if ((flags & 3) == 0) {
                int width = backgroundImage.getIconWidth();
                int height = backgroundImage.getIconWidth();
                if ((flags & 4) == 4) {
                    paintX = (int)(x + w * hPosition -
                                  (float)width * hPosition);
                }
                else {
                    paintX = (int)x + (int)hPosition;
                }
                if ((flags & 8) == 8) {
                    paintY = (int)(y + h * vPosition -
                                  (float)height * vPosition);
                }
                else {
                    paintY = (int)y + (int)vPosition;
                }
                if (clip == null ||
                    !((paintX + width <= clip.x) ||
                      (paintY + height <= clip.y) ||
                      (paintX >= clip.x + clip.width) ||
                      (paintY >= clip.y + clip.height))) {
                    backgroundImage.paintIcon(null, g, paintX, paintY);
                }
            }
            else {
                int width = backgroundImage.getIconWidth();
                int height = backgroundImage.getIconHeight();
                if (width > 0 && height > 0) {
                    paintX = (int)x;
                    paintY = (int)y;
                    paintMaxX = (int)(x + w);
                    paintMaxY = (int)(y + h);
                    if (updatePaintCoordinates(clip, width, height)) {
                        while (paintX < paintMaxX) {
                            int ySpot = paintY;
                            while (ySpot < paintMaxY) {
                                backgroundImage.paintIcon(null, g, paintX,
                                                          ySpot);
                                ySpot += height;
                            }
                            paintX += width;
                        }
                    }
                }
            }
            if (clip != null) {
                g.setClip(clip.x, clip.y, clip.width, clip.height);
            }
        }
        private boolean updatePaintCoordinates
                 (Rectangle clip, int width, int height){
            if ((flags & 3) == 1) {
                paintMaxY = paintY + 1;
            }
            else if ((flags & 3) == 2) {
                paintMaxX = paintX + 1;
            }
            if (clip != null) {
                if ((flags & 3) == 1 && ((paintY + height <= clip.y) ||
                                         (paintY > clip.y + clip.height))) {
                    return false;
                }
                if ((flags & 3) == 2 && ((paintX + width <= clip.x) ||
                                         (paintX > clip.x + clip.width))) {
                    return false;
                }
                if ((flags & 1) == 1) {
                    if ((clip.x + clip.width) < paintMaxX) {
                        if ((clip.x + clip.width - paintX) % width == 0) {
                            paintMaxX = clip.x + clip.width;
                        }
                        else {
                            paintMaxX = ((clip.x + clip.width - paintX) /
                                         width + 1) * width + paintX;
                        }
                    }
                    if (clip.x > paintX) {
                        paintX = (clip.x - paintX) / width * width + paintX;
                    }
                }
                if ((flags & 2) == 2) {
                    if ((clip.y + clip.height) < paintMaxY) {
                        if ((clip.y + clip.height - paintY) % height == 0) {
                            paintMaxY = clip.y + clip.height;
                        }
                        else {
                            paintMaxY = ((clip.y + clip.height - paintY) /
                                         height + 1) * height + paintY;
                        }
                    }
                    if (clip.y > paintY) {
                        paintY = (clip.y - paintY) / height * height + paintY;
                    }
                }
            }
            return true;
        }
    }
    class ViewAttributeSet extends MuxingAttributeSet {
        ViewAttributeSet(View v) {
            host = v;
            Document doc = v.getDocument();
            SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
            Vector<AttributeSet> muxList = sb.getVector();
            try {
                if (doc instanceof HTMLDocument) {
                    StyleSheet styles = StyleSheet.this;
                    Element elem = v.getElement();
                    AttributeSet a = elem.getAttributes();
                    AttributeSet htmlAttr = styles.translateHTMLToCSS(a);
                    if (htmlAttr.getAttributeCount() != 0) {
                        muxList.addElement(htmlAttr);
                    }
                    if (elem.isLeaf()) {
                        Enumeration keys = a.getAttributeNames();
                        while (keys.hasMoreElements()) {
                            Object key = keys.nextElement();
                            if (key instanceof HTML.Tag) {
                                if (key == HTML.Tag.A) {
                                    Object o = a.getAttribute(key);
                                    if (o != null && o instanceof AttributeSet) {
                                        AttributeSet attr = (AttributeSet)o;
                                        if (attr.getAttribute(HTML.Attribute.HREF) == null) {
                                            continue;
                                        }
                                    }
                                }
                                AttributeSet cssRule = styles.getRule((HTML.Tag) key, elem);
                                if (cssRule != null) {
                                    muxList.addElement(cssRule);
                                }
                            }
                        }
                    } else {
                        HTML.Tag t = (HTML.Tag) a.getAttribute
                                     (StyleConstants.NameAttribute);
                        AttributeSet cssRule = styles.getRule(t, elem);
                        if (cssRule != null) {
                            muxList.addElement(cssRule);
                        }
                    }
                }
                AttributeSet[] attrs = new AttributeSet[muxList.size()];
                muxList.copyInto(attrs);
                setAttributes(attrs);
            }
            finally {
                SearchBuffer.releaseSearchBuffer(sb);
            }
        }
        public boolean isDefined(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                                    ((StyleConstants)key);
                if (cssKey != null) {
                    key = cssKey;
                }
            }
            return super.isDefined(key);
        }
        public Object getAttribute(Object key) {
            if (key instanceof StyleConstants) {
                Object cssKey = css.styleConstantsKeyToCSSKey
                               ((StyleConstants)key);
                if (cssKey != null) {
                    Object value = doGetAttribute(cssKey);
                    if (value instanceof CSS.CssValue) {
                        return ((CSS.CssValue)value).toStyleConstants
                                     ((StyleConstants)key, host);
                    }
                }
            }
            return doGetAttribute(key);
        }
        Object doGetAttribute(Object key) {
            Object retValue = super.getAttribute(key);
            if (retValue != null) {
                return retValue;
            }
            if (key instanceof CSS.Attribute) {
                CSS.Attribute css = (CSS.Attribute) key;
                if (css.isInherited()) {
                    AttributeSet parent = getResolveParent();
                    if (parent != null)
                        return parent.getAttribute(key);
                }
            }
            return null;
        }
        public AttributeSet getResolveParent() {
            if (host == null) {
                return null;
            }
            View parent = host.getParent();
            return (parent != null) ? parent.getAttributes() : null;
        }
        View host;
    }
    static class ResolvedStyle extends MuxingAttributeSet implements
                  Serializable, Style {
        ResolvedStyle(String name, AttributeSet[] attrs, int extendedIndex) {
            super(attrs);
            this.name = name;
            this.extendedIndex = extendedIndex;
        }
        synchronized void insertStyle(Style style, int specificity) {
            AttributeSet[] attrs = getAttributes();
            int maxCounter = attrs.length;
            int counter = 0;
            for (;counter < extendedIndex; counter++) {
                if (specificity > getSpecificity(((Style)attrs[counter]).
                                                 getName())) {
                    break;
                }
            }
            insertAttributeSetAt(style, counter);
            extendedIndex++;
        }
        synchronized void removeStyle(Style style) {
            AttributeSet[] attrs = getAttributes();
            for (int counter = attrs.length - 1; counter >= 0; counter--) {
                if (attrs[counter] == style) {
                    removeAttributeSetAt(counter);
                    if (counter < extendedIndex) {
                        extendedIndex--;
                    }
                    break;
                }
            }
        }
        synchronized void insertExtendedStyleAt(Style attr, int index) {
            insertAttributeSetAt(attr, extendedIndex + index);
        }
        synchronized void addExtendedStyle(Style attr) {
            insertAttributeSetAt(attr, getAttributes().length);
        }
        synchronized void removeExtendedStyleAt(int index) {
            removeAttributeSetAt(extendedIndex + index);
        }
        protected boolean matches(String selector) {
            int sLast = selector.length();
            if (sLast == 0) {
                return false;
            }
            int thisLast = name.length();
            int sCurrent = selector.lastIndexOf(' ');
            int thisCurrent = name.lastIndexOf(' ');
            if (sCurrent >= 0) {
                sCurrent++;
            }
            if (thisCurrent >= 0) {
                thisCurrent++;
            }
            if (!matches(selector, sCurrent, sLast, thisCurrent, thisLast)) {
                return false;
            }
            while (sCurrent != -1) {
                sLast = sCurrent - 1;
                sCurrent = selector.lastIndexOf(' ', sLast - 1);
                if (sCurrent >= 0) {
                    sCurrent++;
                }
                boolean match = false;
                while (!match && thisCurrent != -1) {
                    thisLast = thisCurrent - 1;
                    thisCurrent = name.lastIndexOf(' ', thisLast - 1);
                    if (thisCurrent >= 0) {
                        thisCurrent++;
                    }
                    match = matches(selector, sCurrent, sLast, thisCurrent,
                                    thisLast);
                }
                if (!match) {
                    return false;
                }
            }
            return true;
        }
        boolean matches(String selector, int sCurrent, int sLast,
                       int thisCurrent, int thisLast) {
            sCurrent = Math.max(sCurrent, 0);
            thisCurrent = Math.max(thisCurrent, 0);
            int thisDotIndex = boundedIndexOf(name, '.', thisCurrent,
                                              thisLast);
            int thisPoundIndex = boundedIndexOf(name, '#', thisCurrent,
                                                thisLast);
            int sDotIndex = boundedIndexOf(selector, '.', sCurrent, sLast);
            int sPoundIndex = boundedIndexOf(selector, '#', sCurrent, sLast);
            if (sDotIndex != -1) {
                if (thisDotIndex == -1) {
                    return false;
                }
                if (sCurrent == sDotIndex) {
                    if ((thisLast - thisDotIndex) != (sLast - sDotIndex) ||
                        !selector.regionMatches(sCurrent, name, thisDotIndex,
                                                (thisLast - thisDotIndex))) {
                        return false;
                    }
                }
                else {
                    if ((sLast - sCurrent) != (thisLast - thisCurrent) ||
                        !selector.regionMatches(sCurrent, name, thisCurrent,
                                                (thisLast - thisCurrent))) {
                        return false;
                    }
                }
                return true;
            }
            if (sPoundIndex != -1) {
                if (thisPoundIndex == -1) {
                    return false;
                }
                if (sCurrent == sPoundIndex) {
                    if ((thisLast - thisPoundIndex) !=(sLast - sPoundIndex) ||
                        !selector.regionMatches(sCurrent, name, thisPoundIndex,
                                                (thisLast - thisPoundIndex))) {
                        return false;
                    }
                }
                else {
                    if ((sLast - sCurrent) != (thisLast - thisCurrent) ||
                        !selector.regionMatches(sCurrent, name, thisCurrent,
                                               (thisLast - thisCurrent))) {
                        return false;
                    }
                }
                return true;
            }
            if (thisDotIndex != -1) {
                return (((thisDotIndex - thisCurrent) == (sLast - sCurrent)) &&
                        selector.regionMatches(sCurrent, name, thisCurrent,
                                               thisDotIndex - thisCurrent));
            }
            if (thisPoundIndex != -1) {
                return (((thisPoundIndex - thisCurrent) ==(sLast - sCurrent))&&
                        selector.regionMatches(sCurrent, name, thisCurrent,
                                               thisPoundIndex - thisCurrent));
            }
            return (((thisLast - thisCurrent) == (sLast - sCurrent)) &&
                    selector.regionMatches(sCurrent, name, thisCurrent,
                                           thisLast - thisCurrent));
        }
        int boundedIndexOf(String string, char search, int start,
                           int end) {
            int retValue = string.indexOf(search, start);
            if (retValue >= end) {
                return -1;
            }
            return retValue;
        }
        public void addAttribute(Object name, Object value) {}
        public void addAttributes(AttributeSet attributes) {}
        public void removeAttribute(Object name) {}
        public void removeAttributes(Enumeration<?> names) {}
        public void removeAttributes(AttributeSet attributes) {}
        public void setResolveParent(AttributeSet parent) {}
        public String getName() {return name;}
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
        public ChangeListener[] getChangeListeners() {
            return new ChangeListener[0];
        }
        String name;
        private int extendedIndex;
    }
    static class SelectorMapping implements Serializable {
        public SelectorMapping(int specificity) {
            this.specificity = specificity;
        }
        public int getSpecificity() {
            return specificity;
        }
        public void setStyle(Style style) {
            this.style = style;
        }
        public Style getStyle() {
            return style;
        }
        public SelectorMapping getChildSelectorMapping(String selector,
                                                       boolean create) {
            SelectorMapping retValue = null;
            if (children != null) {
                retValue = children.get(selector);
            }
            else if (create) {
                children = new HashMap<String, SelectorMapping>(7);
            }
            if (retValue == null && create) {
                int specificity = getChildSpecificity(selector);
                retValue = createChildSelectorMapping(specificity);
                children.put(selector, retValue);
            }
            return retValue;
        }
        protected SelectorMapping createChildSelectorMapping(int specificity) {
            return new SelectorMapping(specificity);
        }
        protected int getChildSpecificity(String selector) {
            char    firstChar = selector.charAt(0);
            int     specificity = getSpecificity();
            if (firstChar == '.') {
                specificity += 100;
            }
            else if (firstChar == '#') {
                specificity += 10000;
            }
            else {
                specificity += 1;
                if (selector.indexOf('.') != -1) {
                    specificity += 100;
                }
                if (selector.indexOf('#') != -1) {
                    specificity += 10000;
                }
            }
            return specificity;
        }
        private int specificity;
        private Style style;
        private HashMap<String, SelectorMapping> children;
    }
    final static int DEFAULT_FONT_SIZE = 3;
    private CSS css;
    private SelectorMapping selectorMapping;
    private Hashtable<String, ResolvedStyle> resolvedStyles;
    private Vector<StyleSheet> linkedStyleSheets;
    private URL base;
    class CssParser implements CSSParser.CSSParserCallback {
        public AttributeSet parseDeclaration(String string) {
            try {
                return parseDeclaration(new StringReader(string));
            } catch (IOException ioe) {}
            return null;
        }
        public AttributeSet parseDeclaration(Reader r) throws IOException {
            parse(base, r, true, false);
            return declaration.copyAttributes();
        }
        public void parse(URL base, Reader r, boolean parseDeclaration,
                          boolean isLink) throws IOException {
            this.base = base;
            this.isLink = isLink;
            this.parsingDeclaration = parseDeclaration;
            declaration.removeAttributes(declaration);
            selectorTokens.removeAllElements();
            selectors.removeAllElements();
            propertyName = null;
            parser.parse(r, this, parseDeclaration);
        }
        public void handleImport(String importString) {
            URL url = CSS.getURL(base, importString);
            if (url != null) {
                importStyleSheet(url);
            }
        }
        public void handleSelector(String selector) {
            if (!(selector.startsWith(".")
                  || selector.startsWith("#"))) {
                selector = selector.toLowerCase();
            }
            int length = selector.length();
            if (selector.endsWith(",")) {
                if (length > 1) {
                    selector = selector.substring(0, length - 1);
                    selectorTokens.addElement(selector);
                }
                addSelector();
            }
            else if (length > 0) {
                selectorTokens.addElement(selector);
            }
        }
        public void startRule() {
            if (selectorTokens.size() > 0) {
                addSelector();
            }
            propertyName = null;
        }
        public void handleProperty(String property) {
            propertyName = property;
        }
        public void handleValue(String value) {
            if (propertyName != null && value != null && value.length() > 0) {
                CSS.Attribute cssKey = CSS.getAttribute(propertyName);
                if (cssKey != null) {
                    if (cssKey == CSS.Attribute.LIST_STYLE_IMAGE) {
                        if (value != null && !value.equals("none")) {
                            URL url = CSS.getURL(base, value);
                            if (url != null) {
                                value = url.toString();
                            }
                        }
                    }
                    addCSSAttribute(declaration, cssKey, value);
                }
                propertyName = null;
            }
        }
        public void endRule() {
            int n = selectors.size();
            for (int i = 0; i < n; i++) {
                String[] selector = selectors.elementAt(i);
                if (selector.length > 0) {
                    StyleSheet.this.addRule(selector, declaration, isLink);
                }
            }
            declaration.removeAttributes(declaration);
            selectors.removeAllElements();
        }
        private void addSelector() {
            String[] selector = new String[selectorTokens.size()];
            selectorTokens.copyInto(selector);
            selectors.addElement(selector);
            selectorTokens.removeAllElements();
        }
        Vector<String[]> selectors = new Vector<String[]>();
        Vector<String> selectorTokens = new Vector<String>();
        String propertyName;
        MutableAttributeSet declaration = new SimpleAttributeSet();
        boolean parsingDeclaration;
        boolean isLink;
        URL base;
        CSSParser parser = new CSSParser();
    }
    void rebaseSizeMap(int base) {
        final int minimalFontSize = 4;
        sizeMap = new int[sizeMapDefault.length];
        for (int i = 0; i < sizeMapDefault.length; i++) {
            sizeMap[i] = Math.max(base * sizeMapDefault[i] /
                                  sizeMapDefault[CSS.baseFontSizeIndex],
                                  minimalFontSize);
        }
    }
    int[] getSizeMap() {
        return sizeMap;
    }
    boolean isW3CLengthUnits() {
        return w3cLengthUnits;
    }
    static final int sizeMapDefault[] = { 8, 10, 12, 14, 18, 24, 36 };
    private int sizeMap[] = sizeMapDefault;
    private boolean w3cLengthUnits = false;
}
