public class LinkData extends DataWithUpdateElements {
    private int srcID, dstID;
    public LinkData(int src, int dst) {
        srcID = src;
        dstID = dst;
    }
    public int getDstID() {
        return dstID;
    }
    public int getSrcID() {
        return srcID;
    }
}
