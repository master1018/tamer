public class BufImgSurfaceManager extends SurfaceManager {
    protected BufferedImage bImg;
    protected SurfaceData sdDefault;
    public BufImgSurfaceManager(BufferedImage bImg) {
        this.bImg = bImg;
        this.sdDefault = BufImgSurfaceData.createData(bImg);
    }
    public SurfaceData getPrimarySurfaceData() {
        return sdDefault;
    }
    public SurfaceData restoreContents() {
        return sdDefault;
    }
}
