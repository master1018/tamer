public class OrSelector implements Selector {
    private Selector selector1;
    private Selector selector2;
    public OrSelector(Selector s1, Selector s2) {
        this.selector1 = s1;
        this.selector2 = s2;
    }
    public List<Figure> selected(Diagram d) {
        List<Figure> l1 = selector1.selected(d);
        List<Figure> l2 = selector2.selected(d);
        for (Figure f : l2) {
            if (!l1.contains(f)) {
                l1.add(f);
            }
        }
        return l1;
    }
}
