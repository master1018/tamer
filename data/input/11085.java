public class InvertSelector implements Selector {
    private Selector selector;
    public InvertSelector(Selector selector) {
        this.selector = selector;
    }
    public List<Figure> selected(Diagram d) {
        List<Figure> result = new ArrayList<Figure>();
        List<Figure> otherResult = selector.selected(d);
        for (Figure f : d.getFigures()) {
            if (!otherResult.contains(f)) {
                result.add(f);
            }
        }
        return result;
    }
}
