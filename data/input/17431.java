public class DiffGraphCookie implements Node.Cookie {
    private InputGraph a;
    private InputGraph b;
    public DiffGraphCookie(InputGraph a, InputGraph b) {
        this.a = a;
        this.b = b;
    }
    public void openDiff() {
        final GraphViewer viewer = Lookup.getDefault().lookup(GraphViewer.class);
        if(viewer != null) {
            InputGraph diffGraph = Difference.createDiffGraph(a, b);
            viewer.view(diffGraph);
        }
    }
}
