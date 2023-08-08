public class Subsegment {
    public Vector pos;
    public double height;
    public double rad;
    public Subsegment(Vector p, double r, double h) {
        pos = p;
        rad = r;
        height = h;
    }
    public boolean traverseStem(StemTraversal traversal) throws TraversalException {
        return traversal.visitSubsegment(this);
    }
}
