public class BlockConnectionWidget extends ConnectionWidget implements Link {
    private BlockWidget from;
    private BlockWidget to;
    private Port inputSlot;
    private Port outputSlot;
    private List<Point> points;
    private InputBlockEdge edge;
    public BlockConnectionWidget(ControlFlowScene scene, InputBlockEdge edge) {
        super(scene);
        this.edge = edge;
        this.from = (BlockWidget) scene.findWidget(edge.getFrom());
        this.to = (BlockWidget) scene.findWidget(edge.getTo());
        inputSlot = to.getInputSlot();
        outputSlot = from.getOutputSlot();
        points = new ArrayList<Point>();
    }
    public InputBlockEdge getEdge() {
        return edge;
    }
    public Port getTo() {
        return inputSlot;
    }
    public Port getFrom() {
        return outputSlot;
    }
    public void setControlPoints(List<Point> p) {
        this.points = p;
    }
    @Override
    public List<Point> getControlPoints() {
        return points;
    }
    @Override
    public String toString() {
        return "Connection[ " + from.toString() + " - " + to.toString() + "]";
    }
}
