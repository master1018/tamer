public class SplitFilter extends AbstractFilter {
    private String name;
    private Selector selector;
    public SplitFilter(String name, Selector selector) {
        this.name = name;
        this.selector = selector;
    }
    public String getName() {
        return name;
    }
    public void apply(Diagram d) {
        List<Figure> list = selector.selected(d);
        for (Figure f : list) {
            for (OutputSlot os : f.getOutputSlots()) {
                for (Connection c : os.getConnections()) {
                    InputSlot is = c.getInputSlot();
                    is.setName(f.getProperties().get("dump_spec"));
                    String s = f.getProperties().get("short_name");
                    if (s != null) {
                        is.setShortName(s);
                    }
                }
            }
            d.removeFigure(f);
        }
    }
}
