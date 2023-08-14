public class StyleAssociation {
    private SynthStyle _style;
    private Pattern _pattern;
    private Matcher _matcher;
    private int _id;
    public static StyleAssociation createStyleAssociation(
        String text, SynthStyle style)
        throws PatternSyntaxException {
        return createStyleAssociation(text, style, 0);
    }
    public static StyleAssociation createStyleAssociation(
        String text, SynthStyle style, int id)
        throws PatternSyntaxException {
        return new StyleAssociation(text, style, id);
    }
    private StyleAssociation(String text, SynthStyle style, int id)
                 throws PatternSyntaxException {
        _style = style;
        _pattern = Pattern.compile(text);
        _id = id;
    }
    public int getID() {
        return _id;
    }
    public synchronized boolean matches(CharSequence path) {
        if (_matcher == null) {
            _matcher = _pattern.matcher(path);
        }
        else {
            _matcher.reset(path);
        }
        return _matcher.matches();
    }
    public String getText() {
        return _pattern.pattern();
    }
    public SynthStyle getStyle() {
        return _style;
    }
}
