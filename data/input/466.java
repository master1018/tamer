public class PrecCoordInfo extends CoordInfo {
    public int xref;
    public int yref;
    public PrecCoordInfo(int ulx, int uly, int w, int h, int xref, int yref) {
        super(ulx, uly, w, h);
        this.xref = xref;
        this.yref = yref;
    }
    public PrecCoordInfo() {
        super();
    }
    @Override
    public String toString() {
        return super.toString() + ", xref=" + xref + ", yref=" + yref;
    }
}
