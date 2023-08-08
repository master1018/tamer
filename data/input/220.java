public class VisitorFunctor implements Visitor, AbstractVisitorFunctor {
    private DoFunctor functor;
    private int depth;
    public VisitorFunctor() {
    }
    public VisitorFunctor(DoFunctor functor) {
        this();
        this.functor = functor;
    }
    public void reset() {
        depth = 0;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
    public boolean shouldVisit(DataObject object) {
        assert object != null;
        return true;
    }
    public Object getFunctor() {
        return functor;
    }
    public void setFunctor(Object functor) {
        this.functor = (DoFunctor) functor;
    }
    public void visit(Contact item) {
        functor.execute(item);
    }
    public void visit(RootClass item) {
        functor.execute(item);
    }
}
