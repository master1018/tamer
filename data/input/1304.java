public class DataBufferNative extends DataBuffer
{
    protected SurfaceData surfaceData;
    protected int width;
    public DataBufferNative(SurfaceData sData, int type, int width, int height) {
        super(type, width*height);
        this.width = width;
        this.surfaceData = sData;
    }
    protected native int getElem(int x, int y, SurfaceData sData);
    public int getElem(int bank, int i) {
        return getElem(i % width, i / width, surfaceData);
    }
    protected native void setElem(int x, int y, int val, SurfaceData sData);
    public void setElem(int bank, int i, int val) {
        setElem(i % width, i / width, val, surfaceData);
    }
}
