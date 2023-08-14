public class Block implements Cluster {
    private InputBlock inputBlock;
    private Rectangle bounds;
    private Diagram diagram;
    public Block(InputBlock inputBlock, Diagram diagram) {
        this.inputBlock = inputBlock;
        this.diagram = diagram;
    }
    public Cluster getOuter() {
        return null;
    }
    public InputBlock getInputBlock() {
        return inputBlock;
    }
    public Set<? extends Cluster> getSuccessors() {
        Set<Block> succs = new HashSet<Block>();
        for (InputBlock b : inputBlock.getSuccessors()) {
            succs.add(diagram.getBlock(b));
        }
        return succs;
    }
    public Set<? extends Cluster> getPredecessors() {
        Set<Block> succs = new HashSet<Block>();
        for (InputBlock b : inputBlock.getPredecessors()) {
            succs.add(diagram.getBlock(b));
        }
        return succs;
    }
    public void setBounds(Rectangle r) {
        this.bounds = r;
    }
    public Rectangle getBounds() {
        return bounds;
    }
    public int compareTo(Cluster o) {
        return toString().compareTo(o.toString());
    }
}
