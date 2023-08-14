public class ImageReadParam extends IIOParam {
    protected boolean canSetSourceRenderSize = false;
    protected Dimension sourceRenderSize = null;
    protected BufferedImage destination = null;
    protected int[] destinationBands = null;
    protected int minProgressivePass = 0;
    protected int numProgressivePasses = Integer.MAX_VALUE;
    public ImageReadParam() {}
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        super.setDestinationType(destinationType);
        setDestination(null);
    }
    public void setDestination(BufferedImage destination) {
        this.destination = destination;
    }
    public BufferedImage getDestination() {
        return destination;
    }
    public void setDestinationBands(int[] destinationBands) {
        if (destinationBands == null) {
            this.destinationBands = null;
        } else {
            int numBands = destinationBands.length;
            for (int i = 0; i < numBands; i++) {
                int band = destinationBands[i];
                if (band < 0) {
                    throw new IllegalArgumentException("Band value < 0!");
                }
                for (int j = i + 1; j < numBands; j++) {
                    if (band == destinationBands[j]) {
                        throw new IllegalArgumentException("Duplicate band value!");
                    }
                }
            }
            this.destinationBands = (int[])destinationBands.clone();
        }
    }
    public int[] getDestinationBands() {
        if (destinationBands == null) {
            return null;
        } else {
            return (int[])(destinationBands.clone());
        }
    }
    public boolean canSetSourceRenderSize() {
        return canSetSourceRenderSize;
    }
    public void setSourceRenderSize(Dimension size)
        throws UnsupportedOperationException {
        if (!canSetSourceRenderSize()) {
            throw new UnsupportedOperationException
                ("Can't set source render size!");
        }
        if (size == null) {
            this.sourceRenderSize = null;
        } else {
            if (size.width <= 0 || size.height <= 0) {
                throw new IllegalArgumentException("width or height <= 0!");
            }
            this.sourceRenderSize = (Dimension)size.clone();
        }
    }
    public Dimension getSourceRenderSize() {
        return (sourceRenderSize == null) ?
            null : (Dimension)sourceRenderSize.clone();
    }
    public void setSourceProgressivePasses(int minPass, int numPasses) {
        if (minPass < 0) {
            throw new IllegalArgumentException("minPass < 0!");
        }
        if (numPasses <= 0) {
            throw new IllegalArgumentException("numPasses <= 0!");
        }
        if ((numPasses != Integer.MAX_VALUE) &&
            (((minPass + numPasses - 1) & 0x80000000) != 0)) {
            throw new IllegalArgumentException
                ("minPass + numPasses - 1 > INTEGER.MAX_VALUE!");
        }
        this.minProgressivePass = minPass;
        this.numProgressivePasses = numPasses;
    }
    public int getSourceMinProgressivePass() {
        return minProgressivePass;
    }
    public int getSourceMaxProgressivePass() {
        if (numProgressivePasses == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return minProgressivePass + numProgressivePasses - 1;
        }
    }
    public int getSourceNumProgressivePasses() {
        return numProgressivePasses;
    }
}
