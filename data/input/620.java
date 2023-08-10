public abstract class Graphic implements Element, Writable {
    protected Map<String, String> css;
    protected abstract void draw(PdfContentByte cb);
    public Graphic(Map<String, String> css) {
        this.css = css;
    }
    public void draw(PdfContentByte cb, Map<String, String> css) {
        CssSvgAppliers.getInstance().apply(cb, css);
        draw(cb);
        CssSvgAppliers.getInstance().close(cb, css);
    }
    public Map<String, String> getCss() {
        return css;
    }
    public int type() {
        return 0;
    }
    public boolean isContent() {
        return false;
    }
    public boolean isNestable() {
        return false;
    }
    public List<Chunk> getChunks() {
        return null;
    }
    public boolean process(ElementListener listener) {
        return false;
    }
}
