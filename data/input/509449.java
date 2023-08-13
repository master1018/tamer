public final class ImageGraphicAttribute extends GraphicAttribute {
    private Image fImage;
    private float fOriginX;
    private float fOriginY;
    private float fImgWidth;
    private float fImgHeight;
    public ImageGraphicAttribute(Image image, int alignment, float originX, float originY) {
        super(alignment);
        this.fImage = image;
        this.fOriginX = originX;
        this.fOriginY = originY;
        this.fImgWidth = fImage.getWidth(null);
        this.fImgHeight = fImage.getHeight(null);
    }
    public ImageGraphicAttribute(Image image, int alignment) {
        this(image, alignment, 0, 0);
    }
    @Override
    public int hashCode() {
        HashCode hash = new HashCode();
        hash.append(fImage.hashCode());
        hash.append(getAlignment());
        return hash.hashCode();
    }
    public boolean equals(ImageGraphicAttribute iga) {
        if (iga == null) {
            return false;
        }
        if (iga == this) {
            return true;
        }
        return (fOriginX == iga.fOriginX && fOriginY == iga.fOriginY
                && getAlignment() == iga.getAlignment() && fImage.equals(iga.fImage));
    }
    @Override
    public boolean equals(Object obj) {
        try {
            return equals((ImageGraphicAttribute)obj);
        } catch (ClassCastException e) {
            return false;
        }
    }
    @Override
    public void draw(Graphics2D g2, float x, float y) {
        g2.drawImage(fImage, (int)(x - fOriginX), (int)(y - fOriginY), null);
    }
    @Override
    public float getAdvance() {
        return Math.max(0, fImgWidth - fOriginX);
    }
    @Override
    public float getAscent() {
        return Math.max(0, fOriginY);
    }
    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Float(-fOriginX, -fOriginY, fImgWidth, fImgHeight);
    }
    @Override
    public float getDescent() {
        return Math.max(0, fImgHeight - fOriginY);
    }
}
