public abstract class AwtGraphicManager extends GraphicManager {
    public AwtGraphicManager(String appName, GraphicType type) {
        super(appName, type);
    }
    protected abstract Component newNativeCanvas(CanvasRenderer renderer);
    public GraphicView newGraphicView(GraphicModel model, GraphicInput input) {
        AssertUtils.assertNonNullArg(model);
        AssertUtils.assertNonNullArg(input);
        GraphicRenderer renderer = newGraphicRenderer(model);
        GraphicView view = new GraphicView(renderer, input);
        Component nativeCanvas = newNativeCanvas(renderer.getNativePeer());
        view.setGraphicCanvas(new AwtGraphicCanvas(view, (Canvas) nativeCanvas));
        addGraphicView(view);
        nativeCanvas.setSize(new Dimension(1, 1));
        AwtKeyboardWrapper keyboardWrapper = new AwtKeyboardWrapper(nativeCanvas);
        MouseManager mouseManager = new AwtMouseManager(nativeCanvas);
        AwtMouseWrapper mouseWrapper = new AwtMouseWrapper(nativeCanvas, mouseManager);
        AwtFocusWrapper focusWrapper = new AwtFocusWrapper(nativeCanvas);
        PhysicalLayer physicalLayer = new PhysicalLayer(keyboardWrapper, mouseWrapper, focusWrapper);
        input.getLogicalLayer().registerInput((Canvas) nativeCanvas, physicalLayer);
        return view;
    }
}
