public class PNGImageWriterParam extends ImageWriteParam {
    private boolean isInterlace = true;
    public PNGImageWriterParam() {
        super();
    }
    public boolean getInterlace() {
        return isInterlace;
    }
    public void setInterlace(boolean b) {
        isInterlace = b;
    }
}
