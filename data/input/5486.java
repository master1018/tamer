public abstract class IIOParam {
    protected Rectangle sourceRegion = null;
    protected int sourceXSubsampling = 1;
    protected int sourceYSubsampling = 1;
    protected int subsamplingXOffset = 0;
    protected int subsamplingYOffset = 0;
    protected int[] sourceBands = null;
    protected ImageTypeSpecifier destinationType = null;
    protected Point destinationOffset = new Point(0, 0);
    protected IIOParamController defaultController = null;
    protected IIOParamController controller = null;
    protected IIOParam() {
        controller = defaultController;
    }
    public void setSourceRegion(Rectangle sourceRegion) {
        if (sourceRegion == null) {
            this.sourceRegion = null;
            return;
        }
        if (sourceRegion.x < 0) {
            throw new IllegalArgumentException("sourceRegion.x < 0!");
        }
        if (sourceRegion.y < 0){
            throw new IllegalArgumentException("sourceRegion.y < 0!");
        }
        if (sourceRegion.width <= 0) {
            throw new IllegalArgumentException("sourceRegion.width <= 0!");
        }
        if (sourceRegion.height <= 0) {
            throw new IllegalArgumentException("sourceRegion.height <= 0!");
        }
        if (sourceRegion.width <= subsamplingXOffset) {
            throw new IllegalStateException
                ("sourceRegion.width <= subsamplingXOffset!");
        }
        if (sourceRegion.height <= subsamplingYOffset) {
            throw new IllegalStateException
                ("sourceRegion.height <= subsamplingYOffset!");
        }
        this.sourceRegion = (Rectangle)sourceRegion.clone();
    }
    public Rectangle getSourceRegion() {
        if (sourceRegion == null) {
            return null;
        }
        return (Rectangle)sourceRegion.clone();
    }
    public void setSourceSubsampling(int sourceXSubsampling,
                                     int sourceYSubsampling,
                                     int subsamplingXOffset,
                                     int subsamplingYOffset) {
        if (sourceXSubsampling <= 0) {
            throw new IllegalArgumentException("sourceXSubsampling <= 0!");
        }
        if (sourceYSubsampling <= 0) {
            throw new IllegalArgumentException("sourceYSubsampling <= 0!");
        }
        if (subsamplingXOffset < 0 ||
            subsamplingXOffset >= sourceXSubsampling) {
            throw new IllegalArgumentException
                ("subsamplingXOffset out of range!");
        }
        if (subsamplingYOffset < 0 ||
            subsamplingYOffset >= sourceYSubsampling) {
            throw new IllegalArgumentException
                ("subsamplingYOffset out of range!");
        }
        if (sourceRegion != null) {
            if (subsamplingXOffset >= sourceRegion.width ||
                subsamplingYOffset >= sourceRegion.height) {
                throw new IllegalStateException("region contains no pixels!");
            }
        }
        this.sourceXSubsampling = sourceXSubsampling;
        this.sourceYSubsampling = sourceYSubsampling;
        this.subsamplingXOffset = subsamplingXOffset;
        this.subsamplingYOffset = subsamplingYOffset;
    }
    public int getSourceXSubsampling() {
        return sourceXSubsampling;
    }
    public int getSourceYSubsampling() {
        return sourceYSubsampling;
    }
    public int getSubsamplingXOffset() {
        return subsamplingXOffset;
    }
    public int getSubsamplingYOffset() {
        return subsamplingYOffset;
    }
    public void setSourceBands(int[] sourceBands) {
        if (sourceBands == null) {
            this.sourceBands = null;
        } else {
            int numBands = sourceBands.length;
            for (int i = 0; i < numBands; i++) {
                int band = sourceBands[i];
                if (band < 0) {
                    throw new IllegalArgumentException("Band value < 0!");
                }
                for (int j = i + 1; j < numBands; j++) {
                    if (band == sourceBands[j]) {
                        throw new IllegalArgumentException("Duplicate band value!");
                    }
                }
            }
            this.sourceBands = (int[])(sourceBands.clone());
        }
    }
    public int[] getSourceBands() {
        if (sourceBands == null) {
            return null;
        }
        return (int[])(sourceBands.clone());
    }
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        this.destinationType = destinationType;
    }
    public ImageTypeSpecifier getDestinationType() {
        return destinationType;
    }
    public void setDestinationOffset(Point destinationOffset) {
        if (destinationOffset == null) {
            throw new IllegalArgumentException("destinationOffset == null!");
        }
        this.destinationOffset = (Point)destinationOffset.clone();
    }
    public Point getDestinationOffset() {
        return (Point)destinationOffset.clone();
    }
    public void setController(IIOParamController controller) {
        this.controller = controller;
    }
    public IIOParamController getController() {
        return controller;
    }
    public IIOParamController getDefaultController() {
        return defaultController;
    }
    public boolean hasController() {
        return (controller != null);
    }
    public boolean activateController() {
        if (!hasController()) {
            throw new IllegalStateException("hasController() == false!");
        }
        return getController().activate(this);
    }
}
