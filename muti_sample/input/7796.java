class DefaultSynthStyleFactory extends SynthStyleFactory {
    public static final int NAME = 0;
    public static final int REGION = 1;
    private List<StyleAssociation> _styles;
    private BakedArrayList _tmpList;
    private Map<BakedArrayList, SynthStyle> _resolvedStyles;
    private SynthStyle _defaultStyle;
    DefaultSynthStyleFactory() {
        _tmpList = new BakedArrayList(5);
        _styles = new ArrayList<StyleAssociation>();
        _resolvedStyles = new HashMap<BakedArrayList, SynthStyle>();
    }
    public synchronized void addStyle(DefaultSynthStyle style,
                         String path, int type) throws PatternSyntaxException {
        if (path == null) {
            path = ".*";
        }
        if (type == NAME) {
            _styles.add(StyleAssociation.createStyleAssociation(
                            path, style, type));
        }
        else if (type == REGION) {
            _styles.add(StyleAssociation.createStyleAssociation(
                            path.toLowerCase(), style, type));
        }
    }
    public synchronized SynthStyle getStyle(JComponent c, Region id) {
        BakedArrayList matches = _tmpList;
        matches.clear();
        getMatchingStyles(matches, c, id);
        if (matches.size() == 0) {
            return getDefaultStyle();
        }
        matches.cacheHashCode();
        SynthStyle style = getCachedStyle(matches);
        if (style == null) {
            style = mergeStyles(matches);
            if (style != null) {
                cacheStyle(matches, style);
            }
        }
        return style;
    }
    private SynthStyle getDefaultStyle() {
        if (_defaultStyle == null) {
            _defaultStyle = new DefaultSynthStyle();
            ((DefaultSynthStyle)_defaultStyle).setFont(
                new FontUIResource(Font.DIALOG, Font.PLAIN,12));
        }
        return _defaultStyle;
    }
    private void getMatchingStyles(List matches, JComponent c,
                                   Region id) {
        String idName = id.getLowerCaseName();
        String cName = c.getName();
        if (cName == null) {
            cName = "";
        }
        for (int counter = _styles.size() - 1; counter >= 0; counter--){
            StyleAssociation sa = _styles.get(counter);
            String path;
            if (sa.getID() == NAME) {
                path = cName;
            }
            else {
                path = idName;
            }
            if (sa.matches(path) && matches.indexOf(sa.getStyle()) == -1) {
                matches.add(sa.getStyle());
            }
        }
    }
    private void cacheStyle(List styles, SynthStyle style) {
        BakedArrayList cachedStyles = new BakedArrayList(styles);
        _resolvedStyles.put(cachedStyles, style);
    }
    private SynthStyle getCachedStyle(List styles) {
        if (styles.size() == 0) {
            return null;
        }
        return _resolvedStyles.get(styles);
    }
    private SynthStyle mergeStyles(List styles) {
        int size = styles.size();
        if (size == 0) {
            return null;
        }
        else if (size == 1) {
            return (SynthStyle)((DefaultSynthStyle)styles.get(0)).clone();
        }
        DefaultSynthStyle style = (DefaultSynthStyle)styles.get(size - 1);
        style = (DefaultSynthStyle)style.clone();
        for (int counter = size - 2; counter >= 0; counter--) {
            style = ((DefaultSynthStyle)styles.get(counter)).addTo(style);
        }
        return style;
    }
}
