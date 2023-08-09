public class MatcherSelector implements Selector {
    private PropertyMatcher matcher;
    public MatcherSelector(PropertyMatcher matcher) {
        this.matcher = matcher;
    }
    public List<Figure> selected(Diagram d) {
        Properties.PropertySelector<Figure> selector = new Properties.PropertySelector<Figure>(d.getFigures());
        List<Figure> list = selector.selectMultiple(matcher);
        return list;
    }
}
