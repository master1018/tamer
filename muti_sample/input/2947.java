public class RemoveSelfLoopsFilter extends AbstractFilter {
    private String name;
    public RemoveSelfLoopsFilter(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void apply(Diagram d) {
        for (Figure f : d.getFigures()) {
            for (InputSlot is : f.getInputSlots()) {
                List<Connection> toRemove = new ArrayList<Connection>();
                for (Connection c : is.getConnections()) {
                    if (c.getOutputSlot().getFigure() == f) {
                        toRemove.add(c);
                    }
                }
                for (Connection c : toRemove) {
                    c.remove();
                    OutputSlot os = c.getOutputSlot();
                    if (os.getConnections().size() == 0) {
                        f.removeSlot(os);
                    }
                    c.getInputSlot().setShortName("O");
                    c.getInputSlot().setName("Self Loop");
                }
            }
        }
    }
}
