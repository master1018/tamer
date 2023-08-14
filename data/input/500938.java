public abstract class IIOParam {
    protected Rectangle sourceRegion;
    protected int sourceXSubsampling = 1;
    protected int sourceYSubsampling = 1;
    protected int subsamplingXOffset;
    protected int subsamplingYOffset;
    protected int[] sourceBands;
    protected ImageTypeSpecifier destinationType;
    protected Point destinationOffset = new Point(0, 0);
    protected IIOParamController defaultController;
    protected IIOParamController controller;
    protected IIOParam() {
    }
    public void setSourceRegion(Rectangle sourceRegion) {
        if (sourceRegion != null) {
            if (sourceRegion.x < 0) {
                throw new IllegalArgumentException("x < 0");
            }
            if (sourceRegion.y < 0) {
                throw new IllegalArgumentException("y < 0");
            }
            if (sourceRegion.width <= 0) {
                throw new IllegalArgumentException("width <= 0");
            }
            if (sourceRegion.height <= 0) {
                throw new IllegalArgumentException("height <= 0");
            }
            if (sourceRegion.width <= subsamplingXOffset) {
                throw new IllegalArgumentException("width <= subsamplingXOffset");
            }
            if (sourceRegion.height <= subsamplingYOffset) {
                throw new IllegalArgumentException("height <= subsamplingXOffset");
            }
            this.sourceRegion = (Rectangle)sourceRegion.clone();
        } else {
            this.sourceRegion = null;
        }
    }
    public Rectangle getSourceRegion() {
        if (sourceRegion == null) {
            return null;
        }
        return (Rectangle)sourceRegion.clone();
    }
    public void setSourceSubsampling(int sourceXSubsampling, int sourceYSubsampling,
            int subsamplingXOffset, int subsamplingYOffset) {
        if (sourceXSubsampling <= 0) {
            throw new IllegalArgumentException("sourceXSubsampling <= 0");
        }
        if (sourceYSubsampling <= 0) {
            throw new IllegalArgumentException("sourceYSubsampling <= 0");
        }
        if (subsamplingXOffset <= 0 || subsamplingXOffset >= sourceXSubsampling) {
            throw new IllegalArgumentException("subsamplingXOffset is wrong");
        }
        if (subsamplingYOffset <= 0 || subsamplingYOffset >= sourceYSubsampling) {
            throw new IllegalArgumentException("subsamplingYOffset is wrong");
        }
        if (sourceRegion != null) {
            if (sourceRegion.width <= subsamplingXOffset
                    || sourceRegion.height <= subsamplingYOffset) {
                throw new IllegalArgumentException("there are no pixels in region");
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
        throw new UnsupportedOperationException("not implemented yet");
    }
    public int[] getSourceBands() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public ImageTypeSpecifier getDestinationType() {
        throw new UnsupportedOperationException("not implemented yet");
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
        throw new UnsupportedOperationException("not implemented yet");
    }
    public IIOParamController getController() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public IIOParamController getDefaultController() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public boolean hasController() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    public boolean activateController() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
