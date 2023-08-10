public class DefaultLinkEditor extends DefaultFigureEditor {
    protected DefaultLabelRenderer renderer;
    public DefaultLinkEditor() {
        this(new DefaultLabelRenderer());
    }
    public DefaultLinkEditor(DefaultLabelRenderer renderer) {
        super(renderer);
        this.renderer = (DefaultLabelRenderer) getComponent();
    }
    public Component getFigureEditorComponent(Diagram diagram, Figure figure, boolean isSelected) {
        return renderer.getRendererComponent(diagram, figure, false);
    }
    public Rectangle2D getDecoratedBounds(Diagram diagram, Figure figure, Rectangle2D rcBounds) {
        return renderer.getDecoratedBounds(diagram, figure, rcBounds);
    }
}
