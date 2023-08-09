public class IIOImage {
    protected RenderedImage image;
    protected Raster raster;
    protected List<? extends BufferedImage> thumbnails;
    protected IIOMetadata metadata;
    public IIOImage(RenderedImage image, List<? extends BufferedImage> thumbnails,
            IIOMetadata metadata) {
        if (image == null) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        this.raster = null;
        this.image = image;
        this.thumbnails = thumbnails;
        this.metadata = metadata;
    }
    public IIOImage(Raster raster, List<? extends BufferedImage> thumbnails, IIOMetadata metadata) {
        if (raster == null) {
            throw new IllegalArgumentException("raster should not be NULL");
        }
        this.image = null;
        this.raster = raster;
        this.thumbnails = thumbnails;
        this.metadata = metadata;
    }
    public RenderedImage getRenderedImage() {
        return image;
    }
    public void setRenderedImage(RenderedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        raster = null;
        this.image = image;
    }
    public boolean hasRaster() {
        return raster != null;
    }
    public Raster getRaster() {
        return raster;
    }
    public void setRaster(Raster raster) {
        if (raster == null) {
            throw new IllegalArgumentException("raster should not be NULL");
        }
        image = null;
        this.raster = raster;
    }
    public int getNumThumbnails() {
        return thumbnails != null ? thumbnails.size() : 0;
    }
    public BufferedImage getThumbnail(int index) {
        if (thumbnails != null) {
            return thumbnails.get(index);
        }
        throw new IndexOutOfBoundsException("no thumbnails were set");
    }
    public List<? extends BufferedImage> getThumbnails() {
        return thumbnails;
    }
    public void setThumbnails(List<? extends BufferedImage> thumbnails) {
        this.thumbnails = thumbnails;
    }
    public IIOMetadata getMetadata() {
        return metadata;
    }
    public void setMetadata(IIOMetadata metadata) {
        this.metadata = metadata;
    }
}
