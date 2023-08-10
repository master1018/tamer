public class PlaceView extends VertexView {
    private static final long serialVersionUID = -7064431429428811576L;
    private PlaceRenderer RENDERER = new PlaceRenderer();
    public PlaceView(Object cell) {
        super(cell);
    }
    public CellViewRenderer getRenderer() {
        return RENDERER;
    }
}
