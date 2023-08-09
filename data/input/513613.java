public class ImageReadParam extends IIOParam {
    protected boolean canSetSourceRenderSize;
    protected BufferedImage destination;
    protected int[] destinationBands;
    protected int minProgressivePass;
    protected int numProgressivePasses;
    protected Dimension sourceRenderSize;
    public boolean canSetSourceRenderSize() {
        return canSetSourceRenderSize;
    }
    public BufferedImage getDestination() {
        return destination;
    }
    public int[] getDestinationBands() {
        return destinationBands;
    }
    public int getSourceMaxProgressivePass() {
        if (getSourceNumProgressivePasses() == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return getSourceMinProgressivePass() + getSourceNumProgressivePasses() - 1;
    }
    public int getSourceMinProgressivePass() {
        return minProgressivePass;
    }
    public int getSourceNumProgressivePasses() {
        return numProgressivePasses;
    }
    public Dimension getSourceRenderSize() {
        return sourceRenderSize;
    }
    public void setDestination(BufferedImage destination) {
        this.destination = destination;
    }
    public void setDestinationBands(int[] destinationBands) {
        this.destinationBands = destinationBands;
    }
    @Override
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        this.destinationType = destinationType;
    }
    public void setSourceProgressivePasses(int minPass, int numPasses) {
        minProgressivePass = minPass;
        numProgressivePasses = numPasses;
    }
    public void setSourceRenderSize(Dimension size) throws UnsupportedOperationException {
        if (!canSetSourceRenderSize) {
            throw new UnsupportedOperationException("can't set source renderer size");
        }
        sourceRenderSize = size;
    }
}
