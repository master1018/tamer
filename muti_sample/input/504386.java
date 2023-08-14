public class UiDocumentEditPart extends UiElementEditPart {
    private Display mDisplay;
    private FreeformLayer mLayer;
    private ImageBackground mImage;
    private Label mChild = null;
    final static class ImageBackground extends AbstractBackground {
        private BufferedImage mBufferedImage;
        private Image mImage;
        ImageBackground() {
        }
        ImageBackground(BufferedImage image, Display display) {
            setImage(image, display);
        }
        @Override
        public void paintBackground(IFigure figure, Graphics graphics, Insets insets) {
            if (mImage != null) {
                Rectangle rect = getPaintRectangle(figure, insets);
                graphics.drawImage(mImage, rect.x, rect.y);
            }
        }
        void setImage(BufferedImage image, Display display) {
            if (image != null) {
                int[] data = ((DataBufferInt)image.getData().getDataBuffer()).getData();
                ImageData imageData = new ImageData(image.getWidth(), image.getHeight(), 32,
                      new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF));
                imageData.setPixels(0, 0, data.length, data, 0);
                mImage = new Image(display, imageData);
            } else {
                mImage = null;
            }
        }
        BufferedImage getBufferedImage() {
            return mBufferedImage;
        }
    }
    public UiDocumentEditPart(UiDocumentNode uiDocumentNode, Display display) {
        super(uiDocumentNode);
        mDisplay = display;
    }
    @Override
    protected IFigure createFigure() {
        mLayer = new FreeformLayer();
        mLayer.setLayoutManager(new FreeformLayout());
        mLayer.setOpaque(true);
        mLayer.setBackgroundColor(ColorConstants.lightGray);
        return mLayer;
    }
    @Override
    protected void refreshVisuals() {
        UiElementNode model = (UiElementNode)getModel();
        Object editData = model.getEditData();
        if (editData instanceof BufferedImage) {
            BufferedImage image = (BufferedImage)editData;
            if (mImage == null || image != mImage.getBufferedImage()) {
                mImage = new ImageBackground(image, mDisplay);
            }
            mLayer.setBorder(mImage);
            if (mChild != null && mChild.getParent() == mLayer) {
                mLayer.remove(mChild);
            }
        } else if (editData instanceof String) {
            mLayer.setBorder(null);
            if (mChild == null) {
                mChild = new Label();
            }
            mChild.setText((String)editData);
            if (mChild != null && mChild.getParent() != mLayer) {
                mLayer.add(mChild);
            }
            Rectangle bounds = mChild.getTextBounds();
            bounds.x = bounds.y = 0;
            mLayer.setConstraint(mChild, bounds);
        }
        refreshChildrenVisuals();
    }
    @Override
    protected void hideSelection() {
    }
    @Override
    protected void showSelection() {
    }
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installLayoutEditPolicy(this);
    }
    @Override
    public EditPart getTargetEditPart(Request request) {
        if (request != null && request.getType() == RequestConstants.REQ_CREATE) {
            if (request instanceof DropRequest) {
                Point where = ((DropRequest) request).getLocation().getCopy();
                UiElementNode uiNode = getUiNode();
                if (uiNode instanceof UiDocumentNode) {
                    Object editData = uiNode.getEditData();
                    if (editData instanceof BufferedImage) {
                        BufferedImage image = (BufferedImage)editData;
                        int w = image.getWidth();
                        int h = image.getHeight();
                        if (where.x > w || where.y > h) {
                            return null;
                        }
                    }
                }
            }
            if (getChildren().size() > 0) {
                Object o = getChildren().get(0);
                if (o instanceof EditPart) {
                    return ((EditPart) o).getTargetEditPart(request);
                }
            }
        }
        return super.getTargetEditPart(request);
    }
}
